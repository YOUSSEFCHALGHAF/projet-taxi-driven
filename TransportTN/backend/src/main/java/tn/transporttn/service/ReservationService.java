package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.commun.PageResponse;
import tn.transporttn.dto.reservation.*;
import tn.transporttn.exception.RegleMetierException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.*;
import tn.transporttn.model.enums.StatutReservation;
import tn.transporttn.model.enums.TypeLigne;
import tn.transporttn.model.enums.TypeNotification;
import tn.transporttn.repository.*;
import tn.transporttn.tempsreel.TempsReelService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    private static final int TAILLE_PAGE_MAX = 50;

    private final UtilisateurRepository utilisateurRepository;
    private final DepartRepository departRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationCollectifRepository reservationCollectifRepository;
    private final ReservationBusRepository reservationBusRepository;
    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;
    private final TempsReelService tempsReelService;

    // ---------------- Taxi collectif ----------------

    @Transactional
    public ReservationCollectifResponse reserverCollectif(Long clientId, ReservationCollectifRequest r) {
        Utilisateur client = client(clientId);
        Depart depart = departReservable(r.departId(), TypeLigne.COLLECTIF);

        depart.reserver(r.nombrePlaces());   // vérifie places restantes et date ; @Version protège la concurrence

        ReservationCollectif reservation = ReservationCollectif.builder()
                .client(client)
                .depart(depart)
                .nombrePlaces(r.nombrePlaces())
                .montant(depart.getLigne().getPrix().multiply(BigDecimal.valueOf(r.nombrePlaces())))
                .modePaiement(r.modePaiement())
                .statut(StatutReservation.ACCEPTEE)      // confirmée immédiatement
                .build();
        reservationCollectifRepository.save(reservation);

        notificationService.notifier(client, TypeNotification.RESERVATION, "Places confirmées",
                r.nombrePlaces() + " place(s) " + depart.getLigne().getItineraire() + " le "
                        + depart.getDateHeure().format(FORMAT_DATE) + ". Référence : " + reservation.getReference());

        return ReservationCollectifResponse.depuis(reservation);
    }

    // ---------------- Bus ----------------

    @Transactional
    public ReservationBusResponse acheterTickets(Long clientId, ReservationBusRequest r) {
        Utilisateur client = client(clientId);
        Depart depart = departReservable(r.departId(), TypeLigne.BUS);

        depart.reserver(r.nombreTickets());

        ReservationBus reservation = ReservationBus.builder()
                .client(client)
                .depart(depart)
                .nombreTickets(r.nombreTickets())
                .montant(depart.getLigne().getPrix().multiply(BigDecimal.valueOf(r.nombreTickets())))
                .modePaiement(r.modePaiement())
                .statut(StatutReservation.ACCEPTEE)
                .build();

        for (int i = 0; i < r.nombreTickets(); i++) {
            String code = UUID.randomUUID().toString();
            reservation.ajouterTicket(Ticket.builder()
                    .codeUnique(code)
                    .qrCodeBase64(qrCodeService.genererPngBase64(QrCodeService.contenuTicket(code)))
                    .build());
        }
        reservationBusRepository.save(reservation);   // les tickets sont enregistrés en cascade

        notificationService.notifier(client, TypeNotification.TICKET, "Tickets confirmés",
                r.nombreTickets() + " ticket(s) " + depart.getLigne().getItineraire() + " le "
                        + depart.getDateHeure().format(FORMAT_DATE) + ". Présentez le QR code à l'embarquement.");

        return ReservationBusResponse.depuis(reservation);
    }

    // ---------------- Espace client ----------------

    @Transactional(readOnly = true)
    public PageResponse<ReservationResumeResponse> historique(Long clientId, int page, int taille) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(taille, 1), TAILLE_PAGE_MAX));
        return PageResponse.depuis(
                reservationRepository.findByClientIdOrderByDateCreationDesc(clientId, pageable),
                ReservationResumeResponse::depuis);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> mesTickets(Long clientId) {
        return ticketRepository.findTicketsDuClient(clientId).stream().map(TicketResponse::depuis).toList();
    }

    /** Annulation polymorphe : chaque type applique sa propre règle via annuler(). */
    @Transactional
    public ReservationResumeResponse annuler(Long clientId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .filter(r -> r.getClient().getId().equals(clientId))
                .orElseThrow(() -> new RessourceIntrouvableException("Réservation introuvable"));

        reservation.annuler();   // taxi : refuse si EN_COURS ; collectif/bus : libère les places

        if (reservation instanceof ReservationTaxi taxi && taxi.getChauffeur() != null) {
            Chauffeur chauffeur = taxi.getChauffeur();
            chauffeur.setDisponible(true);
            notificationService.notifier(chauffeur.getUtilisateur(), TypeNotification.COURSE,
                    "Course annulée", "Le client a annulé la course " + taxi.getReference() + ".");
            tempsReelService.publierCourse(taxi);   // la course disparaît de l'écran du chauffeur

        }

        notificationService.notifier(reservation.getClient(), TypeNotification.RESERVATION,
                "Réservation annulée", "Votre réservation " + reservation.getReference() + " a été annulée.");

        return ReservationResumeResponse.depuis(reservation);
    }

    // ---------------- Outils ----------------

    private Utilisateur client(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }

    private Depart departReservable(Long departId, TypeLigne typeAttendu) {
        Depart depart = departRepository.findById(departId)
                .orElseThrow(() -> new RessourceIntrouvableException("Départ introuvable"));
        Ligne ligne = depart.getLigne();

        if (ligne.getType() != typeAttendu) {
            throw new RegleMetierException(typeAttendu == TypeLigne.BUS
                    ? "Ce départ ne correspond pas à une ligne de bus"
                    : "Ce départ ne correspond pas à une ligne de taxi collectif");
        }
        if (!ligne.isActive()) {
            throw new RegleMetierException("Cette ligne n'est plus en service");
        }
        return depart;
    }
}
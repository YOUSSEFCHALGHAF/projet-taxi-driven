package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.taxi.EstimationRequest;
import tn.transporttn.dto.taxi.EstimationResponse;
import tn.transporttn.dto.taxi.ReservationTaxiRequest;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;
import tn.transporttn.exception.ConflitException;
import tn.transporttn.exception.RegleMetierException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.Chauffeur;
import tn.transporttn.model.ReservationTaxi;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.StatutReservation;
import tn.transporttn.model.enums.TypeNotification;
import tn.transporttn.repository.ChauffeurRepository;
import tn.transporttn.repository.ReservationTaxiRepository;
import tn.transporttn.repository.UtilisateurRepository;
import tn.transporttn.util.GeoUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import tn.transporttn.tempsreel.TempsReelService;
import static tn.transporttn.model.enums.StatutReservation.*;

@Service
@RequiredArgsConstructor
public class TaxiService {

    /** La route est en moyenne 30 % plus longue que la ligne droite. */
    public static final double COEFFICIENT_ROUTIER = 1.3;
    public static final double RAYON_RECHERCHE_KM = 10.0;
    public static final double VITESSE_MOYENNE_KMH = 30.0;   // circulation urbaine
    private static final double DISTANCE_MIN_KM = 0.3;
    private static final double DISTANCE_MAX_KM = 150.0;

    public static final List<StatutReservation> STATUTS_ACTIFS = List.of(EN_ATTENTE, ACCEPTEE, EN_COURS);
    private final TempsReelService tempsReelService;
    private final ReservationTaxiRepository reservationTaxiRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NotificationService notificationService;
    @Transactional(readOnly = true)
    public EstimationResponse estimer(EstimationRequest r) {
        double km = distanceRoutiere(r.latDepart(), r.lngDepart(), r.latArrivee(), r.lngArrivee());
        verifierDistance(km);

        long proches = chauffeurRepository.findDisponiblesAvecPosition().stream()
                .filter(c -> GeoUtils.distanceKm(c.getLatitude(), c.getLongitude(),
                        r.latDepart(), r.lngDepart()) <= RAYON_RECHERCHE_KM)
                .count();

        return new EstimationResponse(km, ReservationTaxi.calculerPrix(km), dureeMinutes(km), (int) proches);
    }

    @Transactional
    public ReservationTaxiResponse reserver(Long clientId, ReservationTaxiRequest r) {
        if (reservationTaxiRepository
                .findFirstByClientIdAndStatutInOrderByDateCreationDesc(clientId, STATUTS_ACTIFS).isPresent()) {
            throw new ConflitException(
                    "Vous avez déjà une course en cours. Terminez-la ou annulez-la avant d'en réserver une autre.");
        }

        Utilisateur client = utilisateurRepository.findById(clientId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));

        double km = distanceRoutiere(r.latDepart(), r.lngDepart(), r.latArrivee(), r.lngArrivee());
        verifierDistance(km);

        ReservationTaxi course = ReservationTaxi.builder()
                .client(client)
                .adresseDepart(r.adresseDepart().trim())
                .latDepart(r.latDepart())
                .lngDepart(r.lngDepart())
                .adresseArrivee(r.adresseArrivee().trim())
                .latArrivee(r.latArrivee())
                .lngArrivee(r.lngArrivee())
                .distanceKm(km)
                .montant(ReservationTaxi.calculerPrix(km))     // prix calculé par le serveur
                .modePaiement(r.modePaiement())
                .build();
        reservationTaxiRepository.save(course);

        if (!attribuerChauffeur(course, Set.of())) {
            // L'exception annule toute la transaction : la réservation n'est pas conservée
            throw new ConflitException(
                    "Aucun chauffeur disponible à proximité pour le moment. Veuillez réessayer dans quelques minutes.");
        }

        notificationService.notifier(client, TypeNotification.RESERVATION, "Course enregistrée",
                "Votre demande " + course.getReference() + " a été transmise à "
                        + course.getChauffeur().getUtilisateur().getNomComplet() + ".");
        tempsReelService.publierCourse(course);   // le chauffeur voit la course apparaître immédiatement
        return ReservationTaxiResponse.depuis(course);
    }

    /**
     * Attribue le chauffeur disponible le plus proche (hors exclus).
     *
     * @return true si un chauffeur a été attribué
     */
    @Transactional
    public boolean attribuerChauffeur(ReservationTaxi course, Set<Long> exclus) {
        List<Chauffeur> candidats = chauffeurRepository.findDisponiblesAvecPosition().stream()
                .filter(c -> !exclus.contains(c.getId()))
                .filter(c -> distanceVersDepart(c, course) <= RAYON_RECHERCHE_KM)
                .sorted(Comparator.comparingDouble((Chauffeur c) -> distanceVersDepart(c, course)))
                .toList();

        for (Chauffeur chauffeur : candidats) {
            if (chauffeurRepository.reserverSiDisponible(chauffeur.getId()) == 1) {
                chauffeur.setDisponible(false);         // aligne l'objet Java sur la base
                course.setChauffeur(chauffeur);
                course.setStatut(EN_ATTENTE);
                notificationService.notifier(chauffeur.getUtilisateur(), TypeNotification.COURSE,
                        "Nouvelle course",
                        course.getAdresseDepart() + " → " + course.getAdresseArrivee()
                                + " (" + course.getMontant() + " DT)");
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public ReservationTaxiResponse consulter(Long clientId, Long courseId) {
        return ReservationTaxiResponse.depuis(courseDuClient(clientId, courseId));
    }

    @Transactional(readOnly = true)
    public PositionResponse positionChauffeur(Long clientId, Long courseId) {
        ReservationTaxi course = courseDuClient(clientId, courseId);
        Chauffeur chauffeur = course.getChauffeur();

        if (chauffeur == null || !(course.getStatut() == ACCEPTEE || course.getStatut() == EN_COURS)) {
            throw new ConflitException("Le suivi est disponible une fois la course acceptée par le chauffeur");
        }
        return new PositionResponse(chauffeur.getLatitude(), chauffeur.getLongitude(),
                chauffeur.getDerniereMiseAJour());
    }

    // ---------------- Outils ----------------

    private ReservationTaxi courseDuClient(Long clientId, Long courseId) {
        return reservationTaxiRepository.findById(courseId)
                .filter(c -> c.getClient().getId().equals(clientId))   // un client ne voit que ses courses
                .orElseThrow(() -> new RessourceIntrouvableException("Course introuvable"));
    }

    private double distanceVersDepart(Chauffeur c, ReservationTaxi course) {
        return GeoUtils.distanceKm(c.getLatitude(), c.getLongitude(), course.getLatDepart(), course.getLngDepart());
    }

    private double distanceRoutiere(double lat1, double lng1, double lat2, double lng2) {
        double km = GeoUtils.distanceKm(lat1, lng1, lat2, lng2) * COEFFICIENT_ROUTIER;
        return Math.round(km * 100) / 100.0;   // 2 décimales
    }

    private int dureeMinutes(double km) {
        return (int) Math.max(1, Math.round(km / VITESSE_MOYENNE_KMH * 60));
    }

    private void verifierDistance(double km) {
        if (km < DISTANCE_MIN_KM) {
            throw new RegleMetierException("Le départ et l'arrivée sont trop proches");
        }
        if (km > DISTANCE_MAX_KM) {
            throw new RegleMetierException(
                    "Pour un trajet de plus de 150 km, utilisez le taxi collectif ou le bus");
        }
    }
}
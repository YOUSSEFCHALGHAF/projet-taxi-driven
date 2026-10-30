package tn.transporttn.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.model.*;
import tn.transporttn.model.enums.Role;
import tn.transporttn.model.enums.StatutReservation;
import tn.transporttn.model.enums.StatutTicket;
import tn.transporttn.model.enums.TypeLigne;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nécessite que MySQL tourne : docker compose up -d
 */
@SpringBootTest
@Transactional
class RepositoriesIntegrationTest {

    private static final String TEL_CLIENT = "+21622111222";

    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private ChauffeurRepository chauffeurRepository;
    @Autowired private LigneRepository ligneRepository;
    @Autowired private DepartRepository departRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ReservationCollectifRepository reservationCollectifRepository;
    @Autowired private ReservationBusRepository reservationBusRepository;
    @Autowired private TicketRepository ticketRepository;

    @Test
    void lesDonneesInitialesSontChargees() {
        assertThat(utilisateurRepository.countByRole(Role.CLIENT)).isGreaterThanOrEqualTo(2);
        assertThat(utilisateurRepository.countByRole(Role.DRIVER)).isGreaterThanOrEqualTo(2);
        assertThat(ligneRepository.findByTypeAndActiveTrueOrderByVilleDepartAscVilleArriveeAsc(TypeLigne.BUS))
                .isNotEmpty();
        assertThat(ligneRepository.findByTypeAndActiveTrueOrderByVilleDepartAscVilleArriveeAsc(TypeLigne.COLLECTIF))
                .isNotEmpty();
    }

    @Test
    void trouverUnClientParTelephone() {
        Utilisateur client = utilisateurRepository.findByTelephone(TEL_CLIENT).orElseThrow();
        assertThat(client.getRole()).isEqualTo(Role.CLIENT);
        assertThat(client.getNomComplet()).isEqualTo("Amira Ben Salah");
    }

    @Test
    void desChauffeursSontDisponiblesAvecUnePosition() {
        List<Chauffeur> chauffeurs = chauffeurRepository.findDisponiblesAvecPosition();
        assertThat(chauffeurs).isNotEmpty();
        assertThat(chauffeurs).allMatch(Chauffeur::aUnePosition);
    }

    @Test
    void reserverDesPlacesEnTaxiCollectif() {
        Utilisateur client = utilisateurRepository.findByTelephone(TEL_CLIENT).orElseThrow();
        Ligne ligne = ligneRepository
                .findByTypeAndActiveTrueOrderByVilleDepartAscVilleArriveeAsc(TypeLigne.COLLECTIF).get(0);
        Depart depart = prochainDepart(ligne, 2);
        int placesAvant = depart.getPlacesRestantes();

        depart.reserver(2);
        ReservationCollectif reservation = ReservationCollectif.builder()
                .client(client)
                .depart(depart)
                .nombrePlaces(2)
                .montant(ligne.getPrix().multiply(BigDecimal.valueOf(2)))
                .statut(StatutReservation.ACCEPTEE)
                .build();
        reservationCollectifRepository.saveAndFlush(reservation);

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getReference()).startsWith("TC-");
        assertThat(departRepository.findById(depart.getId()).orElseThrow().getPlacesRestantes())
                .isEqualTo(placesAvant - 2);
        assertThat(reservationRepository.findByReference(reservation.getReference())).isPresent();
    }

    @Test
    void acheterDesTicketsDeBus() {
        Utilisateur client = utilisateurRepository.findByTelephone(TEL_CLIENT).orElseThrow();
        Ligne ligne = ligneRepository
                .findByTypeAndActiveTrueOrderByVilleDepartAscVilleArriveeAsc(TypeLigne.BUS).get(0);
        Depart depart = prochainDepart(ligne, 2);

        depart.reserver(2);
        ReservationBus reservation = ReservationBus.builder()
                .client(client)
                .depart(depart)
                .nombreTickets(2)
                .montant(ligne.getPrix().multiply(BigDecimal.valueOf(2)))
                .statut(StatutReservation.ACCEPTEE)
                .build();
        reservation.ajouterTicket(Ticket.builder().build());
        reservation.ajouterTicket(Ticket.builder().build());
        reservationBusRepository.saveAndFlush(reservation);

        assertThat(reservation.getReference()).startsWith("BS-");
        assertThat(reservation.getTickets())
                .hasSize(2)
                .allSatisfy(ticket -> {
                    assertThat(ticket.getCodeUnique()).hasSize(36);
                    assertThat(ticket.getStatut()).isEqualTo(StatutTicket.VALIDE);
                });
        assertThat(ticketRepository.findTicketsDuClient(client.getId())).hasSizeGreaterThanOrEqualTo(2);
    }

    private Depart prochainDepart(Ligne ligne, int placesNecessaires) {
        List<Depart> departs = departRepository
                .findByLigneIdAndDateHeureAfterAndPlacesRestantesGreaterThanEqualOrderByDateHeureAsc(
                        ligne.getId(), LocalDateTime.now(), placesNecessaires);
        assertThat(departs)
                .as("Aucun départ futur : réinitialisez la base (docker compose down -v && docker compose up -d)")
                .isNotEmpty();
        return departs.get(0);
    }
}
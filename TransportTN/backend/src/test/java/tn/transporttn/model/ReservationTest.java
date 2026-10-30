package tn.transporttn.model;

import org.junit.jupiter.api.Test;
import tn.transporttn.model.enums.StatutReservation;
import tn.transporttn.model.enums.StatutTicket;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires des règles métier des réservations (sans base de données).
 */
class ReservationTest {

    private Depart nouveauDepart(int total) {
        return Depart.builder()
                .dateHeure(LocalDateTime.now().plusDays(1))
                .placesTotal(total)
                .placesRestantes(total)
                .build();
    }

    @Test
    void taxi_estimerPrix_priseEnChargePlusKilometres() {
        ReservationTaxi taxi = new ReservationTaxi();
        taxi.setDistanceKm(10.0);
        // 0,900 + 10 × 0,700 = 7,900 TND
        assertThat(taxi.estimerPrix()).isEqualByComparingTo("8.400");
    }

    @Test
    void taxi_estimerPrix_sansDistanceLeveUneErreur() {
        assertThatThrownBy(() -> new ReservationTaxi().estimerPrix())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void collectif_annuler_rendLesPlaces() {
        Depart depart = nouveauDepart(8);
        depart.reserver(3);

        ReservationCollectif reservation = new ReservationCollectif();
        reservation.setDepart(depart);
        reservation.setNombrePlaces(3);
        reservation.setStatut(StatutReservation.ACCEPTEE);

        reservation.annuler();

        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(depart.getPlacesRestantes()).isEqualTo(8);
    }

    @Test
    void annuler_refuseUneCourseTerminee() {
        ReservationTaxi taxi = new ReservationTaxi();
        taxi.setStatut(StatutReservation.TERMINEE);
        assertThatThrownBy(taxi::annuler).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bus_annuler_invalideLesTicketsValides() {
        Depart depart = nouveauDepart(50);
        depart.reserver(2);

        ReservationBus bus = new ReservationBus();
        bus.setDepart(depart);
        bus.setNombreTickets(2);
        bus.setStatut(StatutReservation.ACCEPTEE);
        bus.ajouterTicket(Ticket.builder().build());
        bus.ajouterTicket(Ticket.builder().build());

        bus.annuler();

        assertThat(bus.getTickets()).allMatch(t -> t.getStatut() == StatutTicket.ANNULE);
        assertThat(depart.getPlacesRestantes()).isEqualTo(50);
    }

    @Test
    void ticket_utiliser_uneSeuleFois() {
        Ticket ticket = Ticket.builder().build();
        ticket.utiliser();
        assertThat(ticket.getStatut()).isEqualTo(StatutTicket.UTILISE);
        assertThatThrownBy(ticket::utiliser).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chauffeur_mettreAJourPosition_refuseDesCoordonneesInvalides() {
        Chauffeur chauffeur = new Chauffeur();
        chauffeur.mettreAJourPosition(36.8008, 10.1800);
        assertThat(chauffeur.aUnePosition()).isTrue();
        assertThatThrownBy(() -> chauffeur.mettreAJourPosition(120, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

package tn.transporttn.model;

import org.junit.jupiter.api.Test;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTaxiTest {

    @Test
    void prixPour10Km() {
        // 0,900 + 10 × 0,750 = 8,400 DT
        assertThat(ReservationTaxi.calculerPrix(10)).isEqualByComparingTo(new BigDecimal("8.400"));
    }

    @Test
    void prixMinimumAppliquePourUneCourteDistance() {
        // 0,900 + 1 × 0,750 = 1,650 DT → minimum 2,500 DT
        assertThat(ReservationTaxi.calculerPrix(1)).isEqualByComparingTo(new BigDecimal("2.500"));
    }

    @Test
    void courseEnCoursNonAnnulable() {
        ReservationTaxi course = ReservationTaxi.builder()
                .statut(StatutReservation.EN_COURS)
                .build();
        assertThatThrownBy(course::annuler).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void courseEnAttenteAnnulable() {
        ReservationTaxi course = ReservationTaxi.builder().build();   // EN_ATTENTE par défaut
        course.annuler();
        assertThat(course.getStatut()).isEqualTo(StatutReservation.ANNULEE);
    }
}
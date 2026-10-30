package tn.transporttn.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepartTest {

    private Depart departDemain(int placesRestantes) {
        return Depart.builder()
                .dateHeure(LocalDateTime.now().plusDays(1))
                .placesTotal(8)
                .placesRestantes(placesRestantes)
                .build();
    }

    @Test
    void reserverDiminueLesPlacesRestantes() {
        Depart depart = departDemain(8);
        depart.reserver(3);
        assertThat(depart.getPlacesRestantes()).isEqualTo(5);
    }

    @Test
    void reserverPlusQueDisponibleEstRefuse() {
        Depart depart = departDemain(2);
        assertThatThrownBy(() -> depart.reserver(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Places insuffisantes");
    }

    @Test
    void reserverUnDepartPasseEstRefuse() {
        Depart depart = departDemain(8);
        depart.setDateHeure(LocalDateTime.now().minusHours(1));
        assertThatThrownBy(() -> depart.reserver(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà passé");
    }

    @Test
    void libererNeDepassePasLaCapacite() {
        Depart depart = departDemain(7);
        depart.liberer(4);
        assertThat(depart.getPlacesRestantes()).isEqualTo(8);
    }

    @Test
    void departCompletQuandPlusDePlace() {
        Depart depart = departDemain(1);
        depart.reserver(1);
        assertThat(depart.estComplet()).isTrue();
    }
}
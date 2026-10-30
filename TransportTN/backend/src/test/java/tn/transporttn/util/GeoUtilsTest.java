package tn.transporttn.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GeoUtilsTest {

    @Test
    void distanceTunisSousse() {
        // Centre de Tunis → centre de Sousse : ≈ 115,6 km à vol d'oiseau
        double km = GeoUtils.distanceKm(36.8065, 10.1815, 35.8256, 10.6084);
        assertThat(km).isCloseTo(115.6, within(1.0));
    }

    @Test
    void distanceNulleEntreDeuxPointsIdentiques() {
        assertThat(GeoUtils.distanceKm(36.8, 10.18, 36.8, 10.18)).isZero();
    }
}
package tn.transporttn.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tn.transporttn.exception.RegleMetierException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelephoneUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"22111222", "22 111 222", "+21622111222", "+216 22 111 222",
                            "0021622111222", "22-111-222", "22.111.222"})
    void formatsValidesNormalises(String saisie) {
        assertThat(TelephoneUtils.normaliser(saisie)).isEqualTo("+21622111222");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "123456789", "12111222", "abcdefgh", "+33612345678", " "})
    void formatsInvalidesRefuses(String saisie) {
        assertThatThrownBy(() -> TelephoneUtils.normaliser(saisie))
                .isInstanceOf(RegleMetierException.class);
    }
}
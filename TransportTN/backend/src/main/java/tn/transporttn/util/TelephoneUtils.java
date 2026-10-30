package tn.transporttn.util;

import tn.transporttn.exception.RegleMetierException;

/** Normalise les numéros tunisiens au format +216XXXXXXXX. */
public final class TelephoneUtils {

    private TelephoneUtils() {
    }

    public static String normaliser(String brut) {
        if (brut == null || brut.isBlank()) {
            throw new RegleMetierException("Le numéro de téléphone est obligatoire");
        }
        // Supprime espaces, points, tirets et parenthèses
        String numero = brut.replaceAll("[\\s.\\-()]", "");

        if (numero.startsWith("+216")) {
            numero = numero.substring(4);
        } else if (numero.startsWith("00216")) {
            numero = numero.substring(5);
        }

        if (!numero.matches("[2-9]\\d{7}")) {
            throw new RegleMetierException(
                    "Numéro de téléphone tunisien invalide (8 chiffres, ex : 22 123 456)");
        }
        return "+216" + numero;
    }
}
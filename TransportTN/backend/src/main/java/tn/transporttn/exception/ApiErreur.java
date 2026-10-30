package tn.transporttn.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/** Corps JSON unique pour toutes les erreurs de l'API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)   // "details" n'apparaît que s'il est rempli
public record ApiErreur(
        LocalDateTime horodatage,
        int statut,
        String erreur,
        String message,
        String chemin,
        Map<String, String> details
) {

    public static ApiErreur of(HttpStatus statut, String message, String chemin) {
        return of(statut, message, chemin, null);
    }

    public static ApiErreur of(HttpStatus statut, String message, String chemin, Map<String, String> details) {
        return new ApiErreur(LocalDateTime.now(), statut.value(), libelle(statut), message, chemin, details);
    }

    /** Libellé français du code HTTP. */
    private static String libelle(HttpStatus statut) {
        return switch (statut) {
            case BAD_REQUEST -> "Requête invalide";
            case UNAUTHORIZED -> "Non authentifié";
            case FORBIDDEN -> "Accès refusé";
            case NOT_FOUND -> "Ressource introuvable";
            case METHOD_NOT_ALLOWED -> "Méthode non autorisée";
            case CONFLICT -> "Conflit";
            default -> statut.is5xxServerError() ? "Erreur interne du serveur" : statut.getReasonPhrase();
        };
    }
}
package tn.transporttn.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforme toutes les exceptions levées dans les contrôleurs et services
 * en réponses JSON homogènes (ApiErreur) avec un message en français.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Exceptions métier de l'application ----------

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ApiErreur> introuvable(RessourceIntrouvableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ApiErreur> conflit(ConflitException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler({RegleMetierException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErreur> regleMetier(RuntimeException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /** Levée par les entités (ex : Depart.reserver quand il n'y a plus de place). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErreur> etatInvalide(IllegalStateException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** Deux réservations simultanées sur le même départ (@Version). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErreur> concurrence(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return reponse(HttpStatus.CONFLICT,
                "Les places ont été modifiées par une autre réservation, veuillez réessayer", req);
    }

    // ---------- Validation des données reçues ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErreur> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> details.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(
                ApiErreur.of(HttpStatus.BAD_REQUEST, "Certains champs sont invalides", req.getRequestURI(), details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErreur> jsonIllisible(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST, "Le corps de la requête est absent ou n'est pas un JSON valide", req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErreur> typeInvalide(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return reponse(HttpStatus.BAD_REQUEST,
                "Valeur invalide pour le paramètre « " + ex.getName() + " »", req);
    }

    // ---------- Sécurité ----------

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErreur> identifiants(BadCredentialsException ex, HttpServletRequest req) {
        return reponse(HttpStatus.UNAUTHORIZED, "Numéro de téléphone ou mot de passe incorrect", req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErreur> compteDesactive(DisabledException ex, HttpServletRequest req) {
        return reponse(HttpStatus.FORBIDDEN, "Ce compte est désactivé. Contactez l'administrateur.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErreur> authentification(AuthenticationException ex, HttpServletRequest req) {
        return reponse(HttpStatus.UNAUTHORIZED, "Authentification requise", req);
    }

    /** Levée par @PreAuthorize dans les contrôleurs (étape 4). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErreur> accesRefuse(AccessDeniedException ex, HttpServletRequest req) {
        return reponse(HttpStatus.FORBIDDEN, "Vous n'avez pas les droits pour effectuer cette action", req);
    }

    // ---------- Erreurs HTTP génériques ----------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErreur> routeInconnue(NoResourceFoundException ex, HttpServletRequest req) {
        return reponse(HttpStatus.NOT_FOUND, "Cette adresse n'existe pas dans l'API", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErreur> methode(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED,
                "La méthode " + ex.getMethod() + " n'est pas autorisée sur cette adresse", req);
    }

    /** Filet de sécurité : toute erreur imprévue. Le détail technique reste dans les logs. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErreur> inattendue(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {} {}", req.getMethod(), req.getRequestURI(), ex);
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue s'est produite. Veuillez réessayer plus tard.", req);
    }

    private ResponseEntity<ApiErreur> reponse(HttpStatus statut, String message, HttpServletRequest req) {
        return ResponseEntity.status(statut).body(ApiErreur.of(statut, message, req.getRequestURI()));
    }
}
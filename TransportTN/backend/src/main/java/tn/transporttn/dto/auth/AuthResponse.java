package tn.transporttn.dto.auth;

public record AuthResponse(
        String token,
        String type,             // toujours "Bearer"
        long expireDans,         // durée de validité en secondes
        UtilisateurResponse utilisateur
) {
    public static AuthResponse bearer(String token, long expireDans, UtilisateurResponse utilisateur) {
        return new AuthResponse(token, "Bearer", expireDans, utilisateur);
    }
}
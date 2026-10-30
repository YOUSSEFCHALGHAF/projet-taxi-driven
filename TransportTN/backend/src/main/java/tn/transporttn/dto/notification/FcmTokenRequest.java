package tn.transporttn.dto.notification;

import jakarta.validation.constraints.Size;

/** token = null pour ne plus recevoir de notifications push (déconnexion). */
public record FcmTokenRequest(
        @Size(max = 512, message = "Jeton de notification invalide")
        String token
) {
}
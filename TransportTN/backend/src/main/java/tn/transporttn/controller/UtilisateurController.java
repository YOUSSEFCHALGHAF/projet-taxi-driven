package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.notification.FcmTokenRequest;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.NotificationService;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UtilisateurController {
    private final NotificationService notificationService;

    /** Enregistre (ou efface avec null) le jeton Firebase Cloud Messaging de ce navigateur. */
    @PutMapping("/fcm-token")
    public ResponseEntity<Void> jetonFcm(@AuthenticationPrincipal UtilisateurPrincipal utilisateur,
                                         @Valid @RequestBody FcmTokenRequest requete) {
        notificationService.enregistrerJetonFcm(utilisateur.getId(), requete.token());
        return ResponseEntity.noContent().build();
    }
}
package tn.transporttn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.commun.PageResponse;
import tn.transporttn.dto.notification.NotificationResponse;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.NotificationService;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationResponse> lister(@AuthenticationPrincipal UtilisateurPrincipal utilisateur,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int taille) {
        return notificationService.lister(utilisateur.getId(), page, taille);
    }

    @GetMapping("/non-lues")
    public Map<String, Long> nonLues(@AuthenticationPrincipal UtilisateurPrincipal utilisateur) {
        return Map.of("nombre", notificationService.compterNonLues(utilisateur.getId()));
    }

    @PutMapping("/{id}/lue")
    public NotificationResponse marquerLue(@AuthenticationPrincipal UtilisateurPrincipal utilisateur,
                                           @PathVariable Long id) {
        return notificationService.marquerLue(utilisateur.getId(), id);
    }

    @PutMapping("/lues")
    public Map<String, Integer> toutMarquerLu(@AuthenticationPrincipal UtilisateurPrincipal utilisateur) {
        return Map.of("misesAJour", notificationService.toutMarquerLu(utilisateur.getId()));
    }
}
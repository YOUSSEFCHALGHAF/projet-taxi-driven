package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.commun.PageResponse;
import tn.transporttn.dto.notification.NotificationResponse;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.Notification;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.TypeNotification;
import tn.transporttn.push.EnvoiPush;
import tn.transporttn.repository.NotificationRepository;
import tn.transporttn.repository.UtilisateurRepository;
import tn.transporttn.tempsreel.TempsReelService;

/**
 * Une notification = 3 canaux :
 *  1. enregistrée en base (historique de la cloche) ;
 *  2. poussée par WebSocket (cloche mise à jour instantanément si la page est ouverte) ;
 *  3. envoyée par Firebase Cloud Messaging (si l'utilisateur a autorisé les notifications).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int TAILLE_PAGE_MAX = 50;
    private final TempsReelService tempsReelService;
    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ApplicationEventPublisher evenements;

    @Transactional
    public void notifier(Utilisateur destinataire, TypeNotification type, String titre, String message) {
        Notification notification = notificationRepository.save(Notification.builder()
                .utilisateur(destinataire)
                .type(type)
                .titre(titre)
                .message(message)
                .build());

        NotificationResponse reponse = NotificationResponse.depuis(notification);
        tempsReelService.publierNotification(destinataire.getTelephone(), reponse);

        if (destinataire.getFcmToken() != null) {
            evenements.publishEvent(new EnvoiPush(destinataire.getFcmToken(), titre, message, type, reponse.lien()));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> lister(Long utilisateurId, int page, int taille) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(taille, 1), TAILLE_PAGE_MAX));
        return PageResponse.depuis(
                notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(utilisateurId, pageable),
                NotificationResponse::depuis);
    }

    @Transactional(readOnly = true)
    public long compterNonLues(Long utilisateurId) {
        return notificationRepository.countByUtilisateurIdAndLueFalse(utilisateurId);
    }

    @Transactional
    public NotificationResponse marquerLue(Long utilisateurId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUtilisateurId(notificationId, utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Notification introuvable"));
        notification.marquerCommeLue();
        return NotificationResponse.depuis(notification);
    }

    @Transactional
    public int toutMarquerLu(Long utilisateurId) {
        return notificationRepository.marquerToutesCommeLues(utilisateurId);
    }

    /** Rattache ce navigateur (jeton FCM) au compte ; null pour le détacher. */
    @Transactional
    public void enregistrerJetonFcm(Long utilisateurId, String jeton) {
        String nettoye = (jeton == null || jeton.isBlank()) ? null : jeton.trim();
        if (nettoye != null) {
            utilisateurRepository.effacerJetonFcm(nettoye);   // retiré d'un éventuel autre compte
        }
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
        utilisateur.setFcmToken(nettoye);
    }
}
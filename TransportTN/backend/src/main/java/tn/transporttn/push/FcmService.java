package tn.transporttn.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.WebpushConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.transporttn.repository.UtilisateurRepository;

/**
 * Envoie les notifications push via Firebase Cloud Messaging.
 * Messages "data" : c'est notre service worker qui décide de les afficher
 * (uniquement si l'application n'est pas déjà à l'écran).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final ObjectProvider<FirebaseMessaging> firebaseMessaging;   // absent si Firebase désactivé
    private final UtilisateurRepository utilisateurRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void envoyer(EnvoiPush envoi) {
        FirebaseMessaging messaging = firebaseMessaging.getIfAvailable();
        if (messaging == null) {
            return;
        }

        Message message = Message.builder()
                .setToken(envoi.jeton())
                .putData("titre", envoi.titre())
                .putData("message", envoi.message())
                .putData("type", envoi.type().name())
                .putData("lien", envoi.lien())
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("Urgency", "high")   // livraison immédiate
                        .putHeader("TTL", "3600")       // inutile après 1 heure
                        .build())
                .build();

        try {
            log.debug("Notification push envoyée ({})", messaging.send(message));
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                // Le navigateur a retiré l'autorisation ou le jeton a expiré : on l'oublie
                utilisateurRepository.effacerJetonFcm(envoi.jeton());
                log.info("Jeton FCM expiré supprimé");
            } else {
                log.warn("Échec de l'envoi push : {}", e.getMessage());
            }
        }
    }
}
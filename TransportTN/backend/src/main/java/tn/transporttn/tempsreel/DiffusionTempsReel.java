package tn.transporttn.tempsreel;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envoie les messages par WebSocket, une fois la transaction validée.
 * Si la transaction échoue (rollback), rien n'est envoyé.
 * Un utilisateur non connecté ne reçoit rien : il verra l'état à jour en ouvrant la page.
 */
@Component
@RequiredArgsConstructor
public class DiffusionTempsReel {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void diffuser(MessageUtilisateur message) {
        message.destinataires().forEach(telephone ->
                messagingTemplate.convertAndSendToUser(telephone, message.destination(), message.contenu()));
    }
}
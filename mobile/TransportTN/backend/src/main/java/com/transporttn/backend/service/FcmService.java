package com.transporttn.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Envoi de notifications push via Firebase Cloud Messaging.
 * SEUL point d'usage de Firebase dans ce backend (l'authentification n'utilise pas Firebase).
 */
@Service
@Slf4j
public class FcmService {

    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (fcmToken == null || fcmToken.isBlank()) {
            log.warn("Pas de token FCM disponible, notification ignoree: {}", title);
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Notification FCM envoyee: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Erreur lors de l'envoi de la notification FCM: {}", e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Firebase non initialise, notification ignoree (voir FirebaseConfig): {}", title);
        }
    }
}

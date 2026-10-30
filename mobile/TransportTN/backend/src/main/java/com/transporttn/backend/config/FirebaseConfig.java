package com.transporttn.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initialisation de Firebase Admin SDK - UTILISE UNIQUEMENT POUR LES NOTIFICATIONS PUSH (FCM).
 * L'authentification des utilisateurs n'utilise PAS Firebase (voir OtpService / AuthController).
 *
 * Placez votre fichier de credentials (telecharge depuis la Console Firebase :
 * Parametres du projet > Comptes de service > Generer une nouvelle cle privee)
 * a l'emplacement defini par app.firebase.credentials-path (par defaut: firebase-service-account.json
 * a la racine du module backend). Ce fichier NE DOIT JAMAIS etre commite (voir .gitignore).
 */
@Component
@Slf4j
public class FirebaseConfig {

    @Value("${app.firebase.credentials-path:firebase-service-account.json}")
    private String credentialsPath;

    @Value("${app.firebase.enabled:true}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void init() {
        if (!firebaseEnabled) {
            log.warn("Firebase est desactive (app.firebase.enabled=false). Les notifications push ne seront pas envoyees.");
            return;
        }
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = new java.io.FileInputStream(credentialsPath);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialise avec succes (notifications FCM).");
            }
        } catch (IOException e) {
            log.error("Impossible de charger les credentials Firebase depuis '{}'. " +
                    "Les notifications push seront desactivees. Erreur: {}", credentialsPath, e.getMessage());
        }
    }
}

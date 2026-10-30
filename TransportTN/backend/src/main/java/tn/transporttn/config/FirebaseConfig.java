package tn.transporttn.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(FirebaseProperties proprietes) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        try (InputStream cle = new FileInputStream(proprietes.credentialsPath())) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(cle))
                    .build();
            FirebaseApp application = FirebaseApp.initializeApp(options);
            log.info("Firebase Cloud Messaging activé (projet {})", application.getOptions().getProjectId());
            return application;
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Fichier du compte de service Firebase introuvable : "
                    + proprietes.credentialsPath()
                    + ". Corrigez app.firebase.credentials-path ou mettez app.firebase.enabled à false.", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp application) {
        return FirebaseMessaging.getInstance(application);
    }
}
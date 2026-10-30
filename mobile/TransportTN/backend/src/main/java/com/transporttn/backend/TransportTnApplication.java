package com.transporttn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Point d'entree de l'application TransportTN Backend.
 *
 * Architecture: Controller -> Service -> Repository -> Entity (JPA)
 * Authentification: Telephone + OTP (genere par le backend) -> JWT applicatif
 * Notifications: Firebase Cloud Messaging (FCM) uniquement pour les push
 */
@SpringBootApplication
@EnableScheduling
public class TransportTnApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransportTnApplication.class, args);
    }
}

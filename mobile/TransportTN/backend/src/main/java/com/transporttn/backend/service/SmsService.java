package com.transporttn.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service d'envoi de SMS pour les codes OTP.
 *
 * IMPLEMENTATION MOCKEE pour ce projet academique : le code est simplement logue
 * cote serveur (et renvoye dans la reponse API si app.otp.debug-mode=true) au lieu
 * d'etre reellement envoye par SMS.
 *
 * Pour passer en production, remplacer le corps de sendOtp() par un appel a un
 * provider SMS tunisien/international (ex: Twilio, Infobip, ou un agregateur local
 * comme celui utilise par les operateurs Ooredoo/Orange/Tunisie Telecom).
 */
@Service
@Slf4j
public class SmsService {

    @Value("${app.otp.debug-mode:true}")
    private boolean debugMode;

    public void sendOtp(String phone, String code) {
        // TODO PRODUCTION: integrer un vrai provider SMS ici.
        log.info("[SMS SIMULE] Envoi du code OTP {} au numero {}", code, phone);
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}

package com.transporttn.backend.service;

import com.transporttn.backend.entity.OtpCode;
import com.transporttn.backend.repository.OtpCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Generation et verification des codes OTP (One-Time Password) a 6 chiffres.
 * Remplace Firebase Phone Auth pour l'authentification (Firebase reste utilise
 * uniquement pour les notifications push, voir FcmService).
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final SmsService smsService;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private int expirationMinutes;

    /**
     * Genere un nouveau code OTP, le persiste et l'envoie par SMS (simule).
     * @return le code genere (utile uniquement en mode debug pour les tests)
     */
    public String generateAndSendOtp(String phone) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpCode otpCode = OtpCode.builder()
                .phone(phone)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .used(false)
                .build();

        otpCodeRepository.save(otpCode);
        smsService.sendOtp(phone, code);

        return code;
    }

    /**
     * Verifie le code fourni par l'utilisateur. Si valide, marque le code comme utilise
     * (usage unique) pour empecher le rejeu.
     */
    public boolean verifyOtp(String phone, String code) {
        Optional<OtpCode> otpOpt = otpCodeRepository.findTopByPhoneAndUsedFalseOrderByCreatedAtDesc(phone);

        if (otpOpt.isEmpty()) {
            return false;
        }

        OtpCode otp = otpOpt.get();

        if (!otp.getCode().equals(code)) {
            return false;
        }

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);
        return true;
    }
}

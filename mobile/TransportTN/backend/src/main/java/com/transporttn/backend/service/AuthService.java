package com.transporttn.backend.service;

import com.transporttn.backend.dto.AuthResponseDto;
import com.transporttn.backend.dto.OtpVerifyDto;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.exception.BadRequestException;
import com.transporttn.backend.repository.UserRepository;
import com.transporttn.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OtpService otpService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * Etape 1 du flux d'authentification: demande d'envoi d'un code OTP.
     * @return le code genere si le mode debug est actif (sinon null, le code est envoye par SMS)
     */
    public String requestOtp(String phone) {
        return otpService.generateAndSendOtp(phone);
    }

    /**
     * Etape 2 du flux d'authentification: verification du code OTP.
     * Si l'utilisateur n'existe pas encore (premiere connexion), il est cree automatiquement
     * (necessite le champ "name" dans la requete).
     */
    public AuthResponseDto verifyOtpAndLogin(OtpVerifyDto request) {
        boolean valid = otpService.verifyOtp(request.getPhone(), request.getCode());

        if (!valid) {
            throw new BadRequestException("Code OTP invalide ou expire");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    if (request.getName() == null || request.getName().isBlank()) {
                        throw new BadRequestException("Le nom est requis pour la premiere inscription");
                    }
                    User newUser = User.builder()
                            .phone(request.getPhone())
                            .name(request.getName())
                            .role(User.Role.CLIENT)
                            .build();
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole().name());

        return AuthResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .build();
    }
}

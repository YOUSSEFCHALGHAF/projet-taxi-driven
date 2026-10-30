package com.transporttn.backend.controller;

import com.transporttn.backend.dto.AuthResponseDto;
import com.transporttn.backend.dto.OtpRequestDto;
import com.transporttn.backend.dto.OtpVerifyDto;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Etape 1: demande d'un code OTP envoye par SMS (simule) au numero fourni. */
    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, Object>> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        String debugCode = authService.requestOtp(request.getPhone());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Code OTP envoye au " + request.getPhone());
        // En mode debug (dev/academique), on renvoie le code pour faciliter les tests sans SMS reel.
        response.put("debugCode", debugCode);
        return ResponseEntity.ok(response);
    }

    /** Etape 2: verification du code OTP -> retourne un JWT applicatif. */
    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyDto request) {
        return ResponseEntity.ok(authService.verifyOtpAndLogin(request));
    }

    /** Profil de l'utilisateur actuellement connecte (a partir du JWT). */
    @GetMapping("/me")
    public ResponseEntity<User> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
}

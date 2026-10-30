package com.transporttn.backend.controller;

import com.transporttn.backend.dto.FcmTokenDto;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** Enregistre/met a jour le token FCM de l'appareil, necessaire pour recevoir les notifications push. */
    @PutMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody FcmTokenDto request) {
        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }
}

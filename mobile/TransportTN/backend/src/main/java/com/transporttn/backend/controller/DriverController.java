package com.transporttn.backend.controller;

import com.transporttn.backend.dto.DriverLocationDto;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    /** Mise a jour de la position GPS du chauffeur (route REST, en complement du flux Firestore cote Android). */
    @PutMapping("/location")
    public ResponseEntity<Void> updateLocation(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody DriverLocationDto request) {
        driverService.updateLocation(user, request.getLatitude(), request.getLongitude());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/available")
    public ResponseEntity<Void> updateAvailability(@AuthenticationPrincipal User user,
                                                     @RequestParam boolean available) {
        driverService.updateAvailability(user, available);
        return ResponseEntity.noContent().build();
    }
}

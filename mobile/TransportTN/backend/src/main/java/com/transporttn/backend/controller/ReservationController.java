package com.transporttn.backend.controller;

import com.transporttn.backend.dto.CollectifReservationRequestDto;
import com.transporttn.backend.dto.ReservationStatusUpdateDto;
import com.transporttn.backend.dto.TaxiReservationRequestDto;
import com.transporttn.backend.entity.Reservation;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/taxi")
    public ResponseEntity<Reservation> createTaxi(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody TaxiReservationRequestDto request) {
        return ResponseEntity.ok(reservationService.createTaxiReservation(user, request));
    }

    @PostMapping("/collectif")
    public ResponseEntity<Reservation> createCollectif(@AuthenticationPrincipal User user,
                                                         @Valid @RequestBody CollectifReservationRequestDto request) {
        return ResponseEntity.ok(reservationService.createCollectifReservation(user, request));
    }

    /** Mise a jour du statut d'une reservation (utilise par le chauffeur ou l'admin). */
    @PutMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody ReservationStatusUpdateDto request) {
        return ResponseEntity.ok(reservationService.updateStatus(id, request.getStatus()));
    }

    /** Historique paginee des reservations du client connecte. */
    @GetMapping("/history")
    public ResponseEntity<Page<Reservation>> history(@AuthenticationPrincipal User user,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reservationService.getHistory(user.getId(), pageable));
    }
}

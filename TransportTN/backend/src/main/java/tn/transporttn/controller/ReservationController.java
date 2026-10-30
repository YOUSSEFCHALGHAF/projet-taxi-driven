package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.commun.PageResponse;
import tn.transporttn.dto.reservation.*;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/collectif")
    public ResponseEntity<ReservationCollectifResponse> collectif(
            @AuthenticationPrincipal UtilisateurPrincipal client,
            @Valid @RequestBody ReservationCollectifRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.reserverCollectif(client.getId(), requete));
    }

    @PostMapping("/bus")
    public ResponseEntity<ReservationBusResponse> bus(@AuthenticationPrincipal UtilisateurPrincipal client,
                                                      @Valid @RequestBody ReservationBusRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.acheterTickets(client.getId(), requete));
    }

    /** Ex : /api/reservations/historique?page=0&taille=10 */
    @GetMapping("/historique")
    public PageResponse<ReservationResumeResponse> historique(@AuthenticationPrincipal UtilisateurPrincipal client,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int taille) {
        return reservationService.historique(client.getId(), page, taille);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> tickets(@AuthenticationPrincipal UtilisateurPrincipal client) {
        return reservationService.mesTickets(client.getId());
    }

    @PutMapping("/{id}/annulation")
    public ReservationResumeResponse annuler(@AuthenticationPrincipal UtilisateurPrincipal client,
                                             @PathVariable Long id) {
        return reservationService.annuler(client.getId(), id);
    }
}
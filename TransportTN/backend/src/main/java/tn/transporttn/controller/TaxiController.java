package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.taxi.EstimationRequest;
import tn.transporttn.dto.taxi.EstimationResponse;
import tn.transporttn.dto.taxi.ReservationTaxiRequest;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.TaxiService;

@RestController
@RequestMapping("/api/reservations/taxi")
@RequiredArgsConstructor
public class TaxiController {

    private final TaxiService taxiService;

    @PostMapping("/estimation")
    public EstimationResponse estimer(@Valid @RequestBody EstimationRequest requete) {
        return taxiService.estimer(requete);
    }

    @PostMapping
    public ResponseEntity<ReservationTaxiResponse> reserver(@AuthenticationPrincipal UtilisateurPrincipal client,
                                                           @Valid @RequestBody ReservationTaxiRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxiService.reserver(client.getId(), requete));
    }

    @GetMapping("/{id}")
    public ReservationTaxiResponse consulter(@AuthenticationPrincipal UtilisateurPrincipal client,
                                             @PathVariable Long id) {
        return taxiService.consulter(client.getId(), id);
    }

    /** Suivi par interrogation régulière ; remplacé par WebSocket à l'étape 6 (gardé en secours). */
    @GetMapping("/{id}/position")
    public PositionResponse position(@AuthenticationPrincipal UtilisateurPrincipal client,
                                     @PathVariable Long id) {
        return taxiService.positionChauffeur(client.getId(), id);
    }
}
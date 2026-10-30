package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.chauffeur.ChangementStatutRequest;
import tn.transporttn.dto.chauffeur.ChauffeurResponse;
import tn.transporttn.dto.chauffeur.DisponibiliteRequest;
import tn.transporttn.dto.chauffeur.PositionRequest;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.ChauffeurService;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class ChauffeurController {

    private final ChauffeurService chauffeurService;

    @GetMapping("/profil")
    public ChauffeurResponse profil(@AuthenticationPrincipal UtilisateurPrincipal chauffeur) {
        return chauffeurService.profil(chauffeur.getId());
    }

    @PutMapping("/disponibilite")
    public ChauffeurResponse disponibilite(@AuthenticationPrincipal UtilisateurPrincipal chauffeur,
                                           @Valid @RequestBody DisponibiliteRequest requete) {
        return chauffeurService.changerDisponibilite(chauffeur.getId(), requete.disponible());
    }

    @PutMapping("/position")
    public PositionResponse position(@AuthenticationPrincipal UtilisateurPrincipal chauffeur,
                                     @Valid @RequestBody PositionRequest requete) {
        return chauffeurService.mettreAJourPosition(chauffeur.getId(), requete.latitude(), requete.longitude());
    }

    @GetMapping("/courses")
    public List<ReservationTaxiResponse> courses(@AuthenticationPrincipal UtilisateurPrincipal chauffeur) {
        return chauffeurService.coursesActives(chauffeur.getId());
    }

    @PutMapping("/courses/{id}/statut")
    public ReservationTaxiResponse statut(@AuthenticationPrincipal UtilisateurPrincipal chauffeur,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ChangementStatutRequest requete) {
        return chauffeurService.changerStatut(chauffeur.getId(), id, requete.statut());
    }

    @PutMapping("/courses/{id}/refus")
    public ResponseEntity<Void> refuser(@AuthenticationPrincipal UtilisateurPrincipal chauffeur,
                                        @PathVariable Long id) {
        chauffeurService.refuser(chauffeur.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
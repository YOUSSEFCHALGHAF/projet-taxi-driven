package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.admin.*;
import tn.transporttn.dto.auth.UtilisateurResponse;
import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.dto.catalogue.LigneResponse;
import tn.transporttn.dto.chauffeur.ChauffeurResponse;
import tn.transporttn.dto.reservation.TicketResponse;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.AdminService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---------- Lignes ----------

    @GetMapping("/lignes")
    public List<LigneResponse> lignes() {
        return adminService.listerLignes();
    }

    @PostMapping("/lignes")
    public ResponseEntity<LigneResponse> creerLigne(@Valid @RequestBody LigneRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.creerLigne(requete));
    }

    @PutMapping("/lignes/{id}")
    public LigneResponse modifierLigne(@PathVariable Long id, @Valid @RequestBody LigneRequest requete) {
        return adminService.modifierLigne(id, requete);
    }

    @PatchMapping("/lignes/{id}/activation")
    public LigneResponse activationLigne(@PathVariable Long id, @RequestParam boolean active) {
        return adminService.changerActivationLigne(id, active);
    }

    // ---------- Départs ----------

    @PostMapping("/lignes/{id}/departs")
    public ResponseEntity<DepartResponse> creerDepart(@PathVariable Long id,
                                                      @Valid @RequestBody DepartRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.creerDepart(id, requete));
    }

    @PostMapping("/lignes/{id}/departs/generation")
    public ResponseEntity<GenerationDepartsResponse> genererDeparts(@PathVariable Long id,
                                                                    @Valid @RequestBody GenerationDepartsRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.genererDeparts(id, requete));
    }

    @DeleteMapping("/departs/{id}")
    public ResponseEntity<Void> supprimerDepart(@PathVariable Long id) {
        adminService.supprimerDepart(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Chauffeurs et comptes ----------

    @GetMapping("/chauffeurs")
    public List<ChauffeurResponse> chauffeurs() {
        return adminService.listerChauffeurs();
    }

    @PostMapping("/chauffeurs")
    public ResponseEntity<ChauffeurResponse> creerChauffeur(@Valid @RequestBody ChauffeurCreationRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.creerChauffeur(requete));
    }

    @PatchMapping("/utilisateurs/{id}/activation")
    public UtilisateurResponse activationUtilisateur(@AuthenticationPrincipal UtilisateurPrincipal admin,
                                                     @PathVariable Long id,
                                                     @RequestParam boolean actif) {
        return adminService.changerActivationUtilisateur(admin.getId(), id, actif);
    }

    // ---------- Statistiques et tickets ----------

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() {
        return adminService.statistiques();
    }

    @PostMapping("/tickets/validation")
    public TicketResponse validerTicket(@Valid @RequestBody TicketValidationRequest requete) {
        return adminService.validerTicket(requete.code());
    }
}
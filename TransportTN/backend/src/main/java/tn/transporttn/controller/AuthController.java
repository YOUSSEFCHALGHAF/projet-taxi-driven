package tn.transporttn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.auth.AuthResponse;
import tn.transporttn.dto.auth.InscriptionRequest;
import tn.transporttn.dto.auth.LoginRequest;
import tn.transporttn.dto.auth.UtilisateurResponse;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Créer un compte client. Réponse : 201 + jeton (connexion automatique). */
    @PostMapping("/inscription")
    public ResponseEntity<AuthResponse> inscription(@Valid @RequestBody InscriptionRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrire(requete));
    }

    /** Se connecter avec téléphone + mot de passe. Réponse : 200 + jeton. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest requete) {
        return authService.connecter(requete);
    }

    /** Profil de l'utilisateur connecté (identifié par son jeton). */
    @GetMapping("/me")
    public UtilisateurResponse moi(@AuthenticationPrincipal UtilisateurPrincipal utilisateur) {
        return authService.profil(utilisateur.getId());
    }
}
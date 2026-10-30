package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.auth.AuthResponse;
import tn.transporttn.dto.auth.InscriptionRequest;
import tn.transporttn.dto.auth.LoginRequest;
import tn.transporttn.dto.auth.UtilisateurResponse;
import tn.transporttn.exception.ConflitException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;
import tn.transporttn.repository.UtilisateurRepository;
import tn.transporttn.security.JwtService;
import tn.transporttn.security.UtilisateurPrincipal;
import tn.transporttn.util.TelephoneUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Inscription publique : crée toujours un CLIENT.
     * Les comptes DRIVER et ADMIN sont créés par un administrateur (étape 4).
     */
    @Transactional
    public AuthResponse inscrire(InscriptionRequest requete) {
        String telephone = TelephoneUtils.normaliser(requete.telephone());
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new ConflitException("Ce numéro de téléphone est déjà utilisé");
        }

        String email = normaliserEmail(requete.email());
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new ConflitException("Cette adresse e-mail est déjà utilisée");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(requete.nom().trim())
                .prenom(requete.prenom().trim())
                .telephone(telephone)
                .email(email)
                .motDePasse(passwordEncoder.encode(requete.motDePasse()))
                .role(Role.CLIENT)
                .build();
        utilisateurRepository.save(utilisateur);

        return genererReponse(utilisateur);   // connexion automatique après inscription
    }

    /**
     * Vérifie téléphone + mot de passe via Spring Security.
     * Lève BadCredentialsException (401) ou DisabledException (403).
     */
    @Transactional(readOnly = true)
    public AuthResponse connecter(LoginRequest requete) {
        String telephone = TelephoneUtils.normaliser(requete.telephone());

        Authentication authentification = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(telephone, requete.motDePasse()));

        UtilisateurPrincipal principal = (UtilisateurPrincipal) authentification.getPrincipal();
        Utilisateur utilisateur = utilisateurRepository.findById(principal.getId())
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));

        return genererReponse(utilisateur);
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse profil(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .map(UtilisateurResponse::depuis)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }

    private AuthResponse genererReponse(Utilisateur utilisateur) {
        String token = jwtService.genererToken(UtilisateurPrincipal.depuis(utilisateur));
        return AuthResponse.bearer(token, jwtService.getExpirationSecondes(),
                UtilisateurResponse.depuis(utilisateur));
    }

    private String normaliserEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
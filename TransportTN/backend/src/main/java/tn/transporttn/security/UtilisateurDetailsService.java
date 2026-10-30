package tn.transporttn.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.repository.UtilisateurRepository;

/** Charge un utilisateur depuis MySQL à partir de son numéro de téléphone. */
@Service
@RequiredArgsConstructor
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String telephone) throws UsernameNotFoundException {
        return utilisateurRepository.findByTelephone(telephone)
                .map(UtilisateurPrincipal::depuis)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
    }
}
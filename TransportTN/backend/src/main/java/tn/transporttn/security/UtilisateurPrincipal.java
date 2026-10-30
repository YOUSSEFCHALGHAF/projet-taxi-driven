package tn.transporttn.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;

import java.util.Collection;
import java.util.List;

@Getter
public class UtilisateurPrincipal implements UserDetails {

    private final Long id;
    private final String telephone;
    private final String motDePasse;
    private final String nomComplet;
    private final Role role;
    private final boolean actif;

    private UtilisateurPrincipal(Long id, String telephone, String motDePasse,
                                 String nomComplet, Role role, boolean actif) {
        this.id = id;
        this.telephone = telephone;
        this.motDePasse = motDePasse;
        this.nomComplet = nomComplet;
        this.role = role;
        this.actif = actif;
    }

    public static UtilisateurPrincipal depuis(Utilisateur u) {
        return new UtilisateurPrincipal(
                u.getId(), u.getTelephone(), u.getMotDePasse(),
                u.getNomComplet(), u.getRole(), u.isActif());
    }

    /** Spring Security attend le préfixe ROLE_ pour hasRole("ADMIN"). */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    /** L'identifiant de connexion est le numéro de téléphone. */
    @Override
    public String getUsername() {
        return telephone;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
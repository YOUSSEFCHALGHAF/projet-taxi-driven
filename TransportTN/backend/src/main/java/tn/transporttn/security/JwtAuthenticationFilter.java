package tn.transporttn.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lit l'en-tête "Authorization: Bearer <jeton>", vérifie le jeton
 * et déclare l'utilisateur comme authentifié pour la durée de la requête.
 * En cas de problème, il n'interrompt pas la requête : il laisse
 * Spring Security répondre 401 si la route est protégée.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Attribut de requête lu par RestAuthenticationEntryPoint pour un message précis. */
    public static final String ATTRIBUT_ERREUR_JWT = "transporttn.jwt.erreur";

    private static final String PREFIXE = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest requete,
                                    HttpServletResponse reponse,
                                    FilterChain chaine) throws ServletException, IOException {

        String entete = requete.getHeader(HttpHeaders.AUTHORIZATION);
        if (entete == null || !entete.startsWith(PREFIXE)) {
            chaine.doFilter(requete, reponse);   // pas de jeton : route publique ou 401 plus loin
            return;
        }

        String jeton = entete.substring(PREFIXE.length()).trim();
        try {
            String telephone = jwtService.extraireTelephone(jeton);
            if (telephone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails utilisateur = userDetailsService.loadUserByUsername(telephone);

                if (utilisateur.isEnabled()) {
                    var authentification = new UsernamePasswordAuthenticationToken(
                            utilisateur, null, utilisateur.getAuthorities());
                    authentification.setDetails(new WebAuthenticationDetailsSource().buildDetails(requete));

                    SecurityContext contexte = SecurityContextHolder.createEmptyContext();
                    contexte.setAuthentication(authentification);
                    SecurityContextHolder.setContext(contexte);
                } else {
                    requete.setAttribute(ATTRIBUT_ERREUR_JWT, "Ce compte est désactivé");
                }
            }
        } catch (ExpiredJwtException e) {
            requete.setAttribute(ATTRIBUT_ERREUR_JWT, "Session expirée, veuillez vous reconnecter");
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            requete.setAttribute(ATTRIBUT_ERREUR_JWT, "Jeton d'authentification invalide");
        }

        chaine.doFilter(requete, reponse);
    }
}
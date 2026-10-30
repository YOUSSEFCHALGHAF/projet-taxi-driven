package tn.transporttn.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Sécurité des WebSocket :
 *  - CONNECT   : le jeton JWT (en-tête STOMP "Authorization") est obligatoire et vérifié ;
 *  - SUBSCRIBE : uniquement les files personnelles /user/queue/... ;
 *  - SEND      : interdit (le navigateur passe par l'API REST pour agir).
 */
@Component
@RequiredArgsConstructor
public class AuthentificationStompInterceptor implements ChannelInterceptor {

    private static final String PREFIXE = "Bearer ";
    private static final String FILES_AUTORISEES = "/user/queue/";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel canal) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT -> authentifier(accessor);
            case SUBSCRIBE -> verifierAbonnement(accessor);
            case SEND -> throw new MessagingException("Envoi de messages non autorisé");
            default -> {
                // DISCONNECT, battements de cœur… : rien à vérifier
            }
        }
        return message;
    }

    private void authentifier(StompHeaderAccessor accessor) {
        String entete = accessor.getFirstNativeHeader("Authorization");
        if (entete == null || !entete.startsWith(PREFIXE)) {
            throw new MessagingException("Authentification requise");
        }
        try {
            String telephone = jwtService.extraireTelephone(entete.substring(PREFIXE.length()).trim());
            UserDetails utilisateur = userDetailsService.loadUserByUsername(telephone);
            if (!utilisateur.isEnabled()) {
                throw new MessagingException("Ce compte est désactivé");
            }
            // getName() de cet objet = numéro de téléphone : c'est la clé des files /user/...
            accessor.setUser(new UsernamePasswordAuthenticationToken(utilisateur, null, utilisateur.getAuthorities()));
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            throw new MessagingException("Jeton d'authentification invalide ou expiré");
        }
    }

    private void verifierAbonnement(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (accessor.getUser() == null || destination == null || !destination.startsWith(FILES_AUTORISEES)) {
            throw new MessagingException("Abonnement refusé : " + destination);
        }
    }
}
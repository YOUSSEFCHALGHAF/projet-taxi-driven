package tn.transporttn.security;

import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthentificationStompInterceptorTest {

    private static final String TEL = "+21622111222";

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final MessageChannel canal = mock(MessageChannel.class);
    private final AuthentificationStompInterceptor intercepteur =
            new AuthentificationStompInterceptor(jwtService, userDetailsService);

    private UtilisateurPrincipal client(boolean actif) {
        return UtilisateurPrincipal.depuis(Utilisateur.builder()
                .id(1L).nom("Ben Salah").prenom("Amira").telephone(TEL)
                .motDePasse("hash").role(Role.CLIENT).actif(actif).build());
    }

    private Message<byte[]> trame(StompCommand commande, String autorisation, String destination, Principal utilisateur) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(commande);
        if (autorisation != null) accessor.setNativeHeader("Authorization", autorisation);
        if (destination != null) accessor.setDestination(destination);
        if (utilisateur != null) accessor.setUser(utilisateur);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private Principal connecte() {
        UtilisateurPrincipal u = client(true);
        return new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
    }

    @Test
    void connexionAvecJetonValideAttacheLUtilisateur() {
        when(jwtService.extraireTelephone("bon-jeton")).thenReturn(TEL);
        when(userDetailsService.loadUserByUsername(TEL)).thenReturn(client(true));
        Message<byte[]> message = trame(StompCommand.CONNECT, "Bearer bon-jeton", null, null);

        intercepteur.preSend(message, canal);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo(TEL);
    }

    @Test
    void connexionSansJetonRefusee() {
        assertThatThrownBy(() -> intercepteur.preSend(trame(StompCommand.CONNECT, null, null, null), canal))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void jetonInvalideRefuse() {
        when(jwtService.extraireTelephone("faux")).thenThrow(new MalformedJwtException("mal formé"));
        assertThatThrownBy(() -> intercepteur.preSend(trame(StompCommand.CONNECT, "Bearer faux", null, null), canal))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void compteDesactiveRefuse() {
        when(jwtService.extraireTelephone("jeton")).thenReturn(TEL);
        when(userDetailsService.loadUserByUsername(TEL)).thenReturn(client(false));
        assertThatThrownBy(() -> intercepteur.preSend(trame(StompCommand.CONNECT, "Bearer jeton", null, null), canal))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void abonnementAUneFilePersonnelleAutorise() {
        assertThatCode(() -> intercepteur.preSend(
                trame(StompCommand.SUBSCRIBE, null, "/user/queue/courses", connecte()), canal))
                .doesNotThrowAnyException();
    }

    @Test
    void abonnementDirectAUneFileInterneRefuse() {
        assertThatThrownBy(() -> intercepteur.preSend(
                trame(StompCommand.SUBSCRIBE, null, "/queue/courses-user123", connecte()), canal))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void envoiDeMessageRefuse() {
        assertThatThrownBy(() -> intercepteur.preSend(trame(StompCommand.SEND, null, "/app/test", connecte()), canal))
                .isInstanceOf(MessagingException.class);
    }
}
package tn.transporttn.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import tn.transporttn.security.AuthentificationStompInterceptor;

/**
 * Point de connexion : ws://localhost:8080/ws
 * Files personnelles : /user/queue/courses et /user/queue/notifications
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;
    private final AuthentificationStompInterceptor authentificationStomp;
    private TaskScheduler planificateur;

    /** Planificateur fourni par Spring, utilisé pour les battements de cœur (heartbeats). */
    @Autowired
    public void setPlanificateur(@Lazy @Qualifier("messageBrokerTaskScheduler") TaskScheduler planificateur) {
        this.planificateur = planificateur;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registre) {
        registre.addEndpoint("/ws")
                .setAllowedOrigins(corsProperties.allowedOrigins().toArray(new String[0]));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registre) {
        registre.enableSimpleBroker("/queue")
                .setHeartbeatValue(new long[]{10_000, 10_000})   // détecte les connexions mortes
                .setTaskScheduler(planificateur);
        registre.setApplicationDestinationPrefixes("/app");
        registre.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authentificationStomp);
    }
}
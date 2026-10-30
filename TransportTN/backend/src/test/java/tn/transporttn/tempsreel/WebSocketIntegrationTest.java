package tn.transporttn.tempsreel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Nécessite MySQL : docker compose up -d */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    private static final String TEL_CLIENT = "+21622111222";
    private static final String TEL_AUTRE_CLIENT = "+21698333444";

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    private WebSocketStompClient client;

    @BeforeEach
    void creerClient() {
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @AfterEach
    void arreterClient() {
        client.stop();
    }

    @SuppressWarnings("rawtypes")
    private String jeton(String telephone) {
        Map corps = rest.postForObject("/api/auth/login",
                Map.of("telephone", telephone, "motDePasse", "Password123"), Map.class);
        return (String) corps.get("token");
    }

    private CompletableFuture<StompSession> connecter(String jeton) {
        StompHeaders entetes = new StompHeaders();
        if (jeton != null) entetes.add("Authorization", "Bearer " + jeton);
        return client.connectAsync("ws://localhost:" + port + "/ws",
                new WebSocketHttpHeaders(), entetes, new StompSessionHandlerAdapter() { });
    }

    @Test
    void connexionAvecJetonValide() throws Exception {
        StompSession session = connecter(jeton(TEL_CLIENT)).get(5, SECONDS);
        assertThat(session.isConnected()).isTrue();
        session.disconnect();
    }

    @Test
    void connexionSansJetonRefusee() {
        assertThatThrownBy(() -> connecter(null).get(5, SECONDS))
                .isInstanceOfAny(ExecutionException.class, TimeoutException.class);
    }

    @Test
    void unMessageNArriveQuASonDestinataire() throws Exception {
        BlockingQueue<Map<String, Object>> recus = new LinkedBlockingQueue<>();
        StompSession session = connecter(jeton(TEL_CLIENT)).get(5, SECONDS);

        session.subscribe("/user/queue/courses", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                recus.add((Map<String, Object>) payload);
            }
        });
        Thread.sleep(500);   // le temps que l'abonnement soit enregistré côté serveur

        messagingTemplate.convertAndSendToUser(TEL_CLIENT, "/queue/courses", Map.of("type", "POUR_AMIRA"));
        messagingTemplate.convertAndSendToUser(TEL_AUTRE_CLIENT, "/queue/courses", Map.of("type", "POUR_YOUSSEF"));

        assertThat(recus.poll(5, SECONDS)).containsEntry("type", "POUR_AMIRA");
        assertThat(recus.poll(1, SECONDS)).isNull();   // le message de Youssef n'arrive pas chez Amira
        session.disconnect();
    }
}
package tn.transporttn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base des tests d'API : MockMvc + vraie base MySQL (docker compose up -d).
 * Chaque test est annulé (rollback) à la fin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class ApiTestSupport {

    protected static final String MDP = "Password123";
    protected static final String TEL_CLIENT = "+21622111222";
    protected static final String TEL_CLIENT_2 = "+21698333444";
    protected static final String TEL_ADMIN = "+21620000000";
    protected static final String TEL_CHAUFFEUR_1 = "+21655444555";   // Lafayette (Tunis)
    protected static final String TEL_CHAUFFEUR_2 = "+21650666777";   // Ariana

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    protected String bearer(String telephone) throws Exception {
        MvcResult resultat = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", telephone, "motDePasse", MDP))))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + lire(resultat).get("token").asText();
    }

    protected String json(Object objet) throws Exception {
        return objectMapper.writeValueAsString(objet);
    }

    protected JsonNode lire(MvcResult resultat) throws Exception {
        return objectMapper.readTree(resultat.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    /** Premier départ de demain sur la première ligne du type demandé (BUS ou COLLECTIF). */
    protected JsonNode premierDepartDemain(String type) throws Exception {
        JsonNode lignes = lire(mockMvc.perform(get("/api/lignes").param("type", type))
                .andExpect(status().isOk()).andReturn());
        long ligneId = lignes.get(0).get("id").asLong();

        JsonNode departs = lire(mockMvc.perform(get("/api/lignes/{id}/departs", ligneId)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk()).andReturn());

        assertThat(departs.size())
                .as("Aucun départ demain : réinitialisez la base (docker compose down -v && docker compose up -d)")
                .isGreaterThan(0);
        return departs.get(0);
    }
}
package tn.transporttn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Nécessite MySQL : docker compose up -d
 * Comptes de démonstration : mot de passe "Password123".
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    private static final String MDP = "Password123";
    private static final String TEL_CLIENT = "+21622111222";
    private static final String TEL_ADMIN = "+21620000000";
    private static final String TEL_CHAUFFEUR = "+21655444555";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ---------------- Connexion ----------------

    @Test
    void connexionReussieRenvoieUnJeton() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", "22 111 222", "motDePasse", MDP))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.utilisateur.telephone").value(TEL_CLIENT))
                .andExpect(jsonPath("$.utilisateur.role").value("CLIENT"))
                .andExpect(jsonPath("$.utilisateur.motDePasse").doesNotExist());
    }

    @Test
    void mauvaisMotDePasseRenvoie401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", TEL_CLIENT, "motDePasse", "Mauvais123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statut").value(401));
    }

    @Test
    void connexionSansChampsRenvoie400AvecDetails() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.telephone", notNullValue()))
                .andExpect(jsonPath("$.details.motDePasse", notNullValue()));
    }

    // ---------------- Inscription ----------------

    @Test
    void inscriptionCreeUnClient() throws Exception {
        mockMvc.perform(post("/api/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nom", "Hammami", "prenom", "Leila",
                                "telephone", "29 876 543", "email", "Leila@Example.tn",
                                "motDePasse", "MotDePasse1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.utilisateur.telephone").value("+21629876543"))
                .andExpect(jsonPath("$.utilisateur.email").value("leila@example.tn"))
                .andExpect(jsonPath("$.utilisateur.role").value("CLIENT"));
    }

    @Test
    void inscriptionAvecTelephoneExistantRenvoie409() throws Exception {
        mockMvc.perform(post("/api/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nom", "Test", "prenom", "Doublon",
                                "telephone", TEL_CLIENT, "motDePasse", "MotDePasse1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statut").value(409));
    }

    @Test
    void motDePasseTropFaibleRenvoie400() throws Exception {
        mockMvc.perform(post("/api/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nom", "Test", "prenom", "Faible",
                                "telephone", "29 000 111", "motDePasse", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.motDePasse", notNullValue()));
    }

    // ---------------- /me et jetons ----------------

    @Test
    void profilSansJetonRenvoie401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));
    }

    @Test
    void profilAvecJetonValide() throws Exception {
        String jeton = obtenirJeton(TEL_CLIENT);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value(TEL_CLIENT))
                .andExpect(jsonPath("$.prenom").value("Amira"));
    }

    @Test
    void jetonInvalideRenvoie401AvecMessage() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", containsString("invalide")));
    }

    // ---------------- Rôles ----------------

    @Test
    void clientNePeutPasAccederAuxRoutesAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/statistiques").header("Authorization", "Bearer " + obtenirJeton(TEL_CLIENT)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statut").value(403));
    }

    @Test
    void adminAccedeAuxRoutesAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/statistiques").header("Authorization", "Bearer " + obtenirJeton(TEL_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void chauffeurAccedeAuxRoutesChauffeurMaisPasAdmin() throws Exception {
        String jeton = obtenirJeton(TEL_CHAUFFEUR);
        mockMvc.perform(get("/api/driver/profil").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/statistiques").header("Authorization", "Bearer " + jeton))
                .andExpect(status().isForbidden());
    }

    // ---------------- CORS ----------------

    @Test
    void corsAutoriseLeFrontendVite() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsRefuseUnSiteInconnu() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://site-malveillant.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---------------- Outils ----------------

    private String obtenirJeton(String telephone) throws Exception {
        String corps = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", telephone, "motDePasse", MDP))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corps).get("token").asText();
    }

    private String json(Object objet) throws Exception {
        return objectMapper.writeValueAsString(objet);
    }
}
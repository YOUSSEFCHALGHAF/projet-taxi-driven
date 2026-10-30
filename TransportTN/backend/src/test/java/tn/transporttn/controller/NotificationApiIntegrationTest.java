package tn.transporttn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import tn.transporttn.repository.UtilisateurRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationApiIntegrationTest extends ApiTestSupport {

    @Autowired private UtilisateurRepository utilisateurRepository;

    private void reserverUnePlace(String jeton) throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");
        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombrePlaces", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated());
    }

    @Test
    void uneReservationApparaitDansLaCloche() throws Exception {
        String jeton = bearer(TEL_CLIENT);
        reserverUnePlace(jeton);

        mockMvc.perform(get("/api/notifications").header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[0].type").value("RESERVATION"))
                .andExpect(jsonPath("$.contenu[0].lien").value("/historique"))
                .andExpect(jsonPath("$.contenu[0].lue").value(false));

        mockMvc.perform(get("/api/notifications/non-lues").header("Authorization", jeton))
                .andExpect(jsonPath("$.nombre", greaterThanOrEqualTo(1)));

        mockMvc.perform(put("/api/notifications/lues").header("Authorization", jeton))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications/non-lues").header("Authorization", jeton))
                .andExpect(jsonPath("$.nombre").value(0));
    }

    @Test
    void onNePeutPasLireLaNotificationDUnAutre() throws Exception {
        String jeton = bearer(TEL_CLIENT);
        reserverUnePlace(jeton);
        long id = lire(mockMvc.perform(get("/api/notifications").header("Authorization", jeton)).andReturn())
                .get("contenu").get(0).get("id").asLong();

        mockMvc.perform(put("/api/notifications/{id}/lue", id).header("Authorization", bearer(TEL_CLIENT_2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void unJetonFcmNAppartientQuAuDernierCompteConnecte() throws Exception {
        String corps = json(Map.of("token", "jeton-navigateur-123"));

        mockMvc.perform(put("/api/users/me/fcm-token").header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isNoContent());
        mockMvc.perform(put("/api/users/me/fcm-token").header("Authorization", bearer(TEL_CLIENT_2))
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findByTelephone(TEL_CLIENT).orElseThrow().getFcmToken()).isNull();
        assertThat(utilisateurRepository.findByTelephone(TEL_CLIENT_2).orElseThrow().getFcmToken())
                .isEqualTo("jeton-navigateur-123");
    }

    @Test
    void laDeconnexionEffaceLeJetonFcm() throws Exception {
        String jeton = bearer(TEL_CLIENT);
        mockMvc.perform(put("/api/users/me/fcm-token").header("Authorization", jeton)
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("token", "abc"))));

        mockMvc.perform(put("/api/users/me/fcm-token").header("Authorization", jeton)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\": null}"))
                .andExpect(status().isNoContent());

        assertThat(utilisateurRepository.findByTelephone(TEL_CLIENT).orElseThrow().getFcmToken()).isNull();
    }
}
package tn.transporttn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ParcoursClientIntegrationTest extends ApiTestSupport {

    // ---------- Catalogue ----------

    @Test
    void catalogueAccessibleSansConnexion() throws Exception {
        mockMvc.perform(get("/api/lignes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))));

        mockMvc.perform(get("/api/lignes").param("type", "BUS").param("villeArrivee", "sousse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].villeArrivee").value("Sousse"));
    }

    @Test
    void departsDansLePasseRefuses() throws Exception {
        mockMvc.perform(get("/api/lignes/1/departs").param("date", LocalDate.now().minusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void typeDeLigneInvalideRenvoie400() throws Exception {
        mockMvc.perform(get("/api/lignes").param("type", "AVION"))
                .andExpect(status().isBadRequest());
    }

    // ---------- Taxi collectif ----------

    @Test
    void reserverDesPlacesEnCollectif() throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");
        int placesAvant = depart.get("placesRestantes").asInt();

        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombrePlaces", 2, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference", startsWith("TC-")))
                .andExpect(jsonPath("$.statut").value("ACCEPTEE"))
                .andExpect(jsonPath("$.nombrePlaces").value(2))
                .andExpect(jsonPath("$.depart.placesRestantes").value(placesAvant - 2));
    }

    @Test
    void plusDeQuatrePlacesRefusees() throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");

        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombrePlaces", 5, "modePaiement", "ESPECES"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.nombrePlaces", notNullValue()));
    }

    @Test
    void departDeBusRefusePourUnCollectif() throws Exception {
        JsonNode departBus = premierDepartDemain("BUS");

        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", departBus.get("id").asLong(),
                                "nombrePlaces", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unChauffeurNePeutPasReserver() throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");

        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CHAUFFEUR_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombrePlaces", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isForbidden());
    }

    // ---------- Bus et tickets ----------

    @Test
    void acheterDesTicketsDeBusAvecQrCodes() throws Exception {
        JsonNode depart = premierDepartDemain("BUS");
        String jeton = bearer(TEL_CLIENT);

        mockMvc.perform(post("/api/reservations/bus")
                        .header("Authorization", jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombreTickets", 2, "modePaiement", "CARTE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference", startsWith("BS-")))
                .andExpect(jsonPath("$.tickets", hasSize(2)))
                .andExpect(jsonPath("$.tickets[0].codeUnique", hasLength(36)))
                .andExpect(jsonPath("$.tickets[0].qrCodeBase64", startsWith("iVBOR")))   // signature PNG
                .andExpect(jsonPath("$.tickets[0].statut").value("VALIDE"));

        mockMvc.perform(get("/api/reservations/tickets").header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ---------- Historique et annulation ----------

    @Test
    void historiquePagineEtAnnulationLibereLesPlaces() throws Exception {
        String jeton = bearer(TEL_CLIENT);
        JsonNode depart = premierDepartDemain("COLLECTIF");
        long departId = depart.get("id").asLong();
        int placesAvant = depart.get("placesRestantes").asInt();

        JsonNode reservation = lire(mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", departId, "nombrePlaces", 3, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(get("/api/reservations/historique").param("page", "0").param("taille", "5")
                        .header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.contenu[0].type").value("COLLECTIF"))
                .andExpect(jsonPath("$.taille").value(5));

        mockMvc.perform(put("/api/reservations/{id}/annulation", reservation.get("id").asLong())
                        .header("Authorization", jeton))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));

        // Les 3 places sont de nouveau disponibles
        JsonNode departApres = premierDepartDemain("COLLECTIF");
        org.assertj.core.api.Assertions.assertThat(departApres.get("placesRestantes").asInt()).isEqualTo(placesAvant);

        // Une deuxième annulation est refusée
        mockMvc.perform(put("/api/reservations/{id}/annulation", reservation.get("id").asLong())
                        .header("Authorization", jeton))
                .andExpect(status().isConflict());
    }

    @Test
    void unClientNePeutPasAnnulerLaReservationDUnAutre() throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");
        JsonNode reservation = lire(mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombrePlaces", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated()).andReturn());

        mockMvc.perform(put("/api/reservations/{id}/annulation", reservation.get("id").asLong())
                        .header("Authorization", bearer(TEL_CLIENT_2)))
                .andExpect(status().isNotFound());
    }
}
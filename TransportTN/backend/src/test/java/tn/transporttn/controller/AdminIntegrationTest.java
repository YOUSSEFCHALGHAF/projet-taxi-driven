package tn.transporttn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminIntegrationTest extends ApiTestSupport {

    private static Map<String, Object> ligne(String type, int capacite) {
        return Map.of("type", type, "villeDepart", "Tunis", "villeArrivee", "Gabès",
                "stationDepart", "Gare routière Bab Alioua", "stationArrivee", "Gare routière de Gabès",
                "prix", 27.500, "capacite", capacite, "dureeMinutes", 360);
    }

    @Test
    void creerUneLigneEtGenererSesDeparts() throws Exception {
        String admin = bearer(TEL_ADMIN);

        JsonNode ligne = lire(mockMvc.perform(post("/api/admin/lignes")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ligne("BUS", 50))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.villeArrivee").value("Gabès"))
                .andReturn());
        long ligneId = ligne.get("id").asLong();

        LocalDate demain = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/admin/lignes/{id}/departs/generation", ligneId)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("dateDebut", demain.toString(),
                                "dateFin", demain.plusDays(1).toString(),
                                "heures", List.of("08:00", "16:00")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departsCrees").value(4))
                .andExpect(jsonPath("$.departsIgnores").value(0));

        // Visibles dans le catalogue public
        mockMvc.perform(get("/api/lignes/{id}/departs", ligneId).param("date", demain.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].placesRestantes").value(50));
    }

    @Test
    void louageDePlusDeHuitPlacesRefuse() throws Exception {
        mockMvc.perform(post("/api/admin/lignes")
                        .header("Authorization", bearer(TEL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ligne("COLLECTIF", 12))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerUnChauffeurQuiPeutSeConnecter() throws Exception {
        mockMvc.perform(post("/api/admin/chauffeurs")
                        .header("Authorization", bearer(TEL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "nom", "Mejri", "prenom", "Karim", "telephone", "52 333 999",
                                "motDePasse", "Password123", "numeroPermis", "tn-permis-777001",
                                "matricule", "230tu5512", "modeleVehicule", "Kia Rio (jaune)"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricule").value("230 TU 5512"))
                .andExpect(jsonPath("$.disponible").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", "52333999", "motDePasse", "Password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilisateur.role").value("DRIVER"));
    }

    @Test
    void statistiquesReserveesALAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/statistiques").header("Authorization", bearer(TEL_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clients", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.lignesActives", greaterThanOrEqualTo(7)))
                .andExpect(jsonPath("$.reservationsParStatut.ANNULEE", notNullValue()));

        mockMvc.perform(get("/api/admin/statistiques").header("Authorization", bearer(TEL_CLIENT)))
                .andExpect(status().isForbidden());
    }

    @Test
    void validerUnTicketUneSeuleFois() throws Exception {
        JsonNode depart = premierDepartDemain("BUS");
        JsonNode achat = lire(mockMvc.perform(post("/api/reservations/bus")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", depart.get("id").asLong(),
                                "nombreTickets", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated()).andReturn());
        String scan = "TRANSPORTTN:" + achat.get("tickets").get(0).get("codeUnique").asText();
        String admin = bearer(TEL_ADMIN);

        mockMvc.perform(post("/api/admin/tickets/validation")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("code", scan))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("UTILISE"));

        mockMvc.perform(post("/api/admin/tickets/validation")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("code", scan))))
                .andExpect(status().isConflict());
    }

    @Test
    void departAvecReservationsNonSupprimable() throws Exception {
        JsonNode depart = premierDepartDemain("COLLECTIF");
        long departId = depart.get("id").asLong();

        mockMvc.perform(post("/api/reservations/collectif")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("departId", departId, "nombrePlaces", 1, "modePaiement", "ESPECES"))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/admin/departs/{id}", departId).header("Authorization", bearer(TEL_ADMIN)))
                .andExpect(status().isConflict());
    }

    @Test
    void compteDesactiveNePeutPlusSeConnecter() throws Exception {
        long idYoussef = lire(mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", TEL_CLIENT_2, "motDePasse", MDP))))
                .andReturn()).get("utilisateur").get("id").asLong();

        mockMvc.perform(patch("/api/admin/utilisateurs/{id}/activation", idYoussef)
                        .param("actif", "false")
                        .header("Authorization", bearer(TEL_ADMIN)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("telephone", TEL_CLIENT_2, "motDePasse", MDP))))
                .andExpect(status().isForbidden());
    }
}
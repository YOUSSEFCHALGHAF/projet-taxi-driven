package tn.transporttn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ParcoursTaxiIntegrationTest extends ApiTestSupport {

    /** Avenue Habib Bourguiba (Tunis) → La Marsa. Chauffeur le plus proche : Mohamed Jaziri. */
    private static final Map<String, Object> COURSE = Map.of(
            "adresseDepart", "Avenue Habib Bourguiba, Tunis",
            "latDepart", 36.8000, "lngDepart", 10.1850,
            "adresseArrivee", "La Marsa Plage",
            "latArrivee", 36.8782, "lngArrivee", 10.3247,
            "modePaiement", "ESPECES");

    @Test
    void estimationDuPrix() throws Exception {
        mockMvc.perform(post("/api/reservations/taxi/estimation")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("latDepart", 36.8000, "lngDepart", 10.1850,
                                "latArrivee", 36.8782, "lngArrivee", 10.3247))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm", greaterThan(10.0)))
                .andExpect(jsonPath("$.prixEstime", greaterThan(2.5)))
                .andExpect(jsonPath("$.chauffeursDisponibles").value(2));
    }

    @Test
    void coordonneesHorsTunisieRefusees() throws Exception {
        mockMvc.perform(post("/api/reservations/taxi/estimation")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("latDepart", 48.85, "lngDepart", 2.35,       // Paris
                                "latArrivee", 36.8782, "lngArrivee", 10.3247))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.latDepart", notNullValue()));
    }

    @Test
    void reservationAttribueLeChauffeurLePlusProche() throws Exception {
        mockMvc.perform(post("/api/reservations/taxi")
                        .header("Authorization", bearer(TEL_CLIENT))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(COURSE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reference", startsWith("TX-")))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.chauffeur.telephone").value(TEL_CHAUFFEUR_1));
    }

    @Test
    void deuxCoursesActivesPourLeMemeClientRefusees() throws Exception {
        String jeton = bearer(TEL_CLIENT);
        reserver(jeton);

        mockMvc.perform(post("/api/reservations/taxi")
                        .header("Authorization", jeton)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(COURSE)))
                .andExpect(status().isConflict());
    }

    @Test
    void cycleCompletDUneCourse() throws Exception {
        String client = bearer(TEL_CLIENT);
        String chauffeur = bearer(TEL_CHAUFFEUR_1);
        long id = reserver(client);

        mockMvc.perform(get("/api/driver/courses").header("Authorization", chauffeur))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Position non disponible tant que la course n'est pas acceptée
        mockMvc.perform(get("/api/reservations/taxi/{id}/position", id).header("Authorization", client))
                .andExpect(status().isConflict());

        changerStatut(chauffeur, id, "ACCEPTEE").andExpect(status().isOk());

        mockMvc.perform(get("/api/reservations/taxi/{id}/position", id).header("Authorization", client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude", notNullValue()));

        changerStatut(chauffeur, id, "EN_COURS").andExpect(status().isOk());

        // Une course en cours ne peut plus être annulée par le client
        mockMvc.perform(put("/api/reservations/{id}/annulation", id).header("Authorization", client))
                .andExpect(status().isConflict());

        changerStatut(chauffeur, id, "TERMINEE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINEE"));

        // Le chauffeur est de nouveau disponible
        mockMvc.perform(get("/api/driver/profil").header("Authorization", chauffeur))
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void transitionInterditeRefusee() throws Exception {
        long id = reserver(bearer(TEL_CLIENT));
        changerStatut(bearer(TEL_CHAUFFEUR_1), id, "TERMINEE").andExpect(status().isConflict());
    }

    @Test
    void refusReattribueAuChauffeurSuivant() throws Exception {
        String client = bearer(TEL_CLIENT);
        long id = reserver(client);

        mockMvc.perform(put("/api/driver/courses/{id}/refus", id).header("Authorization", bearer(TEL_CHAUFFEUR_1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reservations/taxi/{id}", id).header("Authorization", client))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.chauffeur.telephone").value(TEL_CHAUFFEUR_2));
    }

    @Test
    void unChauffeurNeVoitPasLaCourseDUnAutre() throws Exception {
        long id = reserver(bearer(TEL_CLIENT));
        changerStatut(bearer(TEL_CHAUFFEUR_2), id, "ACCEPTEE").andExpect(status().isNotFound());
    }

    @Test
    void unClientNeVoitPasLaCourseDUnAutre() throws Exception {
        long id = reserver(bearer(TEL_CLIENT));
        mockMvc.perform(get("/api/reservations/taxi/{id}", id).header("Authorization", bearer(TEL_CLIENT_2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void miseAJourDePositionDuChauffeur() throws Exception {
        String chauffeur = bearer(TEL_CHAUFFEUR_1);

        mockMvc.perform(put("/api/driver/position")
                        .header("Authorization", chauffeur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("latitude", 36.8065, "longitude", 10.1815))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(36.8065))
                .andExpect(jsonPath("$.derniereMiseAJour", notNullValue()));

        mockMvc.perform(put("/api/driver/position")
                        .header("Authorization", chauffeur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("latitude", 48.85, "longitude", 2.35))))
                .andExpect(status().isBadRequest());
    }

    // ---------- Outils ----------

    private long reserver(String jetonClient) throws Exception {
        return lire(mockMvc.perform(post("/api/reservations/taxi")
                        .header("Authorization", jetonClient)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(COURSE)))
                .andExpect(status().isCreated()).andReturn()).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions changerStatut(String jetonChauffeur, long id,
                                                                             String statut) throws Exception {
        return mockMvc.perform(put("/api/driver/courses/{id}/statut", id)
                .header("Authorization", jetonChauffeur)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("statut", statut))));
    }
}
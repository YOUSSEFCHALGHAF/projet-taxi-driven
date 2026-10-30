package tn.transporttn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.transporttn.repository.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de vérification de l'étape 2 : confirme que l'application
 * démarre, se connecte à MySQL et lit les données d'exemple.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final UtilisateurRepository utilisateurRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final LigneRepository ligneRepository;
    private final DepartRepository departRepository;
    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> health() {
        Map<String, Object> tables = new LinkedHashMap<>();
        tables.put("utilisateurs", utilisateurRepository.count());
        tables.put("chauffeurs", chauffeurRepository.count());
        tables.put("lignes", ligneRepository.count());
        tables.put("departs", departRepository.count());
        tables.put("reservations", reservationRepository.count());
        tables.put("tickets", ticketRepository.count());
        tables.put("notifications", notificationRepository.count());

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("statut", "UP");
        reponse.put("baseDeDonnees", "MySQL connectée");
        reponse.put("horodatage", LocalDateTime.now());
        reponse.put("tables", tables);
        return reponse;
    }
}

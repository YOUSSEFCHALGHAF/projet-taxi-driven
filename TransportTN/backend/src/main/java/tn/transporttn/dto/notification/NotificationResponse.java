package tn.transporttn.dto.notification;

import tn.transporttn.model.Notification;
import tn.transporttn.model.enums.Role;
import tn.transporttn.model.enums.TypeNotification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String titre,
        String message,
        TypeNotification type,
        boolean lue,
        LocalDateTime dateEnvoi,
        String lien                 // page du frontend à ouvrir au clic
) {
    public static NotificationResponse depuis(Notification n) {
        return new NotificationResponse(n.getId(), n.getTitre(), n.getMessage(), n.getType(), n.isLue(),
                n.getDateEnvoi(), lienPour(n.getUtilisateur().getRole(), n.getType()));
    }

    /** Calculé à la volée : pas besoin de nouvelle colonne en base. */
    public static String lienPour(Role role, TypeNotification type) {
        return switch (role) {
            case DRIVER -> "/chauffeur";
            case ADMIN -> "/admin";
            case CLIENT -> switch (type) {
                case TICKET -> "/mes-tickets";
                case COURSE -> "/tableau-de-bord";
                default -> "/historique";
            };
        };
    }
}
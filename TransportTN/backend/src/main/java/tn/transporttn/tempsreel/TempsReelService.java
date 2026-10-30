package tn.transporttn.tempsreel;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.notification.NotificationResponse;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;
import tn.transporttn.model.Chauffeur;
import tn.transporttn.model.ReservationTaxi;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Prépare les messages temps réel. À appeler DANS une transaction :
 * le contenu est construit tout de suite (relations JPA accessibles),
 * l'envoi n'a lieu qu'après le COMMIT.
 */
@Service
@RequiredArgsConstructor
public class TempsReelService {

    public static final String FILE_COURSES = "/queue/courses";
    public static final String FILE_NOTIFICATIONS = "/queue/notifications";

    private final ApplicationEventPublisher evenements;

    /** Nouvel état d'une course → client, chauffeur attribué, et éventuels autres destinataires. */
    public void publierCourse(ReservationTaxi course, String... autresDestinataires) {
        Set<String> destinataires = new LinkedHashSet<>();
        destinataires.add(course.getClient().getTelephone());
        if (course.getChauffeur() != null) {
            destinataires.add(course.getChauffeur().getUtilisateur().getTelephone());
        }
        destinataires.addAll(Arrays.asList(autresDestinataires));

        evenements.publishEvent(new MessageUtilisateur(destinataires, FILE_COURSES,
                EvenementCourse.course(ReservationTaxiResponse.depuis(course))));
    }

    /** Position du chauffeur → seulement le client de la course suivie. */
    public void publierPosition(ReservationTaxi course) {
        Chauffeur chauffeur = course.getChauffeur();
        PositionResponse position = new PositionResponse(
                chauffeur.getLatitude(), chauffeur.getLongitude(), chauffeur.getDerniereMiseAJour());

        evenements.publishEvent(new MessageUtilisateur(List.of(course.getClient().getTelephone()), FILE_COURSES,
                EvenementCourse.position(course.getId(), position)));
    }

    /** Nouvelle notification → la cloche de l'utilisateur, instantanément. */
    public void publierNotification(String telephone, NotificationResponse notification) {
        evenements.publishEvent(new MessageUtilisateur(List.of(telephone), FILE_NOTIFICATIONS, notification));
    }
}
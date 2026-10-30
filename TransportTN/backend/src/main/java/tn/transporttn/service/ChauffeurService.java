package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.chauffeur.ChauffeurResponse;
import tn.transporttn.dto.commun.PositionResponse;
import tn.transporttn.dto.taxi.ReservationTaxiResponse;
import tn.transporttn.exception.ConflitException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.Chauffeur;
import tn.transporttn.model.ReservationTaxi;
import tn.transporttn.model.enums.StatutReservation;
import tn.transporttn.model.enums.TypeNotification;
import tn.transporttn.repository.ChauffeurRepository;
import tn.transporttn.repository.ReservationTaxiRepository;
import tn.transporttn.tempsreel.TempsReelService;

import java.util.List;
import java.util.Set;

import static tn.transporttn.model.enums.StatutReservation.*;

@Service
@RequiredArgsConstructor
public class ChauffeurService {

    private final ChauffeurRepository chauffeurRepository;
    private final ReservationTaxiRepository reservationTaxiRepository;
    private final TaxiService taxiService;
    private final NotificationService notificationService;
    private final TempsReelService tempsReelService;
    private static final List<StatutReservation> COURSES_SUIVIES = List.of(ACCEPTEE, EN_COURS);

    @Transactional(readOnly = true)
    public ChauffeurResponse profil(Long utilisateurId) {
        return ChauffeurResponse.depuis(chauffeur(utilisateurId));
    }

    @Transactional
    public ChauffeurResponse changerDisponibilite(Long utilisateurId, boolean disponible) {
        Chauffeur chauffeur = chauffeur(utilisateurId);
        if (disponible && reservationTaxiRepository
                .existsByChauffeurIdAndStatutIn(chauffeur.getId(), TaxiService.STATUTS_ACTIFS)) {
            throw new ConflitException("Terminez votre course en cours avant de redevenir disponible");
        }
        chauffeur.setDisponible(disponible);
        return ChauffeurResponse.depuis(chauffeur);
    }

    @Transactional
    public PositionResponse mettreAJourPosition(Long utilisateurId, double latitude, double longitude) {
        Chauffeur chauffeur = chauffeur(utilisateurId);
        chauffeur.mettreAJourPosition(latitude, longitude);

        // Le client de la course en cours voit le taxi bouger en direct
        reservationTaxiRepository
                .findFirstByChauffeurIdAndStatutInOrderByDateCreationDesc(chauffeur.getId(), COURSES_SUIVIES)
                .ifPresent(tempsReelService::publierPosition);

        return new PositionResponse(chauffeur.getLatitude(), chauffeur.getLongitude(),
                chauffeur.getDerniereMiseAJour());
    }

    @Transactional(readOnly = true)
    public List<ReservationTaxiResponse> coursesActives(Long utilisateurId) {
        Chauffeur chauffeur = chauffeur(utilisateurId);
        return reservationTaxiRepository
                .findByChauffeurIdAndStatutInOrderByDateCreationDesc(chauffeur.getId(), TaxiService.STATUTS_ACTIFS)
                .stream().map(ReservationTaxiResponse::depuis).toList();
    }

    /** Machine à états : EN_ATTENTE → ACCEPTEE → EN_COURS → TERMINEE. */
    @Transactional
    public ReservationTaxiResponse changerStatut(Long utilisateurId, Long courseId, StatutReservation nouveau) {
        Chauffeur chauffeur = chauffeur(utilisateurId);
        ReservationTaxi course = courseDuChauffeur(chauffeur, courseId);
        StatutReservation actuel = course.getStatut();

        boolean autorise = switch (nouveau) {
            case ACCEPTEE -> actuel == EN_ATTENTE;
            case EN_COURS -> actuel == ACCEPTEE;
            case TERMINEE -> actuel == EN_COURS;
            default -> false;   // ANNULEE : passer par /refus ; EN_ATTENTE : impossible
        };
        if (!autorise) {
            throw new ConflitException("Changement de statut impossible : " + actuel + " → " + nouveau);
        }
        course.setStatut(nouveau);

        switch (nouveau) {
            case ACCEPTEE -> notificationService.notifier(course.getClient(), TypeNotification.COURSE,
                    "Chauffeur en route",
                    chauffeur.getUtilisateur().getNomComplet() + " arrive (" + chauffeur.getModeleVehicule()
                            + ", " + chauffeur.getMatricule() + ").");
            case EN_COURS -> notificationService.notifier(course.getClient(), TypeNotification.COURSE,
                    "Course démarrée", "Bon trajet vers " + course.getAdresseArrivee() + " !");
            case TERMINEE -> {
                chauffeur.setDisponible(true);
                notificationService.notifier(course.getClient(), TypeNotification.COURSE,
                        "Course terminée", "Montant à régler : " + course.getMontant() + " DT. Merci !");
            }
            default -> {
            }
        }
        tempsReelService.publierCourse(course);
        return ReservationTaxiResponse.depuis(course);
    }

    /** Le chauffeur refuse : la course passe au chauffeur suivant, ou est annulée. */
    @Transactional
    public void refuser(Long utilisateurId, Long courseId) {
        Chauffeur chauffeur = chauffeur(utilisateurId);
        ReservationTaxi course = courseDuChauffeur(chauffeur, courseId);
        if (course.getStatut() != EN_ATTENTE) {
            throw new ConflitException("Seule une course en attente peut être refusée");
        }

        String telephoneQuiRefuse = chauffeur.getUtilisateur().getTelephone();
        chauffeur.setDisponible(true);
        course.setChauffeur(null);

        if (!taxiService.attribuerChauffeur(course, Set.of(chauffeur.getId()))) {
            course.setStatut(ANNULEE);
            notificationService.notifier(course.getClient(), TypeNotification.COURSE, "Course annulée",
                    "Aucun chauffeur n'est disponible pour " + course.getReference()
                            + ". Veuillez réessayer plus tard.");
        }

        // Le client, le nouveau chauffeur et celui qui a refusé reçoivent le nouvel état.
        tempsReelService.publierCourse(course, telephoneQuiRefuse);
    }

    // ---------------- Outils ----------------

    private Chauffeur chauffeur(Long utilisateurId) {
        return chauffeurRepository.findByUtilisateurId(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Profil chauffeur introuvable"));
    }

    private ReservationTaxi courseDuChauffeur(Chauffeur chauffeur, Long courseId) {
        return reservationTaxiRepository.findById(courseId)
                .filter(c -> c.getChauffeur() != null && c.getChauffeur().getId().equals(chauffeur.getId()))
                .orElseThrow(() -> new RessourceIntrouvableException("Course introuvable"));
    }
}

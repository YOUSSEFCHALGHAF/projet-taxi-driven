package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.admin.*;
import tn.transporttn.dto.auth.UtilisateurResponse;
import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.dto.catalogue.LigneResponse;
import tn.transporttn.dto.chauffeur.ChauffeurResponse;
import tn.transporttn.dto.reservation.TicketResponse;
import tn.transporttn.exception.ConflitException;
import tn.transporttn.exception.RegleMetierException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.*;
import tn.transporttn.model.enums.*;
import tn.transporttn.repository.*;
import tn.transporttn.util.TelephoneUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int CAPACITE_MAX_LOUAGE = 8;
    private static final int GENERATION_MAX_JOURS = 60;

    private final LigneRepository ligneRepository;
    private final DepartRepository departRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ChauffeurRepository chauffeurRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTaxiRepository reservationTaxiRepository;
    private final ReservationCollectifRepository reservationCollectifRepository;
    private final ReservationBusRepository reservationBusRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    // ======================= Lignes =======================

    @Transactional(readOnly = true)
    public List<LigneResponse> listerLignes() {
        return ligneRepository.findAllByOrderByTypeAscVilleDepartAscVilleArriveeAsc()
                .stream().map(LigneResponse::depuis).toList();
    }

    @Transactional
    public LigneResponse creerLigne(LigneRequest r) {
        Ligne ligne = new Ligne();
        appliquer(ligne, r);
        ligne.setActive(true);
        return LigneResponse.depuis(ligneRepository.save(ligne));
    }

    @Transactional
    public LigneResponse modifierLigne(Long id, LigneRequest r) {
        Ligne ligne = ligne(id);
        if (ligne.getType() != r.type() && departRepository.existsByLigneId(id)) {
            throw new ConflitException("Impossible de changer le type d'une ligne qui a déjà des départs");
        }
        appliquer(ligne, r);   // la nouvelle capacité s'applique aux départs créés ensuite
        return LigneResponse.depuis(ligne);
    }

    @Transactional
    public LigneResponse changerActivationLigne(Long id, boolean active) {
        Ligne ligne = ligne(id);
        ligne.setActive(active);
        return LigneResponse.depuis(ligne);
    }

    // ======================= Départs =======================

    @Transactional
    public DepartResponse creerDepart(Long ligneId, DepartRequest r) {
        Ligne ligne = ligne(ligneId);
        LocalDateTime dateHeure = r.dateHeure().truncatedTo(ChronoUnit.MINUTES);

        if (departRepository.existsByLigneIdAndDateHeure(ligneId, dateHeure)) {
            throw new ConflitException("Un départ existe déjà à cette date et heure sur cette ligne");
        }
        return DepartResponse.depuis(departRepository.save(nouveauDepart(ligne, dateHeure)));
    }

    @Transactional
    public GenerationDepartsResponse genererDeparts(Long ligneId, GenerationDepartsRequest r) {
        Ligne ligne = ligne(ligneId);

        if (r.dateFin().isBefore(r.dateDebut())) {
            throw new RegleMetierException("La date de fin doit être après la date de début");
        }
        if (ChronoUnit.DAYS.between(r.dateDebut(), r.dateFin()) >= GENERATION_MAX_JOURS) {
            throw new RegleMetierException("La période ne peut pas dépasser " + GENERATION_MAX_JOURS + " jours");
        }

        int crees = 0;
        int ignores = 0;
        LocalDateTime maintenant = LocalDateTime.now();
        Set<LocalTime> heures = new TreeSet<>(r.heures());   // supprime les doublons et trie

        for (LocalDate jour = r.dateDebut(); !jour.isAfter(r.dateFin()); jour = jour.plusDays(1)) {
            for (LocalTime heure : heures) {
                LocalDateTime dateHeure = jour.atTime(heure.truncatedTo(ChronoUnit.MINUTES));
                if (dateHeure.isBefore(maintenant) || departRepository.existsByLigneIdAndDateHeure(ligneId, dateHeure)) {
                    ignores++;
                } else {
                    departRepository.save(nouveauDepart(ligne, dateHeure));
                    crees++;
                }
            }
        }
        return new GenerationDepartsResponse(crees, ignores);
    }

    @Transactional
    public void supprimerDepart(Long departId) {
        Depart depart = departRepository.findById(departId)
                .orElseThrow(() -> new RessourceIntrouvableException("Départ introuvable"));
        if (reservationCollectifRepository.existsByDepartId(departId)
                || reservationBusRepository.existsByDepartId(departId)) {
            throw new ConflitException("Ce départ a des réservations : il ne peut pas être supprimé");
        }
        departRepository.delete(depart);
    }

    // ======================= Chauffeurs et comptes =======================

    @Transactional(readOnly = true)
    public List<ChauffeurResponse> listerChauffeurs() {
        return chauffeurRepository.findAllAvecUtilisateur().stream().map(ChauffeurResponse::depuis).toList();
    }

    @Transactional
    public ChauffeurResponse creerChauffeur(ChauffeurCreationRequest r) {
        String telephone = TelephoneUtils.normaliser(r.telephone());
        if (utilisateurRepository.existsByTelephone(telephone)) {
            throw new ConflitException("Ce numéro de téléphone est déjà utilisé");
        }
        String email = (r.email() == null || r.email().isBlank()) ? null : r.email().trim().toLowerCase(Locale.ROOT);
        if (email != null && utilisateurRepository.existsByEmail(email)) {
            throw new ConflitException("Cette adresse e-mail est déjà utilisée");
        }
        String matricule = normaliserMatricule(r.matricule());
        if (chauffeurRepository.existsByMatricule(matricule)) {
            throw new ConflitException("Ce matricule est déjà enregistré");
        }
        String permis = r.numeroPermis().trim().toUpperCase(Locale.ROOT);
        if (chauffeurRepository.existsByNumeroPermis(permis)) {
            throw new ConflitException("Ce numéro de permis est déjà enregistré");
        }

        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .nom(r.nom().trim())
                .prenom(r.prenom().trim())
                .telephone(telephone)
                .email(email)
                .motDePasse(passwordEncoder.encode(r.motDePasse()))
                .role(Role.DRIVER)
                .build());

        Chauffeur chauffeur = chauffeurRepository.save(Chauffeur.builder()
                .utilisateur(utilisateur)
                .numeroPermis(permis)
                .matricule(matricule)
                .modeleVehicule(r.modeleVehicule().trim())
                .disponible(false)       // le chauffeur se rend disponible lui-même
                .build());

        return ChauffeurResponse.depuis(chauffeur);
    }

    @Transactional
    public UtilisateurResponse changerActivationUtilisateur(Long adminId, Long utilisateurId, boolean actif) {
        if (adminId.equals(utilisateurId)) {
            throw new RegleMetierException("Vous ne pouvez pas désactiver votre propre compte");
        }
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
        utilisateur.setActif(actif);

        if (!actif && utilisateur.getRole() == Role.DRIVER) {
            chauffeurRepository.findByUtilisateurId(utilisateurId).ifPresent(c -> c.setDisponible(false));
        }
        return UtilisateurResponse.depuis(utilisateur);
    }

    // ======================= Tickets =======================

    @Transactional
    public TicketResponse validerTicket(String texteScanne) {
        String code = tn.transporttn.service.QrCodeService.extraireCode(texteScanne);
        Ticket ticket = ticketRepository.findByCodeUnique(code)
                .orElseThrow(() -> new RessourceIntrouvableException("Ticket inconnu"));
        ticket.marquerUtilise();   // lève une erreur 409 si déjà utilisé ou annulé
        return TicketResponse.depuis(ticket);
    }

    // ======================= Statistiques =======================

    @Transactional(readOnly = true)
    public StatistiquesResponse statistiques() {
        Map<StatutReservation, Long> parStatut = new EnumMap<>(StatutReservation.class);
        for (StatutReservation statut : StatutReservation.values()) {
            parStatut.put(statut, reservationRepository.countByStatut(statut));
        }
        BigDecimal montant = Optional
                .ofNullable(reservationRepository.sommeMontantsHorsStatut(StatutReservation.ANNULEE))
                .orElse(BigDecimal.ZERO);

        return new StatistiquesResponse(
                utilisateurRepository.countByRole(Role.CLIENT),
                utilisateurRepository.countByRole(Role.DRIVER),
                chauffeurRepository.countByDisponibleTrue(),
                ligneRepository.countByActiveTrue(),
                departRepository.countByDateHeureAfter(LocalDateTime.now()),
                reservationRepository.count(),
                reservationTaxiRepository.count(),
                reservationCollectifRepository.count(),
                reservationBusRepository.count(),
                reservationRepository.countByDateCreationAfter(LocalDate.now().atStartOfDay()),
                parStatut,
                ticketRepository.count(),
                ticketRepository.countByStatut(StatutTicket.UTILISE),
                montant);
    }

    // ======================= Outils =======================

    private Ligne ligne(Long id) {
        return ligneRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Ligne introuvable"));
    }

    private void appliquer(Ligne ligne, LigneRequest r) {
        if (r.villeDepart().trim().equalsIgnoreCase(r.villeArrivee().trim())) {
            throw new RegleMetierException("La ville de départ et la ville d'arrivée doivent être différentes");
        }
        if (r.type() == TypeLigne.COLLECTIF && r.capacite() > CAPACITE_MAX_LOUAGE) {
            throw new RegleMetierException("Un taxi collectif (louage) compte au maximum 8 places");
        }
        ligne.setType(r.type());
        ligne.setVilleDepart(r.villeDepart().trim());
        ligne.setVilleArrivee(r.villeArrivee().trim());
        ligne.setStationDepart(r.stationDepart().trim());
        ligne.setStationArrivee(r.stationArrivee().trim());
        ligne.setPrix(r.prix());
        ligne.setCapacite(r.capacite());
        ligne.setDureeMinutes(r.dureeMinutes());
    }

    private Depart nouveauDepart(Ligne ligne, LocalDateTime dateHeure) {
        return Depart.builder()
                .ligne(ligne)
                .dateHeure(dateHeure)
                .placesTotal(ligne.getCapacite())
                .placesRestantes(ligne.getCapacite())
                .build();
    }

    /** "215tu4821" → "215 TU 4821" */
    private String normaliserMatricule(String brut) {
        return brut.trim().toUpperCase(Locale.ROOT).replaceAll("\\s*TU\\s*", " TU ");
    }
}
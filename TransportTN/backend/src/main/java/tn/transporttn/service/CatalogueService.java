package tn.transporttn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.dto.catalogue.LigneResponse;
import tn.transporttn.exception.RegleMetierException;
import tn.transporttn.exception.RessourceIntrouvableException;
import tn.transporttn.model.Ligne;
import tn.transporttn.model.enums.TypeLigne;
import tn.transporttn.repository.DepartRepository;
import tn.transporttn.repository.LigneRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogueService {

    private final LigneRepository ligneRepository;
    private final DepartRepository departRepository;

    public List<LigneResponse> rechercherLignes(TypeLigne type, String villeDepart, String villeArrivee) {
        return ligneRepository.rechercher(type, nettoyer(villeDepart), nettoyer(villeArrivee))
                .stream().map(LigneResponse::depuis).toList();
    }

    public LigneResponse ligne(Long id) {
        return LigneResponse.depuis(ligneActive(id));
    }

    public Map<String, List<String>> villes() {
        return Map.of(
                "departs", ligneRepository.findVillesDepart(),
                "arrivees", ligneRepository.findVillesArrivee());
    }

    /** Départs encore à venir pour le jour demandé (aujourd'hui par défaut). */
    public List<DepartResponse> departs(Long ligneId, LocalDate date) {
        Ligne ligne = ligneActive(ligneId);
        LocalDate aujourdhui = LocalDate.now();
        LocalDate jour = date != null ? date : aujourdhui;

        if (jour.isBefore(aujourdhui)) {
            throw new RegleMetierException("La date de voyage ne peut pas être dans le passé");
        }

        LocalDateTime debut = jour.equals(aujourdhui) ? LocalDateTime.now() : jour.atStartOfDay();
        LocalDateTime fin = jour.atTime(LocalTime.MAX);

        return departRepository.findByLigneEtPeriode(ligne.getId(), debut, fin)
                .stream().map(DepartResponse::depuis).toList();
    }

    private Ligne ligneActive(Long id) {
        return ligneRepository.findById(id)
                .filter(Ligne::isActive)
                .orElseThrow(() -> new RessourceIntrouvableException("Ligne introuvable"));
    }

    private String nettoyer(String texte) {
        return (texte == null || texte.isBlank()) ? null : texte.trim();
    }
}
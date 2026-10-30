package tn.transporttn.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.transporttn.dto.catalogue.DepartResponse;
import tn.transporttn.dto.catalogue.LigneResponse;
import tn.transporttn.model.enums.TypeLigne;
import tn.transporttn.service.CatalogueService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lignes")
@RequiredArgsConstructor
public class CatalogueController {

    private final CatalogueService catalogueService;

    /** Ex : /api/lignes?type=BUS&villeDepart=Tunis&villeArrivee=Sousse */
    @GetMapping
    public List<LigneResponse> lignes(@RequestParam(required = false) TypeLigne type,
                                      @RequestParam(required = false) String villeDepart,
                                      @RequestParam(required = false) String villeArrivee) {
        return catalogueService.rechercherLignes(type, villeDepart, villeArrivee);
    }

    @GetMapping("/villes")
    public Map<String, List<String>> villes() {
        return catalogueService.villes();
    }

    @GetMapping("/{id}")
    public LigneResponse ligne(@PathVariable Long id) {
        return catalogueService.ligne(id);
    }

    /** Ex : /api/lignes/3/departs?date=2026-11-15 */
    @GetMapping("/{id}/departs")
    public List<DepartResponse> departs(@PathVariable Long id,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return catalogueService.departs(id, date);
    }
}
package com.transporttn.backend.controller;

import com.transporttn.backend.dto.LigneResponseDto;
import com.transporttn.backend.entity.Ligne;
import com.transporttn.backend.service.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class LigneController {

    private final LigneService ligneService;

    /** Liste des lignes de Bus disponibles. */
    @GetMapping("/lignes/bus")
    public ResponseEntity<List<LigneResponseDto>> getBusLignes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ligneService.getLignes(Ligne.Mode.BUS, date));
    }

    /** Liste des lignes de Taxi Collectif disponibles, avec places restantes si une date est fournie. */
    @GetMapping("/lignes/collectif")
    public ResponseEntity<List<LigneResponseDto>> getCollectifLignes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ligneService.getLignes(Ligne.Mode.COLLECTIF, date));
    }
}

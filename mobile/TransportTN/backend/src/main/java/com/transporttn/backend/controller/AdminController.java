package com.transporttn.backend.controller;

import com.transporttn.backend.entity.Ligne;
import com.transporttn.backend.service.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints reserves au role ADMIN (voir SecurityConfig). Perimetre P1: creation de lignes uniquement. */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LigneService ligneService;

    @PostMapping("/lignes")
    public ResponseEntity<Ligne> createLigne(@RequestBody Ligne ligne) {
        return ResponseEntity.ok(ligneService.createLigne(ligne));
    }
}

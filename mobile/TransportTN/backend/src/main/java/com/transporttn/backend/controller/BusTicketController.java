package com.transporttn.backend.controller;

import com.transporttn.backend.dto.BusTicketRequestDto;
import com.transporttn.backend.entity.Ticket;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class BusTicketController {

    private final TicketService ticketService;

    @PostMapping("/bus")
    public ResponseEntity<Ticket> purchaseTicket(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody BusTicketRequestDto request) {
        return ResponseEntity.ok(ticketService.purchaseTicket(user, request));
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> myTickets(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ticketService.getMyTickets(user.getId()));
    }
}

package com.transporttn.backend.service;

import com.transporttn.backend.dto.BusTicketRequestDto;
import com.transporttn.backend.entity.Ligne;
import com.transporttn.backend.entity.Ticket;
import com.transporttn.backend.entity.User;
import com.transporttn.backend.exception.BadRequestException;
import com.transporttn.backend.exception.ResourceNotFoundException;
import com.transporttn.backend.repository.LigneRepository;
import com.transporttn.backend.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final LigneRepository ligneRepository;
    private final FcmService fcmService;

    @Transactional
    public Ticket purchaseTicket(User client, BusTicketRequestDto request) {
        Ligne ligne = ligneRepository.findById(request.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne introuvable"));

        if (ligne.getMode() != Ligne.Mode.BUS) {
            throw new BadRequestException("Cette ligne n'est pas une ligne de bus");
        }

        // Genere un QR code infalsifiable : UUID aleatoire encode en base64
        String qrCode = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());

        double totalPrice = ligne.getPrice() * request.getQuantity();

        Ticket ticket = Ticket.builder()
                .client(client)
                .ligne(ligne)
                .travelDate(request.getTravelDate())
                .quantity(request.getQuantity())
                .qrCode(qrCode)
                .price(totalPrice)
                .status(Ticket.Status.VALID)
                .build();

        ticket = ticketRepository.save(ticket);

        fcmService.sendNotification(client.getFcmToken(),
                "Ticket achete !",
                request.getQuantity() + " ticket(s) pour " + ligne.getName(),
                Map.of("ticketId", String.valueOf(ticket.getId()), "type", "TICKET_PURCHASED"));

        return ticket;
    }

    public List<Ticket> getMyTickets(Long clientId) {
        return ticketRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }
}

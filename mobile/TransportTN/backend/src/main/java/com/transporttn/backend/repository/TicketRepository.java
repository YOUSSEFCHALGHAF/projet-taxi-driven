package com.transporttn.backend.repository;

import com.transporttn.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByClientIdOrderByCreatedAtDesc(Long clientId);
    Optional<Ticket> findByQrCode(String qrCode);
}

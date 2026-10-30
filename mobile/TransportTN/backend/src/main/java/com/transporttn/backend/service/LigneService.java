package com.transporttn.backend.service;

import com.transporttn.backend.dto.LigneResponseDto;
import com.transporttn.backend.entity.Ligne;
import com.transporttn.backend.entity.Reservation;
import com.transporttn.backend.repository.LigneRepository;
import com.transporttn.backend.repository.ReservationSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LigneService {

    private final LigneRepository ligneRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public List<LigneResponseDto> getLignes(Ligne.Mode mode, LocalDate travelDate) {
        List<Ligne> lignes = ligneRepository.findByModeAndActiveTrue(mode);

        return lignes.stream().map(ligne -> {
            int seatsAvailable = ligne.getTotalSeats();
            if (mode == Ligne.Mode.COLLECTIF && travelDate != null) {
                int reserved = reservationSeatRepository.countReservedSeats(
                        ligne.getId(), travelDate, Reservation.Status.CANCELLED);
                seatsAvailable = ligne.getTotalSeats() - reserved;
            }
            return LigneResponseDto.builder()
                    .id(ligne.getId())
                    .name(ligne.getName())
                    .departure(ligne.getDeparture())
                    .destination(ligne.getDestination())
                    .departTime(ligne.getDepartTime())
                    .price(ligne.getPrice())
                    .totalSeats(ligne.getTotalSeats())
                    .seatsAvailable(seatsAvailable)
                    .mode(ligne.getMode().name())
                    .build();
        }).toList();
    }

    public Ligne createLigne(Ligne ligne) {
        return ligneRepository.save(ligne);
    }
}

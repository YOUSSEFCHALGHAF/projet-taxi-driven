package com.transporttn.backend.service;

import com.transporttn.backend.dto.CollectifReservationRequestDto;
import com.transporttn.backend.dto.TaxiReservationRequestDto;
import com.transporttn.backend.entity.*;
import com.transporttn.backend.exception.BadRequestException;
import com.transporttn.backend.exception.ConflictException;
import com.transporttn.backend.exception.ResourceNotFoundException;
import com.transporttn.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final LigneRepository ligneRepository;
    private final DriverRepository driverRepository;
    private final MatchingService matchingService;
    private final FcmService fcmService;

    private static final double PRICE_PER_KM = 1.2; // DT / km, tarif indicatif

    @Transactional
    public Reservation createTaxiReservation(User client, TaxiReservationRequestDto request) {
        double distanceKm = matchingService.haversineDistanceKm(
                request.getPickupLat(), request.getPickupLng(),
                request.getDropoffLat(), request.getDropoffLng());

        double price = Math.round(distanceKm * PRICE_PER_KM * 100.0) / 100.0;

        Reservation reservation = Reservation.builder()
                .client(client)
                .type(Reservation.Type.TAXI)
                .status(Reservation.Status.PENDING)
                .pickupAddress(request.getPickupAddress())
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .dropoffAddress(request.getDropoffAddress())
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .paymentMode(request.getPaymentMode())
                .distanceKm(Math.round(distanceKm * 100.0) / 100.0)
                .price(price)
                .build();

        reservation = reservationRepository.save(reservation);

        // Algorithme de matching : recherche asynchrone du chauffeur le plus proche
        Optional<Driver> nearestDriver = matchingService.findNearestAvailableDriver(
                request.getPickupLat(), request.getPickupLng());

        if (nearestDriver.isPresent()) {
            Driver driver = nearestDriver.get();
            reservation.setDriver(driver);
            reservation.setStatus(Reservation.Status.ASSIGNED);
            driver.setAvailable(false);
            driverRepository.save(driver);
            reservation = reservationRepository.save(reservation);

            // Notification push au chauffeur et au client
            fcmService.sendNotification(driver.getUser().getFcmToken(),
                    "Nouvelle course assignee",
                    "Prise en charge: " + request.getPickupAddress(),
                    Map.of("reservationId", String.valueOf(reservation.getId()), "type", "TAXI_ASSIGNED"));

            fcmService.sendNotification(client.getFcmToken(),
                    "Chauffeur trouve !",
                    "Votre chauffeur arrive bientot.",
                    Map.of("reservationId", String.valueOf(reservation.getId()), "type", "TAXI_ASSIGNED"));
        } else {
            fcmService.sendNotification(client.getFcmToken(),
                    "Recherche en cours",
                    "Nous cherchons un chauffeur disponible pres de vous.",
                    Map.of("reservationId", String.valueOf(reservation.getId()), "type", "TAXI_PENDING"));
        }

        return reservation;
    }

    @Transactional
    public Reservation createCollectifReservation(User client, CollectifReservationRequestDto request) {
        Ligne ligne = ligneRepository.findById(request.getLigneId())
                .orElseThrow(() -> new ResourceNotFoundException("Ligne introuvable"));

        if (ligne.getMode() != Ligne.Mode.COLLECTIF) {
            throw new BadRequestException("Cette ligne n'est pas une ligne de taxi collectif");
        }

        int reserved = reservationSeatRepository.countReservedSeats(
                ligne.getId(), request.getTravelDate(), Reservation.Status.CANCELLED);
        int remaining = ligne.getTotalSeats() - reserved;

        if (remaining < request.getSeatsCount()) {
            throw new ConflictException("Places insuffisantes. Places restantes: " + remaining);
        }

        double price = ligne.getPrice() * request.getSeatsCount();

        Reservation reservation = Reservation.builder()
                .client(client)
                .type(Reservation.Type.COLLECTIF)
                .status(Reservation.Status.CONFIRMED)
                .price(price)
                .build();
        reservation = reservationRepository.save(reservation);

        ReservationSeat seat = ReservationSeat.builder()
                .reservation(reservation)
                .ligne(ligne)
                .seatsCount(request.getSeatsCount())
                .travelDate(request.getTravelDate())
                .build();
        reservationSeatRepository.save(seat);

        fcmService.sendNotification(client.getFcmToken(),
                "Reservation confirmee",
                request.getSeatsCount() + " place(s) reservee(s) sur la ligne " + ligne.getName(),
                Map.of("reservationId", String.valueOf(reservation.getId()), "type", "COLLECTIF_CONFIRMED"));

        return reservation;
    }

    @Transactional
    public Reservation updateStatus(Long reservationId, Reservation.Status newStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation introuvable"));

        reservation.setStatus(newStatus);

        if (newStatus == Reservation.Status.COMPLETED || newStatus == Reservation.Status.CANCELLED) {
            if (reservation.getDriver() != null) {
                Driver driver = reservation.getDriver();
                driver.setAvailable(true);
                driverRepository.save(driver);
            }
        }

        reservation = reservationRepository.save(reservation);

        String title = switch (newStatus) {
            case IN_PROGRESS -> "Course demarree";
            case COMPLETED -> "Course terminee";
            case CANCELLED -> "Course annulee";
            default -> "Mise a jour de votre course";
        };

        fcmService.sendNotification(reservation.getClient().getFcmToken(), title,
                "Statut: " + newStatus.name(),
                Map.of("reservationId", String.valueOf(reservation.getId()), "type", "STATUS_UPDATE"));

        return reservation;
    }

    public Page<Reservation> getHistory(Long clientId, Pageable pageable) {
        return reservationRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
    }
}

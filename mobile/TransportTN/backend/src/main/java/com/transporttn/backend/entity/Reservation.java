package com.transporttn.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reservation pour le mode TAXI (course privee) et COLLECTIF (places sur une ligne).
 * Pour le mode COLLECTIF, voir aussi ReservationSeat pour le detail des places/ligne/date.
 */
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    // Champs specifiques au mode TAXI
    @Column(name = "pickup_address", length = 255)
    private String pickupAddress;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lng")
    private Double pickupLng;

    @Column(name = "dropoff_address", length = 255)
    private String dropoffAddress;

    @Column(name = "dropoff_lat")
    private Double dropoffLat;

    @Column(name = "dropoff_lng")
    private Double dropoffLng;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    private Double price;

    private Double distanceKm;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Type {
        TAXI, COLLECTIF
    }

    public enum Status {
        PENDING,      // en attente de matching (taxi) ou confirmation (collectif)
        ASSIGNED,     // chauffeur assigne (taxi)
        CONFIRMED,    // reservation confirmee (collectif)
        IN_PROGRESS,  // course en cours (taxi)
        COMPLETED,    // terminee
        CANCELLED     // annulee
    }

    public enum PaymentMode {
        ESPECES, CARTE
    }
}

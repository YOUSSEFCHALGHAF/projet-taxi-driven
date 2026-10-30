package com.transporttn.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Detail des places reservees pour le Taxi Collectif, lie a une Reservation (type=COLLECTIF).
 */
@Entity
@Table(name = "reservation_seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "ligne_id", nullable = false)
    private Ligne ligne;

    @Column(name = "seats_count", nullable = false)
    private Integer seatsCount;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;
}

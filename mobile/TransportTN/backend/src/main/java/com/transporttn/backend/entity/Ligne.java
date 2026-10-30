package com.transporttn.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Une ligne de transport, utilisee a la fois pour le Bus interurbain et le Taxi Collectif.
 */
@Entity
@Table(name = "lignes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ligne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String departure;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "depart_time", nullable = false)
    private LocalTime departTime;

    @Column(nullable = false)
    private Double price;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Mode mode;

    @Builder.Default
    private Boolean active = true;

    public enum Mode {
        BUS, COLLECTIF
    }
}

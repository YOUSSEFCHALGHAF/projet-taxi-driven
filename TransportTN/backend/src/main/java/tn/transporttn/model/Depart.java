package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "departs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligne_id", nullable = false)
    private Ligne ligne;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "places_total", nullable = false)
    private int placesTotal;

    @Column(name = "places_restantes", nullable = false)
    private int placesRestantes;

    /**
     * Verrou optimiste : Hibernate ajoute "WHERE version = ?" à chaque UPDATE.
     * Si deux réservations modifient le même départ en même temps,
     * la seconde échoue avec une OptimisticLockException.
     */
    @Version
    private Long version;

    /** Retire des places disponibles (taxi collectif ou bus). */
    public void reserver(int nombre) {
        if (nombre <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif");
        }
        if (dateHeure.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce départ est déjà passé");
        }
        if (nombre > placesRestantes) {
            throw new IllegalStateException(
                    "Places insuffisantes : il reste " + placesRestantes + " place(s)");
        }
        placesRestantes -= nombre;
    }

    /** Rend des places (annulation). Ne dépasse jamais la capacité totale. */
    public void liberer(int nombre) {
        if (nombre <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif");
        }
        placesRestantes = Math.min(placesTotal, placesRestantes + nombre);
    }

    public boolean estComplet() {
        return placesRestantes == 0;
    }
}
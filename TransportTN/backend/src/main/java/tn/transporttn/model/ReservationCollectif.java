package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "reservations_collectif")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReservationCollectif extends Reservation {

    public static final int PLACES_MIN = 1;
    public static final int PLACES_MAX = 4;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depart_id", nullable = false)
    private Depart depart;

    @Column(name = "nombre_places", nullable = false)
    private int nombrePlaces;

    @Override
    protected String prefixeReference() {
        return "TC";
    }

    @Override
    public void annuler() {
        super.annuler();
        depart.liberer(nombrePlaces);   // les places redeviennent disponibles
    }
}
package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "reservations_taxi")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReservationTaxi extends Reservation {

    /** Tarifs indicatifs (en dinars) — pourront passer en configuration plus tard. */
    public static final BigDecimal PRISE_EN_CHARGE = new BigDecimal("0.900");
    public static final BigDecimal TARIF_PAR_KM    = new BigDecimal("0.750");
    public static final BigDecimal TARIF_MINIMUM   = new BigDecimal("2.500");

    /** Null tant qu'aucun chauffeur n'a été attribué. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private Chauffeur chauffeur;

    @Column(name = "adresse_depart", nullable = false)
    private String adresseDepart;

    @Column(name = "lat_depart", nullable = false)
    private Double latDepart;

    @Column(name = "lng_depart", nullable = false)
    private Double lngDepart;

    @Column(name = "adresse_arrivee", nullable = false)
    private String adresseArrivee;

    @Column(name = "lat_arrivee", nullable = false)
    private Double latArrivee;

    @Column(name = "lng_arrivee", nullable = false)
    private Double lngArrivee;

    @Column(name = "distance_km", nullable = false)
    private Double distanceKm;

    @Override
    protected String prefixeReference() {
        return "TX";
    }

    public BigDecimal estimerPrix() {
         if (distanceKm == null) {
            throw new IllegalStateException("La distance doit être connue pour estimer le prix");
        }
        return calculerPrix(distanceKm);
    }

    /** Prix = prise en charge + distance × tarif/km, avec un minimum. */
    public static BigDecimal calculerPrix(double distanceKm) {
        if (distanceKm < 0) {
            throw new IllegalArgumentException("La distance ne peut pas être négative");
        }
        BigDecimal prix = PRISE_EN_CHARGE.add(TARIF_PAR_KM.multiply(BigDecimal.valueOf(distanceKm)));
        return prix.max(TARIF_MINIMUM).setScale(3, RoundingMode.HALF_UP);
    }

    @Override
    public void annuler() {
        if (getStatut() == StatutReservation.EN_COURS) {
            throw new IllegalStateException("Une course déjà commencée ne peut pas être annulée");
        }
        super.annuler();
    }
}
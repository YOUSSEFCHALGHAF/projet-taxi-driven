package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import tn.transporttn.model.enums.TypeLigne;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lignes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ligne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeLigne type;

    @Column(name = "ville_depart", nullable = false, length = 60)
    private String villeDepart;

    @Column(name = "ville_arrivee", nullable = false, length = 60)
    private String villeArrivee;

    @Column(name = "station_depart", nullable = false, length = 120)
    private String stationDepart;

    @Column(name = "station_arrivee", nullable = false, length = 120)
    private String stationArrivee;

    /** Prix unitaire en dinars tunisiens (3 décimales). */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal prix;

    @Column(nullable = false)
    private int capacite;

    @Column(name = "duree_minutes", nullable = false)
    private int dureeMinutes;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    /** Composition : supprimer une ligne supprime ses départs. */
    @Builder.Default
    @OneToMany(mappedBy = "ligne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Depart> departs = new ArrayList<>();

    public void ajouterDepart(Depart depart) {
        departs.add(depart);
        depart.setLigne(this);
    }

    public String getItineraire() {
        return villeDepart + " → " + villeArrivee;
    }
}
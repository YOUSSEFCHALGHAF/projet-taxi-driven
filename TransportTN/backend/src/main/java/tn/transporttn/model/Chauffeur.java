package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chauffeurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chauffeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @Column(name = "numero_permis", nullable = false, unique = true, length = 30)
    private String numeroPermis;

    @Column(nullable = false, unique = true, length = 20)
    private String matricule;

    @Column(name = "modele_vehicule", nullable = false, length = 80)
    private String modeleVehicule;

    @Builder.Default
    @Column(nullable = false)
    private boolean disponible = false;

    private Double latitude;

    private Double longitude;

    @Column(name = "derniere_mise_a_jour")
    private LocalDateTime derniereMiseAJour;

    /** Met à jour la position GPS envoyée par le navigateur du chauffeur. */
    public void mettreAJourPosition(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordonnées GPS invalides");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.derniereMiseAJour = LocalDateTime.now();
    }

    public boolean aUnePosition() {
        return latitude != null && longitude != null;
    }
}
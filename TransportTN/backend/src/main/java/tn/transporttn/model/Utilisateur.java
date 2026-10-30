package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import tn.transporttn.model.enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "motDePasse")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nom;

    @Column(nullable = false, length = 80)
    private String prenom;

    /** Format international tunisien : +216XXXXXXXX */
    @Column(nullable = false, unique = true, length = 12)
    private String telephone;

    @Column(unique = true, length = 120)
    private String email;

    /** Hash BCrypt — jamais le mot de passe en clair. */
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Jeton Firebase Cloud Messaging du navigateur (notifications push). */
    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void avantEnregistrement() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }
}

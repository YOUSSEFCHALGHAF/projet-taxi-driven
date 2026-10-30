package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import tn.transporttn.model.enums.StatutTicket;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_bus_id", nullable = false)
    private ReservationBus reservation;

    /** UUID encodé dans le QR code et vérifié au contrôle. */
    @Column(name = "code_unique", nullable = false, unique = true, length = 36, updatable = false)
    private String codeUnique;

    /** Image PNG du QR code en Base64 (générée à l'étape 4 avec ZXing). */
    @Column(name = "qr_code_base64", columnDefinition = "MEDIUMTEXT")
    private String qrCodeBase64;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutTicket statut = StatutTicket.VALIDE;

    @Column(name = "date_emission", nullable = false, updatable = false)
    private LocalDateTime dateEmission;

    @PrePersist
    protected void avantEnregistrement() {
        if (codeUnique == null) {
            codeUnique = UUID.randomUUID().toString();
        }
        if (dateEmission == null) {
            dateEmission = LocalDateTime.now();
        }
    }

    /** Appelé lors du scan par le contrôleur. */
    public void marquerUtilise() {
        if (statut != StatutTicket.VALIDE) {
            throw new IllegalStateException("Ce ticket n'est pas valide (statut : " + statut + ")");
        }
        statut = StatutTicket.UTILISE;
    }

    public void annuler() {
        if (statut == StatutTicket.UTILISE) {
            throw new IllegalStateException("Un ticket déjà utilisé ne peut pas être annulé");
        }
        statut = StatutTicket.ANNULE;
    }
        public void utiliser() {
        if (this.statut == StatutTicket.UTILISE) {
            throw new IllegalStateException("Ce ticket a déjà été utilisé");
        }
        this.statut = StatutTicket.UTILISE;
    }
}
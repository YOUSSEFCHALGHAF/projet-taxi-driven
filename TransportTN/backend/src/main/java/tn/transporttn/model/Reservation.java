package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tn.transporttn.model.enums.ModePaiement;
import tn.transporttn.model.enums.StatutReservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class Reservation {

    private static final DateTimeFormatter FORMAT_REFERENCE = DateTimeFormatter.ofPattern("yyMMdd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence lisible, ex : TX-261102-A1B2C3D4 */
    @Column(nullable = false, unique = true, length = 20, updatable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal montant;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_paiement", nullable = false, length = 20)
    private ModePaiement modePaiement = ModePaiement.ESPECES;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** Chaque sous-classe fournit son préfixe : TX, TC ou BS. */
    protected abstract String prefixeReference();

    @PrePersist
    protected void avantEnregistrement() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
        if (reference == null) {
            String aleatoire = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
            reference = prefixeReference() + "-" + FORMAT_REFERENCE.format(dateCreation) + "-" + aleatoire;
        }
    }

    /** Règle commune ; les sous-classes la complètent (libérer des places...). */
    public void annuler() {
        if (statut == StatutReservation.TERMINEE || statut == StatutReservation.ANNULEE) {
            throw new IllegalStateException("Cette réservation ne peut plus être annulée");
        }
        statut = StatutReservation.ANNULEE;
    }
}
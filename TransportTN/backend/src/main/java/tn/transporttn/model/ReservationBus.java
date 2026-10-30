package tn.transporttn.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations_bus")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReservationBus extends Reservation {

    public static final int TICKETS_MAX = 10;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "depart_id", nullable = false)
    private Depart depart;

    @Column(name = "nombre_tickets", nullable = false)
    private int nombreTickets;

    /** Composition : les tickets sont enregistrés et supprimés avec la réservation. */
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    public void ajouterTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setReservation(this);
    }

    @Override
    protected String prefixeReference() {
        return "BS";
    }

    @Override
    public void annuler() {
        if (depart.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Impossible d'annuler : le bus est déjà parti");
        }
        super.annuler();
        tickets.forEach(Ticket::annuler);
        depart.liberer(nombreTickets);
    }
}
package tn.transporttn.model.enums;

public enum StatutReservation {
    EN_ATTENTE,
    ACCEPTEE,
    EN_COURS,
    TERMINEE,
    ANNULEE;


    /** Une réservation dans cet état peut-elle encore être annulée ? */
    public boolean estAnnulable() {
        return this == EN_ATTENTE || this == ACCEPTEE;
    }
}

package tn.transporttn.push;

import tn.transporttn.model.enums.TypeNotification;

/** Événement interne : une notification push est à envoyer après le COMMIT. */
public record EnvoiPush(String jeton, String titre, String message, TypeNotification type, String lien) {
}
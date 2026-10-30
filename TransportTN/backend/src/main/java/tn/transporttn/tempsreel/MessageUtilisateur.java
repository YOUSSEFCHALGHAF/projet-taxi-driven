package tn.transporttn.tempsreel;

import java.util.Collection;

/**
 * Message à pousser par WebSocket, publié pendant la transaction
 * et réellement envoyé après le COMMIT (voir DiffusionTempsReel).
 *
 * @param destinataires numéros de téléphone (= nom de l'utilisateur dans la session WebSocket)
 * @param destination   file personnelle, ex : /queue/courses
 */
public record MessageUtilisateur(Collection<String> destinataires, String destination, Object contenu) {
}
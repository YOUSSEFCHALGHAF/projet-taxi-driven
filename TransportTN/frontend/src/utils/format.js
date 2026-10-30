const formatPrix = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 3, maximumFractionDigits: 3 });

/** 8.4 → "8,400 DT" (le dinar a 3 décimales) */
export const prix = (valeur) => (valeur == null ? '—' : `${formatPrix.format(Number(valeur))} DT`);

/** Les dates du backend sont des LocalDateTime sans fuseau : lues comme heure locale. */
export const dateHeure = (iso) =>
  iso
    ? new Date(iso).toLocaleString('fr-FR', {
        weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
      })
    : '—';

export const heure = (iso) =>
  new Date(iso).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });

export const dateLongue = (iso) =>
  new Date(iso).toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });

/** Date locale au format AAAA-MM-JJ (toISOString() utiliserait l'heure UTC). */
export const dateISO = (date = new Date()) => {
  const mois = String(date.getMonth() + 1).padStart(2, '0');
  const jour = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${mois}-${jour}`;
};

export const LIBELLES_STATUT = {
  EN_ATTENTE: 'En attente',
  ACCEPTEE: 'Acceptée',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  ANNULEE: 'Annulée',
};

export const LIBELLES_TICKET = { VALIDE: 'Valide', UTILISE: 'Utilisé', ANNULE: 'Annulé' };

export const LIBELLES_PAIEMENT = { ESPECES: 'Espèces', CARTE: 'Carte bancaire' };

export const LIBELLES_TYPE = { TAXI: 'Taxi', COLLECTIF: 'Louage', BUS: 'Bus' };

/** Page d'arrivée selon le rôle après connexion. */
export const accueilSelonRole = (role) =>
  ({ CLIENT: '/tableau-de-bord', DRIVER: '/chauffeur', ADMIN: '/admin' })[role] ?? '/';
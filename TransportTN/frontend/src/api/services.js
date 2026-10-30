import api from './client';

/** Renvoie directement le corps JSON de la réponse. */
const donnees = (requete) => requete.then((reponse) => reponse.data);

export const authApi = {
  connexion: (telephone, motDePasse) => donnees(api.post('/api/auth/login', { telephone, motDePasse })),
  inscription: (formulaire) => donnees(api.post('/api/auth/inscription', formulaire)),
  moi: () => donnees(api.get('/api/auth/me')),
};

export const catalogueApi = {
  lignes: (filtres = {}) => donnees(api.get('/api/lignes', { params: filtres })),
  villes: () => donnees(api.get('/api/lignes/villes')),
  departs: (ligneId, date) => donnees(api.get(`/api/lignes/${ligneId}/departs`, { params: { date } })),
};

export const taxiApi = {
  estimer: (points) => donnees(api.post('/api/reservations/taxi/estimation', points)),
  reserver: (course) => donnees(api.post('/api/reservations/taxi', course)),
  consulter: (id) => donnees(api.get(`/api/reservations/taxi/${id}`)),
  position: (id) => donnees(api.get(`/api/reservations/taxi/${id}/position`)),
};

export const reservationApi = {
  collectif: (reservation) => donnees(api.post('/api/reservations/collectif', reservation)),
  bus: (reservation) => donnees(api.post('/api/reservations/bus', reservation)),
  historique: (page = 0, taille = 10) =>
    donnees(api.get('/api/reservations/historique', { params: { page, taille } })),
  tickets: () => donnees(api.get('/api/reservations/tickets')),
  annuler: (id) => donnees(api.put(`/api/reservations/${id}/annulation`)),
};

export const chauffeurApi = {
  profil: () => donnees(api.get('/api/driver/profil')),
  disponibilite: (disponible) => donnees(api.put('/api/driver/disponibilite', { disponible })),
  position: (latitude, longitude) => donnees(api.put('/api/driver/position', { latitude, longitude })),
  courses: () => donnees(api.get('/api/driver/courses')),
  statut: (id, statut) => donnees(api.put(`/api/driver/courses/${id}/statut`, { statut })),
  refuser: (id) => donnees(api.put(`/api/driver/courses/${id}/refus`)),
};

export const adminApi = {
  statistiques: () => donnees(api.get('/api/admin/statistiques')),
  lignes: () => donnees(api.get('/api/admin/lignes')),
  creerLigne: (ligne) => donnees(api.post('/api/admin/lignes', ligne)),
  modifierLigne: (id, ligne) => donnees(api.put(`/api/admin/lignes/${id}`, ligne)),
  activerLigne: (id, active) => donnees(api.patch(`/api/admin/lignes/${id}/activation`, null, { params: { active } })),
  genererDeparts: (ligneId, generation) =>
    donnees(api.post(`/api/admin/lignes/${ligneId}/departs/generation`, generation)),
  chauffeurs: () => donnees(api.get('/api/admin/chauffeurs')),
  creerChauffeur: (chauffeur) => donnees(api.post('/api/admin/chauffeurs', chauffeur)),
  activerUtilisateur: (id, actif) =>
    donnees(api.patch(`/api/admin/utilisateurs/${id}/activation`, null, { params: { actif } })),
  validerTicket: (code) => donnees(api.post('/api/admin/tickets/validation', { code })),
};
export const utilisateurApi = {
  /** token = null pour ne plus recevoir de notifications push sur ce navigateur */
  enregistrerJetonFcm: (token) => donnees(api.put('/api/users/me/fcm-token', { token })),
};

export const notificationApi = {
  lister: (page = 0, taille = 10) => donnees(api.get('/api/notifications', { params: { page, taille } })),
  nonLues: () => donnees(api.get('/api/notifications/non-lues')),
  marquerLue: (id) => donnees(api.put(`/api/notifications/${id}/lue`)),
  toutMarquerLu: () => donnees(api.put('/api/notifications/lues')),
};
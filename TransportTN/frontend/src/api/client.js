import axios from 'axios';

export const CLE_JETON = 'transporttn.jeton';
export const EVENEMENT_SESSION_EXPIREE = 'transporttn:session-expiree';

/** Erreur unique utilisée dans toute l'application (lit le format ApiErreur du backend). */
export class ErreurApi extends Error {
  constructor(message, statut = 0, details = {}) {
    super(message);
    this.name = 'ErreurApi';
    this.statut = statut;      // code HTTP (0 = serveur injoignable)
    this.details = details;    // { champ: message } pour les erreurs de validation
  }
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8082',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// 1. Avant chaque requête : ajoute le jeton JWT s'il existe
api.interceptors.request.use((config) => {
  const jeton = localStorage.getItem(CLE_JETON);
  if (jeton) {
    config.headers.Authorization = `Bearer ${jeton}`;
  }
  return config;
});

// 2. Après chaque réponse en erreur : convertit en ErreurApi avec un message en français
api.interceptors.response.use(
  (reponse) => reponse,
  (erreur) => {
    const reponse = erreur.response;

    if (!reponse) {
      const message = erreur.code === 'ECONNABORTED'
        ? 'Le serveur met trop de temps à répondre. Vérifiez votre connexion.'
        : 'Impossible de joindre le serveur. Vérifiez votre connexion internet.';
      return Promise.reject(new ErreurApi(message, 0));
    }

    const corps = reponse.data ?? {};
    const estConnexion = erreur.config?.url?.includes('/api/auth/login');

    // Jeton expiré ou invalide pendant la navigation : on ferme la session
    if (reponse.status === 401 && !estConnexion && localStorage.getItem(CLE_JETON)) {
      window.dispatchEvent(new Event(EVENEMENT_SESSION_EXPIREE));
    }

    return Promise.reject(new ErreurApi(
      corps.message ?? 'Une erreur est survenue. Veuillez réessayer.',
      reponse.status,
      corps.details ?? {},
    ));
  },
);

export default api;
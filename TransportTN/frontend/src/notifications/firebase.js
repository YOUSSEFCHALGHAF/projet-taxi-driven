import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, isSupported } from 'firebase/messaging';
import { utilisateurApi } from '../api/services';

const configuration = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};
const CLE_VAPID = import.meta.env.VITE_FIREBASE_VAPID_KEY;

const firebaseConfigure = Boolean(
  configuration.apiKey && configuration.projectId && configuration.messagingSenderId && configuration.appId && CLE_VAPID,
);

let messagingPromesse = null;

/** Initialise Firebase une seule fois, et seulement si le navigateur sait recevoir des push. */
function obtenirMessaging() {
  if (!messagingPromesse) {
    messagingPromesse = (async () => {
      if (!firebaseConfigure || !(await isSupported())) return null;
      return getMessaging(initializeApp(configuration));
    })();
  }
  return messagingPromesse;
}

/** Faux si Firebase n'est pas configuré ou si le navigateur ne gère pas les push (ex. Safari en navigation privée). */
export async function notificationsDisponibles() {
  return firebaseConfigure && 'Notification' in window && 'serviceWorker' in navigator && (await isSupported());
}

async function enregistrerAupresDuServeur(messaging) {
  const enregistrement = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
  const jeton = await getToken(messaging, { vapidKey: CLE_VAPID, serviceWorkerRegistration: enregistrement });
  await utilisateurApi.enregistrerJetonFcm(jeton);
  return jeton;
}

/** À appeler sur un clic : le navigateur affiche sa demande d'autorisation. */
export async function activerNotificationsPush() {
  const messaging = await obtenirMessaging();
  if (!messaging) {
    throw new Error('Ce navigateur ne permet pas les notifications push.');
  }
  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    throw new Error('Notifications refusées. Vous pourrez les autoriser plus tard dans les réglages du site.');
  }
  return enregistrerAupresDuServeur(messaging);
}

/** Après connexion : si l'autorisation a déjà été donnée, rattache ce navigateur au compte, sans rien demander. */
export async function reprendreSiDejaAutorise() {
  if (!(await notificationsDisponibles()) || Notification.permission !== 'granted') return;
  const messaging = await obtenirMessaging();
  if (messaging) await enregistrerAupresDuServeur(messaging);
}

/** Avant déconnexion : ce navigateur ne recevra plus les notifications de ce compte. */
export async function detacherCeNavigateur() {
  try {
    await utilisateurApi.enregistrerJetonFcm(null);
  } catch {
    // jeton JWT déjà expiré : rien à faire
  }
}
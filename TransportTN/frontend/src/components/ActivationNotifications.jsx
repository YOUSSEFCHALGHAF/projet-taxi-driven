import { useEffect, useState } from 'react';
import { activerNotificationsPush, notificationsDisponibles } from '../notifications/firebase';

/**
 * Proposition d'activer les notifications push, affichée au moment où elles sont utiles
 * (après une réservation, dans l'espace chauffeur…) et jamais au chargement de la page.
 * Ne s'affiche pas si c'est déjà fait ou si le navigateur ne le permet pas.
 */
export default function ActivationNotifications({ texte }) {
  const [etat, setEtat] = useState('verification');   // verification | indisponible | a-demander | envoi | active | refusee
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    let annule = false;
    notificationsDisponibles().then((disponible) => {
      if (annule) return;
      if (!disponible) setEtat('indisponible');
      else if (Notification.permission === 'granted') setEtat('active');
      else if (Notification.permission === 'denied') setEtat('refusee');
      else setEtat('a-demander');
    });
    return () => {
      annule = true;
    };
  }, []);

  async function activer() {
    setErreur(null);
    setEtat('envoi');
    try {
      await activerNotificationsPush();
      setEtat('active');
    } catch (err) {
      setErreur(err.message);
      setEtat(Notification.permission === 'denied' ? 'refusee' : 'a-demander');
    }
  }

  if (etat === 'verification' || etat === 'indisponible' || etat === 'active') return null;

  return (
    <div className="space-y-2 rounded-lg border border-sidi/20 bg-sidi/5 p-4 text-sm">
      {etat === 'refusee' ? (
        <p>
          Les notifications sont bloquées pour ce site. Pour les recevoir, autorisez-les dans les réglages du
          navigateur (icône à gauche de l’adresse).
        </p>
      ) : (
        <>
          <p>{texte}</p>
          <button type="button" className="btn-principal py-1.5" onClick={activer} disabled={etat === 'envoi'}>
            {etat === 'envoi' ? 'Activation…' : 'Activer les notifications'}
          </button>
        </>
      )}
      {erreur && <p className="font-semibold text-louage">{erreur}</p>}
    </div>
  );
}
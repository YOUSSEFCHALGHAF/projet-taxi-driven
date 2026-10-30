import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router';
import { notificationApi } from '../api/services';
import { useAbonnement } from '../hooks/useTempsReel';
import { reprendreSiDejaAutorise } from '../notifications/firebase';
import { dateHeure } from '../utils/format';
import ActivationNotifications from './ActivationNotifications';

/** Cloche de l'en-tête : notifications enregistrées en base + arrivée instantanée par WebSocket. */
export default function Cloche() {
  const navigate = useNavigate();
  const [ouvert, setOuvert] = useState(false);
  const [nonLues, setNonLues] = useState(0);
  const [liste, setListe] = useState(null);
  const [annonce, setAnnonce] = useState('');
  const conteneur = useRef(null);
  const bouton = useRef(null);

  useEffect(() => {
    notificationApi.nonLues().then((r) => setNonLues(r.nombre)).catch(() => {});
    // Si l'utilisateur avait déjà autorisé les push, ce navigateur est rattaché à son compte
    reprendreSiDejaAutorise().catch(() => {});
  }, []);

  // Nouvelle notification envoyée par le serveur pendant que la page est ouverte
  useAbonnement('/user/queue/notifications', (notification) => {
    setNonLues((n) => n + 1);
    setListe((actuelle) => (actuelle ? [notification, ...actuelle].slice(0, 10) : actuelle));
    setAnnonce(`Nouvelle notification : ${notification.titre}`);
  });

  // Fermeture au clic à l'extérieur ou avec Échap
  useEffect(() => {
    if (!ouvert) return undefined;
    const surClic = (e) => {
      if (!conteneur.current?.contains(e.target)) setOuvert(false);
    };
    const surTouche = (e) => {
      if (e.key === 'Escape') {
        setOuvert(false);
        bouton.current?.focus();
      }
    };
    document.addEventListener('mousedown', surClic);
    document.addEventListener('keydown', surTouche);
    return () => {
      document.removeEventListener('mousedown', surClic);
      document.removeEventListener('keydown', surTouche);
    };
  }, [ouvert]);

  async function basculer() {
    const ouvrir = !ouvert;
    setOuvert(ouvrir);
    if (ouvrir) {
      try {
        setListe((await notificationApi.lister(0, 10)).contenu);
      } catch {
        setListe([]);
      }
    }
  }

  async function ouvrirNotification(notification) {
    if (!notification.lue) {
      notificationApi.marquerLue(notification.id).catch(() => {});
      setNonLues((n) => Math.max(0, n - 1));
    }
    setOuvert(false);
    navigate(notification.lien);
  }

  async function toutMarquerLu() {
    await notificationApi.toutMarquerLu();
    setNonLues(0);
    setListe((actuelle) => actuelle?.map((n) => ({ ...n, lue: true })));
  }

  return (
    <div ref={conteneur} className="relative">
      <button
        ref={bouton}
        type="button"
        onClick={basculer}
        aria-expanded={ouvert}
        aria-haspopup="true"
        aria-label={nonLues > 0 ? `Notifications, ${nonLues} non lue(s)` : 'Notifications'}
        className="relative grid h-10 w-10 place-items-center rounded-lg hover:bg-encre/5"
      >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
          <path d="M6 8a6 6 0 1 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
          <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
        </svg>
        {nonLues > 0 && (
          <span className="absolute -right-0.5 -top-0.5 grid h-5 min-w-5 place-items-center rounded-full bg-louage px-1 text-[11px] font-bold text-white">
            {nonLues > 9 ? '9+' : nonLues}
          </span>
        )}
      </button>

      <p className="sr-only" aria-live="polite">{annonce}</p>

      {ouvert && (
        <div className="absolute right-0 top-full z-[1001] mt-2 w-[min(92vw,22rem)] overflow-hidden rounded-xl border border-encre/10 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-encre/10 px-4 py-3">
            <p className="font-bold">Notifications</p>
            {nonLues > 0 && (
              <button type="button" className="text-sm font-bold text-sidi hover:underline" onClick={toutMarquerLu}>
                Tout marquer comme lu
              </button>
            )}
          </div>

          {liste === null && <p className="px-4 py-6 text-sm text-encre-doux">Chargement…</p>}
          {liste?.length === 0 && <p className="px-4 py-6 text-sm text-encre-doux">Aucune notification pour l’instant.</p>}
          {liste?.length > 0 && (
            <ul className="max-h-96 divide-y divide-encre/10 overflow-y-auto">
              {liste.map((n) => (
                <li key={n.id}>
                  <button
                    type="button"
                    onClick={() => ouvrirNotification(n)}
                    className={`block w-full px-4 py-3 text-left hover:bg-chaux ${n.lue ? '' : 'bg-sidi/5'}`}
                  >
                    <span className="flex items-start gap-2">
                      {!n.lue && <span aria-label="non lue" className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-sidi" />}
                      <span className="min-w-0">
                        <span className="block font-bold">{n.titre}</span>
                        <span className="block text-sm text-encre-doux">{n.message}</span>
                        <span className="mt-1 block text-xs text-encre-doux">{dateHeure(n.dateEnvoi)}</span>
                      </span>
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}

          <div className="border-t border-encre/10 p-3">
            <ActivationNotifications texte="Recevez aussi vos notifications quand TransportTN n’est pas ouvert." />
          </div>
        </div>
      )}
    </div>
  );
}
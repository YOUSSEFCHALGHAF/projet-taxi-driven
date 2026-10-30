import { useCallback, useEffect, useRef, useState } from 'react';
import { chauffeurApi } from '../api/services';
import Alerte from '../components/Alerte';
import BadgeStatut from '../components/BadgeStatut';
import CarteTrajet from '../components/CarteTrajet';
import Chargement from '../components/Chargement';
import Itineraire from '../components/Itineraire';
import { useSondage } from '../hooks/useSondage';
import { LIBELLES_PAIEMENT, prix } from '../utils/format';
import ActivationNotifications from '../components/ActivationNotifications';
import { useAbonnement, useTempsReel } from '../hooks/useTempsReel';
const INTERVALLE_ENVOI_MS = 5000;
const ERREURS_GPS = {
  1: 'Vous avez refusé la localisation. Autorisez-la dans les réglages du navigateur.',
  2: 'Position indisponible. Vérifiez que le GPS est activé.',
  3: 'La localisation met trop de temps à répondre.',
};

/** Boutons proposés au chauffeur selon le statut de la course. */
const ACTIONS = {
  EN_ATTENTE: [['ACCEPTEE', 'Accepter la course', 'btn-principal']],
  ACCEPTEE: [['EN_COURS', 'Client à bord, démarrer', 'btn-principal']],
  EN_COURS: [['TERMINEE', 'Terminer la course', 'btn-principal']],
};

export default function EspaceChauffeur() {
  const [profil, setProfil] = useState(null);
  const [courses, setCourses] = useState([]);
  const [erreur, setErreur] = useState(null);
  const [action, setAction] = useState(null);         // action en cours (évite les doubles clics)
  const [partage, setPartage] = useState(false);      // partage GPS actif
  const [derniereEnvoyee, setDerniereEnvoyee] = useState(null);
  const [erreurGps, setErreurGps] = useState(null);
  const dernierEnvoi = useRef(0);
  const { connecte } = useTempsReel();
  const chargerProfil = useCallback(() => chauffeurApi.profil().then(setProfil).catch(setErreur), []);

  useEffect(() => {
    chargerProfil();
  }, [chargerProfil]);

  // Nouvelle course, annulation par le client… : le serveur prévient immédiatement
  useAbonnement('/user/queue/courses', (evenement) => {
    if (evenement.type !== 'COURSE') return;
    chauffeurApi.courses().then(setCourses).catch(setErreur);
    chargerProfil();
  });

  // Secours : toutes les 5 s sans WebSocket, toutes les 30 s avec
  useSondage(() => {
    chauffeurApi.courses().then(setCourses).catch(setErreur);
  }, connecte ? 30000 : 5000);

  // Partage de position : le navigateur fournit la position, on l'envoie au plus toutes les 5 s
  useEffect(() => {
    if (!partage) return undefined;
    const surveillance = navigator.geolocation.watchPosition(
      async ({ coords }) => {
        const maintenant = Date.now();
        if (maintenant - dernierEnvoi.current < INTERVALLE_ENVOI_MS) return;
        dernierEnvoi.current = maintenant;
        try {
          setDerniereEnvoyee(await chauffeurApi.position(coords.latitude, coords.longitude));
          setErreurGps(null);
        } catch (err) {
          setErreurGps(err.message);
        }
      },
      (err) => {
        setErreurGps(ERREURS_GPS[err.code] ?? 'Erreur de localisation.');
        setPartage(false);
      },
      { enableHighAccuracy: true, maximumAge: 5000, timeout: 20000 },
    );
    return () => navigator.geolocation.clearWatch(surveillance);
  }, [partage]);

  function basculerPartage() {
    if (!partage && !('geolocation' in navigator)) {
      setErreurGps('Votre navigateur ne permet pas la géolocalisation.');
      return;
    }
    setPartage(!partage);
  }

  /** Position placée à la main sur la carte (démonstration sans GPS). */
  async function placerManuellement({ latitude, longitude }) {
    try {
      setDerniereEnvoyee(await chauffeurApi.position(latitude, longitude));
      setErreurGps(null);
    } catch (err) {
      setErreurGps(err.message);
    }
  }

  async function executer(cle, appel) {
    setErreur(null);
    setAction(cle);
    try {
      await appel();
      setCourses(await chauffeurApi.courses());
      await chargerProfil();
    } catch (err) {
      setErreur(err);
    } finally {
      setAction(null);
    }
  }

  if (!profil) return erreur ? <Alerte>{erreur}</Alerte> : <Chargement texte="Chargement de votre profil…" />;

  const position = derniereEnvoyee ?? profil;
  const courseSuivie = courses.find((c) => c.statut === 'ACCEPTEE' || c.statut === 'EN_COURS');

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold">Bonjour {profil.nomComplet}</h1>
          <p className="text-encre-doux">
            {profil.modeleVehicule}{' '}
            <span className="ml-1 rounded border-2 border-encre px-1.5 text-sm font-bold">{profil.matricule}</span>
          </p>
        </div>
        <button
          type="button"
          aria-pressed={profil.disponible}
          onClick={() => executer('dispo', () => chauffeurApi.disponibilite(!profil.disponible))}
          disabled={action === 'dispo'}
          className={`inline-flex items-center gap-3 rounded-full px-5 py-2.5 font-bold ${
            profil.disponible ? 'bg-bus text-white' : 'bg-encre/10 text-encre'
          }`}
        >
          <span aria-hidden="true" className={`h-3 w-3 rounded-full ${profil.disponible ? 'bg-white' : 'bg-encre/40'}`} />
          {profil.disponible ? 'Disponible pour des courses' : 'Indisponible'}
        </button>
      </div>

      <Alerte>{erreur}</Alerte>
      <ActivationNotifications texte="Recevez les nouvelles courses même quand cet onglet est en arrière-plan." />

      <div className="grid gap-6 lg:grid-cols-[1fr_24rem]">
        <section aria-labelledby="titre-courses" className="space-y-4">
          <h2 id="titre-courses" className="text-xl font-bold">Courses</h2>
          {courses.length === 0 && (
            <p className="panneau text-encre-doux">
              {profil.disponible
                ? 'Aucune course pour le moment. Les nouvelles demandes apparaissent ici automatiquement.'
                : 'Passez en « Disponible » pour recevoir des courses.'}
            </p>
          )}
          {courses.map((c) => (
            <article key={c.id} className="panneau space-y-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="font-bold">{c.clientNomComplet}</p>
                  <a href={`tel:${c.clientTelephone}`} className="text-sm text-sidi underline">{c.clientTelephone}</a>
                </div>
                <BadgeStatut statut={c.statut} />
              </div>
              <Itineraire depart={c.adresseDepart} arrivee={c.adresseArrivee} couleur="bg-taxi" />
              <p className="flex justify-between text-sm">
                <span className="text-encre-doux">{c.distanceKm.toFixed(1)} km, {LIBELLES_PAIEMENT[c.modePaiement]}</span>
                <strong className="tabular-nums">{prix(c.montant)}</strong>
              </p>
              <div className="flex flex-wrap gap-2">
                {(ACTIONS[c.statut] ?? []).map(([statut, libelle, style]) => (
                  <button
                    key={statut}
                    type="button"
                    className={style}
                    disabled={action !== null}
                    onClick={() => executer(`${c.id}-${statut}`, () => chauffeurApi.statut(c.id, statut))}
                  >
                    {action === `${c.id}-${statut}` ? 'Envoi…' : libelle}
                  </button>
                ))}
                {c.statut === 'EN_ATTENTE' && (
                  <button
                    type="button"
                    className="btn-danger"
                    disabled={action !== null}
                    onClick={() => executer(`${c.id}-refus`, () => chauffeurApi.refuser(c.id))}
                  >
                    Refuser
                  </button>
                )}
              </div>
            </article>
          ))}
        </section>

        <section aria-labelledby="titre-position" className="panneau h-fit space-y-4">
          <h2 id="titre-position" className="text-xl font-bold">Ma position</h2>
          <p className="text-sm text-encre-doux">
            Le client voit votre position pendant la course. Partagez-la depuis votre téléphone,
            ou cliquez sur la carte pour la placer à la main.
          </p>
          <button
            type="button"
            aria-pressed={partage}
            className={partage ? 'btn-danger w-full' : 'btn-principal w-full'}
            onClick={basculerPartage}
          >
            {partage ? 'Arrêter le partage' : 'Partager ma position GPS'}
          </button>
          <Alerte>{erreurGps}</Alerte>
          <CarteTrajet
            depart={courseSuivie && { latitude: courseSuivie.latDepart, longitude: courseSuivie.lngDepart }}
            arrivee={courseSuivie && { latitude: courseSuivie.latArrivee, longitude: courseSuivie.lngArrivee }}
            chauffeur={position}
            onClic={placerManuellement}
            hauteur="h-72"
          />
          {position?.derniereMiseAJour && (
            <p className="text-xs text-encre-doux">
              Dernière position envoyée à {new Date(position.derniereMiseAJour).toLocaleTimeString('fr-FR')}
            </p>
          )}
        </section>
      </div>
    </div>
  );
}
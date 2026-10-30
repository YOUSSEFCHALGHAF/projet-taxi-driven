import { useState } from 'react';
import { Link, useParams } from 'react-router';
import { reservationApi, taxiApi } from '../api/services';
import Alerte from '../components/Alerte';
import BadgeStatut from '../components/BadgeStatut';
import CarteTrajet from '../components/CarteTrajet';
import Chargement from '../components/Chargement';
import Itineraire from '../components/Itineraire';
import { useSondage } from '../hooks/useSondage';
import { prix, LIBELLES_PAIEMENT } from '../utils/format';
import ActivationNotifications from '../components/ActivationNotifications';
import { useAbonnement, useTempsReel } from '../hooks/useTempsReel';
const ETAPES = [
  ['EN_ATTENTE', 'Demande envoyée au chauffeur'],
  ['ACCEPTEE', 'Le chauffeur arrive'],
  ['EN_COURS', 'Vous êtes en route'],
  ['TERMINEE', 'Arrivé à destination'],
];
const ACTIFS = ['EN_ATTENTE', 'ACCEPTEE', 'EN_COURS'];

export default function SuiviCourse() {
  const { id } = useParams();
  const [course, setCourse] = useState(null);
  const [position, setPosition] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [annulation, setAnnulation] = useState(false);
  const { connecte } = useTempsReel();
  const enCours = course === null || ACTIFS.includes(course.statut);

    // Temps réel : statut et position poussés par le serveur dès qu'ils changent
  useAbonnement('/user/queue/courses', (evenement) => {
    if (String(evenement.courseId) !== String(id)) return;
    if (evenement.type === 'COURSE') setCourse(evenement.course);
    if (evenement.type === 'POSITION') setPosition(evenement.position);
  });

  // Secours : toutes les 5 s sans WebSocket, toutes les 30 s avec
  useSondage(async () => {
    try {
      const donnees = await taxiApi.consulter(id);
      setCourse(donnees);
      if (donnees.statut === 'ACCEPTEE' || donnees.statut === 'EN_COURS') {
        setPosition(await taxiApi.position(id));
      }
      setErreur(null);
    } catch (err) {
      setErreur(err);
    }
  }, connecte ? 30000 : 5000, enCours);
  async function annuler() {
    if (!window.confirm('Annuler cette course ?')) return;
    setAnnulation(true);
    try {
      await reservationApi.annuler(id);
      setCourse(await taxiApi.consulter(id));
    } catch (err) {
      setErreur(err);
    } finally {
      setAnnulation(false);
    }
  }

  if (!course) {
    return erreur ? <Alerte>{erreur}</Alerte> : <Chargement texte="Chargement de la course…" />;
  }

  const indexEtape = ETAPES.findIndex(([statut]) => statut === course.statut);
  const chauffeur = course.chauffeur;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-3xl font-extrabold">Votre course</h1>
          <p className="text-sm text-encre-doux">Référence {course.reference}</p>
        </div>
        <BadgeStatut statut={course.statut} />
      </div>

      <Alerte>{erreur}</Alerte>
      {course.statut === 'ANNULEE' && (
        <Alerte type="info">
          Cette course est annulée. <Link to="/taxi" className="underline">Commander un autre taxi</Link>
        </Alerte>
      )}

      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <CarteTrajet
          depart={{ latitude: course.latDepart, longitude: course.lngDepart }}
          arrivee={{ latitude: course.latArrivee, longitude: course.lngArrivee }}
          chauffeur={position}
          hauteur="h-[24rem] lg:h-[30rem]"
        />

        <div className="space-y-5">
          {course.statut !== 'ANNULEE' && (
            <ol className="panneau space-y-3" aria-label="Avancement de la course">
              {ETAPES.map(([statut, libelle], i) => {
                const faite = i < indexEtape || course.statut === 'TERMINEE';
                const actuelle = i === indexEtape && course.statut !== 'TERMINEE';
                return (
                  <li key={statut} className="flex items-center gap-3" aria-current={actuelle ? 'step' : undefined}>
                    <span
                      aria-hidden="true"
                      className={`grid h-7 w-7 shrink-0 place-items-center rounded-full text-xs font-bold ${
                        faite ? 'bg-bus text-white' : actuelle ? 'bg-taxi text-encre' : 'bg-encre/10 text-encre-doux'
                      }`}
                    >
                      {faite ? '✓' : i + 1}
                    </span>
                    <span className={actuelle ? 'font-bold' : faite ? '' : 'text-encre-doux'}>{libelle}</span>
                  </li>
                );
              })}
            </ol>
          )}

          {chauffeur && (
            <div className="panneau">
              <p className="text-sm text-encre-doux">Votre chauffeur</p>
              <p className="text-lg font-bold">{chauffeur.nomComplet}</p>
              <p className="text-encre-doux">{chauffeur.modeleVehicule}</p>
              <p className="mt-2 inline-block rounded border-2 border-encre bg-white px-2 py-0.5 font-bold tracking-wide tabular-nums">
                {chauffeur.matricule}
              </p>
              <a href={`tel:${chauffeur.telephone}`} className="btn-secondaire mt-3 w-full">Appeler le chauffeur</a>
            </div>
          )}

          <div className="panneau space-y-3">
            <Itineraire depart={course.adresseDepart} arrivee={course.adresseArrivee} couleur="bg-taxi" />
            <p className="flex justify-between border-t border-encre/10 pt-3">
              <span>{LIBELLES_PAIEMENT[course.modePaiement]}, {course.distanceKm.toFixed(1)} km</span>
              <strong className="tabular-nums">{prix(course.montant)}</strong>
            </p>
          </div>
          {enCours && (
            <ActivationNotifications texte="Soyez prévenu de l’arrivée du chauffeur, même si cet onglet est fermé." />
          )}
          {(course.statut === 'EN_ATTENTE' || course.statut === 'ACCEPTEE') && (
            <button type="button" className="btn-danger w-full" onClick={annuler} disabled={annulation}>
              {annulation ? 'Annulation…' : 'Annuler la course'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { reservationApi } from '../api/services';
import Alerte from '../components/Alerte';
import BadgeStatut from '../components/BadgeStatut';
import Chargement from '../components/Chargement';
import { useAuth } from '../hooks/useAuth';
import { dateHeure, LIBELLES_TYPE, prix } from '../utils/format';

const SERVICES = [
  { vers: '/taxi', nom: 'Taxi', texte: 'Une course maintenant, au chauffeur le plus proche.', bande: 'bg-taxi' },
  { vers: '/collectif', nom: 'Louage', texte: 'Une à quatre places entre deux villes.', bande: 'bg-louage' },
  { vers: '/bus', nom: 'Bus', texte: 'Des tickets avec QR code pour un départ.', bande: 'bg-bus' },
];

const STATUTS_ACTIFS = ['EN_ATTENTE', 'ACCEPTEE', 'EN_COURS'];

export default function TableauDeBord() {
  const { utilisateur } = useAuth();
  const [recentes, setRecentes] = useState(null);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    reservationApi.historique(0, 5).then((page) => setRecentes(page.contenu)).catch(setErreur);
  }, []);

  const courseActive = recentes?.find((r) => r.type === 'TAXI' && STATUTS_ACTIFS.includes(r.statut));

  return (
    <div className="space-y-10">
      <h1 className="text-3xl font-extrabold sm:text-4xl">Bonjour {utilisateur.prenom}, où allez-vous&nbsp;?</h1>

      {courseActive && (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border-2 border-taxi bg-taxi/10 p-4">
          <p className="font-bold">
            Votre taxi est en cours ({courseActive.description}).
          </p>
          <Link to={`/taxi/${courseActive.id}`} className="btn-principal">Suivre ma course</Link>
        </div>
      )}

      <nav aria-label="Réserver" className="grid gap-4 sm:grid-cols-3">
        {SERVICES.map((s) => (
          <Link
            key={s.vers}
            to={s.vers}
            className="group relative overflow-hidden rounded-xl border border-encre/10 bg-white p-5 pt-7 transition-colors hover:border-encre/30"
          >
            <span aria-hidden="true" className={`absolute inset-x-0 top-0 h-2 ${s.bande}`} />
            <span className="block font-titre text-2xl font-bold">{s.nom}</span>
            <span className="mt-1 block text-encre-doux">{s.texte}</span>
            <span className="mt-4 block text-sm font-bold text-sidi group-hover:underline">Réserver</span>
          </Link>
        ))}
      </nav>

      <section aria-labelledby="titre-recentes">
        <div className="mb-3 flex items-baseline justify-between">
          <h2 id="titre-recentes" className="text-xl font-bold">Dernières réservations</h2>
          <Link to="/historique" className="text-sm font-bold text-sidi underline">Tout l’historique</Link>
        </div>
        <Alerte>{erreur}</Alerte>
        {recentes === null && !erreur && <Chargement />}
        {recentes?.length === 0 && (
          <p className="panneau text-encre-doux">Vous n’avez encore rien réservé. Choisissez un mode de transport ci-dessus.</p>
        )}
        {recentes?.length > 0 && (
          <ul className="panneau divide-y divide-encre/10 p-0">
            {recentes.map((r) => (
              <li key={r.id} className="flex flex-wrap items-center gap-x-4 gap-y-1 px-5 py-3">
                <span className="w-16 text-sm font-bold">{LIBELLES_TYPE[r.type]}</span>
                <span className="min-w-0 flex-1 truncate">{r.description}</span>
                <span className="text-sm text-encre-doux">{dateHeure(r.dateVoyage)}</span>
                <span className="w-24 text-right text-sm font-bold tabular-nums">{prix(r.montant)}</span>
                <BadgeStatut statut={r.statut} />
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
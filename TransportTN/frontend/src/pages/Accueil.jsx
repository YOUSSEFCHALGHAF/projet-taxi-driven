import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { catalogueApi } from '../api/services';
import { useAuth } from '../hooks/useAuth';
import { accueilSelonRole, dateISO, heure } from '../utils/format';

const MODES = [
  {
    nom: 'Taxi',
    bande: 'bg-taxi',
    texte: 'Indiquez votre départ et votre destination sur la carte. Le chauffeur disponible le plus proche reçoit la course, et le prix est affiché avant de confirmer.',
  },
  {
    nom: 'Louage',
    bande: 'bg-louage',
    texte: 'Réservez de 1 à 4 places dans un taxi collectif entre deux villes. Les places restantes sont affichées pour chaque départ.',
  },
  {
    nom: 'Bus',
    bande: 'bg-bus',
    texte: 'Achetez jusqu’à 10 tickets pour un départ. Chaque ticket a son propre QR code, à présenter à l’embarquement.',
  },
];

/** Tableau des prochains départs, comme dans une gare routière. */
function TableauDesDeparts() {
  const [departs, setDeparts] = useState(null);
  const [jour, setJour] = useState("Aujourd'hui");
  const [indisponible, setIndisponible] = useState(false);

  useEffect(() => {
    let annule = false;

    async function charger(date) {
      const lignes = await catalogueApi.lignes();
      const parLigne = await Promise.all(
        lignes.map((ligne) =>
          catalogueApi.departs(ligne.id, date).then((liste) => liste.map((d) => ({ ...d, ligne }))),
        ),
      );
      return parLigne.flat().filter((d) => !d.complet).sort((a, b) => a.dateHeure.localeCompare(b.dateHeure));
    }

    (async () => {
      try {
        let liste = await charger(dateISO());
        if (liste.length === 0) {
          const demain = new Date();
          demain.setDate(demain.getDate() + 1);
          liste = await charger(dateISO(demain));
          if (!annule) setJour('Demain');
        }
        if (!annule) setDeparts(liste.slice(0, 6));
      } catch {
        if (!annule) setIndisponible(true);
      }
    })();

    return () => {
      annule = true;
    };
  }, []);

  return (
    <section aria-labelledby="titre-departs" className="rounded-2xl bg-encre p-5 text-white sm:p-6">
      <div className="mb-4 flex items-baseline justify-between gap-4">
        <h2 id="titre-departs" className="text-xl font-bold">Prochains départs</h2>
        <span className="text-sm text-white/60">{jour}</span>
      </div>

      {indisponible && <p className="text-white/70">Les horaires ne sont pas disponibles pour le moment.</p>}
      {!indisponible && departs === null && <p className="text-white/70">Chargement des horaires…</p>}
      {departs?.length === 0 && <p className="text-white/70">Aucun départ programmé.</p>}

      {departs?.length > 0 && (
        <ul className="divide-y divide-white/10">
          {departs.map((d) => (
            <li key={d.id} className="grid grid-cols-[4.5rem_1fr_auto] items-center gap-3 py-3">
              <span className="font-titre text-2xl font-bold tabular-nums text-taxi">{heure(d.dateHeure)}</span>
              <span className="min-w-0">
                <span className="block truncate font-bold">{d.ligne.villeArrivee}</span>
                <span className="block truncate text-sm text-white/60">
                  {d.ligne.type === 'BUS' ? 'Bus' : 'Louage'} depuis {d.ligne.stationDepart}
                </span>
              </span>
              <span className="text-right text-sm tabular-nums text-white/80">
                {d.placesRestantes} {d.placesRestantes > 1 ? 'places' : 'place'}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default function Accueil() {
  const { utilisateur } = useAuth();

  return (
    <div className="space-y-16">
      <section className="grid items-center gap-10 lg:grid-cols-[1.15fr_1fr]">
        <div>
          <h1 className="max-w-xl text-4xl font-extrabold sm:text-5xl">
            Taxi, louage ou bus&nbsp;: réservez votre trajet avant de partir.
          </h1>
          <p className="mt-5 max-w-lg text-lg text-encre-doux">
            Commandez un taxi, gardez vos places en louage ou achetez vos tickets de bus depuis votre
            navigateur, sur téléphone comme sur ordinateur.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            {utilisateur ? (
              <Link to={accueilSelonRole(utilisateur.role)} className="btn-principal px-6 py-3 text-base">
                Aller à mon espace
              </Link>
            ) : (
              <>
                <Link to="/inscription" className="btn-principal px-6 py-3 text-base">Créer un compte</Link>
                <Link to="/connexion" className="btn-secondaire px-6 py-3 text-base">Se connecter</Link>
              </>
            )}
          </div>
        </div>
        <TableauDesDeparts />
      </section>

      <section aria-label="Les trois modes de transport" className="grid gap-8 md:grid-cols-3">
        {MODES.map((mode) => (
          <div key={mode.nom}>
            <span aria-hidden="true" className={`mb-4 block h-1.5 w-16 rounded-full ${mode.bande}`} />
            <h2 className="text-2xl font-bold">{mode.nom}</h2>
            <p className="mt-2 text-encre-doux">{mode.texte}</p>
          </div>
        ))}
      </section>
    </div>
  );
}
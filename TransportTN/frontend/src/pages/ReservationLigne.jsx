import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router';
import { catalogueApi, reservationApi } from '../api/services';
import Alerte from '../components/Alerte';
import Champ from '../components/Champ';
import Chargement from '../components/Chargement';
import Itineraire from '../components/Itineraire';
import { dateISO, heure, prix } from '../utils/format';
import ActivationNotifications from '../components/ActivationNotifications';
const CONFIG = {
  COLLECTIF: {
    titre: 'Réserver un louage',
    intro: 'Choisissez une ligne, un départ, puis réservez de 1 à 4 places.',
    max: 4,
    unite: 'place',
    couleur: 'bg-louage',
    cleQuantite: 'nombrePlaces',
    envoyer: reservationApi.collectif,
  },
  BUS: {
    titre: 'Acheter des tickets de bus',
    intro: 'Choisissez une ligne et un départ. Chaque ticket aura son QR code.',
    max: 10,
    unite: 'ticket',
    couleur: 'bg-bus',
    cleQuantite: 'nombreTickets',
    envoyer: reservationApi.bus,
  },
};

const pluriel = (n, mot) => `${n} ${mot}${n > 1 ? 's' : ''}`;

/** Page commune au taxi collectif (louage) et au bus : seule la configuration change. */
export default function ReservationLigne({ type }) {
  const config = CONFIG[type];

  const [lignes, setLignes] = useState(null);
  const [villeDepart, setVilleDepart] = useState('');
  const [villeArrivee, setVilleArrivee] = useState('');
  const [ligne, setLigne] = useState(null);
  const [date, setDate] = useState(dateISO());
  const [departs, setDeparts] = useState(null);
  const [depart, setDepart] = useState(null);
  const [quantite, setQuantite] = useState(1);
  const [modePaiement, setModePaiement] = useState('ESPECES');
  const [resultat, setResultat] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  useEffect(() => {
    catalogueApi.lignes({ type }).then(setLignes).catch(setErreur);
  }, [type]);

  // Départs du jour choisi pour la ligne choisie
  useEffect(() => {
    if (!ligne) return undefined;
    let annule = false;
    catalogueApi.departs(ligne.id, date)
      .then((liste) => !annule && setDeparts(liste))
      .catch((err) => !annule && setErreur(err));
    return () => {
      annule = true;
    };
  }, [ligne, date, resultat]);

  const villesDepart = useMemo(() => [...new Set((lignes ?? []).map((l) => l.villeDepart))].sort(), [lignes]);
  const villesArrivee = useMemo(() => [...new Set((lignes ?? []).map((l) => l.villeArrivee))].sort(), [lignes]);
  const lignesFiltrees = (lignes ?? []).filter(
    (l) => (!villeDepart || l.villeDepart === villeDepart) && (!villeArrivee || l.villeArrivee === villeArrivee),
  );

  const maximum = depart ? Math.min(config.max, depart.placesRestantes) : config.max;

  function changerLigne(l) {
    setLigne(l);
    setDeparts(null);
    setDepart(null);
  }

  function changerDate(nouvelle) {
    setDate(nouvelle);
    setDeparts(null);
    setDepart(null);
  }

  function choisirDepart(d) {
    setDepart(d);
    setQuantite((q) => Math.min(q, Math.min(config.max, d.placesRestantes)));
  }

  async function confirmer() {
    setErreur(null);
    setEnvoi(true);
    try {
      const reponse = await config.envoyer({ departId: depart.id, [config.cleQuantite]: quantite, modePaiement });
      setResultat(reponse);
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  function recommencer() {
    setResultat(null);
    setDeparts(null);
    setDepart(null);
    setQuantite(1);
  }

  if (resultat) {
    return (
      <div className="mx-auto max-w-2xl space-y-6">
        <h1 className="text-3xl font-extrabold">Réservation confirmée</h1>
        <Alerte type="succes">
          {type === 'BUS' ? pluriel(resultat.nombreTickets, 'ticket') : pluriel(resultat.nombrePlaces, 'place')}{' '}
          pour {resultat.depart.itineraire}, le {new Date(resultat.depart.dateHeure).toLocaleDateString('fr-FR')} à{' '}
          {heure(resultat.depart.dateHeure)}. Référence {resultat.reference}, total {prix(resultat.montant)}.
        </Alerte>

        {type === 'BUS' && (
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            {resultat.tickets.map((t, i) => (
              <figure key={t.id} className="panneau p-3 text-center">
                <img src={`data:image/png;base64,${t.qrCodeBase64}`} alt={`QR code du ticket ${i + 1}`} className="mx-auto w-full max-w-40" />
                <figcaption className="mt-1 text-sm text-encre-doux">Ticket {i + 1}</figcaption>
              </figure>
            ))}
          </div>
        )}
        <ActivationNotifications texte="Recevez un rappel et les éventuels changements sur ce trajet, même quand le site est fermé." />
        <div className="flex flex-wrap gap-3">
          <Link to={type === 'BUS' ? '/mes-tickets' : '/historique'} className="btn-principal">
            {type === 'BUS' ? 'Voir mes tickets' : 'Voir mes réservations'}
          </Link>
          <button type="button" className="btn-secondaire" onClick={recommencer}>Faire une autre réservation</button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">{config.titre}</h1>
        <p className="mt-1 text-encre-doux">{config.intro}</p>
      </div>

      <Alerte>{erreur}</Alerte>

      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <div className="space-y-6">
          {/* 1. Ligne */}
          <section aria-labelledby="titre-ligne" className="space-y-3">
            <h2 id="titre-ligne" className="text-lg font-bold">1. Ligne</h2>
            <div className="grid gap-3 sm:grid-cols-2">
              <Champ label="Ville de départ" as="select" value={villeDepart} onChange={(e) => setVilleDepart(e.target.value)}>
                <option value="">Toutes</option>
                {villesDepart.map((v) => <option key={v}>{v}</option>)}
              </Champ>
              <Champ label="Ville d’arrivée" as="select" value={villeArrivee} onChange={(e) => setVilleArrivee(e.target.value)}>
                <option value="">Toutes</option>
                {villesArrivee.map((v) => <option key={v}>{v}</option>)}
              </Champ>
            </div>

            {lignes === null && !erreur && <Chargement texte="Chargement des lignes…" />}
            {lignes && lignesFiltrees.length === 0 && (
              <p className="text-encre-doux">Aucune ligne pour ce trajet. Essayez une autre ville.</p>
            )}
            <div className="grid gap-3 sm:grid-cols-2">
              {lignesFiltrees.map((l) => (
                <button
                  key={l.id}
                  type="button"
                  aria-pressed={ligne?.id === l.id}
                  onClick={() => changerLigne(l)}
                  className={`rounded-xl border-2 bg-white p-4 text-left transition-colors ${
                    ligne?.id === l.id ? 'border-encre' : 'border-transparent ring-1 ring-encre/10 hover:ring-encre/30'
                  }`}
                >
                  <Itineraire depart={l.villeDepart} sousDepart={l.stationDepart} arrivee={l.villeArrivee} sousArrivee={l.stationArrivee} couleur={config.couleur} />
                  <p className="mt-3 flex justify-between text-sm">
                    <span className="text-encre-doux">Environ {Math.round(l.dureeMinutes / 6) / 10} h</span>
                    <strong className="tabular-nums">{prix(l.prix)} / {config.unite}</strong>
                  </p>
                </button>
              ))}
            </div>
          </section>

          {/* 2. Départ */}
          {ligne && (
            <section aria-labelledby="titre-depart" className="space-y-3">
              <h2 id="titre-depart" className="text-lg font-bold">2. Départ</h2>
              <Champ
                label="Date du voyage"
                type="date"
                min={dateISO()}
                value={date}
                onChange={(e) => changerDate(e.target.value)}
                className="max-w-xs"
              />
              {departs === null && <Chargement texte="Chargement des départs…" />}
              {departs?.length === 0 && <p className="text-encre-doux">Plus aucun départ ce jour-là. Choisissez une autre date.</p>}
              <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                {departs?.map((d) => (
                  <button
                    key={d.id}
                    type="button"
                    disabled={d.complet}
                    aria-pressed={depart?.id === d.id}
                    onClick={() => choisirDepart(d)}
                    className={`rounded-lg border-2 bg-white px-3 py-2 text-left disabled:cursor-not-allowed disabled:opacity-40 ${
                      depart?.id === d.id ? 'border-encre' : 'border-transparent ring-1 ring-encre/10 hover:ring-encre/30'
                    }`}
                  >
                    <span className="block font-titre text-xl font-bold tabular-nums">{heure(d.dateHeure)}</span>
                    <span className="block text-sm text-encre-doux">
                      {d.complet ? 'Complet' : `${pluriel(d.placesRestantes, 'place')} libre${d.placesRestantes > 1 ? 's' : ''}`}
                    </span>
                  </button>
                ))}
              </div>
            </section>
          )}
        </div>

        {/* 3. Récapitulatif */}
        <aside className="panneau h-fit space-y-4 lg:sticky lg:top-24" aria-labelledby="titre-recap">
          <h2 id="titre-recap" className="text-lg font-bold">3. Confirmer</h2>
          {!depart ? (
            <p className="text-encre-doux">Choisissez une ligne puis un départ.</p>
          ) : (
            <>
              <Itineraire
                depart={`${ligne.villeDepart}, ${heure(depart.dateHeure)}`}
                sousDepart={ligne.stationDepart}
                arrivee={`${ligne.villeArrivee}, vers ${heure(depart.arriveeEstimee)}`}
                sousArrivee={ligne.stationArrivee}
                couleur={config.couleur}
              />

              <div>
                <p id="libelle-quantite" className="mb-1.5 text-sm font-bold">Nombre de {config.unite}s</p>
                <div className="flex items-center gap-3" role="group" aria-labelledby="libelle-quantite">
                  <button type="button" className="btn-secondaire h-10 w-10 p-0 text-lg" onClick={() => setQuantite(quantite - 1)} disabled={quantite <= 1} aria-label="Retirer un">−</button>
                  <span className="w-8 text-center text-xl font-bold tabular-nums" aria-live="polite">{quantite}</span>
                  <button type="button" className="btn-secondaire h-10 w-10 p-0 text-lg" onClick={() => setQuantite(quantite + 1)} disabled={quantite >= maximum} aria-label="Ajouter un">+</button>
                </div>
              </div>

              <Champ label="Paiement" as="select" value={modePaiement} onChange={(e) => setModePaiement(e.target.value)}>
                <option value="ESPECES">Espèces</option>
                <option value="CARTE">Carte bancaire</option>
              </Champ>

              <p className="flex items-baseline justify-between border-t border-encre/10 pt-3">
                <span>Total</span>
                <strong className="font-titre text-2xl tabular-nums">{prix(ligne.prix * quantite)}</strong>
              </p>

              <button type="button" className="btn-principal w-full py-3" onClick={confirmer} disabled={envoi}>
                {envoi ? 'Réservation…' : type === 'BUS' ? 'Acheter les tickets' : 'Réserver les places'}
              </button>
            </>
          )}
        </aside>
      </div>
    </div>
  );
}
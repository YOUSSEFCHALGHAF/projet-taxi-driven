import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router';
import { reservationApi } from '../api/services';
import Alerte from '../components/Alerte';
import BadgeStatut from '../components/BadgeStatut';
import Chargement from '../components/Chargement';
import { dateHeure, LIBELLES_TYPE, prix } from '../utils/format';

const TAILLE = 10;
const COULEUR_TYPE = { TAXI: 'bg-taxi', COLLECTIF: 'bg-louage', BUS: 'bg-bus' };

function estAnnulable(r) {
  if (r.type === 'TAXI') return r.statut === 'EN_ATTENTE' || r.statut === 'ACCEPTEE';
  return r.statut === 'ACCEPTEE' && new Date(r.dateVoyage) > new Date();
}

export default function Historique() {
  const [page, setPage] = useState(0);
  const [resultat, setResultat] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [enCours, setEnCours] = useState(null);   // id de la réservation en cours d'annulation

  const charger = useCallback(() => {
    reservationApi.historique(page, TAILLE).then(setResultat).catch(setErreur);
  }, [page]);

  useEffect(charger, [charger]);

  async function annuler(reservation) {
    if (!window.confirm(`Annuler la réservation ${reservation.reference} ?`)) return;
    setErreur(null);
    setMessage(null);
    setEnCours(reservation.id);
    try {
      await reservationApi.annuler(reservation.id);
      setMessage(`Réservation ${reservation.reference} annulée.`);
      charger();
    } catch (err) {
      setErreur(err);
    } finally {
      setEnCours(null);
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-extrabold">Historique</h1>
      <Alerte>{erreur}</Alerte>
      <Alerte type="succes">{message}</Alerte>

      {resultat === null && !erreur && <Chargement />}
      {resultat?.totalElements === 0 && (
        <p className="panneau text-encre-doux">
          Aucune réservation pour l’instant. <Link to="/tableau-de-bord" className="font-bold text-sidi underline">Réserver un trajet</Link>
        </p>
      )}

      {resultat?.contenu.length > 0 && (
        <>
          <ul className="space-y-3">
            {resultat.contenu.map((r) => (
              <li key={r.id} className="panneau relative overflow-hidden pl-7">
                <span aria-hidden="true" className={`absolute inset-y-0 left-0 w-1.5 ${COULEUR_TYPE[r.type]}`} />
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-sm font-bold text-encre-doux">{LIBELLES_TYPE[r.type]}, réf. {r.reference}</p>
                    <p className="font-bold">{r.description}</p>
                    <p className="text-sm text-encre-doux">{dateHeure(r.dateVoyage)}</p>
                  </div>
                  <div className="text-right">
                    <p className="font-bold tabular-nums">{prix(r.montant)}</p>
                    <BadgeStatut statut={r.statut} />
                  </div>
                </div>
                {(estAnnulable(r) || (r.type === 'TAXI' && r.statut === 'EN_COURS')) && (
                  <div className="mt-3 flex gap-2">
                    {r.type === 'TAXI' && <Link to={`/taxi/${r.id}`} className="btn-secondaire py-1.5">Suivre la course</Link>}
                    {estAnnulable(r) && (
                      <button type="button" className="btn-danger py-1.5" onClick={() => annuler(r)} disabled={enCours === r.id}>
                        {enCours === r.id ? 'Annulation…' : 'Annuler'}
                      </button>
                    )}
                  </div>
                )}
              </li>
            ))}
          </ul>

          <nav aria-label="Pages de l'historique" className="flex items-center justify-between">
            <button type="button" className="btn-secondaire" onClick={() => setPage(page - 1)} disabled={page === 0}>
              Précédent
            </button>
            <span className="text-sm text-encre-doux">Page {resultat.page + 1} sur {Math.max(resultat.totalPages, 1)}</span>
            <button type="button" className="btn-secondaire" onClick={() => setPage(page + 1)} disabled={resultat.derniere}>
              Suivant
            </button>
          </nav>
        </>
      )}
    </div>
  );
}
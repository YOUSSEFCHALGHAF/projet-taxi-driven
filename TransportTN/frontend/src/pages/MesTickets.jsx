import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router';
import { reservationApi } from '../api/services';
import Alerte from '../components/Alerte';
import BadgeStatut from '../components/BadgeStatut';
import Chargement from '../components/Chargement';
import Itineraire from '../components/Itineraire';
import { dateLongue, heure } from '../utils/format';

function Ticket({ ticket, onAgrandir }) {
  const utilisable = ticket.statut === 'VALIDE';
  return (
    <article
      className={`ticket grid grid-cols-[1fr_9.5rem] rounded-xl border border-encre/10 bg-white ${utilisable ? '' : 'opacity-60'}`}
      style={{ '--decoupe': 'calc(100% - 9.5rem)' }}
    >
      <div className="space-y-3 p-5">
        <div className="flex items-start justify-between gap-2">
          <p className="font-bold capitalize">{dateLongue(ticket.dateHeureDepart)}</p>
          <BadgeStatut statut={ticket.statut} />
        </div>
        <p className="font-titre text-3xl font-bold tabular-nums">{heure(ticket.dateHeureDepart)}</p>
        <Itineraire
          depart={ticket.itineraire.split(' → ')[0]}
          sousDepart={ticket.stationDepart}
          arrivee={ticket.itineraire.split(' → ')[1]}
          sousArrivee={ticket.stationArrivee}
          couleur="bg-bus"
        />
        <p className="text-xs text-encre-doux">Réservation {ticket.reference}</p>
      </div>
      <div className="flex flex-col items-center justify-center gap-2 border-l-2 border-dashed border-encre/15 p-3">
        <button type="button" onClick={() => onAgrandir(ticket)} disabled={!utilisable} className="rounded-md disabled:cursor-not-allowed">
          <img src={`data:image/png;base64,${ticket.qrCodeBase64}`} alt="QR code du ticket" className="w-full" />
          {utilisable && <span className="mt-1 block text-xs font-bold text-sidi">Agrandir</span>}
        </button>
      </div>
    </article>
  );
}

export default function MesTickets() {
  const [tickets, setTickets] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [agrandi, setAgrandi] = useState(null);
  const dialogue = useRef(null);
  const [maintenant] = useState(() => new Date());

  useEffect(() => {
    reservationApi.tickets().then(setTickets).catch(setErreur);
  }, []);

  useEffect(() => {
    if (agrandi) dialogue.current?.showModal();
  }, [agrandi]);

  const aVenir = (tickets ?? [])
    .filter((t) => t.statut === 'VALIDE' && new Date(t.dateHeureDepart) >= maintenant)
    .sort((a, b) => a.dateHeureDepart.localeCompare(b.dateHeureDepart));
  const autres = (tickets ?? []).filter((t) => !aVenir.includes(t));

  return (
    <div className="space-y-8">
      <h1 className="text-3xl font-extrabold">Mes tickets</h1>
      <Alerte>{erreur}</Alerte>
      {tickets === null && !erreur && <Chargement texte="Chargement des tickets…" />}

      {tickets?.length === 0 && (
        <p className="panneau text-encre-doux">
          Vous n’avez pas encore de ticket. <Link to="/bus" className="font-bold text-sidi underline">Acheter un ticket de bus</Link>
        </p>
      )}

      {aVenir.length > 0 && (
        <section aria-labelledby="titre-avenir" className="space-y-4">
          <h2 id="titre-avenir" className="text-xl font-bold">À venir</h2>
          <div className="grid gap-5 md:grid-cols-2">
            {aVenir.map((t) => <Ticket key={t.id} ticket={t} onAgrandir={setAgrandi} />)}
          </div>
        </section>
      )}

      {autres.length > 0 && (
        <section aria-labelledby="titre-passes" className="space-y-4">
          <h2 id="titre-passes" className="text-xl font-bold">Utilisés, annulés ou passés</h2>
          <div className="grid gap-5 md:grid-cols-2">
            {autres.map((t) => <Ticket key={t.id} ticket={t} onAgrandir={setAgrandi} />)}
          </div>
        </section>
      )}

      <dialog
        ref={dialogue}
        onClose={() => setAgrandi(null)}
        className="m-auto w-[min(92vw,26rem)] rounded-2xl p-6 backdrop:bg-encre/70"
      >
        {agrandi && (
          <div className="space-y-3 text-center">
            <p className="font-bold">{agrandi.itineraire}, {heure(agrandi.dateHeureDepart)}</p>
            <img src={`data:image/png;base64,${agrandi.qrCodeBase64}`} alt="QR code du ticket, à présenter au contrôleur" className="mx-auto w-full" />
            <p className="break-all text-xs text-encre-doux">{agrandi.codeUnique}</p>
            <form method="dialog">
              <button className="btn-principal w-full">Fermer</button>
            </form>
          </div>
        )}
      </dialog>
    </div>
  );
}
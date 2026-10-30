import { useRef, useState } from 'react';
import { adminApi } from '../../api/services';
import Alerte from '../../components/Alerte';
import Champ from '../../components/Champ';
import { dateHeure } from '../../utils/format';

/**
 * Validation d'un ticket à l'embarquement.
 * Un lecteur de QR code USB « tape » le contenu puis Entrée : le formulaire part tout seul.
 */
export default function ControleTickets() {
  const [code, setCode] = useState('');
  const [resultat, setResultat] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);
  const champ = useRef(null);

  async function valider(e) {
    e.preventDefault();
    if (!code.trim()) return;
    setErreur(null);
    setResultat(null);
    setEnvoi(true);
    try {
      setResultat(await adminApi.validerTicket(code.trim()));
      setCode('');
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
      champ.current?.focus();
    }
  }

  return (
    <div className="max-w-xl space-y-5">
      <form onSubmit={valider} className="panneau space-y-4">
        <Champ
          ref={champ}
          label="Code du ticket"
          aide="Scannez le QR code ou collez son contenu (TRANSPORTTN:…)."
          value={code}
          onChange={(e) => setCode(e.target.value)}
          autoFocus
          autoComplete="off"
        />
        <button type="submit" className="btn-principal w-full" disabled={envoi || !code.trim()}>
          {envoi ? 'Vérification…' : 'Valider le ticket'}
        </button>
      </form>

      <Alerte>{erreur}</Alerte>
      {resultat && (
        <div role="status" className="rounded-xl border-2 border-bus bg-bus/5 p-5">
          <p className="font-titre text-2xl font-bold text-bus">Ticket valide, embarquement autorisé</p>
          <p className="mt-2 font-bold">{resultat.itineraire}</p>
          <p className="text-encre-doux">Départ {dateHeure(resultat.dateHeureDepart)}, {resultat.stationDepart}</p>
          <p className="text-sm text-encre-doux">Réservation {resultat.reference}</p>
        </div>
      )}
    </div>
  );
}
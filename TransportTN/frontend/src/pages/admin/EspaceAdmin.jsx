import { useSearchParams } from 'react-router';
import ControleTickets from './ControleTickets';
import GestionChauffeurs from './GestionChauffeurs.jsx';
import GestionLignes from './GestionLignes';
import Statistiques from './Statistiques';

const ONGLETS = [
  ['statistiques', 'Vue d’ensemble', Statistiques],
  ['lignes', 'Lignes et départs', GestionLignes],
  ['chauffeurs', 'Chauffeurs', GestionChauffeurs],
  ['tickets', 'Contrôle des tickets', ControleTickets],
];

export default function EspaceAdmin() {
  const [parametres, setParametres] = useSearchParams();
  const actif = ONGLETS.find(([cle]) => cle === parametres.get('onglet')) ?? ONGLETS[0];
  const [, , Contenu] = actif;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-extrabold">Administration</h1>

      <div role="tablist" aria-label="Sections d'administration" className="flex gap-1 overflow-x-auto border-b border-encre/10">
        {ONGLETS.map(([cle, libelle]) => (
          <button
            key={cle}
            type="button"
            role="tab"
            id={`onglet-${cle}`}
            aria-selected={actif[0] === cle}
            aria-controls="panneau-admin"
            onClick={() => setParametres({ onglet: cle })}
            className={`-mb-px whitespace-nowrap border-b-2 px-4 py-2.5 text-sm font-bold ${
              actif[0] === cle ? 'border-sidi text-sidi' : 'border-transparent text-encre-doux hover:text-encre'
            }`}
          >
            {libelle}
          </button>
        ))}
      </div>

      <div role="tabpanel" id="panneau-admin" aria-labelledby={`onglet-${actif[0]}`}>
        <Contenu />
      </div>
    </div>
  );
}
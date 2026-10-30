import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api/services';
import Alerte from '../../components/Alerte';
import Champ from '../../components/Champ';
import Chargement from '../../components/Chargement';
import { dateISO, prix } from '../../utils/format';

const LIGNE_VIDE = {
  type: 'BUS', villeDepart: '', villeArrivee: '', stationDepart: '', stationArrivee: '',
  prix: '', capacite: '', dureeMinutes: '',
};
const nombreOuNull = (v) => (v === '' || v == null ? null : Number(v));

function FormulaireLigne({ ligne, onEnregistree, onAnnuler }) {
  const [valeurs, setValeurs] = useState(ligne ?? LIGNE_VIDE);
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);
  const modifier = (champ) => (e) => setValeurs({ ...valeurs, [champ]: e.target.value });
  const d = erreur?.details ?? {};

  async function soumettre(e) {
    e.preventDefault();
    setErreur(null);
    setEnvoi(true);
    const corps = {
      ...valeurs,
      prix: nombreOuNull(valeurs.prix),
      capacite: nombreOuNull(valeurs.capacite),
      dureeMinutes: nombreOuNull(valeurs.dureeMinutes),
    };
    try {
      await (ligne ? adminApi.modifierLigne(ligne.id, corps) : adminApi.creerLigne(corps));
      onEnregistree(ligne ? 'Ligne modifiée.' : 'Ligne créée.');
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <form onSubmit={soumettre} className="panneau grid gap-4 sm:grid-cols-2" noValidate>
      <h3 className="text-lg font-bold sm:col-span-2">{ligne ? `Modifier ${ligne.villeDepart} → ${ligne.villeArrivee}` : 'Nouvelle ligne'}</h3>
      {erreur && <div className="sm:col-span-2"><Alerte>{erreur}</Alerte></div>}
      <Champ label="Type" as="select" value={valeurs.type} onChange={modifier('type')} erreur={d.type}>
        <option value="BUS">Bus</option>
        <option value="COLLECTIF">Louage (taxi collectif)</option>
      </Champ>
      <Champ label="Prix par place (DT)" type="number" step="0.001" min="0.1" value={valeurs.prix} onChange={modifier('prix')} erreur={d.prix} />
      <Champ label="Ville de départ" value={valeurs.villeDepart} onChange={modifier('villeDepart')} erreur={d.villeDepart} />
      <Champ label="Ville d’arrivée" value={valeurs.villeArrivee} onChange={modifier('villeArrivee')} erreur={d.villeArrivee} />
      <Champ label="Station de départ" value={valeurs.stationDepart} onChange={modifier('stationDepart')} erreur={d.stationDepart} />
      <Champ label="Station d’arrivée" value={valeurs.stationArrivee} onChange={modifier('stationArrivee')} erreur={d.stationArrivee} />
      <Champ
        label="Capacité (places)"
        type="number"
        min="1"
        aide={valeurs.type === 'COLLECTIF' ? '8 places maximum pour un louage.' : undefined}
        value={valeurs.capacite}
        onChange={modifier('capacite')}
        erreur={d.capacite}
      />
      <Champ label="Durée du trajet (minutes)" type="number" min="5" value={valeurs.dureeMinutes} onChange={modifier('dureeMinutes')} erreur={d.dureeMinutes} />
      <div className="flex gap-2 sm:col-span-2">
        <button type="submit" className="btn-principal" disabled={envoi}>{envoi ? 'Enregistrement…' : 'Enregistrer la ligne'}</button>
        <button type="button" className="btn-secondaire" onClick={onAnnuler}>Annuler</button>
      </div>
    </form>
  );
}

function GenerationDeparts({ ligne, onTerminee, onAnnuler }) {
  const [dateDebut, setDateDebut] = useState(dateISO());
  const [dateFin, setDateFin] = useState(() => {
    const fin = new Date();
    fin.setDate(fin.getDate() + 6);
    return dateISO(fin);
  });
  const [heures, setHeures] = useState('07:00, 10:30, 14:00, 18:00');
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  async function soumettre(e) {
    e.preventDefault();
    setErreur(null);
    setEnvoi(true);
    try {
      const liste = heures.split(/[\s,;]+/).filter(Boolean);
      const r = await adminApi.genererDeparts(ligne.id, { dateDebut, dateFin, heures: liste });
      onTerminee(`${r.departsCrees} départ(s) créé(s), ${r.departsIgnores} ignoré(s) (déjà existants ou passés).`);
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <form onSubmit={soumettre} className="panneau grid gap-4 sm:grid-cols-3" noValidate>
      <h3 className="text-lg font-bold sm:col-span-3">Programmer des départs : {ligne.villeDepart} → {ligne.villeArrivee}</h3>
      {erreur && <div className="sm:col-span-3"><Alerte>{erreur}</Alerte></div>}
      <Champ label="Du" type="date" min={dateISO()} value={dateDebut} onChange={(e) => setDateDebut(e.target.value)} erreur={erreur?.details?.dateDebut} />
      <Champ label="Au" type="date" min={dateDebut} value={dateFin} onChange={(e) => setDateFin(e.target.value)} erreur={erreur?.details?.dateFin} />
      <Champ label="Heures de départ" aide="Séparées par des virgules" value={heures} onChange={(e) => setHeures(e.target.value)} erreur={erreur?.details?.heures} />
      <div className="flex gap-2 sm:col-span-3">
        <button type="submit" className="btn-principal" disabled={envoi}>{envoi ? 'Création…' : 'Créer les départs'}</button>
        <button type="button" className="btn-secondaire" onClick={onAnnuler}>Annuler</button>
      </div>
    </form>
  );
}

export default function GestionLignes() {
  const [lignes, setLignes] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [mode, setMode] = useState(null);  // { type: 'creer' | 'modifier' | 'departs', ligne }

  const charger = useCallback(() => {
    adminApi.lignes().then(setLignes).catch(setErreur);
  }, []);

  useEffect(charger, [charger]);

  function terminer(texte) {
    setMode(null);
    setMessage(texte);
    charger();
  }

  async function basculer(ligne) {
    setErreur(null);
    try {
      await adminApi.activerLigne(ligne.id, !ligne.active);
      terminer(ligne.active ? 'Ligne mise hors service.' : 'Ligne remise en service.');
    } catch (err) {
      setErreur(err);
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-encre-doux">Les lignes hors service n’apparaissent plus dans le catalogue.</p>
        <button type="button" className="btn-principal" onClick={() => { setMessage(null); setMode({ type: 'creer' }); }}>
          Ajouter une ligne
        </button>
      </div>

      <Alerte>{erreur}</Alerte>
      <Alerte type="succes">{message}</Alerte>

      {mode?.type === 'creer' && <FormulaireLigne onEnregistree={terminer} onAnnuler={() => setMode(null)} />}
      {mode?.type === 'modifier' && (
        <FormulaireLigne key={mode.ligne.id} ligne={mode.ligne} onEnregistree={terminer} onAnnuler={() => setMode(null)} />
      )}
      {mode?.type === 'departs' && (
        <GenerationDeparts key={mode.ligne.id} ligne={mode.ligne} onTerminee={terminer} onAnnuler={() => setMode(null)} />
      )}

      {lignes === null && !erreur && <Chargement />}
      {lignes && (
        <div className="overflow-x-auto rounded-xl border border-encre/10 bg-white">
          <table className="w-full min-w-[46rem] text-left text-sm">
            <thead className="border-b border-encre/10 bg-chaux text-encre-doux">
              <tr>
                <th scope="col" className="px-4 py-3 font-bold">Type</th>
                <th scope="col" className="px-4 py-3 font-bold">Trajet</th>
                <th scope="col" className="px-4 py-3 text-right font-bold">Prix</th>
                <th scope="col" className="px-4 py-3 text-right font-bold">Places</th>
                <th scope="col" className="px-4 py-3 font-bold">État</th>
                <th scope="col" className="px-4 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-encre/10">
              {lignes.map((l) => (
                <tr key={l.id} className={l.active ? '' : 'text-encre-doux'}>
                  <td className="px-4 py-3">
                    <span className="inline-flex items-center gap-2">
                      <span aria-hidden="true" className={`h-2.5 w-2.5 rounded-full ${l.type === 'BUS' ? 'bg-bus' : 'bg-louage'}`} />
                      {l.type === 'BUS' ? 'Bus' : 'Louage'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className="font-bold">{l.villeDepart} → {l.villeArrivee}</span>
                    <span className="block text-xs text-encre-doux">{l.stationDepart}</span>
                  </td>
                  <td className="px-4 py-3 text-right tabular-nums">{prix(l.prix)}</td>
                  <td className="px-4 py-3 text-right tabular-nums">{l.capacite}</td>
                  <td className="px-4 py-3">{l.active ? 'En service' : 'Hors service'}</td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-1">
                      <button type="button" className="btn-discret" onClick={() => setMode({ type: 'departs', ligne: l })}>Départs</button>
                      <button type="button" className="btn-discret" onClick={() => setMode({ type: 'modifier', ligne: l })}>Modifier</button>
                      <button type="button" className="btn-discret" onClick={() => basculer(l)}>
                        {l.active ? 'Désactiver' : 'Activer'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
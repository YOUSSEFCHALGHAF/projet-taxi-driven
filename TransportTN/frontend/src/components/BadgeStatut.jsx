import { LIBELLES_STATUT, LIBELLES_TICKET } from '../utils/format';

const COULEURS = {
  EN_ATTENTE: 'bg-amber-100 text-amber-900',
  ACCEPTEE: 'bg-sky-100 text-sky-900',
  EN_COURS: 'bg-indigo-100 text-indigo-900',
  TERMINEE: 'bg-emerald-100 text-emerald-900',
  ANNULEE: 'bg-slate-200 text-slate-700',
  VALIDE: 'bg-emerald-100 text-emerald-900',
  UTILISE: 'bg-slate-200 text-slate-700',
  ANNULE: 'bg-slate-200 text-slate-700',
};

export default function BadgeStatut({ statut }) {
  const libelle = LIBELLES_STATUT[statut] ?? LIBELLES_TICKET[statut] ?? statut;
  return (
    <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-bold ${COULEURS[statut] ?? 'bg-slate-100'}`}>
      {libelle}
    </span>
  );
}
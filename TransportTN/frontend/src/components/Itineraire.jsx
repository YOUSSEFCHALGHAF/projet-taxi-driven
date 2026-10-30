/**
 * Trajet dessiné comme sur un plan de transport : un arrêt, une ligne, un arrêt.
 * `couleur` : classe Tailwind de fond du mode (bg-taxi, bg-louage, bg-bus).
 */
export default function Itineraire({ depart, sousDepart, arrivee, sousArrivee, couleur = 'bg-sidi' }) {
  return (
    <div className="relative pl-6">
      <span aria-hidden="true" className={`absolute bottom-3 left-[6px] top-3 w-0.5 ${couleur} opacity-50`} />
      <div className="relative pb-3">
        <span aria-hidden="true" className={`absolute -left-6 top-1 h-3.5 w-3.5 rounded-full ring-4 ring-white ${couleur}`} />
        <p className="font-bold">{depart}</p>
        {sousDepart && <p className="text-sm text-encre-doux">{sousDepart}</p>}
      </div>
      <div className="relative">
        <span aria-hidden="true" className="absolute -left-6 top-1 h-3.5 w-3.5 rounded-full border-[3px] border-encre bg-white" />
        <p className="font-bold">{arrivee}</p>
        {sousArrivee && <p className="text-sm text-encre-doux">{sousArrivee}</p>}
      </div>
    </div>
  );
}
const STYLES = {
  erreur: 'border-louage/30 bg-louage/5 text-louage',
  succes: 'border-bus/30 bg-bus/5 text-bus',
  info: 'border-sidi/30 bg-sidi/5 text-sidi-fonce',
};

/** Message affiché dans la page. Accepte un texte ou une ErreurApi. */
export default function Alerte({ type = 'erreur', children }) {
  if (!children) return null;
  const contenu = children instanceof Error ? children.message : children;
  return (
    <div role={type === 'erreur' ? 'alert' : 'status'} className={`rounded-lg border px-4 py-3 text-sm font-semibold ${STYLES[type]}`}>
      {contenu}
    </div>
  );
}
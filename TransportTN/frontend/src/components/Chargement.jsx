export default function Chargement({ texte = 'Chargement…' }) {
  return (
    <div role="status" className="flex items-center gap-3 py-10 text-encre-doux">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-encre/20 border-t-sidi" aria-hidden="true" />
      {texte}
    </div>
  );
}
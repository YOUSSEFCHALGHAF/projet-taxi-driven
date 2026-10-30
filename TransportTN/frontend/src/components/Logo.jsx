/** Deux arrêts reliés par un trajet pointillé : départ (jaune) et arrivée (bleu). */
export default function Logo({ taille = 28 }) {
  return (
    <svg width={taille} height={taille} viewBox="0 0 32 32" aria-hidden="true">
      <rect width="32" height="32" rx="8" fill="#13233F" />
      <path d="M9 22 L23 10" stroke="#F5F7FA" strokeWidth="2.5" strokeLinecap="round" strokeDasharray="1 4.5" />
      <circle cx="9" cy="22" r="4" fill="#F2C230" />
      <circle cx="23" cy="10" r="4" fill="#5B9BE6" />
    </svg>
  );
}
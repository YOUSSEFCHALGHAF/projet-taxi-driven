import { useEffect, useRef } from 'react';

/**
 * Appelle `rappel` tout de suite puis toutes les `delaiMs` millisecondes,
 * tant que `actif` est vrai. S'arrête automatiquement quand le composant disparaît.
 * (Le temps réel par WebSocket remplacera ce sondage à l'étape 6.)
 */
export function useSondage(rappel, delaiMs, actif = true) {
  const rappelCourant = useRef(rappel);

  useEffect(() => {
    rappelCourant.current = rappel;
  });

  useEffect(() => {
    if (!actif) return undefined;
    rappelCourant.current();
    const id = setInterval(() => rappelCourant.current(), delaiMs);
    return () => clearInterval(id);
  }, [delaiMs, actif]);
}
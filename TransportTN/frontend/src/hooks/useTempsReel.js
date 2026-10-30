import { useContext, useEffect, useRef } from 'react';
import { TempsReelContext } from '../context/contexteTempsReel';

export function useTempsReel() {
  const contexte = useContext(TempsReelContext);
  if (!contexte) {
    throw new Error('useTempsReel doit être utilisé à l’intérieur de <TempsReelProvider>');
  }
  return contexte;
}

/**
 * Reçoit les messages d'une destination STOMP tant que le composant est affiché.
 * Ex. : useAbonnement('/user/queue/courses', (message) => ...)
 */
export function useAbonnement(destination, rappel, actif = true) {
  const { abonner } = useTempsReel();
  const rappelCourant = useRef(rappel);

  useEffect(() => {
    rappelCourant.current = rappel;
  });

  useEffect(() => {
    if (!actif) return undefined;
    return abonner(destination, (message) => rappelCourant.current(message));
  }, [abonner, destination, actif]);
}
import { Client } from '@stomp/stompjs';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { CLE_JETON } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import { TempsReelContext } from './contexteTempsReel';

const URL_WS = import.meta.env.VITE_WS_URL
  ?? `${(import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/^http/, 'ws')}/ws`;

/**
 * Une seule connexion WebSocket (STOMP) par onglet, ouverte quand l'utilisateur est connecté.
 * Les pages s'abonnent avec useAbonnement() ; les abonnements sont refaits après une reconnexion.
 */
export function TempsReelProvider({ children }) {
  const { utilisateur } = useAuth();
  const [connecte, setConnecte] = useState(false);
  const client = useRef(null);
  const ecouteurs = useRef(new Map());   // destination → Set de fonctions
  const abonnements = useRef(new Map()); // destination → abonnement STOMP actif

  const souscrire = useCallback((stomp, destination) => {
    if (abonnements.current.has(destination)) return;
    abonnements.current.set(destination, stomp.subscribe(destination, (trame) => {
      let contenu;
      try {
        contenu = JSON.parse(trame.body);
      } catch {
        return;
      }
      ecouteurs.current.get(destination)?.forEach((ecouteur) => ecouteur(contenu));
    }));
  }, []);

  useEffect(() => {
    if (!utilisateur) return undefined;
    const actifs = abonnements.current;

    const stomp = new Client({
      brokerURL: URL_WS,
      // Le navigateur ne peut pas ajouter d'en-tête à la connexion WebSocket :
      // le jeton est envoyé dans la trame STOMP CONNECT, vérifiée par le serveur.
      connectHeaders: { Authorization: `Bearer ${localStorage.getItem(CLE_JETON)}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        abonnements.current.clear();
        ecouteurs.current.forEach((_, destination) => souscrire(stomp, destination));
        setConnecte(true);
      },
      onWebSocketClose: () => {
        abonnements.current.clear();
        setConnecte(false);
      },
      onStompError: (trame) => console.warn('Temps réel refusé :', trame.headers.message),
    });

    client.current = stomp;
    stomp.activate();

    return () => {
      client.current = null;
      actifs.clear();
      setConnecte(false);
      stomp.deactivate();
    };
  }, [utilisateur, souscrire]);

  /** Renvoie une fonction de désabonnement. */
  const abonner = useCallback((destination, ecouteur) => {
    if (!ecouteurs.current.has(destination)) ecouteurs.current.set(destination, new Set());
    ecouteurs.current.get(destination).add(ecouteur);
    if (client.current?.connected) souscrire(client.current, destination);

    return () => {
      const ensemble = ecouteurs.current.get(destination);
      ensemble?.delete(ecouteur);
      if (ensemble && ensemble.size === 0) {
        ecouteurs.current.delete(destination);
        try {
          abonnements.current.get(destination)?.unsubscribe();
        } catch {
          // connexion déjà fermée
        }
        abonnements.current.delete(destination);
      }
    };
  }, [souscrire]);

  const valeur = useMemo(() => ({ connecte, abonner }), [connecte, abonner]);

  return <TempsReelContext.Provider value={valeur}>{children}</TempsReelContext.Provider>;
}
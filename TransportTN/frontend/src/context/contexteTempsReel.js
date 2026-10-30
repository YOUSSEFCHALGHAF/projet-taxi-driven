import { createContext } from 'react';

export const TempsReelContext = createContext(null);
/**
 * Adresse du WebSocket :
 *  - VITE_WS_URL si elle est définie (développement : ws://localhost:8080/ws) ;
 *  - sinon déduite de VITE_API_URL ;
 *  - sinon (production, API derrière le même Nginx) : même domaine que la page, en wss:// si HTTPS.
 */
function adresseWebSocket() {
  if (import.meta.env.VITE_WS_URL) return import.meta.env.VITE_WS_URL;
  const api = import.meta.env.VITE_API_URL;
  if (api) return `${api.replace(/^http/, 'ws')}/ws`;
  const protocole = window.location.protocol === 'https:' ? 'wss' : 'ws';
  return `${protocole}://${window.location.host}/ws`;
}
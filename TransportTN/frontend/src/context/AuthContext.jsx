import { useCallback, useEffect, useMemo, useState } from 'react';
import { CLE_JETON, EVENEMENT_SESSION_EXPIREE } from '../api/client';
import { authApi } from '../api/services';
import { AuthContext } from './contexteAuth';

export function AuthProvider({ children }) {
  const [utilisateur, setUtilisateur] = useState(null);
  // Vrai seulement si un jeton est stocké et doit être vérifié au démarrage
  const [initialisation, setInitialisation] = useState(() => Boolean(localStorage.getItem(CLE_JETON)));
  const [sessionExpiree, setSessionExpiree] = useState(false);

  const deconnecter = useCallback(() => {
    localStorage.removeItem(CLE_JETON);
    setUtilisateur(null);
  }, []);

  // Au chargement : si un jeton est stocké, on vérifie qu'il est encore valide
  useEffect(() => {
    if (!localStorage.getItem(CLE_JETON)) return;
    authApi.moi()
      .then(setUtilisateur)
      .catch(() => localStorage.removeItem(CLE_JETON))
      .finally(() => setInitialisation(false));
  }, []);

  // Un appel API a reçu un 401 : le jeton a expiré
  useEffect(() => {
    const surExpiration = () => {
      deconnecter();
      setSessionExpiree(true);
    };
    window.addEventListener(EVENEMENT_SESSION_EXPIREE, surExpiration);
    return () => window.removeEventListener(EVENEMENT_SESSION_EXPIREE, surExpiration);
  }, [deconnecter]);

  const ouvrirSession = useCallback((reponse) => {
    localStorage.setItem(CLE_JETON, reponse.token);
    setUtilisateur(reponse.utilisateur);
    setSessionExpiree(false);
    return reponse.utilisateur;
  }, []);

  const connecter = useCallback(
    async (telephone, motDePasse) => ouvrirSession(await authApi.connexion(telephone, motDePasse)),
    [ouvrirSession],
  );

  const inscrire = useCallback(
    async (formulaire) => ouvrirSession(await authApi.inscription(formulaire)),
    [ouvrirSession],
  );

  const valeur = useMemo(
    () => ({ utilisateur, initialisation, sessionExpiree, connecter, inscrire, deconnecter }),
    [utilisateur, initialisation, sessionExpiree, connecter, inscrire, deconnecter],
  );

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>;
}
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { accueilSelonRole } from '../utils/format';
import Chargement from './Chargement';

/** Réservé aux utilisateurs connectés ayant l'un des rôles indiqués. */
export function RouteProtegee({ roles }) {
  const { utilisateur, initialisation } = useAuth();
  const location = useLocation();

  if (initialisation) return <Chargement texte="Vérification de la session…" />;
  if (!utilisateur) {
    return <Navigate to="/connexion" replace state={{ depuis: location.pathname }} />;
  }
  if (roles && !roles.includes(utilisateur.role)) {
    return <Navigate to={accueilSelonRole(utilisateur.role)} replace />;
  }
  return <Outlet />;
}

/** Pages connexion / inscription : inutiles si on est déjà connecté. */
export function RouteVisiteur() {
  const { utilisateur, initialisation } = useAuth();

  if (initialisation) return <Chargement texte="Vérification de la session…" />;
  if (utilisateur) return <Navigate to={accueilSelonRole(utilisateur.role)} replace />;
  return <Outlet />;
}
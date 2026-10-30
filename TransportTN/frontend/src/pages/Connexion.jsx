import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
import Alerte from '../components/Alerte';
import Champ from '../components/Champ';
import { useAuth } from '../hooks/useAuth';
import { accueilSelonRole } from '../utils/format';

export default function Connexion() {
  const { connecter, sessionExpiree } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [telephone, setTelephone] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  async function soumettre(evenement) {
    evenement.preventDefault();
    setErreur(null);
    setEnvoi(true);
    try {
      const utilisateur = await connecter(telephone, motDePasse);
      navigate(location.state?.depuis ?? accueilSelonRole(utilisateur.role), { replace: true });
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-3xl font-extrabold">Se connecter</h1>
      <p className="mt-2 text-encre-doux">Avec votre numéro de téléphone tunisien.</p>

      <form onSubmit={soumettre} className="panneau mt-6 space-y-4" noValidate>
        {sessionExpiree && <Alerte type="info">Votre session a expiré. Reconnectez-vous pour continuer.</Alerte>}
        <Alerte>{erreur}</Alerte>

        <Champ
          label="Numéro de téléphone"
          type="tel"
          inputMode="tel"
          autoComplete="tel"
          placeholder="22 123 456"
          value={telephone}
          onChange={(e) => setTelephone(e.target.value)}
          erreur={erreur?.details?.telephone}
          required
        />
        <Champ
          label="Mot de passe"
          type="password"
          autoComplete="current-password"
          value={motDePasse}
          onChange={(e) => setMotDePasse(e.target.value)}
          erreur={erreur?.details?.motDePasse}
          required
        />

        <button type="submit" className="btn-principal w-full py-3" disabled={envoi}>
          {envoi ? 'Connexion…' : 'Se connecter'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm">
        Pas encore de compte ? <Link to="/inscription" className="font-bold text-sidi underline">Créer un compte</Link>
      </p>

      <details className="mt-8 rounded-lg border border-dashed border-encre/20 p-4 text-sm text-encre-doux">
        <summary className="cursor-pointer font-bold text-encre">Comptes de démonstration</summary>
        <p className="mt-2">Mot de passe pour tous : <strong>Password123</strong></p>
        <ul className="mt-1 space-y-0.5">
          <li>Client : 22 111 222</li>
          <li>Chauffeur : 55 444 555</li>
          <li>Administrateur : 20 000 000</li>
        </ul>
      </details>
    </div>
  );
}
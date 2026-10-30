import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import Alerte from '../components/Alerte';
import Champ from '../components/Champ';
import { useAuth } from '../hooks/useAuth';

const VIDE = { nom: '', prenom: '', telephone: '', email: '', motDePasse: '', confirmation: '' };

/** Mêmes règles que le backend, pour prévenir l'utilisateur avant l'envoi. */
function valider(f) {
  const erreurs = {};
  if (!f.nom.trim()) erreurs.nom = 'Le nom est obligatoire';
  if (!f.prenom.trim()) erreurs.prenom = 'Le prénom est obligatoire';
  const chiffres = f.telephone.replace(/[\s.\-()]/g, '').replace(/^(\+216|00216)/, '');
  if (!/^[2-9]\d{7}$/.test(chiffres)) erreurs.telephone = 'Numéro tunisien à 8 chiffres, ex : 22 123 456';
  if (f.email && !/^\S+@\S+\.\S+$/.test(f.email)) erreurs.email = "L'adresse e-mail est invalide";
  if (!/^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(f.motDePasse)) {
    erreurs.motDePasse = 'Au moins 8 caractères, dont une lettre et un chiffre';
  }
  if (f.confirmation !== f.motDePasse) erreurs.confirmation = 'Les deux mots de passe sont différents';
  return erreurs;
}

export default function Inscription() {
  const { inscrire } = useAuth();
  const navigate = useNavigate();

  const [formulaire, setFormulaire] = useState(VIDE);
  const [erreursLocales, setErreursLocales] = useState({});
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);

  const erreurs = { ...erreursLocales, ...(erreur?.details ?? {}) };
  const modifier = (champ) => (e) => setFormulaire({ ...formulaire, [champ]: e.target.value });

  async function soumettre(evenement) {
    evenement.preventDefault();
    setErreur(null);
    const verification = valider(formulaire);
    setErreursLocales(verification);
    if (Object.keys(verification).length > 0) return;

    setEnvoi(true);
    try {
      const { confirmation: _ignore, ...donnees } = formulaire;
      await inscrire({ ...donnees, email: donnees.email.trim() || null });
      navigate('/tableau-de-bord', { replace: true });
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="text-3xl font-extrabold">Créer un compte</h1>
      <p className="mt-2 text-encre-doux">Gratuit, et vous pourrez réserver tout de suite.</p>

      <form onSubmit={soumettre} className="panneau mt-6 grid gap-4 sm:grid-cols-2" noValidate>
        {erreur && (
          <div className="sm:col-span-2">
            <Alerte>{erreur}</Alerte>
          </div>
        )}
        <Champ label="Prénom" autoComplete="given-name" value={formulaire.prenom} onChange={modifier('prenom')} erreur={erreurs.prenom} />
        <Champ label="Nom" autoComplete="family-name" value={formulaire.nom} onChange={modifier('nom')} erreur={erreurs.nom} />
        <Champ
          className="sm:col-span-2"
          label="Numéro de téléphone"
          type="tel"
          inputMode="tel"
          autoComplete="tel"
          placeholder="22 123 456"
          aide="Il servira à vous connecter."
          value={formulaire.telephone}
          onChange={modifier('telephone')}
          erreur={erreurs.telephone}
        />
        <Champ
          className="sm:col-span-2"
          label="E-mail (facultatif)"
          type="email"
          autoComplete="email"
          value={formulaire.email}
          onChange={modifier('email')}
          erreur={erreurs.email}
        />
        <Champ
          label="Mot de passe"
          type="password"
          autoComplete="new-password"
          aide="8 caractères minimum, avec une lettre et un chiffre."
          value={formulaire.motDePasse}
          onChange={modifier('motDePasse')}
          erreur={erreurs.motDePasse}
        />
        <Champ
          label="Confirmer le mot de passe"
          type="password"
          autoComplete="new-password"
          value={formulaire.confirmation}
          onChange={modifier('confirmation')}
          erreur={erreurs.confirmation}
        />
        <button type="submit" className="btn-principal py-3 sm:col-span-2" disabled={envoi}>
          {envoi ? 'Création du compte…' : 'Créer mon compte'}
        </button>
      </form>

      <p className="mt-4 text-center text-sm">
        Déjà inscrit ? <Link to="/connexion" className="font-bold text-sidi underline">Se connecter</Link>
      </p>
    </div>
  );
}
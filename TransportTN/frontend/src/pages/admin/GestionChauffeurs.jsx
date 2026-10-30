import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api/services';
import Alerte from '../../components/Alerte';
import Champ from '../../components/Champ';
import Chargement from '../../components/Chargement';

const VIDE = {
  prenom: '', nom: '', telephone: '', email: '', motDePasse: '',
  numeroPermis: '', matricule: '', modeleVehicule: '',
};

function FormulaireChauffeur({ onCree, onAnnuler }) {
  const [valeurs, setValeurs] = useState(VIDE);
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);
  const modifier = (champ) => (e) => setValeurs({ ...valeurs, [champ]: e.target.value });
  const d = erreur?.details ?? {};

  async function soumettre(e) {
    e.preventDefault();
    setErreur(null);
    setEnvoi(true);
    try {
      const chauffeur = await adminApi.creerChauffeur({ ...valeurs, email: valeurs.email.trim() || null });
      onCree(`Compte créé pour ${chauffeur.nomComplet}. Il peut se connecter avec son numéro.`);
    } catch (err) {
      setErreur(err);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <form onSubmit={soumettre} className="panneau grid gap-4 sm:grid-cols-2" noValidate>
      <h3 className="text-lg font-bold sm:col-span-2">Nouveau chauffeur</h3>
      {erreur && <div className="sm:col-span-2"><Alerte>{erreur}</Alerte></div>}
      <Champ label="Prénom" value={valeurs.prenom} onChange={modifier('prenom')} erreur={d.prenom} />
      <Champ label="Nom" value={valeurs.nom} onChange={modifier('nom')} erreur={d.nom} />
      <Champ label="Téléphone" type="tel" placeholder="55 123 456" value={valeurs.telephone} onChange={modifier('telephone')} erreur={d.telephone} />
      <Champ label="E-mail (facultatif)" type="email" value={valeurs.email} onChange={modifier('email')} erreur={d.email} />
      <Champ
        label="Mot de passe provisoire"
        type="text"
        autoComplete="off"
        aide="À communiquer au chauffeur."
        value={valeurs.motDePasse}
        onChange={modifier('motDePasse')}
        erreur={d.motDePasse}
      />
      <Champ label="Numéro de permis" value={valeurs.numeroPermis} onChange={modifier('numeroPermis')} erreur={d.numeroPermis} />
      <Champ label="Matricule" placeholder="215 TU 4821" value={valeurs.matricule} onChange={modifier('matricule')} erreur={d.matricule} />
      <Champ label="Véhicule" placeholder="Peugeot 301 (jaune)" value={valeurs.modeleVehicule} onChange={modifier('modeleVehicule')} erreur={d.modeleVehicule} />
      <div className="flex gap-2 sm:col-span-2">
        <button type="submit" className="btn-principal" disabled={envoi}>{envoi ? 'Création…' : 'Créer le compte'}</button>
        <button type="button" className="btn-secondaire" onClick={onAnnuler}>Annuler</button>
      </div>
    </form>
  );
}

export default function GestionChauffeurs() {
  const [chauffeurs, setChauffeurs] = useState(null);
  const [erreur, setErreur] = useState(null);
  const [message, setMessage] = useState(null);
  const [formulaire, setFormulaire] = useState(false);

  const charger = useCallback(() => {
    adminApi.chauffeurs().then(setChauffeurs).catch(setErreur);
  }, []);

  useEffect(charger, [charger]);

  async function basculerCompte(c) {
    setErreur(null);
    try {
      await adminApi.activerUtilisateur(c.utilisateurId, !c.actif);
      setMessage(c.actif ? `Compte de ${c.nomComplet} désactivé.` : `Compte de ${c.nomComplet} réactivé.`);
      charger();
    } catch (err) {
      setErreur(err);
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex justify-end">
        <button type="button" className="btn-principal" onClick={() => { setMessage(null); setFormulaire(true); }}>
          Ajouter un chauffeur
        </button>
      </div>
      <Alerte>{erreur}</Alerte>
      <Alerte type="succes">{message}</Alerte>

      {formulaire && (
        <FormulaireChauffeur
          onCree={(texte) => { setFormulaire(false); setMessage(texte); charger(); }}
          onAnnuler={() => setFormulaire(false)}
        />
      )}

      {chauffeurs === null && !erreur && <Chargement />}
      {chauffeurs?.length === 0 && <p className="panneau text-encre-doux">Aucun chauffeur enregistré.</p>}
      <ul className="grid gap-4 md:grid-cols-2">
        {chauffeurs?.map((c) => (
          <li key={c.id} className={`panneau space-y-2 ${c.actif ? '' : 'opacity-60'}`}>
            <div className="flex items-start justify-between gap-2">
              <div>
                <p className="font-bold">{c.nomComplet}</p>
                <p className="text-sm text-encre-doux">{c.telephone}</p>
              </div>
              <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${c.disponible ? 'bg-emerald-100 text-emerald-900' : 'bg-slate-200 text-slate-700'}`}>
                {c.disponible ? 'Disponible' : 'Indisponible'}
              </span>
            </div>
            <p className="text-sm">
              {c.modeleVehicule}, <strong>{c.matricule}</strong>
            </p>
            <p className="text-sm text-encre-doux">Permis {c.numeroPermis}</p>
            <button type="button" className={c.actif ? 'btn-danger py-1.5' : 'btn-secondaire py-1.5'} onClick={() => basculerCompte(c)}>
              {c.actif ? 'Désactiver le compte' : 'Réactiver le compte'}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
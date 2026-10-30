import { Link } from 'react-router';

export default function Introuvable() {
  return (
    <div className="py-16 text-center">
      <h1 className="text-4xl font-extrabold">Cette page n’existe pas</h1>
      <p className="mt-3 text-encre-doux">L’adresse est peut-être mal saisie.</p>
      <Link to="/" className="btn-principal mt-6">Revenir à l’accueil</Link>
    </div>
  );
}
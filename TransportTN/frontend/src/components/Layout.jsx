import Logo from './Logo';
import { Link, NavLink, Outlet, useNavigate } from 'react-router';
import { useAuth } from '../hooks/useAuth';
import { detacherCeNavigateur } from '../notifications/firebase';
import Cloche from './Cloche';
const LIENS = {
  CLIENT: [
    ['/tableau-de-bord', 'Mon espace'],
    ['/taxi', 'Taxi'],
    ['/collectif', 'Louage'],
    ['/bus', 'Bus'],
    ['/mes-tickets', 'Mes tickets'],
    ['/historique', 'Historique'],
  ],
  DRIVER: [['/chauffeur', 'Mes courses']],
  ADMIN: [['/admin', 'Administration']],
};

export default function Layout() {
  const { utilisateur, deconnecter } = useAuth();
  const navigate = useNavigate();
  const liens = utilisateur ? LIENS[utilisateur.role] ?? [] : [];

  async function seDeconnecter() {
    await detacherCeNavigateur();   // plus de notifications push de ce compte sur ce navigateur
    deconnecter();
    navigate('/');
  }

  return (
    <div className="flex min-h-screen flex-col">
      <a
        href="#contenu"
        className="sr-only focus:not-sr-only focus:absolute focus:left-2 focus:top-2 focus:z-[1100] focus:rounded focus:bg-white focus:px-3 focus:py-2"
      >
        Aller au contenu
      </a>

      <header className="sticky top-0 z-[1000] border-b border-encre/10 bg-white/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-6 gap-y-2 px-4 py-3">
          <Link to="/" className="flex items-center gap-2 font-titre text-xl font-extrabold">
            <Logo />
            TransportTN
          </Link>

          {liens.length > 0 && (
            <nav
              aria-label="Navigation principale"
              className="order-3 -mx-4 flex w-full gap-1 overflow-x-auto px-4 pb-1 md:order-none md:mx-0 md:w-auto md:px-0 md:pb-0"
            >
              {liens.map(([vers, libelle]) => (
                <NavLink
                  key={vers}
                  to={vers}
                  className={({ isActive }) =>
                    `whitespace-nowrap rounded-md px-3 py-1.5 text-sm font-bold transition-colors ${
                      isActive ? 'bg-encre text-white' : 'text-encre-doux hover:bg-encre/5'
                    }`
                  }
                >
                  {libelle}
                </NavLink>
              ))}
            </nav>
          )}

          <div className="ml-auto flex items-center gap-2">
            {utilisateur ? (
              <>
                <Cloche />
                <span className="hidden text-sm text-encre-doux lg:inline">
                  {utilisateur.prenom} {utilisateur.nom}
                </span>
                <button type="button" onClick={seDeconnecter} className="btn-secondaire">
                  Se déconnecter
                </button>
              </>
            ) : (
              <>
                <Link to="/connexion" className="btn-discret">Se connecter</Link>
                <Link to="/inscription" className="btn-principal">Créer un compte</Link>
              </>
            )}
          </div>
        </div>
      </header>

      <main id="contenu" className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t border-encre/10 py-6 text-center text-sm text-encre-doux">
        TransportTN, projet académique. Cartes © contributeurs OpenStreetMap.
      </footer>
    </div>
  );
}
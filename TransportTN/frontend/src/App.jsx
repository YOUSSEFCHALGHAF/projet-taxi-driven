import { BrowserRouter, Route, Routes } from 'react-router';
import Layout from './components/Layout';
import { RouteProtegee, RouteVisiteur } from './components/Routes';
import { AuthProvider } from './context/AuthContext';
import { TempsReelProvider } from './context/TempsReelContext';
import Accueil from './pages/Accueil';
import Connexion from './pages/Connexion';
import EspaceAdmin from './pages/admin/EspaceAdmin';
import EspaceChauffeur from './pages/EspaceChauffeur';
import Historique from './pages/Historique';
import Inscription from './pages/Inscription';
import Introuvable from './pages/Introuvable';
import MesTickets from './pages/MesTickets';
import ReservationLigne from './pages/ReservationLigne';
import SuiviCourse from './pages/SuiviCourse';
import TableauDeBord from './pages/TableauDeBord';
import Taxi from './pages/Taxi';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <TempsReelProvider>
        <Routes>
          <Route element={<Layout />}>
            {/* Public */}
            <Route index element={<Accueil />} />
            <Route element={<RouteVisiteur />}>
              <Route path="connexion" element={<Connexion />} />
              <Route path="inscription" element={<Inscription />} />
            </Route>

            {/* Client */}
            <Route element={<RouteProtegee roles={['CLIENT']} />}>
              <Route path="tableau-de-bord" element={<TableauDeBord />} />
              <Route path="taxi" element={<Taxi />} />
              <Route path="taxi/:id" element={<SuiviCourse />} />
              {/* key : force un nouvel état en passant de Louage à Bus */}
              <Route path="collectif" element={<ReservationLigne key="COLLECTIF" type="COLLECTIF" />} />
              <Route path="bus" element={<ReservationLigne key="BUS" type="BUS" />} />
              <Route path="mes-tickets" element={<MesTickets />} />
              <Route path="historique" element={<Historique />} />
            </Route>

            {/* Chauffeur */}
            <Route element={<RouteProtegee roles={['DRIVER']} />}>
              <Route path="chauffeur" element={<EspaceChauffeur />} />
            </Route>

            {/* Administrateur */}
            <Route element={<RouteProtegee roles={['ADMIN']} />}>
              <Route path="admin" element={<EspaceAdmin />} />
            </Route>

            <Route path="*" element={<Introuvable />} />
          </Route>
        </Routes>
        </TempsReelProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
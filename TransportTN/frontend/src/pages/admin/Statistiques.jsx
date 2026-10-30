import { useEffect, useState } from 'react';
import { adminApi } from '../../api/services';
import Alerte from '../../components/Alerte';
import Chargement from '../../components/Chargement';
import { LIBELLES_STATUT, prix } from '../../utils/format';

function Chiffre({ valeur, libelle, detail }) {
  return (
    <div className="panneau">
      <p className="font-titre text-3xl font-bold tabular-nums">{valeur}</p>
      <p className="font-bold">{libelle}</p>
      {detail && <p className="text-sm text-encre-doux">{detail}</p>}
    </div>
  );
}

export default function Statistiques() {
  const [stats, setStats] = useState(null);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    adminApi.statistiques().then(setStats).catch(setErreur);
  }, []);

  if (erreur) return <Alerte>{erreur}</Alerte>;
  if (!stats) return <Chargement />;

  const parType = [
    ['Taxi', stats.reservationsTaxi, 'bg-taxi'],
    ['Louage', stats.reservationsCollectif, 'bg-louage'],
    ['Bus', stats.reservationsBus, 'bg-bus'],
  ];
  const maxType = Math.max(1, ...parType.map(([, n]) => n));
  const maxStatut = Math.max(1, ...Object.values(stats.reservationsParStatut));

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Chiffre valeur={stats.reservationsTotal} libelle="Réservations" detail={`dont ${stats.reservationsAujourdhui} aujourd’hui`} />
        <Chiffre valeur={prix(stats.montantTotalHorsAnnulations)} libelle="Montant réservé" detail="hors annulations" />
        <Chiffre valeur={stats.clients} libelle="Clients" />
        <Chiffre valeur={stats.chauffeurs} libelle="Chauffeurs" detail={`${stats.chauffeursDisponibles} disponible(s) maintenant`} />
        <Chiffre valeur={stats.lignesActives} libelle="Lignes en service" />
        <Chiffre valeur={stats.departsAVenir} libelle="Départs à venir" />
        <Chiffre valeur={stats.ticketsEmis} libelle="Tickets de bus" detail={`${stats.ticketsUtilises} déjà contrôlé(s)`} />
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <section className="panneau space-y-3" aria-labelledby="titre-type">
          <h2 id="titre-type" className="text-lg font-bold">Réservations par mode</h2>
          {parType.map(([libelle, n, couleur]) => (
            <div key={libelle} className="grid grid-cols-[5rem_1fr_3rem] items-center gap-3 text-sm">
              <span>{libelle}</span>
              <span className="h-3 rounded-full bg-encre/5">
                <span className={`block h-3 rounded-full ${couleur}`} style={{ width: `${(n / maxType) * 100}%` }} />
              </span>
              <span className="text-right font-bold tabular-nums">{n}</span>
            </div>
          ))}
        </section>

        <section className="panneau space-y-3" aria-labelledby="titre-statut">
          <h2 id="titre-statut" className="text-lg font-bold">Réservations par statut</h2>
          {Object.entries(stats.reservationsParStatut).map(([statut, n]) => (
            <div key={statut} className="grid grid-cols-[6rem_1fr_3rem] items-center gap-3 text-sm">
              <span>{LIBELLES_STATUT[statut]}</span>
              <span className="h-3 rounded-full bg-encre/5">
                <span className="block h-3 rounded-full bg-sidi" style={{ width: `${(n / maxStatut) * 100}%` }} />
              </span>
              <span className="text-right font-bold tabular-nums">{n}</span>
            </div>
          ))}
        </section>
      </div>
    </div>
  );
}
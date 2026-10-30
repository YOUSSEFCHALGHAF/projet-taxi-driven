import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { taxiApi } from '../api/services';
import Alerte from '../components/Alerte';
import CarteTrajet from '../components/CarteTrajet';
import Champ from '../components/Champ';
import { prix } from '../utils/format';
import { adresseDepuisPosition } from '../utils/geocodage';

export default function Taxi() {
  const navigate = useNavigate();
  const [depart, setDepart] = useState(null);       // { latitude, longitude, adresse }
  const [arrivee, setArrivee] = useState(null);
  const [cible, setCible] = useState('depart');      // point placé au prochain clic
  const [resultatEstimation, setResultatEstimation] = useState(null);   // { cle, estimation, erreur }
  const [modePaiement, setModePaiement] = useState('ESPECES');
  const [erreur, setErreur] = useState(null);
  const [envoi, setEnvoi] = useState(false);
  const [localisation, setLocalisation] = useState(false);

  /** Place un point puis cherche son adresse (sans écraser un point déplacé entre-temps). */
  function placer(quel, point) {
    const definir = quel === 'depart' ? setDepart : setArrivee;
    definir({ ...point, adresse: 'Recherche de l’adresse…' });
    adresseDepuisPosition(point.latitude, point.longitude).then((adresse) =>
      definir((actuel) =>
        actuel?.latitude === point.latitude && actuel?.longitude === point.longitude ? { ...actuel, adresse } : actuel,
      ),
    );
  }

  function surClicCarte(point) {
    placer(cible, point);
    if (cible === 'depart') setCible('arrivee');
  }

  function utiliserMaPosition() {
    if (!('geolocation' in navigator)) {
      setErreur('Votre navigateur ne permet pas la géolocalisation.');
      return;
    }
    setLocalisation(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        placer('depart', { latitude: pos.coords.latitude, longitude: pos.coords.longitude });
        setCible('arrivee');
        setLocalisation(false);
      },
      () => {
        setErreur('Position introuvable. Autorisez la localisation ou cliquez sur la carte.');
        setLocalisation(false);
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  }

  // Nouvelle estimation dès que les deux points sont connus
  const cleTrajet = depart && arrivee
    ? [depart.latitude, depart.longitude, arrivee.latitude, arrivee.longitude].join(',')
    : null;

  useEffect(() => {
    if (!cleTrajet) return undefined;
    const [latDepart, lngDepart, latArrivee, lngArrivee] = cleTrajet.split(',').map(Number);
    let annule = false;
    taxiApi.estimer({ latDepart, lngDepart, latArrivee, lngArrivee })
      .then((estimation) => !annule && setResultatEstimation({ cle: cleTrajet, estimation }))
      .catch((erreur) => !annule && setResultatEstimation({ cle: cleTrajet, erreur }));
    return () => {
      annule = true;
    };
  }, [cleTrajet]);

  // On n'affiche que l'estimation du trajet actuel
  const aJour = resultatEstimation?.cle === cleTrajet ? resultatEstimation : null;
  const estimation = aJour?.estimation ?? null;
  const erreurEstimation = aJour?.erreur ?? null;

  async function reserver() {
    setErreur(null);
    setEnvoi(true);
    try {
      const course = await taxiApi.reserver({
        adresseDepart: depart.adresse,
        latDepart: depart.latitude,
        lngDepart: depart.longitude,
        adresseArrivee: arrivee.adresse,
        latArrivee: arrivee.latitude,
        lngArrivee: arrivee.longitude,
        modePaiement,
      });
      navigate(`/taxi/${course.id}`);
    } catch (err) {
      setErreur(err);
      setEnvoi(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-extrabold">Commander un taxi</h1>
        <p className="mt-1 text-encre-doux">Cliquez sur la carte pour placer le départ, puis l’arrivée.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_22rem]">
        <div>
          <CarteTrajet depart={depart} arrivee={arrivee} onClic={surClicCarte} hauteur="h-[26rem] lg:h-[32rem]" />
          <p className="mt-2 text-sm text-encre-doux" aria-live="polite">
            Prochain clic sur la carte : <strong>{cible === 'depart' ? 'point de départ' : 'point d’arrivée'}</strong>
          </p>
        </div>

        <div className="panneau space-y-5">
          <ol className="space-y-5">
            <li className="space-y-2">
              <Champ
                label="1. Départ"
                placeholder="Cliquez sur la carte"
                value={depart?.adresse ?? ''}
                onChange={(e) => setDepart(depart && { ...depart, adresse: e.target.value })}
                disabled={!depart}
              />
              <div className="flex flex-wrap gap-2">
                <button type="button" className="btn-secondaire py-1.5" onClick={utiliserMaPosition} disabled={localisation}>
                  {localisation ? 'Localisation…' : 'Utiliser ma position'}
                </button>
                {depart && cible !== 'depart' && (
                  <button type="button" className="btn-discret" onClick={() => setCible('depart')}>Déplacer le départ</button>
                )}
              </div>
            </li>

            <li className="space-y-2">
              <Champ
                label="2. Arrivée"
                placeholder="Cliquez sur la carte"
                value={arrivee?.adresse ?? ''}
                onChange={(e) => setArrivee(arrivee && { ...arrivee, adresse: e.target.value })}
                disabled={!arrivee}
              />
              {arrivee && cible !== 'arrivee' && (
                <button type="button" className="btn-discret" onClick={() => setCible('arrivee')}>Déplacer l’arrivée</button>
              )}
            </li>

            <li className="space-y-3">
              <p className="text-sm font-bold">3. Confirmer</p>
              <Alerte>{erreurEstimation}</Alerte>
              {estimation ? (
                <dl className="grid grid-cols-3 gap-2 rounded-lg bg-chaux p-3 text-center">
                  <div>
                    <dt className="text-xs text-encre-doux">Distance</dt>
                    <dd className="font-bold tabular-nums">{estimation.distanceKm.toFixed(1)} km</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-encre-doux">Durée</dt>
                    <dd className="font-bold tabular-nums">{estimation.dureeEstimeeMinutes} min</dd>
                  </div>
                  <div>
                    <dt className="text-xs text-encre-doux">Prix</dt>
                    <dd className="font-bold tabular-nums">{prix(estimation.prixEstime)}</dd>
                  </div>
                  <p className="col-span-3 text-sm text-encre-doux">
                    {estimation.chauffeursDisponibles > 0
                      ? `${estimation.chauffeursDisponibles} chauffeur(s) disponible(s) à proximité`
                      : 'Aucun chauffeur disponible à proximité pour le moment'}
                  </p>
                </dl>
              ) : (
                !erreurEstimation && <p className="text-sm text-encre-doux">Le prix s’affiche dès que les deux points sont placés.</p>
              )}

              <Champ label="Paiement" as="select" value={modePaiement} onChange={(e) => setModePaiement(e.target.value)}>
                <option value="ESPECES">Espèces au chauffeur</option>
                <option value="CARTE">Carte bancaire</option>
              </Champ>

              <Alerte>{erreur}</Alerte>
              <button
                type="button"
                className="btn-principal w-full py-3"
                onClick={reserver}
                disabled={!estimation || envoi || estimation.chauffeursDisponibles === 0}
              >
                {envoi ? 'Recherche d’un chauffeur…' : 'Réserver ce taxi'}
              </button>
            </li>
          </ol>
        </div>
      </div>
    </div>
  );
}
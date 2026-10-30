import { useEffect } from 'react';
import { MapContainer, Marker, Polyline, TileLayer, Tooltip, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';

const CENTRE_TUNIS = [36.8065, 10.1815];

/** Marqueurs dessinés en HTML : évite le problème des icônes Leaflet manquantes avec Vite. */
const pastille = (lettre, fond, texte = '#13233F') =>
  L.divIcon({
    className: '',
    html: `<div style="width:30px;height:30px;border-radius:50%;background:${fond};color:${texte};
      border:3px solid #fff;box-shadow:0 1px 4px rgba(19,35,63,.4);display:grid;place-items:center;
      font:700 13px Manrope,sans-serif">${lettre}</div>`,
    iconSize: [30, 30],
    iconAnchor: [15, 15],
  });

const ICONE_DEPART = pastille('A', '#F2C230');
const ICONE_ARRIVEE = pastille('B', '#13233F', '#fff');
const ICONE_TAXI = L.divIcon({
  className: '',
  html: `<div style="width:36px;height:24px;border-radius:6px;background:#F2C230;border:2px solid #13233F;
    display:grid;place-items:center;font:800 10px Manrope,sans-serif;color:#13233F">TAXI</div>`,
  iconSize: [36, 24],
  iconAnchor: [18, 12],
});

function ClicSurCarte({ onClic }) {
  useMapEvents({
    click: (e) => onClic?.({ latitude: e.latlng.lat, longitude: e.latlng.lng }),
  });
  return null;
}

/** Recadre la carte pour montrer tous les points connus. */
function AjusterVue({ points }) {
  const carte = useMap();
  const cle = JSON.stringify(points);
  useEffect(() => {
    if (points.length === 1) carte.setView(points[0], Math.max(carte.getZoom(), 14));
    if (points.length > 1) carte.fitBounds(points, { padding: [40, 40], maxZoom: 15 });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cle, carte]);
  return null;
}

/**
 * Carte d'un trajet taxi.
 * depart / arrivee / chauffeur : { latitude, longitude } ou null
 * onClic : si fourni, un clic sur la carte renvoie le point choisi
 */
export default function CarteTrajet({ depart, arrivee, chauffeur, onClic, hauteur = 'h-80' }) {
  const points = [depart, arrivee, chauffeur]
    .filter((p) => p?.latitude != null)
    .map((p) => [p.latitude, p.longitude]);

  return (
    <div className={`overflow-hidden rounded-xl border border-encre/10 ${hauteur}`}>
      <MapContainer center={CENTRE_TUNIS} zoom={12} className="h-full w-full" scrollWheelZoom>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {onClic && <ClicSurCarte onClic={onClic} />}
        <AjusterVue points={points} />

        {depart && (
          <Marker position={[depart.latitude, depart.longitude]} icon={ICONE_DEPART}>
            <Tooltip>Départ</Tooltip>
          </Marker>
        )}
        {arrivee && (
          <Marker position={[arrivee.latitude, arrivee.longitude]} icon={ICONE_ARRIVEE}>
            <Tooltip>Arrivée</Tooltip>
          </Marker>
        )}
        {depart && arrivee && (
          <Polyline
            positions={[[depart.latitude, depart.longitude], [arrivee.latitude, arrivee.longitude]]}
            pathOptions={{ color: '#13233F', weight: 3, dashArray: '2 8', lineCap: 'round' }}
          />
        )}
        {chauffeur?.latitude != null && (
          <Marker position={[chauffeur.latitude, chauffeur.longitude]} icon={ICONE_TAXI}>
            <Tooltip>Votre chauffeur</Tooltip>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
}
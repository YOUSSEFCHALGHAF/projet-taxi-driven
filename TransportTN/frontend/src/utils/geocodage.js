/**
 * Transforme des coordonnées en adresse lisible avec Nominatim (OpenStreetMap).
 * Service gratuit : 1 requête par seconde maximum, on ne l'appelle qu'au clic.
 */
export async function adresseDepuisPosition(latitude, longitude) {
  const secours = `Point ${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&zoom=17&accept-language=fr`
      + `&lat=${latitude}&lon=${longitude}`;
    const reponse = await fetch(url);
    if (!reponse.ok) return secours;
    const { address: a = {} } = await reponse.json();
    const morceaux = [
      a.road ?? a.pedestrian ?? a.neighbourhood,
      a.suburb ?? a.quarter,
      a.city ?? a.town ?? a.village,
    ].filter(Boolean);
    return morceaux.length ? [...new Set(morceaux)].join(', ') : secours;
  } catch {
    return secours;
  }
}
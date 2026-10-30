package tn.transporttn.util;

/**
 * Calcul de distance « à vol d'oiseau » entre deux points GPS (formule de Haversine).
 * Sert à trouver le chauffeur le plus proche et à estimer le prix d'une course.
 */
public final class GeoUtils {

    private static final double RAYON_TERRE_KM = 6371.0;

    private GeoUtils() {
        // classe utilitaire : pas d'instance
    }

    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        return RAYON_TERRE_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
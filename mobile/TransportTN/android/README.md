# TransportTN Android

Application Android (Java, SDK 26+) pour TransportTN.

## Ouverture du projet

1. Ouvrez le dossier `android/` directement dans **Android Studio** (File > Open).
   Android Studio générera automatiquement le Gradle Wrapper (`gradlew`,
   `gradle-wrapper.jar`) au premier sync si nécessaire.
2. Attendez la fin du Gradle Sync (il téléchargera les dépendances listées dans
   `app/build.gradle`).

## Configuration requise avant de lancer l'app

### 1. Firebase (notifications push uniquement)

1. Créez un projet Firebase (ou réutilisez celui du backend) sur
   [console.firebase.google.com](https://console.firebase.google.com).
2. Ajoutez une application Android avec le package `com.transporttn.app`.
3. Téléchargez `google-services.json` et placez-le dans `android/app/` (remplace le
   fichier `google-services.json.example` fourni ici à titre indicatif).

**Rappel important** : Firebase n'est utilisé dans cette application QUE pour recevoir
les notifications push (`MyFirebaseMessagingService`). L'authentification utilise le
système OTP du backend Spring Boot (voir le README racine).

### 2. Google Maps

1. Obtenez une clé API sur la
   [Google Cloud Console](https://console.cloud.google.com/google/maps-apis) (activez
   "Maps SDK for Android").
2. Remplacez `YOUR_GOOGLE_MAPS_API_KEY` dans `app/src/main/AndroidManifest.xml`.

### 3. Adresse du backend

Par défaut, l'app pointe vers `http://10.0.2.2:8080/` en debug, ce qui correspond au
`localhost:8080` de votre machine hôte **depuis l'émulateur Android**. Si vous testez
sur un appareil physique, remplacez cette adresse par l'IP de votre machine sur le
réseau local (ou l'URL de votre serveur déployé), dans `app/build.gradle`
(`buildConfigField "String", "API_BASE_URL", ...`).

## Structure du code

```
app/src/main/java/com/transporttn/app/
├── activities/   SplashActivity, LoginActivity, OtpActivity, MainActivity,
│                 TaxiTrackingActivity (carte + suivi GPS), TicketDetailActivity (QR code)
├── fragments/    HomeFragment, TaxiFragment, CollectifFragment, BusFragment, ProfilFragment
├── adapters/     LigneAdapter (RecyclerView pour les lignes bus/collectif)
├── models/       POJOs correspondant aux DTOs de l'API
├── network/      ApiClient (Retrofit + intercepteur JWT), ApiService, TokenManager (stockage chiffré)
└── services/     MyFirebaseMessagingService (réception des notifications push)
```

## Parcours utilisateur implémenté

1. **Splash** → vérifie la session existante (token JWT stocké de façon chiffrée).
2. **Login** → saisie du numéro `+216XXXXXXXX`, demande d'un code OTP.
3. **OTP** → saisie du code à 6 chiffres (+ nom si première inscription) → JWT reçu et
   stocké → enregistrement automatique du token FCM pour les notifications.
4. **Accueil** → choix entre Taxi / Collectif / Bus, navigation par
   `BottomNavigationView`.
5. **Taxi** → carte Google Maps, saisie départ/arrivée, choix du mode de paiement →
   réservation → écran de suivi.
6. **Collectif** → liste des lignes avec places restantes en temps réel, sélection du
   nombre de places (1 à 4).
7. **Bus** → catalogue des lignes, sélection de date/quantité → achat → écran ticket
   avec QR code généré (ZXing).

## Points à compléter pour une version 100% production-ready

Ces points sont volontairement laissés en `TODO` explicite dans le code, car ils
nécessitent votre configuration Firebase/Google Cloud personnelle :

- **Suivi GPS temps réel** (`TaxiTrackingActivity.java`) : remplacer le polling REST
  d'exemple par un listener Firestore (`addSnapshotListener`) sur le document du
  chauffeur, pour un rafraîchissement fluide toutes les 3 secondes sans solliciter le
  backend à chaque fois (conforme au cahier des charges section 3).
- **Géocodage inversé en français** (`TaxiFragment.java`) : intégrer le `Geocoder`
  Android ou l'API Google Places Autocomplete pour convertir les clics sur la carte en
  adresses textuelles, plutôt que la saisie manuelle actuelle.
- Icônes de lancement : des icônes vectorielles simples sont fournies à titre de
  placeholder ; remplacez-les par votre propre identité visuelle avant publication sur
  le Play Store.

## Tests

Le projet inclut les dépendances JUnit 4, Mockito et Espresso (voir
`app/build.gradle`), conformément à l'objectif de couverture du cahier des charges
(80% sur la couche Service). Aucun test n'est fourni dans ce squelette — à compléter
selon votre planning (Phase 6 du cahier des charges).

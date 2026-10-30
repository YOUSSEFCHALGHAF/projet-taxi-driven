# TransportTN

Application mobile de réservation de transport en Tunisie — Taxi, Taxi Collectif, Bus Interurbain.

Ce dépôt contient l'ensemble du projet académique 2025 :

```
TransportTN/
├── backend/     Backend Spring Boot 3.2 (Java 17) + MySQL 8 + JWT + Firebase (notifications uniquement)
├── android/     Application Android (Java, SDK 26+) avec Retrofit, Google Maps, Firebase Cloud Messaging
├── postman/     Collection Postman (25 requêtes) pour tester l'API
└── docs/        Cahier des charges et présentation (fournis par vous)
```

## Choix d'architecture important : authentification

Le cahier des charges original prévoyait une authentification via **Firebase Phone Auth**.
Sur votre demande, **Firebase est utilisé uniquement pour les notifications push (FCM)**.
L'authentification repose donc sur un mécanisme **OTP généré et vérifié par le backend
Spring Boot lui-même** :

1. `POST /api/auth/otp/request` — le backend génère un code à 6 chiffres, le stocke (table
   `otp_codes`, expiration 5 min) et "l'envoie" via `SmsService` (actuellement **mocké** :
   le code est loggé côté serveur et renvoyé dans la réponse API en mode debug, pour
   pouvoir tester sans compte SMS payant).
2. `POST /api/auth/otp/verify` — vérifie le code, crée l'utilisateur s'il n'existe pas
   encore, et retourne un **JWT applicatif** (HMAC-SHA256, expiration 24h).
3. Toutes les routes protégées utilisent ensuite ce JWT via le header
   `Authorization: Bearer <token>`.

**Pour passer en production**, il suffit de remplacer le contenu de
`SmsService.sendOtp()` (`backend/src/main/java/com/transporttn/backend/service/SmsService.java`)
par un appel à un vrai fournisseur SMS (Twilio, Infobip, ou un agrégateur local
tunisien), et de mettre `app.otp.debug-mode=false` dans `application.properties`.

## Démarrage rapide (avec Docker)

```bash
cd backend
docker compose up -d
```

Cela démarre MySQL (avec le schéma + données de test déjà chargés) et le backend sur
`http://localhost:8080`. Voir `backend/README.md` pour le détail.

## Démarrage rapide (sans Docker)

Voir les instructions détaillées dans `backend/README.md` et `android/README.md`.

## Périmètre fonctionnel livré (P1 — Essentiel)

| Module | Statut |
|---|---|
| Authentification (OTP backend) | ✅ |
| Réservation Taxi (carte, matching chauffeur, prix estimé) | ✅ |
| Taxi Collectif (sélection ligne/places) | ✅ |
| Tickets Bus (achat + QR code) | ✅ |
| Suivi GPS temps réel (endpoint REST + squelette Firestore) | ✅ (voir note ci-dessous) |
| Notifications Push (FCM) | ✅ |

Le module "Suivi GPS temps réel" est livré avec un endpoint REST fonctionnel
(`PUT /api/driver/location`) et un écran de suivi (carte Google Maps) côté Android.
Conformément au cahier des charges, la mise à jour **toutes les 3 secondes** en
production devrait utiliser un listener **Firebase Firestore** côté client plutôt que
du polling REST — le point d'intégration est clairement indiqué par un `TODO` dans
`TaxiTrackingActivity.java`, à compléter une fois votre projet Firestore configuré.

Les modules P2 (Historique, Profil) et P3 (Dashboard Admin web) ne sont pas dans ce
livrable, conformément à votre choix de périmètre réduit. L'historique est toutefois
déjà exposé côté API (`GET /api/reservations/history`) si vous souhaitez l'ajouter
facilement côté Android plus tard.

## Prochaines étapes suggérées

1. Créer un projet Firebase, télécharger `google-services.json` (Android) et le fichier
   de credentials du compte de service (backend) — voir les fichiers `.example` fournis.
2. Obtenir une clé API Google Maps et la renseigner dans `AndroidManifest.xml`.
3. Remplacer `SmsService` par un vrai provider SMS avant la mise en production.
4. Ouvrir `android/` dans Android Studio (il proposera de générer le Gradle Wrapper
   automatiquement au premier lancement).
5. Importer `postman/TransportTN.postman_collection.json` dans Postman pour tester l'API.

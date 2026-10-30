# TransportTN Backend

API REST Spring Boot 3.2 (Java 17) pour l'application TransportTN.

## Prérequis

- JDK 17
- Maven 3.9+ (ou utilisez votre IDE, qui gère Maven automatiquement)
- MySQL 8.0 (ou Docker)

## Démarrage avec Docker (recommandé)

```bash
docker compose up -d
```

Démarre MySQL (schéma + données de test chargés automatiquement depuis
`database/schema.sql`) et le backend, exposé sur `http://localhost:8080`.

Pour activer les notifications Firebase dans ce mode, placez votre fichier
`firebase-service-account.json` à la racine de `backend/` avant de lancer
`docker compose up -d` (il est monté automatiquement dans le conteneur).

## Démarrage manuel

1. Créer la base de données (ou laisser Hibernate le faire automatiquement) :
   ```bash
   mysql -u root -p < database/schema.sql
   ```
2. Ajuster `src/main/resources/application.properties` selon votre environnement
   (utilisateur/mot de passe MySQL notamment).
3. Placer votre fichier de credentials Firebase (voir ci-dessous) à la racine du
   module, sous le nom `firebase-service-account.json`, ou désactiver Firebase avec
   `app.firebase.enabled=false` si vous testez sans notifications.
4. Lancer l'application :
   ```bash
   mvn spring-boot:run
   ```

L'API est alors disponible sur `http://localhost:8080`.

## Configuration Firebase (notifications uniquement)

**Firebase n'est utilisé ici QUE pour l'envoi de notifications push (FCM)**, jamais
pour l'authentification (voir le README racine pour le détail du choix d'architecture).

1. Créez un projet sur [console.firebase.google.com](https://console.firebase.google.com).
2. Paramètres du projet > Comptes de service > "Générer une nouvelle clé privée".
3. Renommez le fichier téléchargé en `firebase-service-account.json` et placez-le à la
   racine de `backend/` (ce fichier est dans `.gitignore`, ne le committez jamais).

Si vous n'avez pas encore configuré Firebase, mettez `app.firebase.enabled=false`
dans `application.properties` : le backend démarre normalement et logue simplement un
avertissement à la place d'envoyer les notifications.

## Authentification par OTP (sans Firebase)

Voir le README racine pour le détail. En résumé :

- `POST /api/auth/otp/request` `{ "phone": "+21620123456" }` → génère et "envoie" un
  code à 6 chiffres (loggé côté serveur ; renvoyé dans `debugCode` en mode debug).
- `POST /api/auth/otp/verify` `{ "phone": "...", "code": "...", "name": "..." }` →
  retourne un JWT applicatif.

Le champ `name` n'est requis que lors de la toute première connexion (création de
compte).

## Structure du code

```
src/main/java/com/transporttn/backend/
├── config/       SecurityConfig (JWT, CORS), FirebaseConfig
├── controller/   Endpoints REST
├── dto/          Objets de transfert (requêtes/réponses)
├── entity/       Entités JPA (mappées sur les 6+1 tables MySQL)
├── exception/    Exceptions métier + gestionnaire global
├── repository/   Repositories Spring Data JPA
├── security/     JwtUtil, JwtAuthFilter
└── service/      Logique métier (Auth, Otp, Sms, Matching, Reservation, Ticket, Ligne, Driver, Fcm)
```

## Endpoints principaux

Voir `postman/TransportTN.postman_collection.json` à la racine du projet pour la
liste complète (25 requêtes) avec exemples de corps de requête.

| Méthode | Endpoint | Auth |
|---|---|---|
| POST | /api/auth/otp/request | Non |
| POST | /api/auth/otp/verify | Non |
| GET | /api/auth/me | JWT |
| PUT | /api/users/fcm-token | JWT |
| POST | /api/reservations/taxi | JWT |
| POST | /api/reservations/collectif | JWT |
| POST | /api/reservations/bus | JWT |
| GET | /api/reservations/lignes/bus | JWT |
| GET | /api/reservations/lignes/collectif | JWT |
| GET | /api/reservations/tickets | JWT |
| GET | /api/reservations/history | JWT |
| PUT | /api/reservations/{id}/status | JWT |
| PUT | /api/driver/location | JWT (DRIVER) |
| PUT | /api/driver/available | JWT (DRIVER) |
| POST | /api/admin/lignes | JWT (ADMIN) |

## Tests avec les données de test

Le script `database/schema.sql` insère un client, un chauffeur (avec position GPS à
Tunis) et un admin, ainsi que 2 lignes de bus et 2 lignes de taxi collectif. Ces
comptes n'ont pas de mot de passe (authentification par OTP) : utilisez simplement
leur numéro de téléphone dans `POST /api/auth/otp/request` pour générer un code, puis
`POST /api/auth/otp/verify` pour obtenir un token.

## Déploiement en production

Voir `docker-compose.yml`, `Dockerfile` et `.github/workflows/ci-cd.yml` pour un
pipeline CI/CD prêt à l'emploi (build + tests + déploiement SSH sur VPS Ubuntu).
Pensez à :

- Changer `app.jwt.secret` pour une clé forte, stockée en variable d'environnement.
- Mettre `app.otp.debug-mode=false`.
- Configurer HTTPS via Nginx + Let's Encrypt devant l'application.
- Remplacer `SmsService` par un vrai provider SMS.

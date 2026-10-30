# TransportTN — Étape 1 : Analyse et conception (PlantUML)

Plateforme web de réservation de transport en Tunisie : taxi privé, taxi collectif (places) et bus interurbain (tickets QR).

## Choix techniques retenus (adaptation du cahier des charges)

| Besoin | Cahier des charges initial | Choix retenu |
|---|---|---|
| Authentification | Firebase OTP + JWT | **Spring Security + JWT** (téléphone +216 + mot de passe BCrypt) |
| Suivi temps réel | Firebase Firestore | **WebSocket STOMP** (Spring Boot) |
| Notifications | Firebase Cloud Messaging | **Firebase Cloud Messaging Web** (seul usage de Firebase) + notifications in-app en MySQL |
| Carte | Google Maps JS API | **Leaflet + OpenStreetMap** (gratuit, sans clé API) |
| Frontend | React + Vite, Tailwind, React Router | inchangé |
| Backend | Spring Boot 3.2, Java 17, Maven | inchangé |
| Base de données | MySQL 8 | inchangé (Spring Data JPA / Hibernate) |

## Contenu

| Fichier | Diagramme |
|---|---|
| `01_mvp.puml` | Périmètre MVP (Minimum Viable Product) par priorité |
| `02_architecture.puml` | Architecture en couches (Modèle / Vue / Contrôleur) |
| `03_cas_utilisation.puml` | Cas d'utilisation (Visiteur, Client, Chauffeur, Admin, FCM) |
| `04_classes.puml` | Diagramme de classes du modèle métier |
| `05_sequence_authentification.puml` | Séquence : connexion JWT + enregistrement jeton FCM |
| `06_sequence_reservation_taxi.puml` | Séquence : réservation taxi + notification + suivi temps réel |
| `07_sequence_taxi_collectif.puml` | Séquence : réservation de places en taxi collectif |
| `08_sequence_ticket_bus.puml` | Séquence : achat de tickets bus avec QR code |
| `09_gantt.puml` | Diagramme de Gantt (2 nov. → 25 déc. 2026) |

Les rendus PNG et SVG sont dans `images/`.

## Régénérer les images

```bash
# Prérequis : Java 17+ et Graphviz
java -jar plantuml.jar -tpng -o ../images plantuml/*.puml
```

Ou en ligne : https://www.plantuml.com/plantuml — ou l'extension « PlantUML » de VS Code (Alt+D pour l'aperçu).

## Étapes suivantes

1. ✅ Analyse et conception (ce livrable)
2. Base de données MySQL + projet Spring Boot (entités JPA, repositories)
3. Sécurité : Spring Security + JWT + rôles
4. Services métier et API REST (taxi, collectif, bus, QR code)
5. Frontend React + Vite + Tailwind + React Router
6. Temps réel (WebSocket) et notifications Firebase Cloud Messaging
7. Tests, Docker Compose, README final → **projet complet en zip**

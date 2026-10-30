# TransportTN — Plateforme web de réservation de transport (Tunisie)

Taxi privé · Taxi collectif (louage) · Bus interurbain

| Couche | Technologies |
|---|---|
| Frontend (étape 5) | React + Vite, Tailwind CSS, React Router, Leaflet |
| Backend | Spring Boot 3.2, Java 17, Maven, Spring Data JPA |
| Base de données | MySQL 8 (Docker) |
| Notifications (étape 6) | Firebase Cloud Messaging — seul usage de Firebase |

## Avancement

- [x] **Étape 1** — Conception UML (`docs/conception/`)
- [x] **Étape 2** — Projet Spring Boot, schéma MySQL, entités JPA, repositories, Docker Compose
- [ ] Étape 3 — Spring Security + JWT + rôles
- [ ] Étape 4 — Services métier et API REST (taxi, collectif, bus, QR code)
- [ ] Étape 5 — Frontend React
- [ ] Étape 6 — Suivi temps réel (WebSocket) + notifications Firebase
- [ ] Étape 7 — Tests, Docker complet, déploiement

## Arborescence

```
TransportTN/
├── docker-compose.yml          MySQL 8 + phpMyAdmin
├── .env.example
├── database/
│   ├── 01_schema.sql           tables, clés, index, contraintes CHECK
│   └── 02_donnees_demo.sql     comptes, chauffeurs, lignes, départs sur 7 jours
├── backend/
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd          Maven Wrapper (pas besoin d'installer Maven)
│   └── src/
│       ├── main/java/tn/transporttn/
│       │   ├── TransportTnApplication.java
│       │   ├── model/          entités JPA
│       │   │   └── enums/      Role, TypeLigne, StatutReservation, ...
│       │   ├── repository/     interfaces Spring Data JPA
│       │   └── controller/     HealthController (vérification)
│       ├── main/resources/application.yml
│       └── test/java/...       tests unitaires des règles métier
└── docs/conception/            diagrammes PlantUML de l'étape 1
```

## Prérequis

- **JDK 17** (ou plus récent) — vérifier avec `java -version`
- **Docker Desktop** — vérifier avec `docker --version`
- Un IDE : IntelliJ IDEA (recommandé) ou VS Code avec l'Extension Pack for Java

## Lancement

### 1. Démarrer MySQL

```bash
cd TransportTN
docker compose up -d
docker compose ps        # attendre que mysql soit "healthy" (~30 s)
```

Au premier démarrage, Docker exécute automatiquement `database/01_schema.sql`
puis `database/02_donnees_demo.sql`.

phpMyAdmin : http://localhost:8081 — utilisateur `transporttn`, mot de passe `transporttn_pwd`.

### 2. Démarrer le backend

```bash
cd backend
./mvnw spring-boot:run          # Linux / macOS
mvnw.cmd spring-boot:run        # Windows
```

Le premier lancement télécharge Maven et les dépendances (quelques minutes).

### 3. Vérifier

Ouvrir http://localhost:8080/api/health :

```json
{
  "statut": "UP",
  "baseDeDonnees": "MySQL connectée",
  "tables": {
    "utilisateurs": 6, "chauffeurs": 3, "lignes": 10, "departs": 315,
    "reservations": 1, "tickets": 0, "notifications": 2
  }
}
```

### 4. Lancer les tests unitaires

```bash
./mvnw test
```

## Comptes de démonstration

Mot de passe de tous les comptes : **`Test@1234`** (utilisable à partir de l'étape 3).

| Rôle | Nom | Téléphone |
|---|---|---|
| ADMIN | Amine Ben Salah | +21620000000 |
| CLIENT | Sarra Trabelsi | +21698111111 |
| CLIENT | Youssef Gharbi | +21655222222 |
| DRIVER | Mohamed Jebali (disponible, Tunis centre) | +21622333333 |
| DRIVER | Karim Hammami (disponible, Les Berges du Lac) | +21650444444 |
| DRIVER | Nizar Mansouri (indisponible, Ariana) | +21623555555 |

## Choix de conception de cette étape

- **Héritage JPA `JOINED`** : `reservations` contient les champs communs ;
  `reservations_taxi`, `reservations_collectif` et `reservations_bus` partagent la même clé.
  L'historique d'un client se lit donc en une seule requête polymorphe.
- **Table `departs`** : une ligne (Tunis → Sousse) a plusieurs départs datés ;
  ce sont eux qui portent les places restantes.
- **Verrouillage optimiste** (`@Version` sur `Depart`) : deux clients ne peuvent pas
  réserver la même dernière place.
- **Montants en `DECIMAL(10,3)` / `BigDecimal`** : le dinar tunisien a 3 décimales (millimes).
- **Contraintes `CHECK`** : la base refuse aussi les données invalides
  (téléphone non +216, places négatives, plus de 4 places en louage...).
- **Logique métier dans les entités** (`reserver`, `liberer`, `annuler`, `estimerPrix`),
  testée sans base de données.

## Problèmes fréquents

| Symptôme | Solution |
|---|---|
| `Communications link failure` | MySQL n'est pas prêt : `docker compose ps`, attendre « healthy ». |
| `Access denied for user` | Identifiants différents : vérifier `.env` et `application.yml`. |
| Données de démo absentes | Les scripts ne tournent qu'à la création du volume : `docker compose down -v` puis `up -d`. |
| Port 3307 ou 8081 occupé | Modifier la partie gauche de `ports` dans `docker-compose.yml`. |
| Getters introuvables dans IntelliJ | Installer le plugin Lombok et activer *Annotation Processing*. |
| Sans Docker (XAMPP / MySQL local) | Importer les 2 scripts dans phpMyAdmin, puis lancer avec `DB_URL=jdbc:mysql://localhost:3306/transporttn?serverTimezone=Africa/Tunis` et vos identifiants (`DB_USERNAME`, `DB_PASSWORD`). |

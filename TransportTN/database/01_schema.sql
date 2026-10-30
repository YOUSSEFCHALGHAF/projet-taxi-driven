-- =========================================================
-- TransportTN — Schéma MySQL 8
-- =========================================================
SET NAMES utf8mb4;
USE transporttn;

-- ---------------------------------------------------------
-- 1. Utilisateurs (clients, chauffeurs, administrateurs)
-- ---------------------------------------------------------
CREATE TABLE utilisateurs (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nom            VARCHAR(80)  NOT NULL,
    prenom         VARCHAR(80)  NOT NULL,
    telephone      VARCHAR(12)  NOT NULL,              -- format +216XXXXXXXX
    email          VARCHAR(120) NULL,
    mot_de_passe   VARCHAR(255) NOT NULL,              -- hash BCrypt
    role           VARCHAR(20)  NOT NULL,
    fcm_token      VARCHAR(512) NULL,                  -- jeton Firebase Cloud Messaging
    actif          BOOLEAN      NOT NULL DEFAULT TRUE,
    date_creation  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_utilisateurs_telephone UNIQUE (telephone),
    CONSTRAINT uk_utilisateurs_email     UNIQUE (email),
    CONSTRAINT ck_utilisateurs_role      CHECK (role IN ('CLIENT', 'DRIVER', 'ADMIN')),
    CONSTRAINT ck_utilisateurs_telephone CHECK (telephone REGEXP '^\\+216[2-9][0-9]{7}$')
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 2. Chauffeurs (profil lié 1-1 à un utilisateur DRIVER)
-- ---------------------------------------------------------
CREATE TABLE chauffeurs (
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    utilisateur_id        BIGINT      NOT NULL,
    numero_permis         VARCHAR(30) NOT NULL,
    matricule             VARCHAR(20) NOT NULL,        -- ex : 215 TU 4821
    modele_vehicule       VARCHAR(80) NOT NULL,
    disponible            BOOLEAN     NOT NULL DEFAULT FALSE,
    latitude              DOUBLE      NULL,
    longitude             DOUBLE      NULL,
    derniere_mise_a_jour  DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_chauffeurs_utilisateur UNIQUE (utilisateur_id),
    CONSTRAINT uk_chauffeurs_permis      UNIQUE (numero_permis),
    CONSTRAINT uk_chauffeurs_matricule   UNIQUE (matricule),
    CONSTRAINT fk_chauffeurs_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs (id) ON DELETE CASCADE,
    INDEX idx_chauffeurs_disponible (disponible)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 3. Lignes (bus interurbain et taxi collectif / louage)
-- ---------------------------------------------------------
CREATE TABLE lignes (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    type             VARCHAR(20)   NOT NULL,
    ville_depart     VARCHAR(60)   NOT NULL,
    ville_arrivee    VARCHAR(60)   NOT NULL,
    station_depart   VARCHAR(120)  NOT NULL,
    station_arrivee  VARCHAR(120)  NOT NULL,
    prix             DECIMAL(10,3) NOT NULL,           -- dinar tunisien : 3 décimales (millimes)
    capacite         INT           NOT NULL,
    duree_minutes    INT           NOT NULL,
    active           BOOLEAN       NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT ck_lignes_type     CHECK (type IN ('BUS', 'COLLECTIF')),
    CONSTRAINT ck_lignes_prix     CHECK (prix > 0),
    CONSTRAINT ck_lignes_capacite CHECK (capacite > 0),
    INDEX idx_lignes_recherche (type, active, ville_depart, ville_arrivee)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 4. Départs (instance datée d'une ligne)
-- ---------------------------------------------------------
CREATE TABLE departs (
    id                BIGINT   NOT NULL AUTO_INCREMENT,
    ligne_id          BIGINT   NOT NULL,
    date_heure        DATETIME NOT NULL,
    places_total      INT      NOT NULL,
    places_restantes  INT      NOT NULL,
    version           BIGINT   NOT NULL DEFAULT 0,     -- verrou optimiste JPA (@Version)
    PRIMARY KEY (id),
    CONSTRAINT fk_departs_ligne FOREIGN KEY (ligne_id)
        REFERENCES lignes (id) ON DELETE CASCADE,
    CONSTRAINT uk_departs_ligne_date UNIQUE (ligne_id, date_heure),
    CONSTRAINT ck_departs_places CHECK (places_restantes BETWEEN 0 AND places_total),
    INDEX idx_departs_date (date_heure)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 5. Réservations (table parente — héritage JOINED)
-- ---------------------------------------------------------
CREATE TABLE reservations (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    reference      VARCHAR(20)   NOT NULL,
    client_id      BIGINT        NOT NULL,
    montant        DECIMAL(10,3) NOT NULL,
    statut         VARCHAR(20)   NOT NULL,
    mode_paiement  VARCHAR(20)   NOT NULL,
    date_creation  DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_reservations_reference UNIQUE (reference),
    CONSTRAINT fk_reservations_client FOREIGN KEY (client_id)
        REFERENCES utilisateurs (id),
    CONSTRAINT ck_reservations_statut CHECK (statut IN
        ('EN_ATTENTE', 'ACCEPTEE', 'EN_COURS', 'TERMINEE', 'ANNULEE')),
    CONSTRAINT ck_reservations_paiement CHECK (mode_paiement IN ('ESPECES', 'CARTE')),
    CONSTRAINT ck_reservations_montant CHECK (montant >= 0),
    INDEX idx_reservations_client_date (client_id, date_creation)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 5a. Réservations taxi privé
CREATE TABLE reservations_taxi (
    id               BIGINT       NOT NULL,
    chauffeur_id     BIGINT       NULL,
    adresse_depart   VARCHAR(255) NOT NULL,
    lat_depart       DOUBLE       NOT NULL,
    lng_depart       DOUBLE       NOT NULL,
    adresse_arrivee  VARCHAR(255) NOT NULL,
    lat_arrivee      DOUBLE       NOT NULL,
    lng_arrivee      DOUBLE       NOT NULL,
    distance_km      DOUBLE       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_taxi_reservation FOREIGN KEY (id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_taxi_chauffeur FOREIGN KEY (chauffeur_id)
        REFERENCES chauffeurs (id) ON DELETE SET NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 5b. Réservations taxi collectif (places)
CREATE TABLE reservations_collectif (
    id             BIGINT NOT NULL,
    depart_id      BIGINT NOT NULL,
    nombre_places  INT    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_collectif_reservation FOREIGN KEY (id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_collectif_depart FOREIGN KEY (depart_id)
        REFERENCES departs (id),
    CONSTRAINT ck_collectif_places CHECK (nombre_places BETWEEN 1 AND 4)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 5c. Réservations bus (achat de tickets)
CREATE TABLE reservations_bus (
    id              BIGINT NOT NULL,
    depart_id       BIGINT NOT NULL,
    nombre_tickets  INT    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bus_reservation FOREIGN KEY (id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_bus_depart FOREIGN KEY (depart_id)
        REFERENCES departs (id),
    CONSTRAINT ck_bus_tickets CHECK (nombre_tickets BETWEEN 1 AND 10)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 6. Tickets de bus (QR code unique)
-- ---------------------------------------------------------
CREATE TABLE tickets (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    reservation_bus_id  BIGINT      NOT NULL,
    code_unique         CHAR(36)    NOT NULL,          -- UUID
    qr_code_base64      MEDIUMTEXT  NULL,
    statut              VARCHAR(20) NOT NULL,
    date_emission       DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tickets_code UNIQUE (code_unique),
    CONSTRAINT fk_tickets_reservation FOREIGN KEY (reservation_bus_id)
        REFERENCES reservations_bus (id) ON DELETE CASCADE,
    CONSTRAINT ck_tickets_statut CHECK (statut IN ('VALIDE', 'UTILISE', 'ANNULE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- 7. Notifications (historique in-app ; l'envoi push passe par Firebase)
-- ---------------------------------------------------------
CREATE TABLE notifications (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    utilisateur_id  BIGINT       NOT NULL,
    titre           VARCHAR(120) NOT NULL,
    message         VARCHAR(500) NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    lue             BOOLEAN      NOT NULL DEFAULT FALSE,
    date_envoi      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateurs (id) ON DELETE CASCADE,
    CONSTRAINT ck_notifications_type CHECK (type IN ('RESERVATION', 'COURSE', 'TICKET', 'SYSTEME')),
    INDEX idx_notifications_utilisateur (utilisateur_id, lue, date_envoi)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
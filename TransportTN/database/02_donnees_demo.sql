-- =========================================================
-- TransportTN — Données de démonstration
-- Mot de passe de tous les comptes : Password123
-- =========================================================
SET NAMES utf8mb4;
USE transporttn;

-- ---------- Utilisateurs ----------
INSERT INTO utilisateurs (nom, prenom, telephone, email, mot_de_passe, role, actif, date_creation) VALUES
('Admin',     'TransportTN', '+21620000000', 'admin@transporttn.tn',
 '$2a$10$whNikxbr9M.VWSkBvymZiu0z6Jnh6oapaq5mR/zET0MavloGvLwAG', 'ADMIN',  TRUE, NOW(6)),
('Ben Salah', 'Amira',       '+21622111222', 'amira.bensalah@example.tn',
 '$2a$10$whNikxbr9M.VWSkBvymZiu0z6Jnh6oapaq5mR/zET0MavloGvLwAG', 'CLIENT', TRUE, NOW(6)),
('Trabelsi',  'Youssef',     '+21698333444', NULL,
 '$2a$10$whNikxbr9M.VWSkBvymZiu0z6Jnh6oapaq5mR/zET0MavloGvLwAG', 'CLIENT', TRUE, NOW(6)),
('Jaziri',    'Mohamed',     '+21655444555', 'm.jaziri@example.tn',
 '$2a$10$whNikxbr9M.VWSkBvymZiu0z6Jnh6oapaq5mR/zET0MavloGvLwAG', 'DRIVER', TRUE, NOW(6)),
('Gharbi',    'Sami',        '+21650666777', NULL,
 '$2a$10$whNikxbr9M.VWSkBvymZiu0z6Jnh6oapaq5mR/zET0MavloGvLwAG', 'DRIVER', TRUE, NOW(6));

-- ---------- Chauffeurs (positions dans le Grand Tunis) ----------
INSERT INTO chauffeurs (utilisateur_id, numero_permis, matricule, modele_vehicule,
                        disponible, latitude, longitude, derniere_mise_a_jour)
SELECT id, 'TN-PERMIS-100245', '215 TU 4821', 'Peugeot 301 (jaune)', TRUE, 36.8190, 10.1820, NOW(6)
FROM utilisateurs WHERE telephone = '+21655444555';

INSERT INTO chauffeurs (utilisateur_id, numero_permis, matricule, modele_vehicule,
                        disponible, latitude, longitude, derniere_mise_a_jour)
SELECT id, 'TN-PERMIS-208871', '198 TU 1307', 'Dacia Logan (jaune)', TRUE, 36.8625, 10.1956, NOW(6)
FROM utilisateurs WHERE telephone = '+21650666777';

-- ---------- Lignes (tarifs indicatifs pour la démonstration) ----------
INSERT INTO lignes (type, ville_depart, ville_arrivee, station_depart, station_arrivee,
                    prix, capacite, duree_minutes, active) VALUES
('BUS',       'Tunis', 'Sousse',  'Gare routière Bab Alioua',  'Gare routière de Sousse',   11.500, 50, 150, TRUE),
('BUS',       'Tunis', 'Sfax',    'Gare routière Bab Alioua',  'Gare routière de Sfax',     21.000, 50, 270, TRUE),
('BUS',       'Tunis', 'Bizerte', 'Gare routière Bab Saadoun', 'Gare routière de Bizerte',   5.500, 50,  75, TRUE),
('BUS',       'Tunis', 'Kairouan','Gare routière Bab Alioua',  'Gare routière de Kairouan', 12.800, 50, 180, TRUE),
('COLLECTIF', 'Tunis', 'Nabeul',  'Station Moncef Bey',        'Station louage Nabeul',      6.500,  8,  70, TRUE),
('COLLECTIF', 'Tunis', 'Sousse',  'Station Moncef Bey',        'Station louage Sousse',     12.500,  8, 120, TRUE),
('COLLECTIF', 'Tunis', 'Bizerte', 'Station Bab Saadoun',       'Station louage Bizerte',     5.200,  8,  60, TRUE);

-- ---------- Départs : 7 jours à partir d'aujourd'hui, 4 horaires par jour ----------
INSERT INTO departs (ligne_id, date_heure, places_total, places_restantes, version)
SELECT l.id,
       TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL j.n DAY), h.heure),
       l.capacite,
       l.capacite,
       0
FROM lignes l
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
            UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) j
CROSS JOIN (SELECT '07:00:00' AS heure UNION ALL SELECT '10:30:00'
            UNION ALL SELECT '14:00:00' UNION ALL SELECT '18:00:00') h
WHERE l.active = TRUE;
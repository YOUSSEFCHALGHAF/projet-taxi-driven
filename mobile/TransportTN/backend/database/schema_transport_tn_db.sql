-- ============================================================
-- TransportTN - Schema MySQL 8.0
-- Note: ce script est fourni a titre de reference/documentation.
-- En pratique, Hibernate (spring.jpa.hibernate.ddl-auto=update) cree
-- et met a jour ces tables automatiquement au demarrage du backend.
-- ============================================================

CREATE DATABASE IF NOT EXISTS transport_tn CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE transport_tn;

-- --------------------------------------------------
-- Table: users
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',
    fcm_token VARCHAR(255),
    created_at DATETIME NOT NULL
);

-- --------------------------------------------------
-- Table: otp_codes
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS otp_codes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    code VARCHAR(6) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    INDEX idx_otp_phone (phone)
);

-- --------------------------------------------------
-- Table: drivers
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    vehicle_type VARCHAR(50),
    vehicle_plate VARCHAR(20),
    latitude DOUBLE,
    longitude DOUBLE,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at DATETIME,
    CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- --------------------------------------------------
-- Table: lignes (bus et taxi collectif)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS lignes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    departure VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    depart_time TIME NOT NULL,
    price DOUBLE NOT NULL,
    total_seats INT NOT NULL,
    mode VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- --------------------------------------------------
-- Table: reservations (taxi + collectif)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    driver_id BIGINT,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    pickup_address VARCHAR(255),
    pickup_lat DOUBLE,
    pickup_lng DOUBLE,
    dropoff_address VARCHAR(255),
    dropoff_lat DOUBLE,
    dropoff_lng DOUBLE,
    payment_mode VARCHAR(20),
    price DOUBLE,
    distance_km DOUBLE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_reservation_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_reservation_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

-- --------------------------------------------------
-- Table: reservation_seats (detail places - taxi collectif)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS reservation_seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    ligne_id BIGINT NOT NULL,
    seats_count INT NOT NULL,
    travel_date DATE NOT NULL,
    CONSTRAINT fk_seat_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_ligne FOREIGN KEY (ligne_id) REFERENCES lignes(id)
);

-- --------------------------------------------------
-- Table: tickets (bus)
-- --------------------------------------------------
CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    ligne_id BIGINT NOT NULL,
    travel_date DATE NOT NULL,
    quantity INT NOT NULL,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    price DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_ticket_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_ligne FOREIGN KEY (ligne_id) REFERENCES lignes(id)
);

-- ============================================================
-- Donnees de test
-- ============================================================

INSERT INTO users (phone, name, role, created_at) VALUES
('+21620123456', 'Ahmed Ben Ali', 'CLIENT', NOW()),
('+21622334455', 'Mohamed Chauffeur', 'DRIVER', NOW()),
('+21698765432', 'Admin TransportTN', 'ADMIN', NOW());

INSERT INTO drivers (user_id, vehicle_type, vehicle_plate, latitude, longitude, available, updated_at)
SELECT id, 'Berline', 'TUNIS 1234', 36.8065, 10.1815, TRUE, NOW() FROM users WHERE phone = '+21622334455';

INSERT INTO lignes (name, departure, destination, depart_time, price, total_seats, mode, active) VALUES
('Tunis - Sousse', 'Tunis', 'Sousse', '08:00:00', 15.0, 45, 'BUS', TRUE),
('Tunis - Sfax', 'Tunis', 'Sfax', '09:30:00', 25.0, 45, 'BUS', TRUE),
('Tunis - Bizerte', 'Tunis', 'Bizerte', '07:15:00', 8.0, 8, 'COLLECTIF', TRUE),
('Ariana - Tunis Centre', 'Ariana', 'Tunis Centre', '08:00:00', 3.0, 8, 'COLLECTIF', TRUE);

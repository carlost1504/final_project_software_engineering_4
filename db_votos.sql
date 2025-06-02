-- 1. Crear el usuario
CREATE USER votante_user WITH PASSWORD 'votante_pass';

-- 2. Crear la base de datos y asignar propietario
CREATE DATABASE voting_system OWNER votante_user;

-- IMPORTANTE: Cambiar a la nueva base para el resto de comandos
\c voting_system

-- 3. Crear las tablas bajo el nuevo usuario

-- Tabla de estaciones de votación
CREATE TABLE stations (
    station_id SERIAL PRIMARY KEY,
    location TEXT NOT NULL,
    ip_address TEXT UNIQUE
);

-- Tabla de ciudadanos/votantes
CREATE TABLE voters (
    document VARCHAR(20) PRIMARY KEY,
    full_name TEXT NOT NULL,
    fingerprint_hash TEXT,
    assigned_station_id INTEGER REFERENCES stations(station_id),
    assigned_table_number INTEGER,
    is_enabled BOOLEAN DEFAULT TRUE,
    has_voted BOOLEAN DEFAULT FALSE
);

-- Tabla de candidatos
CREATE TABLE candidates (
    candidate_id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    party TEXT
);

-- Tabla de votos
CREATE TABLE votes (
    vote_id SERIAL PRIMARY KEY,
    document VARCHAR(20) NOT NULL REFERENCES voters(document),
    candidate_id INTEGER NOT NULL REFERENCES candidates(candidate_id),
    station_id INTEGER NOT NULL REFERENCES stations(station_id),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(document)
);

-- Tabla de eventos de seguridad y fraudes
CREATE TABLE security_events (
    event_id SERIAL PRIMARY KEY,
    document VARCHAR(20),
    event_type TEXT,
    description TEXT,
    station_id INTEGER REFERENCES stations(station_id),
    timestamp TIMESTAMP DEFAULT NOW()
);

-- 4. Otorgar permisos completos al usuario (por si ejecutaste esto como otro usuario)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO votante_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO votante_user;

-- Limpiar datos anteriores (en orden para evitar conflictos)
DELETE FROM votes;
DELETE FROM security_events;
DELETE FROM voters;
DELETE FROM candidates;
DELETE FROM stations;

-- 1. Insertar estaciones
INSERT INTO stations (station_id, location, ip_address) VALUES
(1, 'Cali - Colegio A', '192.168.1.10'),
(2, 'Bogotá - Universidad B', '192.168.1.11');

-- 2. Insertar candidatos
INSERT INTO candidates (candidate_id, name, party) VALUES
(101, 'Candidato A', 'Partido Azul'),
(102, 'Candidato B', 'Partido Verde');

-- 3. Insertar votantes
INSERT INTO voters (document, full_name, fingerprint_hash, assigned_station_id, assigned_table_number, is_enabled, has_voted) VALUES
-- ✅ Casos válidos
('112233', 'Juan Pérez', 'hash1', 1, 10, TRUE, FALSE),
('445566', 'Ana Gómez', 'hash2', 1, 11, TRUE, FALSE),
('778899', 'Luis Ramírez', 'hash3', 1, 12, TRUE, FALSE),
-- ❌ No habilitado
('000111', 'Carlos SinPermiso', 'hash4', 1, 13, FALSE, FALSE),
-- ❌ Ya votó
('222333', 'María VotóYa', 'hash5', 1, 14, TRUE, TRUE),
-- ❌ Mesa incorrecta
('999000', 'Laura OtrasMesa', 'hash6', 2, 15, TRUE, FALSE);
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
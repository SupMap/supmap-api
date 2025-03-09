-- docker run --name supmap-database -e POSTGRES_USER=supmap -e POSTGRES_PASSWORD=supmap -e POSTGRES_DB=supmap-database -p 5432:5432 -d postgres

-- Activer l'extension PostGIS (à exécuter dans la bdd)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Table des utilisateurs
CREATE TABLE Users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       name VARCHAR(255),
                       second_name VARCHAR(255),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       oauth2_id INT
);

-- Table des catégories d'incidents (ex: accident, embouteillage, police, etc.)
CREATE TABLE Incident_categories (
                                     category_id SERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE
);

-- Table des types d'incidents spécifiques (ex: carambolage, sens inversé, etc.)
CREATE TABLE incidents (
                           incident_id SERIAL PRIMARY KEY,
                           type_id INT NOT NULL REFERENCES incident_types(type_id),
                           latitude DOUBLE PRECISION NOT NULL,
                           longitude DOUBLE PRECISION NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           confirmed_by_user_id INT REFERENCES users(user_id)
);


-- Table des incidents
-- Utilise un type géographique pour la localisation (Point en SRID 4326)
CREATE TABLE incidents (
                           incident_id SERIAL PRIMARY KEY,
                           type VARCHAR(50) NOT NULL,
                           latitude DOUBLE PRECISION NOT NULL,
                           longitude DOUBLE PRECISION NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           confirmed_by_user_id INT REFERENCES users(user_id)
);


-- Table des itinéraires (Routes)
-- On stocke le point de départ, d'arrivée et la géométrie complète de l'itinéraire (LINESTRING)
CREATE TABLE Routes (
                        route_id SERIAL PRIMARY KEY,
                        user_id INT REFERENCES Users(user_id),
                        start_location GEOGRAPHY(Point,4326) NOT NULL,
                        end_location GEOGRAPHY(Point,4326) NOT NULL,
                        route_geometry GEOGRAPHY(LINESTRING,4326) NOT NULL,
                        total_distance DOUBLE PRECISION, -- en mètres
                        total_duration DOUBLE PRECISION, -- en secondes
                        calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table pour stocker les informations de trafic en temps réel (optionnel)
CREATE TABLE Traffic_info (
                              traffic_id SERIAL PRIMARY KEY,
                              location GEOGRAPHY(Point,4326) NOT NULL,
                              congestion_level INT, -- par exemple, de 1 (faible) à 5 (très élevé)
                              recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
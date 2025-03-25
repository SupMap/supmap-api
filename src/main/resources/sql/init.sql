-- docker run --name supmap-database -e POSTGRES_USER=supmap -e POSTGRES_PASSWORD=supmap -e POSTGRES_DB=supmap-database -p 5432:5432 -d postgis/postgis
-- Activation de l'extension PostGIS (si elle n'est pas déjà installée)
CREATE EXTENSION IF NOT EXISTS postgis;

-------------------------------------------
-- Table des catégories d'incidents
-------------------------------------------
CREATE TABLE incident_categories (
                                     category_id SERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE
);

-------------------------------------------
-- Table des types d'incidents (avec poids)
-------------------------------------------
CREATE TABLE incident_types (
                                type_id SERIAL PRIMARY KEY,
                                category_id INT NOT NULL REFERENCES incident_categories(category_id),
                                name VARCHAR(50) NOT NULL UNIQUE,
                                weight DOUBLE PRECISION NOT NULL DEFAULT 1.0  -- Poids par défaut, pour évaluer l'impact de ce type d'incident
);

-------------------------------------------
-- Table des utilisateurs
-------------------------------------------
CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       name VARCHAR(255),
                       second_name VARCHAR(255),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       oauth2_id INT
);

-------------------------------------------
-- Table des incidents
-------------------------------------------
CREATE TABLE incidents (
                           incident_id SERIAL PRIMARY KEY,
                           type_id INT NOT NULL REFERENCES incident_types(type_id),
                           location GEOGRAPHY(Point,4326) NOT NULL,  -- Stocke la position sous forme de point (longitude, latitude)
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           confirmed_by_user_id INT REFERENCES users(user_id)
);

-------------------------------------------
-- Table des routes (itinéraires)
-------------------------------------------
CREATE TABLE routes (
                        route_id SERIAL PRIMARY KEY,
                        user_id INT REFERENCES users(user_id),
                        start_location GEOGRAPHY(Point,4326) NOT NULL,  -- Point de départ
                        end_location GEOGRAPHY(Point,4326) NOT NULL,      -- Point d'arrivée
                        route_geometry GEOGRAPHY(LineString,4326) NOT NULL,  -- Itinéraire sous forme de ligne (polyline)
                        total_distance DOUBLE PRECISION,
                        total_duration DOUBLE PRECISION,
                        calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- docker run --name supmap-database -e POSTGRES_USER=supmap -e POSTGRES_PASSWORD=supmap -e POSTGRES_DB=supmap-database -p 5432:5432 -d postgres

-- Création de la table Users
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

-- Table des types d'incidents (embouteillage, police, accident, etc.)
CREATE TABLE Incident_categories (
                                     category_id SERIAL PRIMARY KEY,
                                     name VARCHAR(50) NOT NULL UNIQUE -- Exemple : accident, embouteillage, police, etc.
);

-- Table des sous-types d'incidents spécifiques (pour les accidents, par exemple)
CREATE TABLE Incident_types (
                                type_id SERIAL PRIMARY KEY,
                                category_id INT NOT NULL REFERENCES Incident_categories(category_id),
                                name VARCHAR(50) NOT NULL UNIQUE -- Exemple : carambolage, sens inversé, etc.
);

-- Création de la table incidents
CREATE TABLE Incidents (
                           incident_id SERIAL PRIMARY KEY,
                           type_id INT REFERENCES Incident_types(type_id),
                           latitude DECIMAL(10, 7) NOT NULL,
                           longitude DECIMAL(10, 7) NOT NULL,
                           timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           confirmed_by_user_id INT REFERENCES users(user_id)
);
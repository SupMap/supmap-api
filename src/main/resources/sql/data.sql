------------------------------------------------
-- Insertion des données
------------------------------------------------

-- Insertion des rôles
INSERT INTO roles (name) VALUES ('Utilisateur');
INSERT INTO roles (name) VALUES ('Modérateur');
INSERT INTO roles (name) VALUES ('Administrateur');

-- Insertion des catégories d'incidents
INSERT INTO incident_categories (name) VALUES ('Accident');
INSERT INTO incident_categories (name) VALUES ('Embouteillage');
INSERT INTO incident_categories (name) VALUES ('Route fermée');
INSERT INTO incident_categories (name) VALUES ('Contrôle policier');
INSERT INTO incident_categories (name) VALUES ('Obstacle sur la route');

-- Insertion des types d'incidents pour la catégorie "Accident"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Collision entre véhicules', 0.75),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident multiple', 0.5),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident avec blessés', 0.35);

-- Insertion des types d'incidents pour la catégorie "Embouteillage"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Embouteillage majeur', 0.5),
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Circulation ralentie', 0.75);

-- Insertion des types d'incidents pour la catégorie "Route fermée"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Route bloquée', 0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Travaux en cours', 0.5);

-- Insertion des types d'incidents pour la catégorie "Contrôle policier"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Radar fixe', 1.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Contrôle mobile', 1.0);

-- Insertion des types d'incidents pour la catégorie "Obstacle sur la route"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Débris sur la route', 0.95),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Animal sur la chaussée', 1.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Objet sur la route', 0.95);




INSERT INTO incidents (type_id, location)
VALUES (
           6,
           ST_SetSRID(ST_MakePoint(0.7038658958513232, 47.41738403159333), 4326)
       );

------------------------------------------------
-- Insertion des données
------------------------------------------------

-- Insertion des catégories d'incidents
INSERT INTO incident_categories (name) VALUES ('Accident');
INSERT INTO incident_categories (name) VALUES ('Embouteillage');
INSERT INTO incident_categories (name) VALUES ('Route fermée');
INSERT INTO incident_categories (name) VALUES ('Contrôle policier');
INSERT INTO incident_categories (name) VALUES ('Obstacle sur la route');

-- Insertion des types d'incidents pour la catégorie "Accident"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Collision entre véhicules', 2.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident multiple', 3.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident avec blessés', 4.0);

-- Insertion des types d'incidents pour la catégorie "Embouteillage"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Embouteillage majeur', 1.5),
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Circulation ralentie', 1.0);

-- Insertion des types d'incidents pour la catégorie "Route fermée"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Route bloquée', 2.5),
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Travaux en cours', 2.0);

-- Insertion des types d'incidents pour la catégorie "Contrôle policier"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Radar fixe', 1.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Contrôle mobile', 1.0);

-- Insertion des types d'incidents pour la catégorie "Obstacle sur la route"
INSERT INTO incident_types (category_id, name, weight)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Débris sur la route', 1.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Animal sur la chaussée', 1.0),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Objet sur la route', 1.0);
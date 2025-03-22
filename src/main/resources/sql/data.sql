-- Insérer les catégories d'incidents
INSERT INTO incident_categories (name) VALUES ('Accident');
INSERT INTO incident_categories (name) VALUES ('Embouteillage');
INSERT INTO incident_categories (name) VALUES ('Route fermée');
INSERT INTO incident_categories (name) VALUES ('Contrôle policier');
INSERT INTO incident_categories (name) VALUES ('Obstacle sur la route');

-- Insérer les types d'incidents pour la catégorie "Accident"
INSERT INTO incident_types (category_id, name)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Collision entre véhicules'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident multiple'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Accident'), 'Accident avec blessés');

-- Insérer les types d'incidents pour la catégorie "Embouteillage"
INSERT INTO incident_types (category_id, name)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Embouteillage majeur'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Embouteillage'), 'Circulation ralentie');

-- Insérer les types d'incidents pour la catégorie "Route fermée"
INSERT INTO incident_types (category_id, name)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Route bloquée'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Route fermée'), 'Travaux en cours');

-- Insérer les types d'incidents pour la catégorie "Contrôle policier"
INSERT INTO incident_types (category_id, name)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Radar fixe'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Contrôle policier'), 'Contrôle mobile');

-- Insérer les types d'incidents pour la catégorie "Obstacle sur la route"
INSERT INTO incident_types (category_id, name)
VALUES
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Débris sur la route'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Animal sur la chaussée'),
    ((SELECT category_id FROM incident_categories WHERE name = 'Obstacle sur la route'), 'Objet sur la route');

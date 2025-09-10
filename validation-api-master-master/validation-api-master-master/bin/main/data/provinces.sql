INSERT INTO country (id, name, phone_code)
VALUES (1, 'Argentina', 54)
ON CONFLICT DO NOTHING;

INSERT INTO province (id, name, country_id)
VALUES (1, 'Buenos Aires', 1),
       (2, 'Catamarca', 1),
       (3, 'Chaco', 1),
       (4, 'Chubut', 1),
       (5, 'Cordoba', 1),
       (6, 'Corrientes', 1),
       (7, 'Entre Rios', 1),
       (8, 'Formosa', 1),
       (9, 'Jujuy', 1),
       (10, 'La Pampa', 1),
       (11, 'La Rioja', 1),
       (12, 'Mendoza', 1),
       (13, 'Misiones', 1),
       (14, 'Neuquen', 1),
       (15, 'Rio Negro', 1),
       (16, 'Salta', 1),
       (17, 'San Juan', 1),
       (18, 'San Luis', 1),
       (19, 'Santa Cruz', 1),
       (20, 'Santa Fe', 1),
       (21, 'Santiago del Estero', 1),
       (22, 'Tierra del Fuego', 1),
       (23, 'Tucuman', 1)
ON CONFLICT DO NOTHING;
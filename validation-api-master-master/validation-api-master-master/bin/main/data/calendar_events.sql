INSERT INTO calendar_event(id, title, day, month)
VALUES  (1, 'Feriado o Día No Laborable', null, null),
        (2,'Aniv. Revolución de Mayo', 25, 5),
        (3,'Día de la Independencia', 9, 7),
        (4,'Día de la Memoria por la Verdad y Justicia', 24, 3),
        (5,'Día de la Bandera', 20, 6),
        (6,'Día del Veterano y Caídos en la guerra de Malvinas', 2, 4),
        (7,'Día del Trabajador', 1, 5),
        (10,'Aniv. del paso a la inmortalidad de Gral. Martín Miguel de Güemes', 17, 6),
        (12,'Día del paso a la inmortalidad del Gral. José de San Martín', 17, 8),
        (13,'Día del Respeto a la Diversidad Cultural', 12, 10),
        (14,'Día de la Soberanía Nacional', 20, 11),
        (15,'Día de la Inmaculada Virgen María', 8, 12),
        (16,'Navidad', 25, 12),
        (17,'Año nuevo', 1, 1)
ON CONFLICT DO NOTHING;
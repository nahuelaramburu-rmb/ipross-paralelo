insert into id_type (id, name, alias)
values (1, 'Documento Nacional de Identidad', 'DNI'),
       (2, 'Libreta de Enrolamiento', 'LE'),
       (3, 'Libreta Civica', 'LC'),
       (5, 'Pasaporte', 'PAS'),
       (6, 'Documento Temporal', 'TEMP')
on conflict do nothing;
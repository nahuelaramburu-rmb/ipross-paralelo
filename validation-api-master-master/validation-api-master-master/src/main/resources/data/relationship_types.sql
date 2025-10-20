insert into relationship_type (id, name)
values (1, 'Titular'),
       (2, 'Esposo/a'),
       (3, 'Concubino/a'),
       (4, 'Hijo/a'),
       (5, 'Hijastro/a'),
       (6, 'Mayor a Cargo'),
       (7, 'Menor a Cargo'),
       (8, 'Hijo/a Recien Nacido'),
       (9, 'Parentesco Indefinido')
on conflict do nothing;
insert into marital_status (id, name)
values (1, 'Casado/a'),
       (2, 'Soltero/a'),
       (3, 'Viudo/a'),
       (4, 'Divorciado/a'),
       (5, 'Concubinato')
on conflict do nothing;
alter table if exists medicine add column recommendation varchar(255);
update medicine set recommendation = 'Sin recomendacion' where recommendation is null;
alter table medicine alter column recommendation set not null;
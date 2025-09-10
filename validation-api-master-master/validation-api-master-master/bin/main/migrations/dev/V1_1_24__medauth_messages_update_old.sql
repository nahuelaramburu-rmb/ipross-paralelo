alter table if exists medical_authorization add column messages jsonb;
update medical_authorization set messages = '[]' where messages is null;
alter table medical_authorization alter column diagnosis type varchar(1500);
create sequence holiday_seq start 1 increment 1;
create table calendar_event (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), day int4, month int4, title varchar(255) not null, primary key (id));
create table holiday (id int8 not null, client_id varchar(255), created_at timestamp, created_by varchar(255), deleted boolean DEFAULT false not null, deletion_token uuid DEFAULT uuid_nil() not null, modified_at timestamp, modified_by varchar(255), tenant_id uuid not null, date date not null, title varchar(255) not null, calendar_event_id int8 not null, primary key (id));
alter table if exists calendar_event drop constraint if exists UK_lp62uh9kh3v5rm9drohcvafbc;
alter table if exists calendar_event add constraint UK_lp62uh9kh3v5rm9drohcvafbc unique (title);
alter table if exists holiday drop constraint if exists UKkeu2si1s5pbr24ud3tepplkbt;
alter table if exists holiday add constraint UKkeu2si1s5pbr24ud3tepplkbt unique (date, deleted, deletion_token, tenant_id);
alter table if exists holiday add constraint FKldw277lfby2xh871l8mq42oro foreign key (calendar_event_id) references calendar_event;

insert into calendar_event(id, title, day, month)
values  (1, 'Feriado o Día No Laborable', null, null),
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
        (17,'Año nuevo', 1, 1);
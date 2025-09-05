insert into status_scope (id, name) values (9, 'HOMOCLAVE');
insert into status (id, name, status_scope_id) values (29, 'CONSUMIDA', 9), (30, 'VENCIDA', 9), (31, 'ANULADA', 9), (32, 'ACTIVA', 9);
update authorization_type set name = 'HOMOCLAVE' where id = 4;
update medical_authorization set authorization_type_id = 4 where pre_medical_authorization_id is not null;
alter table pre_medical_authorization add column status_id int8;
alter table pre_medical_authorization add constraint fkiv0fk0blfu05wuhwo2no6i23t foreign key (status_id) references status;
update pre_medical_authorization set status_id = 32;
update pre_medical_authorization set status_id = 29 where consumed = true;
update pre_medical_authorization set status_id = 30 where (expiration_date at time zone '3') < date(now() at time zone '-3');
alter table pre_medical_authorization alter column status_id set not null;
alter table pre_medical_authorization_item add column remaining int4;
update pre_medical_authorization_item set remaining = quantity;
update pre_medical_authorization_item set remaining = 0 where pre_medical_authorization_id in (select id from pre_medical_authorization where status_id = 29);
alter table pre_medical_authorization_item alter column remaining set not null;
alter table if exists pre_medical_authorization_item add column auditing boolean DEFAULT false not null;
alter table pre_medical_authorization drop consumed;
insert into scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
values (8, '0 25 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PreMedicalAuthorizationStatusSync',
        'CronJobs', 'PreMedicalAuthorizationStatusSync', true, NULL)
on conflict do nothing;
create table pre_medical_authorization_audit_log (id int8 not null, rev int4 not null, revtype int2, client_id varchar(255), created_at timestamp, created_by varchar(255), modified_at timestamp, modified_by varchar(255), status_id int8, status_mod boolean, primary key (id, rev));
create table pre_medical_authorization_item_audit_log (id int8 not null, rev int4 not null, revtype int2, client_id varchar(255), created_at timestamp, created_by varchar(255), modified_at timestamp, modified_by varchar(255), auditing boolean, auditing_mod boolean, remaining int4, remaining_mod boolean, primary key (id, rev));
alter table if exists pre_medical_authorization_audit_log add constraint FK57m41nubf19otcssx5l4j4x53 foreign key (rev) references revinfo;
alter table if exists pre_medical_authorization_item_audit_log add constraint FK8phtd83use1b3dcbxf0kcjr05 foreign key (rev) references revinfo;
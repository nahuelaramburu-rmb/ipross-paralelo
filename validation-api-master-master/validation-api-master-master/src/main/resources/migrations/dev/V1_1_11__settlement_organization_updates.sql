delete from settlement_item;
delete from settlement;

update medical_authorization_item set settled = false where settled = true;
update medical_authorization_item set settled = false where settled = true;

alter table settlement_item drop column contract_id;
alter table settlement add column contract_id int8;
alter table settlement add constraint fkmkmyx63h0qqvd0a9ikhsyr3g foreign key (contract_id) references contract;
alter table settlement alter column contract_id set not null;
alter table if exists organization add column related_organization_id int8;
alter table if exists organization add constraint FKg37mcanvovthwhe0h1dpapnkr foreign key (related_organization_id) references organization;

update scheduler_job_info set cron_expression = '0 10 0 ? * * *' where id = 3;
update scheduler_job_info set cron_expression = '0 5 0 ? * * *' where id = 2;

insert into scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
values (4, '0 0 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PractitionerKeyCronJob',
        'CronJobs', 'PractitionerKeyCronJob', true, null);
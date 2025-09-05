alter table if exists contract add column active boolean DEFAULT true;
alter table if exists contract add column day_of_settlement integer DEFAULT 31;
alter table if exists contract_audit_log add column active boolean;
alter table if exists contract_audit_log add column active_mod boolean;
alter table if exists contract_audit_log add column day_of_settlement int4;
alter table if exists contract_audit_log add column day_of_settlement_mod boolean;
alter table if exists contract_adjustment add column scope varchar(255);
update contract_adjustment set scope = 'CONTRACT' where scope is null;
alter table if exists contract_adjustment alter column scope set not null;
update authorization_condition set name = 'LIMITE MONETARIO EXCEDIDO' where id = 4;
insert into authorization_condition (id, name) values (5, 'EXCESO EN CONVENIO');
alter table if exists medical_authorization_item add column authorization_condition_id int8;
alter table if exists medical_authorization_item add constraint FK9gy63bj724nrvqgrouyjw76xt foreign key (authorization_condition_id) references authorization_condition;
INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (9, '0 50 23 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.CloseSettlementCronJob',
        'CronJobs', 'CloseSettlementCronJob', true, NULL)
ON CONFLICT DO NOTHING;
alter table medical_authorization_item drop comments;
alter table if exists contract add column auto_settlement boolean DEFAULT false;
alter table if exists contract_audit_log add column auto_settlement boolean;
alter table if exists contract_audit_log add column auto_settlement_mod boolean;
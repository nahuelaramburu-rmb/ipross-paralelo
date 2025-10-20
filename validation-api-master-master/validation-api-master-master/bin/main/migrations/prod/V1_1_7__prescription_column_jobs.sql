alter table if exists prescription add column dtype varchar(255);

alter table if exists prescription_audit_log add column status_id int8;
alter table if exists prescription_audit_log add column status_mod boolean;

update prescription set dtype = 'galbopPrescriptionServiceImpl';

insert into scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
values (5, '0 0 0/1 1/1 * ? *', 'com.capacidad.validationapi.module.scheduler.job.PrescriptionUtilizedStatusSync',
        'CronJobs', 'PrescriptionUtilizedStatusSync', true, NULL);

insert into scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
values (6, '0 15 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PrescriptionExpiredStatusSync',
        'CronJobs', 'PrescriptionExpiredStatusSync', true, NULL);
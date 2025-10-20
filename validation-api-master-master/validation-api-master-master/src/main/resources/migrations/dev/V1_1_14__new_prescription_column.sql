delete from scheduler_job_info where id = 6;
update scheduler_job_info set job_class = 'com.capacidad.validationapi.module.scheduler.job.PrescriptionStatusSync',
                              job_name = 'PrescriptionStatusSync' where id = 5;
alter table if exists prescription add column expiration_date date;
update prescription set expiration_date = created_at + interval '1 month';
alter table prescription alter column expiration_date set not null;
alter table prescription alter column expiration_period set not null;
alter table if exists prescription_exchange_id add constraint UK_bbydjyw6fljq3r91b3olyfbqx unique (exchange_id);
alter table if exists prescription add column preauthorized boolean DEFAULT false not null;
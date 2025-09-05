drop table if exists batch_procedures;
alter table if exists batch add column description varchar(1000);
alter table if exists batch_audit_log add column description varchar(1000);
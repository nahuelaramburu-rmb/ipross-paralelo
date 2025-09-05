create table file_download_key
(
    id                        int8                       not null,
    client_id                 varchar(255),
    created_at                timestamp,
    created_by                varchar(255),
    deleted                   boolean DEFAULT false      not null,
    deletion_token            uuid    DEFAULT uuid_nil() not null,
    modified_at               timestamp,
    modified_by               varchar(255),
    tenant_id                 uuid                       not null,
    download_key              varchar(255)               not null,
    origin                    varchar(255)               not null,
    serialized_authentication bytea                      not null,
    primary key (id)
);
alter table if exists file_download_key
    drop constraint if exists UKgucax5x6ynngd58hjeivqnced;
alter table if exists file_download_key
    add constraint UKgucax5x6ynngd58hjeivqnced unique (download_key, origin, deleted, deletion_token, tenant_id);
create sequence file_download_key_seq start 1 increment 50;

insert into scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
values (7, '0 20 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.FileDownloadKeyDeleteCronJob',
        'CronJobs', 'FileDownloadKeyDeleteCronJob', true, NULL);
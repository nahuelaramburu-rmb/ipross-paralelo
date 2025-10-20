-- CronJobs

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (1, '0 0/10 * 1/1 * ? *', 'com.capacidad.validationapi.module.scheduler.job.UnassignedAuditHistoryCronJob',
        'CronJobs', 'UnassignedAuditHistoryCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (2, '0 5 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.BatchStatusCronJob',
        'CronJobs', 'BatchStatusCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (3, '0 10 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.ProcedureStatusCronJob',
        'CronJobs', 'ProcedureStatusCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (4, '0 0 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PractitionerKeyCronJob',
        'CronJobs', 'PractitionerKeyCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (5, '0 0 1 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PrescriptionStatusSync',
        'CronJobs', 'PrescriptionStatusSync', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (6, '0 15 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.BeneficiaryInsurancePlanCronJob',
        'CronJobs', 'BeneficiaryInsurancePlanCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (7, '0 20 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.FileDownloadKeyDeleteCronJob',
        'CronJobs', 'FileDownloadKeyDeleteCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (8, '0 25 0 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.PreMedicalAuthorizationStatusCronJob',
        'CronJobs', 'PreMedicalAuthorizationStatusCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (9, '0 50 23 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.CloseSettlementCronJob',
        'CronJobs', 'CloseSettlementCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (10, '0 00 03 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.TopicSynchronizationCronJob',
        'CronJobs', 'TopicSynchronizationCronJob', true, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (11, '0 55 23 L * ? *', 'com.capacidad.validationapi.module.scheduler.job.BeneficiaryBudgetClosingCronJob',
        'CronJobs', 'BeneficiaryBudgetClosingCronJob', true, NULL)
ON CONFLICT DO NOTHING;
-- Example of SimpleJob
-- INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
-- VALUES (1, NULL, 'com.capacidad.validationapi.module.scheduler.job.SimpleJob', 'Test_Job', 'Simple Job', false, '600000')
-- ON CONFLICT DO NOTHING;
-- commit ;
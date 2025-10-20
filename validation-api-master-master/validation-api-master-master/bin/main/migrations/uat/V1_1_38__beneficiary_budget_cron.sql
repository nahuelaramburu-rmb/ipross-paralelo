INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (11, '0 55 23 L * ? *', 'com.capacidad.validationapi.module.scheduler.job.BeneficiaryBudgetClosingCronJob',
        'CronJobs', 'BeneficiaryBudgetClosingCronJob', true, NULL)
ON CONFLICT DO NOTHING;
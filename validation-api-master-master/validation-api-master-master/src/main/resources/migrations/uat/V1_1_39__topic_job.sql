INSERT INTO scheduler_job_info (id, cron_expression, job_class, job_group, job_name, cron_job, repeat_time)
VALUES (10, '0 00 03 ? * * *', 'com.capacidad.validationapi.module.scheduler.job.TopicSynchronizationCronJob',
        'CronJobs', 'TopicSynchronizationCronJob', true, NULL)
ON CONFLICT DO NOTHING;
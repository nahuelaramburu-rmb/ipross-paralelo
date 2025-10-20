package com.capacidad.validationapi.module.scheduler.job;

import com.capacidad.validationapi.module.procedure.service.ProcedureService;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@DisallowConcurrentExecution
@Component
public class ProcedureStatusCronJob implements Job {

    private final ProcedureService procedureService;

    @Autowired
    public ProcedureStatusCronJob(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        procedureService.resolveProcedureStatus();
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }
}

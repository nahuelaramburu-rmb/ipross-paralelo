package com.capacidad.validationapi.module.scheduler.job;

import com.capacidad.validationapi.module.prescription.service.PrescriptionService;
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
public class PrescriptionStatusSync implements Job {

    private final PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionStatusSync(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        prescriptionService.syncStatus();
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }

}

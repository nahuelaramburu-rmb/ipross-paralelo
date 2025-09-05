package com.capacidad.validationapi.module.scheduler.job;

import com.capacidad.validationapi.module.settlement.service.SettlementService;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@Log4j2
@DisallowConcurrentExecution
@Component
public class CloseSettlementCronJob implements Job {

    private final SettlementService settlementService;

    @Autowired
    public CloseSettlementCronJob(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        settlementService.closeAllIfContractDayMatchesDate(LocalDate.now());
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }
}

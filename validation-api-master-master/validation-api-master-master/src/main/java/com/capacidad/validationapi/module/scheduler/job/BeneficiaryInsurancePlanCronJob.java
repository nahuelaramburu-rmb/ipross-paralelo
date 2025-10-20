package com.capacidad.validationapi.module.scheduler.job;

import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryInsurancePlanService;
import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@DisallowConcurrentExecution
@Component
public class BeneficiaryInsurancePlanCronJob implements Job {

    private final BeneficiaryInsurancePlanService beneficiaryInsurancePlanService;

    public BeneficiaryInsurancePlanCronJob(BeneficiaryInsurancePlanService beneficiaryInsurancePlanService) {
        this.beneficiaryInsurancePlanService = beneficiaryInsurancePlanService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        beneficiaryInsurancePlanService.removeExpired();
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }
}

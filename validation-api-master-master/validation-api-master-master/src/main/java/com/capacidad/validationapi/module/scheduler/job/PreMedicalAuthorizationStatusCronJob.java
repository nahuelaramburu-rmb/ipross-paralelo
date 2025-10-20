package com.capacidad.validationapi.module.scheduler.job;

import lombok.extern.log4j.Log4j2;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static com.capacidad.validationapi.module.general.reference.StatusReference.PRE_MEDICAL_AUTHORIZATION_EXPIRED;

@Log4j2
@DisallowConcurrentExecution
@Component
public class PreMedicalAuthorizationStatusCronJob implements Job {

    private final TransactionTemplate transactionTemplate;
    @PersistenceContext(unitName = "processing")
    private EntityManager processingEntityManager;

    @Autowired
    public PreMedicalAuthorizationStatusCronJob(@Qualifier("processingTransactionManager") PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        transactionTemplate.execute(transactionStatus -> {
            processingEntityManager.createQuery("update PreMedicalAuthorization p set p.status.id = ?1 where p.expirationDate < ?2")
                    .setParameter(1, PRE_MEDICAL_AUTHORIZATION_EXPIRED.getId())
                    .setParameter(2, LocalDate.now())
                    .executeUpdate();
            transactionStatus.flush();
            return null;
        });
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }

}

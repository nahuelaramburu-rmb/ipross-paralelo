package com.capacidad.validationapi.module.scheduler.config;

import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.scheduler.service.SchedulerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class SchedulerStartUpHandler implements ApplicationRunner {

    private final SchedulerService schedulerService;
    private final ApplicationProperties applicationProperties;

    @Autowired
    public SchedulerStartUpHandler(SchedulerService schedulerService,
                                   ApplicationProperties applicationProperties) {
        this.schedulerService = schedulerService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (Boolean.TRUE.equals(applicationProperties.getQuartzEnabled())) {
            log.info("Schedule all new scheduler jobs at app startup - starting");
            try {
                schedulerService.startAllSchedulers();
                log.info("Schedule all new scheduler jobs at app startup - complete");
            } catch (Exception ex) {
                log.error("Schedule all new scheduler jobs at app startup - error", ex);
            }
        }
    }
}

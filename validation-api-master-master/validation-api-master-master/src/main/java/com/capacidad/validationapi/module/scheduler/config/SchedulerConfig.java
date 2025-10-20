package com.capacidad.validationapi.module.scheduler.config;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Profile("!test")
@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final DataSource dataSource;
    private final QuartzProperties quartzProperties;

    @Autowired
    public SchedulerConfig(@Qualifier("processingDataSource") DataSource dataSource,
                           QuartzProperties quartzProperties) {
        this.dataSource = dataSource;
        this.quartzProperties = quartzProperties;
    }

    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * create scheduler factory
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, PlatformTransactionManager transactionManager) {
        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setQuartzProperties(properties);
        factory.setJobFactory(jobFactory);
        return factory;
    }

}

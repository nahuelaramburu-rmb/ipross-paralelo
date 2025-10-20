package com.capacidad.validationapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

@EnableConfigurationProperties(TaskExecutionProperties.class)
@Configuration
@EnableAsync
public class TaskExecutionConfig extends WebMvcConfig {

    private final TaskExecutionProperties taskExecutionProperties;

    @Autowired
    public TaskExecutionConfig(TaskExecutionProperties taskExecutionProperties) {
        this.taskExecutionProperties = taskExecutionProperties;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(600000);
        configurer.setTaskExecutor(getAsyncExecutor(taskExecutionProperties));
    }

    @Bean(name = "taskExecutor")
    public AsyncTaskExecutor getAsyncExecutor(TaskExecutionProperties taskExecutionProperties) {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        threadPoolTaskExecutor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        threadPoolTaskExecutor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

}

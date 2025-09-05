package com.capacidad.identityservice.config;

import io.undertow.UndertowOptions;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class UndertowConfig {

    @Bean
    public UndertowServletWebServerFactory undertowServletWebServerFactory() {
        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        factory.addBuilderCustomizers(builder -> builder
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true));
        int ioThreads = Math.max(Runtime.getRuntime().availableProcessors(), 4);
        int workerThreads = 250;
        factory.setIoThreads(ioThreads);
        factory.setWorkerThreads(workerThreads);
        log.info("Initializing Undertow Config with {} Worker Threads and {} IO Threads", workerThreads, ioThreads);
        return factory;
    }

}

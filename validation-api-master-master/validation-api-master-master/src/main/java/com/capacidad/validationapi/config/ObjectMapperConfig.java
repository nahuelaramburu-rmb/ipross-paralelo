package com.capacidad.validationapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class ObjectMapperConfig {

    private final ObjectMapper objectMapper;

    @Autowired
    public ObjectMapperConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /*
     * Register JSR310 to HAL ObjectMapper
     * This allows DateTime processing with Jdk8 new Libraries
     * application.properties for ObjectMapper does not apply
     * */
    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    }

}

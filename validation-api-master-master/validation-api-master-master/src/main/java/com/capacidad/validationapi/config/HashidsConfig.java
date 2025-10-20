package com.capacidad.validationapi.config;

import com.capacidad.validationapi.misc.ApplicationProperties;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashidsConfig {

    private final ApplicationProperties applicationProperties;

    @Autowired
    public HashidsConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Bean
    public Hashids hashids() {
        return new Hashids(applicationProperties.getHashidsSalt());
    }

    @Bean("reducedHashids")
    public Hashids reducedHashids() {
        return new Hashids(applicationProperties.getHashidsSalt(), 0, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }

}

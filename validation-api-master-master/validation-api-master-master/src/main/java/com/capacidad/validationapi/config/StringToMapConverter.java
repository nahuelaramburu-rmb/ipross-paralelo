package com.capacidad.validationapi.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.util.Map;

@Configuration
public class StringToMapConverter implements Converter<String, Map<String, Object>> {

    private final ObjectMapper objectMapper;

    public StringToMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @SneakyThrows
    public Map<String, Object> convert(String source) {
        return objectMapper.readValue(source, new TypeReference<>() {
        });
    }
}

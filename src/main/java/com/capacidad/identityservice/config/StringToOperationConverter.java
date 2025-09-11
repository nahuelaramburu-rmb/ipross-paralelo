package com.capacidad.identityservice.config;

import com.capacidad.identityservice.model.Operation;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COMA;

@Converter
public class StringToOperationConverter implements AttributeConverter<List<Operation>, String> {

    @Override
    public String convertToDatabaseColumn(List<Operation> operations) {
        return operations.stream()
                .map(operation -> StringUtils.lowerCase(operation.toString()))
                .collect(Collectors.joining(COMA));
    }

    @Override
    public List<Operation> convertToEntityAttribute(String joined) {
        return Arrays.stream(StringUtils.split(joined, COMA))
                .map(operation -> Operation.valueOf(StringUtils.upperCase(operation)))
                .collect(Collectors.toList());
    }


}

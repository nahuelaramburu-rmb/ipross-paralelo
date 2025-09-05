package com.capacidad.validationapi.module.procedure.config;

import com.capacidad.validationapi.module.procedure.dto.ProcedureDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Configuration
public class StringToProcedureDTOConverter implements Converter<String, ProcedureDTO> {

    private final ObjectMapper objectMapper;
    private final SmartValidator validator;

    @Autowired
    public StringToProcedureDTOConverter(ObjectMapper objectMapper,
                                         @Qualifier("customLocaleValidator") SmartValidator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    @SneakyThrows
    public ProcedureDTO convert(String source) {
        ProcedureDTO result = objectMapper.readValue(source, ProcedureDTO.class);
        validate(result);
        return result;
    }

    private void validate(ProcedureDTO sourceDTO) throws MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(sourceDTO, sourceDTO.getClass().getSimpleName());
        validator.validate(sourceDTO, bindingResult);
        if (bindingResult.hasErrors())
            throw new MethodArgumentNotValidException(new MethodParameter(Object.class.getEnclosingMethod(), 0), bindingResult);
    }

}

package com.capacidad.validationapi.module.procedure.config;

import com.capacidad.validationapi.module.procedure.dto.CertificateProcedureDTO;
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
public class StringToCertificateProcedureDTOConverter implements Converter<String, CertificateProcedureDTO> {

    private final ObjectMapper objectMapper;
    private final SmartValidator validator;

    @Autowired
    public StringToCertificateProcedureDTOConverter(ObjectMapper objectMapper,
                                                    @Qualifier("customLocaleValidator") SmartValidator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    @SneakyThrows
    public CertificateProcedureDTO convert(String source) {
        CertificateProcedureDTO result = objectMapper.readValue(source, CertificateProcedureDTO.class);
        validate(result);
        return result;
    }

    private void validate(CertificateProcedureDTO sourceDTO) throws MethodArgumentNotValidException {
        BindingResult bindingResult = new BeanPropertyBindingResult(sourceDTO, sourceDTO.getClass().getSimpleName());
        validator.validate(sourceDTO, bindingResult);
        if (bindingResult.hasErrors())
            throw new MethodArgumentNotValidException(new MethodParameter(Object.class.getEnclosingMethod(), 0), bindingResult);
    }

}

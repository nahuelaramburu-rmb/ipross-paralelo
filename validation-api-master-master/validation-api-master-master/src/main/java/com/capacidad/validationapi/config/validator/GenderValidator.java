package com.capacidad.validationapi.config.validator;

import com.capacidad.validationapi.config.annotation.ValidGender;
import com.capacidad.validationapi.module.person.model.Gender;
import org.springframework.context.annotation.Configuration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Configuration
public class GenderValidator implements ConstraintValidator<ValidGender, Object> {
    @Override
    public void initialize(ValidGender constraintAnnotation) {
        //Not implemented
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof Gender) {
            return value != Gender.INDISTINTO;
        }
        return false;
    }
}

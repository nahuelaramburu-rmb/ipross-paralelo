package com.capacidad.validationapi.config.annotation;

import com.capacidad.validationapi.config.validator.GenderValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidator.class)
public @interface ValidGender {
    String message() default "Invalid Gender";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

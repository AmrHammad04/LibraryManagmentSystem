package com.library.demo.validation;

import java.time.Year;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PublicationYearValidator
        implements ConstraintValidator<ValidPublicationYear, Integer> {

    @Override
    public boolean isValid(
            Integer value,
            ConstraintValidatorContext context) {

        return value == null || value <= Year.now().getValue();
    }
}
package com.team.placar.infra.securtiy.anotacoes.semNumeros;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoNumbersValidator implements ConstraintValidator<NoNumbers, String> {

    @Override
    public void initialize(NoNumbers constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null values are valid
        }
        return !value.matches(".*\\d.*"); // returns false if there are any digits
    }
}

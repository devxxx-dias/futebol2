package com.team.placar.infra.securtiy.validarSigla;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BrazilStateValidator implements ConstraintValidator<ValidBrazilState, String> {

    private static final Set<String> VALID_STATES = new HashSet<>(Arrays.asList(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
            "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"));

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && VALID_STATES.contains(value.toUpperCase());
    }
}


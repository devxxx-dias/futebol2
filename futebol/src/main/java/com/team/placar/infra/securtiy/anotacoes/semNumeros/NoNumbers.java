package com.team.placar.infra.securtiy.anotacoes.semNumeros;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NoNumbersValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoNumbers {
    String message() default "Verifique se há números em algum nome";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


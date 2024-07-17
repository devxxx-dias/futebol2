package com.team.placar.infra.securtiy.anotacoes.validarSigla;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = BrazilStateValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBrazilState {
    String message() default "SiglaEstado deve ser um estado válido do Brasil";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


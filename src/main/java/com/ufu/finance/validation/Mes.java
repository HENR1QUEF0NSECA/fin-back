package com.ufu.finance.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mês válido: de 1 a 12.
 *
 * Vale para campos de DTO (body) e para parâmetros de query, como ?mes=13.
 * Nulo passa; quando o campo for obrigatório, combine com @NotNull.
 */
@Min(1)
@Max(12)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Mes {

    String message() default "Mês deve ser entre 1 e 12";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

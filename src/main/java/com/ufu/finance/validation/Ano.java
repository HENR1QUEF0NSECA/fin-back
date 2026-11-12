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
 * Ano válido: de 2000 a 2100. É a mesma regra para transações e orçamentos.
 *
 * Vale para campos de DTO (body) e para parâmetros de query, como ?ano=1999.
 * Nulo passa; quando o campo for obrigatório, combine com @NotNull.
 */
@Min(2000)
@Max(2100)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ano {

    String message() default "Ano deve ser entre 2000 e 2100";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

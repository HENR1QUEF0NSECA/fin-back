package com.ufu.finance.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DataTransacaoValidator implements ConstraintValidator<DataTransacao, LocalDate> {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);

    @Override
    public boolean isValid(LocalDate data, ConstraintValidatorContext contexto) {
        return data == null || (!data.isBefore(DATA_MINIMA) && !data.isAfter(LocalDate.now()));
    }
}

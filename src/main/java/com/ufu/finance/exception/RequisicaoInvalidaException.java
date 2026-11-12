package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/** 400: os dados enviados violam uma regra de negócio (ex.: data de início depois da de fim). */
public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}

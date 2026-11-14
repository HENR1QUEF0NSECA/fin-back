package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

public class RequisicaoInvalidaException extends ApiException {

    public RequisicaoInvalidaException(String mensagem) {
        super(HttpStatus.BAD_REQUEST, mensagem);
    }
}

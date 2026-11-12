package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/** 409: a operação criaria um dado duplicado (e-mail já usado, orçamento repetido). */
public class ConflitoException extends ApiException {

    public ConflitoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}

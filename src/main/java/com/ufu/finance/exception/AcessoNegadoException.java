package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/** 403: o recurso existe, mas pertence a outro usuário. */
public class AcessoNegadoException extends ApiException {

    public AcessoNegadoException(String mensagem) {
        super(HttpStatus.FORBIDDEN, mensagem);
    }
}

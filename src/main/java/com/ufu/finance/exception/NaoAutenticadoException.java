package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

public class NaoAutenticadoException extends ApiException {

    public NaoAutenticadoException(String mensagem) {
        super(HttpStatus.UNAUTHORIZED, mensagem);
    }
}

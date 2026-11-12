package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/** 401: credenciais inválidas, token ausente/inválido ou usuário do token inexistente. */
public class NaoAutenticadoException extends ApiException {

    public NaoAutenticadoException(String mensagem) {
        super(HttpStatus.UNAUTHORIZED, mensagem);
    }
}

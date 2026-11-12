package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/** 404: o recurso pedido pelo id na URL não existe. */
public class RecursoNaoEncontradoException extends ApiException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}

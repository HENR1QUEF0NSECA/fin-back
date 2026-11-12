package com.ufu.finance.exception;

import org.springframework.http.HttpStatus;

/**
 * Base das exceções de negócio da API. Cada subclasse fixa o status HTTP, e o
 * GlobalExceptionHandler transforma qualquer uma delas na resposta de erro padrão.
 *
 * Assim o status vem do tipo da exceção, e não do texto da mensagem.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

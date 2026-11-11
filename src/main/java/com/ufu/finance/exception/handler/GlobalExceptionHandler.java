package com.ufu.finance.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura erros de validação do @Valid (campos inválidos no body)
     * Retorna um mapa com todos os campos e suas mensagens de erro
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errosPorCampo = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensagem = error.getDefaultMessage();
            errosPorCampo.put(campo, mensagem);
        });

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Dados inválidos");
        resposta.put("campos", errosPorCampo);
        resposta.put("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Mantém o status definido por quem lançou a exceção, como os
     * ResponseStatusException do OrcamentoService (409 duplicado, 403 acesso negado).
     * Sem este handler, o de RuntimeException abaixo capturava tudo e devolvia 400.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<Map<String, Object>> handleErrorResponseException(ErrorResponseException ex) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", ex.getStatusCode().value());
        resposta.put("erro", ex.getBody().getDetail());
        resposta.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatusCode()).body(resposta);
    }

    /**
     * Captura RuntimeExceptions lançadas pelos Services (ex: email duplicado)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        // Define o status HTTP baseado na mensagem (simples para fins didáticos)
        HttpStatus status = ex.getMessage().contains("não encontrado")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", status.value());
        resposta.put("erro", ex.getMessage());
        resposta.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(resposta);
    }
}
package com.ufu.finance.dto;

/**
 * Resposta do login: o token JWT e quem é o usuário.
 */
public record AuthResponseDTO(String token, String tipo, Long usuarioId, String nome) {

    public AuthResponseDTO(String token, Long usuarioId, String nome) {
        this(token, "Bearer", usuarioId, nome);
    }
}

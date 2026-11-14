package com.ufu.finance.dto;

import com.ufu.finance.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(Long id, String nome, String email, LocalDateTime dataInclusao) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getDataInclusao());
    }
}

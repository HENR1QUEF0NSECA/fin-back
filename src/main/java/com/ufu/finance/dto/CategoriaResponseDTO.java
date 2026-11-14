package com.ufu.finance.dto;

import com.ufu.finance.entity.Categoria;

public record CategoriaResponseDTO(Integer id, String nome) {

    public CategoriaResponseDTO(Categoria categoria) {
        this(categoria.getId(), categoria.getNome());
    }
}

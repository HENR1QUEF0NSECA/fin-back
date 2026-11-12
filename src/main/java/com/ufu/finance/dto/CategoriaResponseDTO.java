package com.ufu.finance.dto;

import com.ufu.finance.entity.Categoria;

/**
 * Categoria como sai na API, sem expor a entidade JPA.
 */
public record CategoriaResponseDTO(Integer id, String nome) {

    public CategoriaResponseDTO(Categoria categoria) {
        this(categoria.getId(), categoria.getNome());
    }
}

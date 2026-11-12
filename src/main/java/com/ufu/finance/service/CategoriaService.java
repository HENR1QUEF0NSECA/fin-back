package com.ufu.finance.service;

import com.ufu.finance.dto.CategoriaResponseDTO;
import com.ufu.finance.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Categorias de transação, compartilhadas entre todos os usuários.
 * São criadas pelo data.sql na inicialização.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /** Todas as categorias, em ordem alfabética. */
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaResponseDTO::new)
                .toList();
    }
}

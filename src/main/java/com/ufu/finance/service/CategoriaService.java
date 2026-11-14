package com.ufu.finance.service;

import com.ufu.finance.dto.CategoriaResponseDTO;
import com.ufu.finance.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaResponseDTO::new)
                .toList();
    }
}

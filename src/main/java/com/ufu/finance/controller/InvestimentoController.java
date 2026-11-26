package com.ufu.finance.controller;

import com.ufu.finance.dto.InvestimentoRequestDTO;
import com.ufu.finance.dto.InvestimentoResponseDTO;
import com.ufu.finance.service.InvestimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investimentos")
@RequiredArgsConstructor
public class InvestimentoController {

    private final InvestimentoService investimentoService;

    @PostMapping
    public ResponseEntity<InvestimentoResponseDTO> criar(
            @Valid @RequestBody InvestimentoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        InvestimentoResponseDTO response = investimentoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvestimentoResponseDTO>> listar(@RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(investimentoService.listar(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, @RequestAttribute Long usuarioId) {
        investimentoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}

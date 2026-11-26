package com.ufu.finance.controller;

import com.ufu.finance.dto.OrcamentoRequestDTO;
import com.ufu.finance.dto.OrcamentoResponseDTO;
import com.ufu.finance.service.OrcamentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private static final String MES = "Mês deve ser entre 1 e 12";

    private static final String ANO = "Ano deve ser entre 2000 e 2100";

    private final OrcamentoService orcamentoService;

    @PostMapping
    public ResponseEntity<OrcamentoResponseDTO> criar(
            @Valid @RequestBody OrcamentoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO response = orcamentoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponseDTO>> listar(@RequestAttribute Long usuarioId) {
        List<OrcamentoResponseDTO> orcamentos = orcamentoService.listarOrcamentosDoUsuario(usuarioId);
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(
            @PathVariable Long id,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO orcamento = orcamentoService.buscarPorId(id, usuarioId);
        return ResponseEntity.ok(orcamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO response = orcamentoService.atualizar(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @RequestAttribute Long usuarioId) {
        orcamentoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mes")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarPorMes(
            @RequestParam @Min(value = 1, message = MES) @Max(value = 12, message = MES) int mes,
            @RequestParam @Min(value = 2000, message = ANO) @Max(value = 2100, message = ANO) int ano,
            @RequestAttribute Long usuarioId) {
        List<OrcamentoResponseDTO> orcamentos = orcamentoService.listarOrcamentosMes(usuarioId, mes, ano);
        return ResponseEntity.ok(orcamentos);
    }

    @GetMapping("/categoria/{categoriaId}/mes")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorCategoriaMes(
            @PathVariable Integer categoriaId,
            @RequestParam @Min(value = 1, message = MES) @Max(value = 12, message = MES) int mes,
            @RequestParam @Min(value = 2000, message = ANO) @Max(value = 2100, message = ANO) int ano,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO orcamento = orcamentoService
                .buscarOrcamentoCategoriaMes(usuarioId, categoriaId, mes, ano);
        return ResponseEntity.ok(orcamento);
    }
}

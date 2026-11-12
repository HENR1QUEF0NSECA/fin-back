package com.ufu.finance.controller;

import com.ufu.finance.dto.OrcamentoRequestDTO;
import com.ufu.finance.dto.OrcamentoResponseDTO;
import com.ufu.finance.service.OrcamentoService;
import com.ufu.finance.validation.Ano;
import com.ufu.finance.validation.Mes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciar orçamentos mensais de categorias.
 */
@RestController
@RequestMapping("/api/orcamentos")
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    /**
     * POST /api/orcamentos
     * Cria um novo orçamento mensal para uma categoria.
     *
     * Body:
     * {
     *   "categoriaId": 1,
     *   "mes": 2,
     *   "ano": 2025,
     *   "limite": 500.00
     * }
     */
    @PostMapping
    public ResponseEntity<OrcamentoResponseDTO> criar(
            @Valid @RequestBody OrcamentoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO response = orcamentoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/orcamentos
     * Lista todos os orçamentos do usuário autenticado.
     */
    @GetMapping
    public ResponseEntity<List<OrcamentoResponseDTO>> listar(@RequestAttribute Long usuarioId) {
        List<OrcamentoResponseDTO> orcamentos = orcamentoService.listarOrcamentosDoUsuario(usuarioId);
        return ResponseEntity.ok(orcamentos);
    }

    /**
     * GET /api/orcamentos/{id}
     * Busca um orçamento específico.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorId(
            @PathVariable Long id,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO orcamento = orcamentoService.buscarPorId(id, usuarioId);
        return ResponseEntity.ok(orcamento);
    }

    /**
     * PUT /api/orcamentos/{id}
     * Atualiza o limite de um orçamento.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrcamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody OrcamentoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO response = orcamentoService.atualizar(id, dto, usuarioId);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/orcamentos/{id}
     * Deleta um orçamento.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @RequestAttribute Long usuarioId) {
        orcamentoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/orcamentos/mes?mes=2&ano=2025
     * Lista orçamentos de um mês específico.
     */
    @GetMapping("/mes")
    public ResponseEntity<List<OrcamentoResponseDTO>> listarPorMes(
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        List<OrcamentoResponseDTO> orcamentos = orcamentoService.listarOrcamentosMes(usuarioId, mes, ano);
        return ResponseEntity.ok(orcamentos);
    }

    /**
     * GET /api/orcamentos/categoria/{categoriaId}/mes?mes=2&ano=2025
     * Busca orçamento de uma categoria em um mês específico.
     */
    @GetMapping("/categoria/{categoriaId}/mes")
    public ResponseEntity<OrcamentoResponseDTO> buscarPorCategoriaMes(
            @PathVariable Integer categoriaId,
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        OrcamentoResponseDTO orcamento = orcamentoService
                .buscarOrcamentoCategoriaMes(usuarioId, categoriaId, mes, ano);
        return ResponseEntity.ok(orcamento);
    }
}



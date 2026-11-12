package com.ufu.finance.controller;

import com.ufu.finance.dto.EvolucaoMensalDTO;
import com.ufu.finance.dto.ResumoMensalDTO;
import com.ufu.finance.dto.TransacaoRequestDTO;
import com.ufu.finance.dto.TransacaoResponseDTO;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.service.TransacaoService;
import com.ufu.finance.validation.Ano;
import com.ufu.finance.validation.Mes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/transacoes
     * Cria uma nova transação para o usuário autenticado.
     *
     * Body:
     * {
     *   "tipo": "D",
     *   "valor": 150.00,
     *   "categoriaId": 1,
     *   "descricao": "Uber para o trabalho"
     * }
     * A data/hora é sempre a do momento da criação.
     */
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody TransacaoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        TransacaoResponseDTO response = transacaoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/transacoes/{id}
     * Remove uma transação. Apenas o dono pode deletar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, @RequestAttribute Long usuarioId) {
        transacaoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ─── Listagens ────────────────────────────────────────────────────────────

    /**
     * GET /api/transacoes
     * Todas as transações do usuário logado.
     */
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listarTodas(@RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorUsuario(usuarioId));
    }

    /**
     * GET /api/transacoes/tipo/{tipo}
     * Filtra por tipo: R (receitas) ou D (despesas).
     *
     * Exemplo: GET /api/transacoes/tipo/D
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorTipo(
            @PathVariable TipoTransacao tipo,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorTipo(usuarioId, tipo));
    }

    /**
     * GET /api/transacoes/mes?mes=2&ano=2025
     * Transações de um mês/ano específico.
     */
    @GetMapping("/mes")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorMes(
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorMesAno(usuarioId, mes, ano));
    }

    /**
     * GET /api/transacoes/periodo?inicio=2025-01-01&fim=2025-02-28
     * Transações em um intervalo de datas.
     */
    @GetMapping("/periodo")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorPeriodo(usuarioId, inicio, fim));
    }

    /**
     * GET /api/transacoes/categoria/{categoriaId}
     * Transações filtradas por categoria.
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorCategoria(
            @PathVariable Integer categoriaId,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorCategoria(usuarioId, categoriaId));
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    /**
     * GET /api/transacoes/dashboard/resumo?mes=2&ano=2025
     * Resumo do mês: totais receita/despesa/saldo + breakdown por categoria.
     * Ideal para cards e gráfico de pizza no front.
     */
    @GetMapping("/dashboard/resumo")
    public ResponseEntity<ResumoMensalDTO> resumoMensal(
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.resumoMensal(usuarioId, mes, ano));
    }

    /**
     * GET /api/transacoes/dashboard/evolucao?ano=2025
     * Totais mês a mês no ano — para gráfico de linha/barras.
     */
    @GetMapping("/dashboard/evolucao")
    public ResponseEntity<EvolucaoMensalDTO> evolucaoAnual(
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.evolucaoAnual(usuarioId, ano));
    }
}
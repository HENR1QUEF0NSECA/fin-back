package com.ufu.finance.controller;

import com.ufu.finance.dto.EvolucaoMensalDTO;
import com.ufu.finance.dto.ResumoMensalDTO;
import com.ufu.finance.dto.TransacaoRequestDTO;
import com.ufu.finance.dto.TransacaoResponseDTO;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.service.TransacaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransacaoController {

    @Autowired
    private TransacaoService transacaoService;

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/transaction
     * Cria uma nova transação para o usuário autenticado.
     *
     * Body:
     * {
     *   "tipo": "D",
     *   "valor": 150.00,
     *   "idCategoria": 1,
     *   "descricao": "Uber para o trabalho"
     * }
     * A data/hora é sempre a do momento da criação.
     */
    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody TransacaoRequestDTO dto,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        TransacaoResponseDTO response = transacaoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/transaction/{id}
     * Remove uma transação. Apenas o dono pode deletar.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("userId");
        transacaoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ─── Listagens ────────────────────────────────────────────────────────────

    /**
     * GET /api/transaction
     * Todas as transações do usuário logado.
     */
    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listarTodas(HttpServletRequest request) {
        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.listarPorUsuario(usuarioId));
    }

    /**
     * GET /api/transaction/tipo/{tipo}
     * Filtra por tipo: R (receitas) ou D (despesas).
     *
     * Exemplo: GET /api/transaction/tipo/D
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorTipo(
            @PathVariable TipoTransacao tipo,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.listarPorTipo(usuarioId, tipo));
    }

    /**
     * GET /api/transaction/mes?mes=2&ano=2025
     * Transações de um mês/ano específico.
     */
    @GetMapping("/mes")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorMes(
            @RequestParam int mes,
            @RequestParam int ano,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.listarPorMesAno(usuarioId, mes, ano));
    }

    /**
     * GET /api/transaction/periodo?inicio=2025-01-01&fim=2025-02-28
     * Transações em um intervalo de datas.
     */
    @GetMapping("/periodo")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.listarPorPeriodo(usuarioId, inicio, fim));
    }

    /**
     * GET /api/transaction/categoria/{idCategoria}
     * Transações filtradas por categoria.
     */
    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorCategoria(
            @PathVariable Integer idCategoria,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.listarPorCategoria(usuarioId, idCategoria));
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    /**
     * GET /api/transaction/dashboard/resumo?mes=2&ano=2025
     * Resumo do mês: totais receita/despesa/saldo + breakdown por categoria.
     * Ideal para cards e gráfico de pizza no front.
     */
    @GetMapping("/dashboard/resumo")
    public ResponseEntity<ResumoMensalDTO> resumoMensal(
            @RequestParam int mes,
            @RequestParam int ano,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.resumoMensal(usuarioId, mes, ano));
    }

    /**
     * GET /api/transaction/dashboard/evolucao?ano=2025
     * Totais mês a mês no ano — para gráfico de linha/barras.
     */
    @GetMapping("/dashboard/evolucao")
    public ResponseEntity<EvolucaoMensalDTO> evolucaoAnual(
            @RequestParam int ano,
            HttpServletRequest request) {

        Long usuarioId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(transacaoService.evolucaoAnual(usuarioId, ano));
    }
}
package com.ufu.finance.controller;

import com.ufu.finance.dto.EvolucaoMensalDTO;
import com.ufu.finance.dto.ResumoMensalDTO;
import com.ufu.finance.dto.TransacaoRequestDTO;
import com.ufu.finance.dto.TransacaoResponseDTO;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.service.TransacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    private static final String MES = "Mês deve ser entre 1 e 12";

    private static final String ANO = "Ano deve ser entre 2000 e 2100";

    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody TransacaoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        TransacaoResponseDTO response = transacaoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransacaoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransacaoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.atualizar(id, dto, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id, @RequestAttribute Long usuarioId) {
        transacaoService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TransacaoResponseDTO>> listarTodas(@RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorTipo(
            @PathVariable TipoTransacao tipo,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorTipo(usuarioId, tipo));
    }

    @GetMapping("/mes")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorMes(
            @RequestParam @Min(value = 1, message = MES) @Max(value = 12, message = MES) int mes,
            @RequestParam @Min(value = 2000, message = ANO) @Max(value = 2100, message = ANO) int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorMesAno(usuarioId, mes, ano));
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorPeriodo(usuarioId, inicio, fim));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<TransacaoResponseDTO>> listarPorCategoria(
            @PathVariable Integer categoriaId,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.listarPorCategoria(usuarioId, categoriaId));
    }

    @GetMapping("/dashboard/resumo")
    public ResponseEntity<ResumoMensalDTO> resumoMensal(
            @RequestParam @Min(value = 1, message = MES) @Max(value = 12, message = MES) int mes,
            @RequestParam @Min(value = 2000, message = ANO) @Max(value = 2100, message = ANO) int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.resumoMensal(usuarioId, mes, ano));
    }

    @GetMapping("/dashboard/evolucao")
    public ResponseEntity<EvolucaoMensalDTO> evolucaoAnual(
            @RequestParam @Min(value = 2000, message = ANO) @Max(value = 2100, message = ANO) int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.evolucaoAnual(usuarioId, ano));
    }
}
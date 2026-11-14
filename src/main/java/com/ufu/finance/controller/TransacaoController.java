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

    @PostMapping
    public ResponseEntity<TransacaoResponseDTO> criar(
            @Valid @RequestBody TransacaoRequestDTO dto,
            @RequestAttribute Long usuarioId) {
        TransacaoResponseDTO response = transacaoService.criar(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
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
            @RequestParam @Mes int mes,
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.resumoMensal(usuarioId, mes, ano));
    }

    @GetMapping("/dashboard/evolucao")
    public ResponseEntity<EvolucaoMensalDTO> evolucaoAnual(
            @RequestParam @Ano int ano,
            @RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(transacaoService.evolucaoAnual(usuarioId, ano));
    }
}
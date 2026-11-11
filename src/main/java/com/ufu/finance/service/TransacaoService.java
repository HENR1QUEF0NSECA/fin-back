package com.ufu.finance.service;

import com.ufu.finance.dto.*;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Transacao;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private OrcamentoService orcamentoService;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    /** Cria uma nova transação vinculada ao usuário autenticado */
    public TransacaoResponseDTO criar(TransacaoRequestDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Transacao transacao = new Transacao();
        transacao.setUsuario(usuario);
        transacao.setCategoria(categoria);
        transacao.setValor(dto.getValor());
        transacao.setDataHoraTransacao(LocalDateTime.now());
        transacao.setDescricao(dto.getDescricao());
        transacao.setTipo(dto.getTipo());

        Transacao saved = transacaoRepository.save(transacao);
        TransacaoResponseDTO response = new TransacaoResponseDTO(saved);

        // RF10: setar alertaOrcamento apenas pra despesas. Para receitas, sempre false
        // (decisão de negócio #5: campo sempre presente na resposta de POST).
        if (saved.getTipo() == TipoTransacao.D) {
            LocalDateTime dh = saved.getDataHoraTransacao();
            boolean alerta = orcamentoService.shouldAlert(
                    usuarioId, dto.getIdCategoria(), dh.getMonthValue(), dh.getYear());
            response.setAlertaOrcamento(alerta);
        } else {
            response.setAlertaOrcamento(false);
        }

        return response;
    }

    /** Remove uma transação — só o dono pode deletar */
    public void deletar(Long idTransacao, Long usuarioId) {
        Transacao transacao = transacaoRepository.findById(idTransacao)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

        if (!transacao.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("Acesso negado: você não pode deletar esta transação");
        }

        transacaoRepository.delete(transacao);
    }

    // ─── Consultas ────────────────────────────────────────────────────────────

    /** Todas as transações do usuário logado */
    public List<TransacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return transacaoRepository
                .findByUsuarioIdOrderByDataHoraTransacaoDesc(usuarioId)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /** Transações filtradas por tipo (R ou D) */
    public List<TransacaoResponseDTO> listarPorTipo(Long usuarioId, TipoTransacao tipo) {
        return transacaoRepository
                .findByUsuarioIdAndTipoOrderByDataHoraTransacaoDesc(usuarioId, tipo)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /** Transações dentro de um mês/ano */
    public List<TransacaoResponseDTO> listarPorMesAno(Long usuarioId, int mes, int ano) {
        validarMesAno(mes, ano);
        return transacaoRepository
                .findByUsuarioIdAndMesAno(usuarioId, mes, ano)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /** Transações filtradas por intervalo de datas */
    public List<TransacaoResponseDTO> listarPorPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RuntimeException("Data de início não pode ser posterior à data de fim");
        }
        // dh_transacao é data+hora: vai do início do primeiro dia até o fim do último
        return transacaoRepository
                .findByUsuarioIdAndDataHoraTransacaoBetweenOrderByDataHoraTransacaoDesc(
                        usuarioId, inicio.atStartOfDay(), fim.atTime(LocalTime.MAX))
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    /** Transações por categoria */
    public List<TransacaoResponseDTO> listarPorCategoria(Long usuarioId, Integer categoriaId) {
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        return transacaoRepository
                .findByUsuarioIdAndCategoriaIdOrderByDataHoraTransacaoDesc(usuarioId, categoriaId)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ─── Dashboard ────────────────────────────────────────────────────────────

    /**
     * Resumo completo do mês: totais de receita/despesa/saldo
     * + breakdown por categoria (para gráfico de pizza)
     */
    public ResumoMensalDTO resumoMensal(Long usuarioId, int mes, int ano) {
        validarMesAno(mes, ano);

        BigDecimal totalReceitas = transacaoRepository
                .sumByTipoAndMesAno(usuarioId, TipoTransacao.R, mes, ano);

        BigDecimal totalDespesas = transacaoRepository
                .sumByTipoAndMesAno(usuarioId, TipoTransacao.D, mes, ano);

        List<GraficoCategoriaDTO> despesasPorCategoria = transacaoRepository
                .sumPorCategoriaNoMes(usuarioId, TipoTransacao.D, mes, ano)
                .stream()
                .map(row -> new GraficoCategoriaDTO((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());

        List<GraficoCategoriaDTO> receitasPorCategoria = transacaoRepository
                .sumPorCategoriaNoMes(usuarioId, TipoTransacao.R, mes, ano)
                .stream()
                .map(row -> new GraficoCategoriaDTO((String) row[0], (BigDecimal) row[1]))
                .collect(Collectors.toList());

        return new ResumoMensalDTO(mes, ano, totalReceitas, totalDespesas,
                despesasPorCategoria, receitasPorCategoria);
    }

    /**
     * Evolução mês a mês de receitas e despesas no ano
     * (para gráfico de linha/barras)
     */
    public EvolucaoMensalDTO evolucaoAnual(Long usuarioId, int ano) {
        if (ano < 2000 || ano > LocalDate.now().getYear() + 1) {
            throw new RuntimeException("Ano inválido");
        }

        List<EvolucaoMensalDTO.PontoMensalDTO> receitas = transacaoRepository
                .sumPorMesNoAno(usuarioId, TipoTransacao.R, ano)
                .stream()
                .map(row -> new EvolucaoMensalDTO.PontoMensalDTO(
                        ((Number) row[0]).intValue(), (BigDecimal) row[1]))
                .collect(Collectors.toList());

        List<EvolucaoMensalDTO.PontoMensalDTO> despesas = transacaoRepository
                .sumPorMesNoAno(usuarioId, TipoTransacao.D, ano)
                .stream()
                .map(row -> new EvolucaoMensalDTO.PontoMensalDTO(
                        ((Number) row[0]).intValue(), (BigDecimal) row[1]))
                .collect(Collectors.toList());

        return new EvolucaoMensalDTO(ano, receitas, despesas);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void validarMesAno(int mes, int ano) {
        if (mes < 1 || mes > 12) {
            throw new RuntimeException("Mês inválido, deve ser entre 1 e 12");
        }
        if (ano < 2000 || ano > LocalDate.now().getYear() + 1) {
            throw new RuntimeException("Ano inválido");
        }
    }
}

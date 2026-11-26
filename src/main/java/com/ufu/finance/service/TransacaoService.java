package com.ufu.finance.service;

import com.ufu.finance.dto.*;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Transacao;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.exception.RecursoNaoEncontradoException;
import com.ufu.finance.exception.RequisicaoInvalidaException;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.InvestimentoRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);

    private final TransacaoRepository transacaoRepository;

    private final UsuarioRepository usuarioRepository;

    private final CategoriaRepository categoriaRepository;

    private final InvestimentoRepository investimentoRepository;

    private final OrcamentoService orcamentoService;

    public TransacaoResponseDTO criar(TransacaoRequestDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RequisicaoInvalidaException("Categoria não encontrada"));

        Transacao transacao = new Transacao();
        transacao.setUsuario(usuario);
        preencher(transacao, dto, categoria);

        return responderComAlerta(transacaoRepository.save(transacao), usuarioId);
    }

    public TransacaoResponseDTO atualizar(Long idTransacao, TransacaoRequestDTO dto, Long usuarioId) {
        Transacao transacao = buscarDoUsuario(idTransacao, usuarioId, "alterar");

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RequisicaoInvalidaException("Categoria não encontrada"));

        preencher(transacao, dto, categoria);

        return responderComAlerta(transacaoRepository.save(transacao), usuarioId);
    }

    public void deletar(Long idTransacao, Long usuarioId) {
        Transacao transacao = buscarDoUsuario(idTransacao, usuarioId, "deletar");
        transacaoRepository.delete(transacao);
    }

    private Transacao buscarDoUsuario(Long idTransacao, Long usuarioId, String acao) {
        Transacao transacao = transacaoRepository.findById(idTransacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada"));

        if (!transacao.getUsuario().getId().equals(usuarioId)) {
            throw new AcessoNegadoException(
                    "Acesso negado: você não pode " + acao + " esta transação");
        }

        return transacao;
    }

    private static void preencher(Transacao transacao, TransacaoRequestDTO dto, Categoria categoria) {
        if (dto.getData() != null && dto.getData().isBefore(DATA_MINIMA)) {
            throw new RequisicaoInvalidaException("Data deve ser a partir de 01/01/2000");
        }
        transacao.setCategoria(categoria);
        transacao.setValor(dto.getValor());
        transacao.setTipo(dto.getTipo());
        transacao.setDescricao(normalizarDescricao(dto.getDescricao()));
        transacao.setDataHoraTransacao(definirDataHora(dto.getData(), transacao.getDataHoraTransacao()));
    }

    private static String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }

    private static LocalDateTime definirDataHora(LocalDate data, LocalDateTime atual) {
        if (data == null) {
            return atual != null ? atual : LocalDateTime.now();
        }
        if (atual != null && atual.toLocalDate().equals(data)) {
            return atual;
        }
        LocalTime hora = atual != null ? atual.toLocalTime() : LocalTime.now();
        return data.atTime(hora);
    }

    private TransacaoResponseDTO responderComAlerta(Transacao transacao, Long usuarioId) {
        TransacaoResponseDTO response = new TransacaoResponseDTO(transacao);

        if (transacao.getTipo() == TipoTransacao.D) {
            LocalDateTime dh = transacao.getDataHoraTransacao();
            response.setAlertaOrcamento(orcamentoService.shouldAlert(
                    usuarioId, transacao.getCategoria().getId(), dh.getMonthValue(), dh.getYear()));
        } else {
            response.setAlertaOrcamento(false);
        }

        return response;
    }

    public List<TransacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return transacaoRepository
                .findByUsuarioIdOrderByDataHoraTransacaoDesc(usuarioId)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TransacaoResponseDTO> listarPorTipo(Long usuarioId, TipoTransacao tipo) {
        return transacaoRepository
                .findByUsuarioIdAndTipoOrderByDataHoraTransacaoDesc(usuarioId, tipo)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TransacaoResponseDTO> listarPorMesAno(Long usuarioId, int mes, int ano) {
        return transacaoRepository
                .findByUsuarioIdAndMesAno(usuarioId, mes, ano)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TransacaoResponseDTO> listarPorPeriodo(Long usuarioId, LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new RequisicaoInvalidaException("Data de início não pode ser posterior à data de fim");
        }
        return transacaoRepository
                .findByUsuarioIdAndDataHoraTransacaoBetweenOrderByDataHoraTransacaoDesc(
                        usuarioId, inicio.atStartOfDay(), fim.atTime(LocalTime.MAX))
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<TransacaoResponseDTO> listarPorCategoria(Long usuarioId, Integer categoriaId) {
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada"));

        return transacaoRepository
                .findByUsuarioIdAndCategoriaIdOrderByDataHoraTransacaoDesc(usuarioId, categoriaId)
                .stream()
                .map(TransacaoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public ResumoMensalDTO resumoMensal(Long usuarioId, int mes, int ano) {
        BigDecimal totalReceitas = transacaoRepository
                .sumByTipoAndMesAno(usuarioId, TipoTransacao.R, mes, ano);

        BigDecimal totalDespesas = transacaoRepository
                .sumByTipoAndMesAno(usuarioId, TipoTransacao.D, mes, ano);

        BigDecimal totalInvestido = investimentoRepository.sumNoMes(usuarioId, mes, ano);

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

        return new ResumoMensalDTO(mes, ano, totalReceitas, totalDespesas, totalInvestido,
                despesasPorCategoria, receitasPorCategoria);
    }

    public EvolucaoMensalDTO evolucaoAnual(Long usuarioId, int ano) {
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

        List<EvolucaoMensalDTO.PontoMensalDTO> investimentos = investimentoRepository
                .sumPorMesNoAno(usuarioId, ano)
                .stream()
                .map(row -> new EvolucaoMensalDTO.PontoMensalDTO(
                        ((Number) row[0]).intValue(), (BigDecimal) row[1]))
                .collect(Collectors.toList());

        return new EvolucaoMensalDTO(ano, receitas, despesas, investimentos);
    }
}

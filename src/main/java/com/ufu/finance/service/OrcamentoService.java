package com.ufu.finance.service;

import com.ufu.finance.dto.OrcamentoRequestDTO;
import com.ufu.finance.dto.OrcamentoResponseDTO;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Orcamento;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.ConflitoException;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.exception.RecursoNaoEncontradoException;
import com.ufu.finance.exception.RequisicaoInvalidaException;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.OrcamentoRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Regras de negócio dos orçamentos mensais por categoria (RF06).
 *
 * Decisões de negócio (validadas com o time em 2026-06-12):
 *  - Unicidade: 1 orçamento por (usuário, categoria, mês, ano) → 409 ao duplicar
 *  - Threshold de alerta: gasto/limite >= 80%
 *  - PUT atualiza todos os campos (categoriaId, mês, ano, limite)
 *  - DELETE livre — transações antigas permanecem, só o limite some
 *  - `consumido` = SUM(transações tipo=D na categoria/mês/ano)
 */
@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private static final BigDecimal ALERT_THRESHOLD = new BigDecimal("0.80");

    private final OrcamentoRepository orcamentoRepository;

    private final UsuarioRepository usuarioRepository;

    private final CategoriaRepository categoriaRepository;

    private final TransacaoRepository transacaoRepository;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public OrcamentoResponseDTO criar(OrcamentoRequestDTO dto, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RequisicaoInvalidaException("Categoria não encontrada"));

        if (orcamentoRepository.existsByUsuarioIdAndCategoriaIdAndMesAndAno(
                usuarioId, dto.getCategoriaId(), dto.getMes(), dto.getAno())) {
            throw new ConflitoException(
                    "Já existe orçamento para esta categoria no mês/ano informado");
        }

        Orcamento orcamento = new Orcamento();
        orcamento.setUsuario(usuario);
        orcamento.setCategoria(categoria);
        orcamento.setMes(dto.getMes());
        orcamento.setAno(dto.getAno());
        orcamento.setLimite(dto.getLimite());

        try {
            Orcamento saved = orcamentoRepository.save(orcamento);
            return montarResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // Defesa em profundidade caso a verificação acima escape em concorrência.
            throw new ConflitoException(
                    "Já existe orçamento para esta categoria no mês/ano informado");
        }
    }

    public OrcamentoResponseDTO buscarPorId(Long id, Long usuarioId) {
        Orcamento orcamento = buscarEAutorizar(id, usuarioId);
        return montarResponse(orcamento);
    }

    public OrcamentoResponseDTO atualizar(Long id, OrcamentoRequestDTO dto, Long usuarioId) {
        Orcamento orcamento = buscarEAutorizar(id, usuarioId);

        // Verifica se a nova combinação (categoria, mês, ano) não colide com outro orçamento.
        boolean mudouChave =
                !orcamento.getCategoria().getId().equals(dto.getCategoriaId())
                        || !orcamento.getMes().equals(dto.getMes())
                        || !orcamento.getAno().equals(dto.getAno());

        if (mudouChave && orcamentoRepository.existsByUsuarioIdAndCategoriaIdAndMesAndAno(
                usuarioId, dto.getCategoriaId(), dto.getMes(), dto.getAno())) {
            throw new ConflitoException(
                    "Já existe orçamento para esta categoria no mês/ano informado");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RequisicaoInvalidaException("Categoria não encontrada"));

        orcamento.setCategoria(categoria);
        orcamento.setMes(dto.getMes());
        orcamento.setAno(dto.getAno());
        orcamento.setLimite(dto.getLimite());

        try {
            Orcamento saved = orcamentoRepository.save(orcamento);
            return montarResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoException(
                    "Já existe orçamento para esta categoria no mês/ano informado");
        }
    }

    public void deletar(Long id, Long usuarioId) {
        Orcamento orcamento = buscarEAutorizar(id, usuarioId);
        orcamentoRepository.delete(orcamento);
    }

    // ─── Consultas ────────────────────────────────────────────────────────────

    public List<OrcamentoResponseDTO> listarOrcamentosDoUsuario(Long usuarioId) {
        return orcamentoRepository.findByUsuarioIdOrderByAnoDescMesDesc(usuarioId).stream()
                .map(this::montarResponse)
                .collect(Collectors.toList());
    }

    public List<OrcamentoResponseDTO> listarOrcamentosMes(Long usuarioId, int mes, int ano) {
        return orcamentoRepository.findByUsuarioIdAndMesAndAno(usuarioId, mes, ano).stream()
                .map(this::montarResponse)
                .collect(Collectors.toList());
    }

    public OrcamentoResponseDTO buscarOrcamentoCategoriaMes(
            Long usuarioId, Integer categoriaId, int mes, int ano) {
        Orcamento orcamento = orcamentoRepository
                .findByUsuarioIdAndCategoriaIdAndMesAndAno(usuarioId, categoriaId, mes, ano)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Orçamento não encontrado para esta categoria/mês"));
        return montarResponse(orcamento);
    }

    // ─── Helper público pra TransacaoService (RF10) ───────────────────────────

    /**
     * Decide se o POST /api/transacoes deve devolver {@code alertaOrcamento: true}.
     * True quando existe orçamento pra (categoria, mês, ano) E o gasto acumulado
     * (já incluindo a transação recém-salva) >= 80% do limite.
     * Quando não há orçamento, retorna false (decisão de negócio #5).
     */
    public boolean shouldAlert(Long usuarioId, Integer categoriaId, int mes, int ano) {
        return orcamentoRepository
                .findByUsuarioIdAndCategoriaIdAndMesAndAno(usuarioId, categoriaId, mes, ano)
                .map(orcamento -> {
                    BigDecimal limite = orcamento.getLimite();
                    if (limite == null || limite.signum() <= 0) return false;
                    BigDecimal consumido = consumidoNoMes(usuarioId, categoriaId, mes, ano);
                    BigDecimal threshold = limite.multiply(ALERT_THRESHOLD);
                    return consumido.compareTo(threshold) >= 0;
                })
                .orElse(false);
    }

    // ─── Internos ─────────────────────────────────────────────────────────────

    private Orcamento buscarEAutorizar(Long id, Long usuarioId) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado"));
        if (!orcamento.getUsuario().getId().equals(usuarioId)) {
            throw new AcessoNegadoException(
                    "Acesso negado: este orçamento pertence a outro usuário");
        }
        return orcamento;
    }

    private OrcamentoResponseDTO montarResponse(Orcamento orcamento) {
        OrcamentoResponseDTO dto = new OrcamentoResponseDTO(orcamento);
        BigDecimal consumido = consumidoNoMes(
                orcamento.getUsuario().getId(),
                orcamento.getCategoria().getId(),
                orcamento.getMes(),
                orcamento.getAno());
        dto.atualizarConsumo(consumido);
        return dto;
    }

    private BigDecimal consumidoNoMes(Long usuarioId, Integer categoriaId, int mes, int ano) {
        BigDecimal soma = transacaoRepository.sumByCategoriaTipoMesAno(
                usuarioId, categoriaId, TipoTransacao.D, mes, ano);
        return soma != null ? soma : BigDecimal.ZERO;
    }
}

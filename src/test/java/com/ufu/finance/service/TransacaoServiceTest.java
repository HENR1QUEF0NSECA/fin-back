package com.ufu.finance.service;

import com.ufu.finance.dto.TransacaoRequestDTO;
import com.ufu.finance.dto.TransacaoResponseDTO;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Transacao;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.RequisicaoInvalidaException;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private OrcamentoService orcamentoService;

    @InjectMocks
    private TransacaoService transacaoService;

    @Test
    void despesaDevolveOAlertaCalculadoPeloOrcamento() {
        prepararCriacao();
        when(orcamentoService.shouldAlert(eq(1L), eq(3), anyInt(), anyInt())).thenReturn(true);

        TransacaoResponseDTO resposta = transacaoService.criar(dto(TipoTransacao.D), 1L);

        assertThat(resposta.getId()).isEqualTo(50L);
        assertThat(resposta.getCategoriaId()).isEqualTo(3);
        assertThat(resposta.getAlertaOrcamento()).isTrue();
    }

    @Test
    void receitaNuncaGeraAlerta() {
        prepararCriacao();

        TransacaoResponseDTO resposta = transacaoService.criar(dto(TipoTransacao.R), 1L);

        assertThat(resposta.getAlertaOrcamento()).isFalse();
        verifyNoInteractions(orcamentoService);
    }

    @Test
    void categoriaInexistenteNoCorpoDevolve400() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(categoriaRepository.findById(3)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transacaoService.criar(dto(TipoTransacao.D), 1L))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Categoria não encontrada");
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void periodoComInicioDepoisDoFimDevolve400() {
        assertThatThrownBy(() -> transacaoService.listarPorPeriodo(
                1L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 31)))
                .isInstanceOf(RequisicaoInvalidaException.class);
        verifyNoInteractions(transacaoRepository);
    }

    @Test
    void transacaoDeOutroUsuarioNaoPodeSerExcluida() {
        Transacao transacao = new Transacao();
        transacao.setId(50L);
        transacao.setUsuario(usuario(2L));
        when(transacaoRepository.findById(50L)).thenReturn(Optional.of(transacao));

        assertThatThrownBy(() -> transacaoService.deletar(50L, 1L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(transacaoRepository, never()).delete(any());
    }

    // ─── Auxiliares ───────────────────────────────────────────────────────────

    private void prepararCriacao() {
        Categoria categoria = new Categoria();
        categoria.setId(3);
        categoria.setNome("Transporte");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(chamada -> {
            Transacao salva = chamada.getArgument(0);
            salva.setId(50L);
            return salva;
        });
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private static TransacaoRequestDTO dto(TipoTransacao tipo) {
        TransacaoRequestDTO dto = new TransacaoRequestDTO();
        dto.setTipo(tipo);
        dto.setValor(new BigDecimal("50.00"));
        dto.setCategoriaId(3);
        dto.setDescricao("Ônibus");
        return dto;
    }
}

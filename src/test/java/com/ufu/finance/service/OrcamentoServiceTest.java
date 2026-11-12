package com.ufu.finance.service;

import com.ufu.finance.dto.OrcamentoRequestDTO;
import com.ufu.finance.entity.Categoria;
import com.ufu.finance.entity.Orcamento;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoTransacao;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.ConflitoException;
import com.ufu.finance.exception.RecursoNaoEncontradoException;
import com.ufu.finance.repository.CategoriaRepository;
import com.ufu.finance.repository.OrcamentoRepository;
import com.ufu.finance.repository.TransacaoRepository;
import com.ufu.finance.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {

    @Mock
    private OrcamentoRepository orcamentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private OrcamentoService orcamentoService;

    @Test
    void orcamentoRepetidoNoMesmoMesDevolve409() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(categoria(3)));
        when(orcamentoRepository.existsByUsuarioIdAndCategoriaIdAndMesAndAno(1L, 3, 5, 2026)).thenReturn(true);

        assertThatThrownBy(() -> orcamentoService.criar(dto(3, 5, 2026, "100"), 1L))
                .isInstanceOf(ConflitoException.class);
        verify(orcamentoRepository, never()).save(any());
    }

    @Test
    void orcamentoDeOutroUsuarioDevolve403() {
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orcamento(2L, "100")));

        assertThatThrownBy(() -> orcamentoService.buscarPorId(10L, 1L))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void orcamentoInexistenteDevolve404() {
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orcamentoService.deletar(10L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Orçamento não encontrado");
    }

    @Test
    void alertaLigaAPartirDe80PorCentoDoLimite() {
        when(orcamentoRepository.findByUsuarioIdAndCategoriaIdAndMesAndAno(1L, 3, 5, 2026))
                .thenReturn(Optional.of(orcamento(1L, "100")));

        gastoNoMes("80.00");
        assertThat(orcamentoService.shouldAlert(1L, 3, 5, 2026)).isTrue();

        gastoNoMes("79.99");
        assertThat(orcamentoService.shouldAlert(1L, 3, 5, 2026)).isFalse();
    }

    @Test
    void semOrcamentoNaoHaAlerta() {
        when(orcamentoRepository.findByUsuarioIdAndCategoriaIdAndMesAndAno(1L, 3, 5, 2026))
                .thenReturn(Optional.empty());

        assertThat(orcamentoService.shouldAlert(1L, 3, 5, 2026)).isFalse();
    }

    // ─── Auxiliares ───────────────────────────────────────────────────────────

    private void gastoNoMes(String valor) {
        when(transacaoRepository.sumByCategoriaTipoMesAno(1L, 3, TipoTransacao.D, 5, 2026))
                .thenReturn(new BigDecimal(valor));
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private static Categoria categoria(Integer id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Transporte");
        return categoria;
    }

    private static Orcamento orcamento(Long donoId, String limite) {
        Orcamento orcamento = new Orcamento();
        orcamento.setId(10L);
        orcamento.setUsuario(usuario(donoId));
        orcamento.setCategoria(categoria(3));
        orcamento.setMes(5);
        orcamento.setAno(2026);
        orcamento.setLimite(new BigDecimal(limite));
        return orcamento;
    }

    private static OrcamentoRequestDTO dto(Integer categoriaId, int mes, int ano, String limite) {
        OrcamentoRequestDTO dto = new OrcamentoRequestDTO();
        dto.setCategoriaId(categoriaId);
        dto.setMes(mes);
        dto.setAno(ano);
        dto.setLimite(new BigDecimal(limite));
        return dto;
    }
}

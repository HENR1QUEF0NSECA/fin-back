package com.ufu.finance.service;

import com.ufu.finance.dto.InvestimentoRequestDTO;
import com.ufu.finance.dto.InvestimentoResponseDTO;
import com.ufu.finance.entity.Investimento;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.enums.TipoInvestimento;
import com.ufu.finance.exception.AcessoNegadoException;
import com.ufu.finance.exception.RequisicaoInvalidaException;
import com.ufu.finance.repository.InvestimentoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestimentoServiceTest {

    @Mock
    private InvestimentoRepository investimentoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private InvestimentoService investimentoService;

    @Test
    void criarGravaOInvestimentoDoUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L)));
        when(investimentoRepository.save(any(Investimento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        InvestimentoResponseDTO resposta = investimentoService.criar(dto("  Tesouro Selic  ", "2025-03-10"), 1L);

        assertThat(resposta.descricao()).isEqualTo("Tesouro Selic");
        assertThat(resposta.tipo()).isEqualTo(TipoInvestimento.RENDA_FIXA);
        assertThat(resposta.valor()).isEqualByComparingTo("500.00");
        assertThat(resposta.data()).isEqualTo(LocalDate.of(2025, 3, 10));
    }

    @Test
    void dataAntesDe2000EhRecusada() {
        assertThatThrownBy(() -> investimentoService.criar(dto("Tesouro Selic", "1999-12-31"), 1L))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("Data deve ser a partir de 01/01/2000");
        verify(investimentoRepository, never()).save(any());
    }

    @Test
    void naoExcluiInvestimentoDeOutroUsuario() {
        Investimento investimento = new Investimento();
        investimento.setUsuario(usuario(2L));
        when(investimentoRepository.findById(10L)).thenReturn(Optional.of(investimento));

        assertThatThrownBy(() -> investimentoService.deletar(10L, 1L))
                .isInstanceOf(AcessoNegadoException.class);
        verify(investimentoRepository, never()).delete(any());
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private static InvestimentoRequestDTO dto(String descricao, String data) {
        InvestimentoRequestDTO dto = new InvestimentoRequestDTO();
        dto.setDescricao(descricao);
        dto.setTipo(TipoInvestimento.RENDA_FIXA);
        dto.setValor(new BigDecimal("500.00"));
        dto.setData(LocalDate.parse(data));
        return dto;
    }
}

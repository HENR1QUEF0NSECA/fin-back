package com.ufu.finance.service;

import com.ufu.finance.dto.UsuarioRequestDTO;
import com.ufu.finance.dto.UsuarioResponseDTO;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.exception.ConflitoException;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void cadastroChecaDuplicidadeComOEmailNormalizado() {
        when(usuarioRepository.existsByEmail("ana@ex.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrarUsuario(dto("  Ana@Ex.COM ")))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Email já está em uso");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastroGravaEmailEmMinusculasESenhaComHash() {
        when(usuarioRepository.existsByEmail("ana@ex.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(chamada -> chamada.getArgument(0));

        UsuarioResponseDTO resposta = usuarioService.cadastrarUsuario(dto("Ana@Ex.com"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@ex.com");
        assertThat(salvo.getValue().getSenha()).isNotEqualTo("senha123");
        assertThat(usuarioService.getPasswordEncoder().matches("senha123", salvo.getValue().getSenha())).isTrue();
        assertThat(resposta.email()).isEqualTo("ana@ex.com");
    }

    @Test
    void usuarioDoTokenQueNaoExisteMaisDevolve401() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarUsuarioLogado(99L))
                .isInstanceOf(NaoAutenticadoException.class);
    }

    private static UsuarioRequestDTO dto(String email) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("Ana Teste");
        dto.setEmail(email);
        dto.setSenha("senha123");
        return dto;
    }
}

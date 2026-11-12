package com.ufu.finance.service.auth;

import com.ufu.finance.dto.AuthResponseDTO;
import com.ufu.finance.dto.LoginDTO;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // Força baixa só para o teste ficar rápido.
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void criarUsuario() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana");
        usuario.setEmail("ana@ex.com");
        usuario.setSenha(encoder.encode("senha123"));
    }

    @Test
    void loginCorretoDevolveTokenEDadosDoUsuario() {
        when(usuarioService.buscarPorEmail("ana@ex.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.getPasswordEncoder()).thenReturn(encoder);
        when(jwtService.generateToken(1L, "Ana")).thenReturn("token-gerado");

        AuthResponseDTO resposta = authService.login(login("ana@ex.com", "senha123"));

        assertThat(resposta.token()).isEqualTo("token-gerado");
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.usuarioId()).isEqualTo(1L);
    }

    @Test
    void senhaErradaDevolve401() {
        when(usuarioService.buscarPorEmail("ana@ex.com")).thenReturn(Optional.of(usuario));
        when(usuarioService.getPasswordEncoder()).thenReturn(encoder);

        assertThatThrownBy(() -> authService.login(login("ana@ex.com", "errada")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage("Email ou senha inválidos");
    }

    @Test
    void emailInexistenteDevolveAMesmaMensagemDaSenhaErrada() {
        when(usuarioService.buscarPorEmail("nao@ex.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(login("nao@ex.com", "senha123")))
                .isInstanceOf(NaoAutenticadoException.class)
                .hasMessage("Email ou senha inválidos");
    }

    private static LoginDTO login(String email, String senha) {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(email);
        dto.setSenha(senha);
        return dto;
    }
}

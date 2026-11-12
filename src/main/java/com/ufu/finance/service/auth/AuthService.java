package com.ufu.finance.service.auth;

import com.ufu.finance.dto.AuthResponseDTO;
import com.ufu.finance.dto.LoginDTO;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    /**
     * Realiza o login do usuário e retorna o token JWT.
     * A mensagem de erro é genérica para não revelar se o email existe ou não.
     */
    public AuthResponseDTO login(LoginDTO loginDTO) {
        Usuario usuario = usuarioService.buscarPorEmail(loginDTO.getEmail())
                .orElseThrow(() -> new NaoAutenticadoException("Email ou senha inválidos"));

        // BCrypt compara a senha digitada com o hash armazenado
        boolean senhaCorreta = usuarioService.getPasswordEncoder()
                .matches(loginDTO.getSenha(), usuario.getSenha());

        if (!senhaCorreta) {
            throw new NaoAutenticadoException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getNome());
        return new AuthResponseDTO(token, usuario.getId(), usuario.getNome());
    }
}

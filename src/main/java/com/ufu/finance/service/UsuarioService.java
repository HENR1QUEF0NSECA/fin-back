package com.ufu.finance.service;

import com.ufu.finance.dto.UsuarioRequestDTO;
import com.ufu.finance.dto.UsuarioResponseDTO;
import com.ufu.finance.entity.Usuario;
import com.ufu.finance.exception.ConflitoException;
import com.ufu.finance.exception.NaoAutenticadoException;
import com.ufu.finance.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // BCrypt com força 12 (padrão é 10; 12 é mais seguro sem impacto perceptível)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    /**
     * Retorna o encoder para que outros serviços possam validar senhas
     */
    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * Cadastra um novo usuário com senha criptografada
     */
    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        // Checa com o e-mail já normalizado, igual ao que é gravado. Antes a checagem
        // usava o e-mail como veio: "Ana@x.com" passava e estourava a unique do banco.
        String email = normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Email já está em uso");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha())); // 🔒 hash BCrypt

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    /**
     * Dados do usuário dono do token. Se ele não existe mais, o token não vale:
     * 401, e não 404.
     */
    public UsuarioResponseDTO buscarUsuarioLogado(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(UsuarioResponseDTO::new)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));
    }

    /**
     * Busca usuário por email (usado no login)
     */
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email));
    }

    /** E-mails são gravados e comparados sempre sem espaços e em minúsculas. */
    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

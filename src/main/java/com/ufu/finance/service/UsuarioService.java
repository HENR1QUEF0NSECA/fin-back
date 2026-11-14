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

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public UsuarioResponseDTO cadastrarUsuario(UsuarioRequestDTO dto) {
        String email = normalizarEmail(dto.getEmail());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflitoException("Email já está em uso");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO buscarUsuarioLogado(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(UsuarioResponseDTO::new)
                .orElseThrow(() -> new NaoAutenticadoException("Usuário não encontrado"));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(normalizarEmail(email));
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

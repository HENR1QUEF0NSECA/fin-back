package com.ufu.finance.controller;

import com.ufu.finance.dto.AuthResponseDTO;
import com.ufu.finance.dto.LoginDTO;
import com.ufu.finance.dto.UsuarioRequestDTO;
import com.ufu.finance.dto.UsuarioResponseDTO;
import com.ufu.finance.service.UsuarioService;
import com.ufu.finance.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rotas públicas de autenticação: tudo em /api/auth/** dispensa o token JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    /**
     * POST /api/auth/login
     * Realiza login e retorna token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    /**
     * POST /api/auth/registro
     * Cadastra um novo usuário.
     */
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrarUsuario(dto));
    }
}

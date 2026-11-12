package com.ufu.finance.controller;

import com.ufu.finance.dto.UsuarioResponseDTO;
import com.ufu.finance.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dados do usuário logado. Não há rota para listar ou consultar outros
 * usuários: cada um só enxerga os próprios dados.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * GET /api/usuarios/me
     * Retorna os dados do usuário dono do token.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioLogado(@RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioLogado(usuarioId));
    }
}

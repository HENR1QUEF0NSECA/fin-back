package com.ufu.finance.controller;

import com.ufu.finance.dto.UsuarioResponseDTO;
import com.ufu.finance.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioLogado(@RequestAttribute Long usuarioId) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioLogado(usuarioId));
    }
}

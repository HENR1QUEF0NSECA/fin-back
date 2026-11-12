package com.ufu.finance.controller;

import com.ufu.finance.dto.UserDTO;
import com.ufu.finance.dto.UserResponseDTO;
import com.ufu.finance.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Cadastra um novo usuário (rota pública).
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> cadastrarUsuario(@Valid @RequestBody UserDTO userDTO) {
        UserResponseDTO user = usuarioService.cadastrarUsuario(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Busca usuário por ID (requer autenticação)
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> buscarUsuario(@PathVariable Long id) {
        UserResponseDTO user = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Lista todos os usuários (requer autenticação)
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    /**
     * Retorna id e nome extraídos do token pelo filtro JWT
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> usuarioLogado(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of(
                "userId", request.getAttribute("userId"),
                "nome",   request.getAttribute("nome"),
                "mensagem", "Você está autenticado!"
        ));
    }
}

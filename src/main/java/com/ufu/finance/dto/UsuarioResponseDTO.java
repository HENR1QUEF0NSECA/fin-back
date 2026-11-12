package com.ufu.finance.dto;

import com.ufu.finance.entity.Usuario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponseDTO {
    
    private Long id;
    private String nome;
    private String email;
    private LocalDateTime dataInclusao;
    
    public UserResponseDTO() {
    }
    
    public UserResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.dataInclusao = usuario.getDataInclusao();
    }
}

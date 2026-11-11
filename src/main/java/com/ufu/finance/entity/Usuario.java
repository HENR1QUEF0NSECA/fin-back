package com.ufu.finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false)
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    private String senha;
    
    @Column(name = "data_inclusao", nullable = false, updatable = false)
    private LocalDateTime dataInclusao;

    @PrePersist
    protected void onCreate() {
        dataInclusao = LocalDateTime.now();
    }

}

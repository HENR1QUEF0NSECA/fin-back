package com.ufu.finance.entity;

import jakarta.persistence.*;

/**
 * Representa uma categoria de transação.
 *
 * A tabela é populada via data.sql com as categorias iniciais.
 * Para adicionar novas categorias basta inserir um novo registro no banco
 * — nenhuma alteração de código é necessária.
 *
 * Exemplo de INSERT:
 *   INSERT INTO categorias (nome) VALUES ('investment');
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    public Categoria() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}

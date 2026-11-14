package com.ufu.finance.enums;

public enum TipoTransacao {
    R("Receita"),
    D("Despesa");

    private final String descricao;

    TipoTransacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

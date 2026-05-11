package com.indux.core.domain.model.modules.form;

import lombok.Getter;

@Getter
public enum DocumentStatus {
    // os dois primeiros valores marcam se é "status" ou "situação"
    PENDENTE(1, false),
    REVISÃO(2, false),
    ABERTO(3, false),
    REJEITADO(4, false),
    APROVADO(5, false),
    REVIEWED(6, false),
    ATRASADO(1, true),
    ANDAMENTO(2, true),
    FINALIZADO(3, true);
    private final int order;
    private final boolean situacao;

    DocumentStatus(int order, boolean situacao) {
        this.order = order;
        this.situacao = situacao;
    }

    /**
     * Posição de ordenação — menor = aparece antes
     */
    public int getOrder() {
        return order;
    }

    /**
     * True se esse valor pertence ao grupo "situacao"
     */
    public boolean isSituacao() {
        return situacao;
    }
}
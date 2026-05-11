package com.indux.modules.ocf.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CausasFinanceiras {
    PAGAMENTO_NAO_CREDITADO("Pagamento não Creditado"),
    PAGAMENTO_A_MAIOR("Pagamento à Maior"),
    PAGAMENTO_A_MENOR("Pagamento à menor"),
    OUTROS("Outros"),
    COLABORADOR_SEM_LINK_DE_ACESSO_FLASH("Colaborador sem Link de Acesso (Flash)"),
    SALARIO("Salário"),
    ERRO_NO_CALCULO_DA_PENSAO("Erro no Cálculo da Pensão"),
    PLANO_DE_SAUDE_REPERCUSSAO_NA_FOLHA("Plano de Saúde (Repercussão na Folha)"),
    ADICIONAL_NOTURNO("Adicional Noturno"),
    HORAS_EXTRAS("Horas Extras"),
    HRA("HRA"),
    TRABALHO_NA_FOLGA("Trabalho na Folga"),
    PREMIACAO("Premiação"),
    TF_CURSO("TF Curso"),
    PERICULOSIDADE("Periculosidade"),
    SOBREAVISO("Sobreaviso"),
    INSALUBRIDADE("Insalubridade"),
    LIMITE_EXCEDIDO("Limite Excedido"),
    ERRO_CADASTRO("Erro Cadastro");

    private final String descricao;

    CausasFinanceiras(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @JsonCreator
    public static CausasFinanceiras fromDescricao(String descricao) {
        for (CausasFinanceiras c : values()) {
            if (c.descricao.equalsIgnoreCase(descricao)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Causa financeira inválida: " + descricao);
    }
}

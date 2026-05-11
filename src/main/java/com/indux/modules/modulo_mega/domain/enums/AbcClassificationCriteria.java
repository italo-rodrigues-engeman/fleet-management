package com.indux.modules.modulo_mega.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AbcClassificationCriteria {

    TOTAL_VALUE("Valor total"),
    QUANTITY("Quantidade"), 
    AVERAGE_PRICE("Preço médio");

    private final String description;

    AbcClassificationCriteria(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    /**
     * Converte String para Enum.
     * Usado pelo Jackson (JSON) e pelo Converter do Spring (URL).
     */
    @JsonCreator
    public static AbcClassificationCriteria fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Remove espaços extras e coloca em caixa alta para normalizar
        String normalized = value.trim().toUpperCase();

        // 1. Tenta encontrar pelo nome exato do Enum (ex: TOTAL_VALUE)
        try {
            return AbcClassificationCriteria.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Se não for o nome exato, continua para o switch de apelidos/descrições
        }

        // 2. Tenta encontrar pelos termos em português ou variações
        switch (normalized) {
            case "VALOR TOTAL":
            case "VALOR_TOTAL":
                return TOTAL_VALUE;
            
            case "QUANTIDADE":
            case "QTD":
            case "qtdItensTotal":
                return QUANTITY;
            
            case "PRECO MEDIO":
            case "PREÇO MÉDIO":
            case "PREÇO MEDIO":
            case "PRECO_MEDIO":
            case "AVERAGE_PRICE":
                return AVERAGE_PRICE;
                
            default:
                throw new IllegalArgumentException(
                    "Critério de classificação inválido: " + value + 
                    ". Valores aceitos: Valor total, Quantidade, Preço médio."
                );
        }
    }
}
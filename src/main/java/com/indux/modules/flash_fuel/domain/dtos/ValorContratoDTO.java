package com.indux.modules.flash_fuel.domain.dtos;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ValorContratoDTO(
    @NotNull(message = "Contrato é obrigatório.") String contrato,
    @NotNull(message = "Valor é obrigatório.") BigDecimal valor
) {
    @JsonCreator
    public static ValorContratoDTO create(
            @JsonProperty("contrato") String contrato,
            @JsonProperty("valor") String valorStr) {
        
        BigDecimal valor = parseBrazilianCurrency(valorStr);
        return new ValorContratoDTO(contrato, valor);
    }
    
    private static BigDecimal parseBrazilianCurrency(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Valor não pode ser nulo ou vazio");
        }
        
        try {
            // Remove espaços e caracteres especiais
            String cleanValue = value.trim()
                    .replace("R$", "")
                    .replace(" ", "")
                    .trim();
            
            // Se já está no formato americano (com ponto), converte diretamente
            if (cleanValue.contains(".") && !cleanValue.contains(",")) {
                return new BigDecimal(cleanValue);
            }
            
            // Se está no formato brasileiro (1.000,00), converte
            if (cleanValue.contains(",")) {
                // Remove pontos de milhares e substitui vírgula por ponto
                String americanFormat = cleanValue.replace(".", "").replace(",", ".");
                return new BigDecimal(americanFormat);
            }
            
            // Se não tem vírgula nem ponto decimal, assume que é um número inteiro
            return new BigDecimal(cleanValue);
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de valor inválido: " + value + 
                    ". Use formato brasileiro (1.000,00) ou americano (1000.00)");
        }
    }
} 
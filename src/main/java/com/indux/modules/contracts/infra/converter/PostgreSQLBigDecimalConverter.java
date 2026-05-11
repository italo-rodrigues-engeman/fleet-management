package com.indux.modules.contracts.infra.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class PostgreSQLBigDecimalConverter implements AttributeConverter<BigDecimal, Object> {

    @Override
    public Object convertToDatabaseColumn(BigDecimal attribute) {
        return attribute;
    }

    @Override
    public BigDecimal convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        
        // Se já é BigDecimal, retorna diretamente
        if (dbData instanceof BigDecimal) {
            return (BigDecimal) dbData;
        }
        
        // Se é String, verifica se é NaN ou null
        if (dbData instanceof String) {
            String strValue = (String) dbData;
            if (strValue.trim().isEmpty() || "NaN".equals(strValue) || "null".equals(strValue)) {
                return null;
            }
            try {
                return new BigDecimal(strValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Se é Number, converte para BigDecimal
        if (dbData instanceof Number) {
            try {
                return new BigDecimal(dbData.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
} 
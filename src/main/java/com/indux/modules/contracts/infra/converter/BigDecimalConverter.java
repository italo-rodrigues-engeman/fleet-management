package com.indux.modules.contracts.infra.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter
public class BigDecimalConverter implements AttributeConverter<BigDecimal, String> {

    @Override
    public String convertToDatabaseColumn(BigDecimal attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public BigDecimal convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "NaN".equals(dbData) || "null".equals(dbData)) {
            return null;
        }
        try {
            return new BigDecimal(dbData);
        } catch (NumberFormatException e) {
            return null;
        }
    }
} 
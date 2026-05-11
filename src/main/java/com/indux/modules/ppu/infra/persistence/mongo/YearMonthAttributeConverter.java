package com.indux.modules.ppu.infra.persistence.mongo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Date;
import java.time.YearMonth;

@Converter(autoApply = true)
public class YearMonthAttributeConverter implements AttributeConverter<YearMonth, Date> {

    @Override
    public Date convertToDatabaseColumn(YearMonth attribute) {
        return attribute != null ? Date.valueOf(attribute.atDay(1)) : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(Date dbData) {
        return dbData != null ? YearMonth.from(dbData.toLocalDate()) : null;
    }
}
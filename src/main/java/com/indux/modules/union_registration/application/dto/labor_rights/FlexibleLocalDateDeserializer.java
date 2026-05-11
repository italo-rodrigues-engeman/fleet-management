package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        
        // Tenta primeiro o formato ISO (yyyy-MM-dd)
        try {
            return LocalDate.parse(dateString, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            // Se falhar, tenta o formato brasileiro (dd/MM/yyyy)
            try {
                return LocalDate.parse(dateString, BR_FORMATTER);
            } catch (DateTimeParseException ex) {
                throw new IOException("Não foi possível deserializar a data: " + dateString + 
                    ". Formatos aceitos: yyyy-MM-dd ou dd/MM/yyyy", ex);
            }
        }
    }
}



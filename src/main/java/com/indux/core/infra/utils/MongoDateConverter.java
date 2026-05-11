package com.indux.core.infra.utils;

import org.bson.Document;

import java.time.Instant;
import java.util.Date;

public class MongoDateConverter {

    /**
     * Converte um documento MongoDB com formato de data para um objeto Date do Java
     * @param document Documento MongoDB com formato de data
     * @return Objeto Date do Java
     */
    public static Date convertToDate(Document document) {
        if (document == null) {
            return null;
        }
        
        if (document.containsKey("$data")) {
            Object dateValue = document.get("$data");
            if (dateValue instanceof String) {
                try {
                    return Date.from(Instant.parse((String) dateValue));
                } catch (Exception e) {
                    return new Date();
                }
            } else if (dateValue instanceof Long) {
                return new Date((Long) dateValue);
            }
        }
        return new Date();
    }
    
    /**
     * Converte um objeto Date do Java para um documento MongoDB com formato de data
     * @param date Objeto Date do Java
     * @return Documento MongoDB com formato de data
     */
    public static Document convertToDocument(Date date) {
        if (date == null) {
            return null;
        }
        
        Document document = new Document();
        document.put("$data", date.toInstant().toString());
        return document;
    }
} 
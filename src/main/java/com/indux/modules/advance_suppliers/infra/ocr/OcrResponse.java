package com.indux.modules.advance_suppliers.infra.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResponse {
    private boolean success;
    private String message;
    private Object data;
    
    // Construtores
    public OcrResponse() {}
    
    public OcrResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
} 
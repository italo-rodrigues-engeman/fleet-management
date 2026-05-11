package com.indux.modules.advance_suppliers.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.infra.http.HttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Slf4j
@Component
public class Ocr {

    private final HttpClient http;
    private final ObjectMapper objectMapper;
    private static final String API_URL = "http://192.168.0.10:8001/api/notas-fiscais/";

    public Ocr(HttpClient http) {
        this.http = http;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envia um arquivo para o microserviço OCR via POST com form-data
     * @param fileName nome do arquivo
     * @param fileStream stream do arquivo
     * @param mimeType tipo MIME do arquivo (ex: "application/pdf", "image/jpeg")
     * @return resposta do OCR
     */
    public OcrNotaFiscalResponse call(String fileName, InputStream fileStream, String mimeType) {
        try {
            var response = http.postMultipart(API_URL, new HashMap<>(), "arquivo", fileName, fileStream, mimeType);
            
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                
                try {
                    return objectMapper.readValue(responseBody, OcrNotaFiscalResponse.class);
                } catch (Exception e) {
                    log.error("Erro ao converter resposta JSON: {}", e.getMessage());
                    return null;
                }
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Erro desconhecido";
                log.error("Erro na chamada OCR: HTTP {} - {}", response.code(), errorBody);
                return null;
            }
            
        } catch (IOException e) {
            log.error("Erro de I/O ao chamar OCR: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Erro inesperado ao chamar OCR: {}", e.getMessage());
            return null;
        }
    }
}
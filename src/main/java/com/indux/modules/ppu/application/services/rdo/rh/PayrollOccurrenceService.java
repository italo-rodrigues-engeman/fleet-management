package com.indux.modules.ppu.application.services.rdo.rh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.http.HttpClient;
import com.indux.modules.ppu.application.dtos.requests.CreatePayrollOccurrenceRequestDTO;
import com.indux.modules.ppu.application.services.rdo.helper.PayrollOccurrenceJustificationFormatter;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.services.UpdateCheckDiff;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayrollOccurrenceService {
    private final HttpClient httpClient;
    private final PayrollOccurrenceJustificationFormatter justificationFormatter;
    private final ObjectMapper objectMapper;
    
    @Value("${app.api.base-url:http://localhost:8080}")
    private String apiBaseUrl;
    
    public PayrollOccurrenceService(HttpClient httpClient, PayrollOccurrenceJustificationFormatter justificationFormatter, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.justificationFormatter = justificationFormatter;
        this.objectMapper = objectMapper;
    }
    
    public void createOccurrenceForChange(
            RDOEntity rdo, 
            UpdateCheckDiff.Change change, 
            String tipoAtendimento, 
            String competencia,
            String authToken
    ) {
        String url = apiBaseUrl + "/api/solicitacoes/ocf/batch/create-occurrences";
        
        String justificativa = justificationFormatter.format(rdo, change);
        
        var request = new CreatePayrollOccurrenceRequestDTO(
                List.of(change.registration()),
                justificativa,
                tipoAtendimento,
                competencia
        );
        
        Map<String, Object> body = convertToMap(request);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        
        if (authToken != null) {
            headers.put("Authorization", "Bearer " + authToken);
        }
        
        try {
            var response = httpClient.post(url, headers, body);
            if (!response.isSuccessful()) {
                throw new ModuleFailure("Falha ao criar ocorrência de folha de pagamento. Status: " + response.code());
            }
        } catch (IOException e) {
            throw new ModuleFailure("Erro ao comunicar com serviço de ocorrências");
        }
    }
    
    public String formatCompetenceToMMyyyy(String competence) {
        try {
            YearMonth yearMonth = YearMonth.parse(competence);
            return String.format("%02d/%d", yearMonth.getMonthValue(), yearMonth.getYear());
        } catch (Exception e) {
            return competence;
        }
    }
    
    private Map<String, Object> convertToMap(CreatePayrollOccurrenceRequestDTO dto) {
        try {
            return objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new ModuleFailure("Erro ao converter DTO de ocorrência para Map");
        }
    }
}

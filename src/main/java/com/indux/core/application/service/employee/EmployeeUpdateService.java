package com.indux.core.application.service.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeUpdateService {
    
    private final RestTemplate restTemplate;
    
    @Autowired
    public EmployeeUpdateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Chama a API externa para obter atualizações de funcionários
     * @return ResponseEntity com os dados da API externa
     */
    public ResponseEntity<Object> getEmployeeUpdate() {
        try {
            String url = "http://192.168.0.10:3031/get-employee-update";
            
            ResponseEntity<Object> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                Object.class
            );
            
            return response;
            
        } catch (RestClientException e) {
            // Em caso de erro na chamada da API externa
            throw new RuntimeException("Erro ao chamar API externa: " + e.getMessage(), e);
        }
    }
}

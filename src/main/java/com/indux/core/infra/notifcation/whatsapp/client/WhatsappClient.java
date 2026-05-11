package com.indux.core.infra.notifcation.whatsapp.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class WhatsappClient {
    private final WebClient webClient;
    @Value("${evolution.key}")
    private String evolutionKey;
    @Value("${evolution.server}")
    private String evolutionServer;
    @Value("${evolution.instance}")
    private String instance;

    public WhatsappClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendMessage(String phone, String message) {
        Map<String, String> payload = Map.of(
                "number", phone,
                "text", message
        );
        
        String url = evolutionServer + "/message/sendText/" + instance;
        
        
        try {
            String response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("apiKey", evolutionKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                    

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            // Erro silencioso - não falha a operação principal
        } catch (Exception e) {
            // Erro silencioso - não falha a operação principal
        }
    }

}
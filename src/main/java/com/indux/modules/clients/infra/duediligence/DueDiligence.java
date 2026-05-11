package com.indux.modules.clients.infra.duediligence;

import com.indux.core.infra.http.HttpClient;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;

@Component
public class DueDiligence {

    private final HttpClient http;
    private static final String API_URL = "http://192.168.0.10:8000/get-dueDiligent/";


    public DueDiligence (HttpClient http){
        this.http = http;
    }

    public DueDiligenceResponse call(String cnpj){
        try {
            var response = http.get(API_URL + cnpj, new HashMap<>());
            if (response.isSuccessful() && response.body() != null){
                // Ler os bytes primeiro para verificar o tipo de resposta
                byte[] responseBytes = response.body().bytes();
                
                // Verificar se é JSON (começa com '{')
                if (responseBytes.length > 0 && responseBytes[0] == '{') {
                    // Resposta é JSON
                    String responseBody = new String(responseBytes, "UTF-8");
                    var object = new JSONObject(responseBody);
                    return DueDiligenceResponse.fromJson(object);
                } else {
                    // Resposta é PDF (binário) - converter para base64
                    String base64Content = Base64.getEncoder().encodeToString(responseBytes);
                    return DueDiligenceResponse.fromBase64(cnpj, base64Content);
                }
            }
        } catch (Exception e){
            throw new RuntimeException("Serviço de DueDiligence indisponível: " + e.toString());
        }
        return null;
        }

}

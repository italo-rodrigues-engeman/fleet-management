package com.indux.modules.ppu.infra.mio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.infra.http.HttpClient;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MioApiGatewayImpl implements MioApiGateway {

    private static final String AUTH_URL = "https://mio.app.br/api/v1/authenticate";
    private static final String DATA_URL = "https://mio.app.br/api/v1/lgp-reports";
    private static final String TIME_SHEET_URL = "https://mio.app.br/api/v1/int-timesheet-get";

    @Value("${mio.username}")
    private String username;

    @Value("${mio.password}")
    private String password;

    @Value("${mio.token-ttl-seconds:2700}")
    private long tokenTtlSeconds;

    private final HttpClient httpClient;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    private final ReentrantLock tokenLock = new ReentrantLock();
    private volatile String cachedToken;
    private volatile long tokenExpiresAtMillis;

    public MioApiGatewayImpl(HttpClient httpClient, OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String authenticate() throws IOException {
        Request request = new Request.Builder()
                .url(AUTH_URL)
                .header("Authorization", Credentials.basic(username, password))
                .post(RequestBody.create(new byte[0]))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro ao autenticar com MIO. Código: " + response.code());
            }
            var body = response.body();
            if (body == null) throw new IOException("MIO authenticate sem body");
            JsonNode json = objectMapper.readTree(body.string());
            var token = json.path("token").asText();
            if (token == null || token.isBlank()) throw new IOException("MIO authenticate retornou token vazio");
            return token;
        }
    }

    private String getValidToken() throws IOException {
        long now = System.currentTimeMillis();
        var token = cachedToken;
        if (token != null && !token.isBlank() && now < tokenExpiresAtMillis) return token;

        tokenLock.lock();
        try {
            now = System.currentTimeMillis();
            token = cachedToken;
            if (token != null && !token.isBlank() && now < tokenExpiresAtMillis) return token;

            var newToken = authenticate();
            cachedToken = newToken;
            tokenExpiresAtMillis = now + Duration.ofSeconds(tokenTtlSeconds).toMillis();
            return newToken;
        } finally {
            tokenLock.unlock();
        }
    }

    private void invalidateToken() {
        cachedToken = null;
        tokenExpiresAtMillis = 0L;
    }

    private JsonNode parseJsonPath(Response response, String path) throws IOException {
        var body = response.body();
        if (body == null) throw new IOException("MIO response sem body");
        return objectMapper.readTree(body.string()).path(path);
    }

    @Override
    public JsonNode fetchBoardedEmployeeData(String token, String initialDate, String finalDate) throws IOException {
        var tk = (token == null || token.isBlank()) ? getValidToken() : token;

        Map<String, Object> body = Map.of("periodo_inicio", initialDate, "periodo_fim", finalDate);

        try (Response response = httpClient.post(DATA_URL, Map.of("Authorization", "Bearer " + tk), body)) {
            if (response.code() == 401) {
                invalidateToken();
                tk = getValidToken();
            } else if (!response.isSuccessful()) {
                throw new IOException("Erro ao obter dados do MIO. Código: " + response.code());
            } else {
                return parseJsonPath(response, "history");
            }
        }

        try (Response retry = httpClient.post(DATA_URL, Map.of("Authorization", "Bearer " + tk), body)) {
            if (!retry.isSuccessful()) {
                throw new IOException("Erro ao obter dados do MIO (retry). Código: " + retry.code());
            }
            return parseJsonPath(retry, "history");
        }
    }

    @Override
    public JsonNode fetchBoardedEmployeeWithStatus(String token, String initialDate, String finalDate) throws IOException {
        var tk = (token == null || token.isBlank()) ? getValidToken() : token;

        Map<String, Object> body = Map.of("dt_inicio", initialDate, "dt_fim", finalDate);

        try (Response response = httpClient.post(TIME_SHEET_URL, Map.of("Authorization", "Bearer " + tk), body)) {
            if (response.code() == 401) {
                invalidateToken();
                tk = getValidToken();
            } else if (!response.isSuccessful()) {
                throw new IOException("Erro ao obter dados do MIO. Status: " + response.code());
            } else {
                return parseJsonPath(response, "data");
            }
        }

        try (Response retry = httpClient.post(TIME_SHEET_URL, Map.of("Authorization", "Bearer " + tk), body)) {
            if (!retry.isSuccessful()) {
                throw new IOException("Erro ao obter dados do MIO (retry). Status: " + retry.code());
            }
            return parseJsonPath(retry, "data");
        }
    }
}

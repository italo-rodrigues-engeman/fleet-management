package com.indux.core.infra.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
class HttpClientImpl implements HttpClient {
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public HttpClientImpl(ObjectMapper objectMapper) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.mapper = objectMapper;
    }

    @Override
    public Response get(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).get();
        headers.forEach(builder::addHeader);
        return client.newCall(builder.build()).execute();
    }

    @Override
    public Response post(String url, Map<String, String> headers, Map<String, Object> body) throws IOException {
        RequestBody requestBody = RequestBody.create(
                mapper.writeValueAsString(body),
                MediaType.parse("application/json")
        );
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        headers.forEach(builder::addHeader);
        return client.newCall(builder.build()).execute();
    }

    @Override
    public Response postMultipart(String url, Map<String, String> headers, String paramName, String fileName, InputStream fileStream, String mimeType) throws IOException {
        byte[] fileBytes = fileStream.readAllBytes();
        
        RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(mimeType));
        
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(paramName, fileName, fileBody)
                .build();
        
        Request.Builder builder = new Request.Builder().url(url).post(requestBody);
        headers.forEach(builder::addHeader);
        
        return client.newCall(builder.build()).execute();
    }

    @Override
    public Response put(String url, Map<String, String> headers, Map<String, Object> body) throws IOException {
        RequestBody requestBody = RequestBody.create(
                mapper.writeValueAsString(body),
                MediaType.parse("application/json")
        );
        Request.Builder builder = new Request.Builder().url(url).put(requestBody);
        headers.forEach(builder::addHeader);
        return client.newCall(builder.build()).execute();
    }

    @Override
    public Response delete(String url, Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url).delete();
        headers.forEach(builder::addHeader);
        return client.newCall(builder.build()).execute();
    }
}

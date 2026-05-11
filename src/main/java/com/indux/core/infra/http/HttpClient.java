package com.indux.core.infra.http;

import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface HttpClient {
    Response get(String url, Map<String, String> headers) throws IOException;

    Response post(String url, Map<String, String> headers, Map<String, Object> body) throws IOException;
    
    Response postMultipart(String url, Map<String, String> headers, String paramName, String fileName, InputStream fileStream, String mimeType) throws IOException;

    Response put(String url, Map<String, String> headers, Map<String, Object> body) throws IOException;

    Response delete(String url, Map<String, String> headers) throws IOException;
}
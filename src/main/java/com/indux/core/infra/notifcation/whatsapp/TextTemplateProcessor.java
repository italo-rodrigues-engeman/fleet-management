package com.indux.core.infra.notifcation.whatsapp;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TextTemplateProcessor {
    public static String renderTemplate(String path, Map<String, Object> variables) {
        try {
            var resource = new ClassPathResource(path);
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return StringSubstitutor.replace(content, variables);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar template: " + path, e);
        }
    }
}

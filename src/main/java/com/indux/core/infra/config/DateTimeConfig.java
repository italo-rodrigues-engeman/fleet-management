package com.indux.core.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Configuration
public class DateTimeConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToInstantConverter());
        registry.addConverter(new StringToLocalDateTimeConverter());
    }

    private static class StringToInstantConverter implements Converter<String, Instant> {
        @Override
        public Instant convert(String source) {
            try {
                return Instant.parse(source);
            } catch (Exception e) {
                try {
                    return LocalDate.parse(source).atStartOfDay().toInstant(ZoneOffset.UTC);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Formato de data inválido. Use ISO-8601 ou YYYY-MM-DD");
                }
            }
        }
    }

    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.isBlank()) return null;
            try {
                // Tente ISO com hora
                return LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                try {
                    // Aceitar apenas data e assumir início do dia
                    return LocalDate.parse(source, DateTimeFormatter.ISO_DATE).atStartOfDay();
                } catch (Exception ex) {
                    // Tente um formato comum "yyyy-MM-dd HH:mm:ss"
                    try {
                        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        return LocalDateTime.parse(source, f);
                    } catch (Exception ex2) {
                        throw new IllegalArgumentException("Formato de data/hora inválido. Use ISO-8601, YYYY-MM-DD ou yyyy-MM-dd HH:mm:ss");
                    }
                }
            }
        }
    }
} 
package com.indux.core.infra.config;

import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
public class MongoConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToInstantConverter());
    }

    @Bean
    @Primary
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new DateToDocumentConverter());
        converters.add(new DocumentToDateConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context, MongoCustomConversions conversions) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
        
        // Remover o _class
        mappingConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        // Usar as conversões personalizadas
        mappingConverter.setCustomConversions(conversions);
        
        // Inicializar o conversor
        mappingConverter.afterPropertiesSet();
        
        return mappingConverter;
    }

    private static class StringToInstantConverter implements Converter<String, Instant> {
        @Override
        public Instant convert(String source) {
            try {
                // Tenta primeiro como ISO-8601 completo
                return Instant.parse(source);
            } catch (Exception e) {
                try {
                    // Se falhar, tenta como data simples (YYYY-MM-DD)
                    return LocalDate.parse(source).atStartOfDay().toInstant(ZoneOffset.UTC);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("Formato de data inválido. Use ISO-8601 ou YYYY-MM-DD");
                }
            }
        }
    }

    @WritingConverter
    static class DateToDocumentConverter implements Converter<Date, Document> {
        @Override
        public Document convert(Date source) {
            if (source == null) {
                return null;
            }
            Document document = new Document();
            document.put("$data", source.toInstant().toString());
            return document;
        }
    }

    @ReadingConverter
    static class DocumentToDateConverter implements Converter<Document, Date> {
        @Override
        public Date convert(Document document) {
            if (document == null) {
                return null;
            }
            
            if (document.containsKey("$data")) {
                Object dateValue = document.get("$data");
                if (dateValue instanceof String) {
                    try {
                        return Date.from(Instant.parse((String) dateValue));
                    } catch (Exception e) {
                        return new Date();
                    }
                } else if (dateValue instanceof Long) {
                    return new Date((Long) dateValue);
                }
            }
            
            return new Date();
        }
    }

    @ReadingConverter
    static class ObjectToDateConverter implements Converter<Object, Date> {
        @Override
        public Date convert(Object source) {
            if (source == null) {
                return null;
            }
            
            if (source instanceof Document) {
                Document doc = (Document) source;
                if (doc.containsKey("$data")) {
                    Object dateValue = doc.get("$data");
                    if (dateValue instanceof String) {
                        try {
                            return Date.from(Instant.parse((String) dateValue));
                        } catch (Exception e) {
                            return new Date();
                        }
                    } else if (dateValue instanceof Long) {
                        return new Date((Long) dateValue);
                    }
                }
            } else if (source instanceof String) {
                try {
                    return Date.from(Instant.parse((String) source));
                } catch (Exception e) {
                    return new Date();
                }
            } else if (source instanceof Long) {
                return new Date((Long) source);
            }
            
            return new Date();
        }
    }
} 
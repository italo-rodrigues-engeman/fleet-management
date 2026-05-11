package com.indux.core.infra.filestorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC para servir arquivos estáticos.
 * Esta classe configura o Spring para servir os arquivos armazenados
 * através de URLs HTTP.
 * 
 * Características:
 * - Mapeia o diretório de uploads para URLs HTTP
 * - Permite acesso aos arquivos via web
 * - Configurável através de propriedades
 * 
 * Exemplo de URL:
 * http://localhost:8080/uploads/arquivo.jpg
 * 
 * @author Indux Team
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Localização do diretório de armazenamento.
     * Valor injetado da propriedade storage.location
     */
    @Value("${storage.location}")
    private String storageLocation;

    /**
     * Configura os manipuladores de recursos para servir arquivos estáticos.
     * Mapeia o diretório de uploads para o padrão de URL /uploads/**
     * 
     * @param registry registro de manipuladores de recursos
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + storageLocation.replace("\\", "/") + "/");

        registry
                .addResourceHandler("/whatsapp/**")
                .addResourceLocations("file:///" + storageLocation.replace("\\", "/") + "/whatsapp_media" +"/");
    }

}

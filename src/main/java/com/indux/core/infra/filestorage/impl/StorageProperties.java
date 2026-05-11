package com.indux.core.infra.filestorage.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Classe de configuração para propriedades do serviço de armazenamento.
 * Esta classe é responsável por carregar as configurações do arquivo de
 * propriedades
 * e disponibilizá-las para o serviço de armazenamento.
 * <p>
 * As propriedades são carregadas do prefixo "storage" no arquivo de
 * configuração.
 * Exemplo de configuração no application.properties:
 * storage.location=upload-dir
 * @author Indux Team
 */
@Component
@ConfigurationProperties("storage")
public class StorageProperties {
    /**
     * Localização do diretório de armazenamento.
     * Valor padrão: "upload-dir"
     */
    private String location = "upload-dir";

    /**
     * Obtém a localização do diretório de armazenamento.
     * @return caminho do diretório de armazenamento
     */
    public String getLocation() {
        return location;
    }

    /**
     * Define a localização do diretório de armazenamento.
     * @param location novo caminho do diretório de armazenamento
     */
    public void setLocation(String location) {
        this.location = location;
    }
}
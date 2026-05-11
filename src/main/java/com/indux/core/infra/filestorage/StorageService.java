package com.indux.core.infra.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Interface principal para serviços de armazenamento de arquivos.
 * Define as operações básicas para manipulação de arquivos, independente do
 * tipo de armazenamento utilizado.
 * <p>
 * Esta interface pode ser implementada para diferentes tipos de armazenamento:
 * - Sistema de arquivos local
 * - FTP
 * - Cloud Storage (S3, Azure Blob, etc)
 * - Outros sistemas de armazenamento
 * @author Indux Team
 */
public interface StorageService {
    /**
     * Inicializa o serviço de armazenamento.
     * Deve ser chamado antes de qualquer operação de armazenamento.
     * Responsável por criar diretórios necessários e validar configurações.
     */
    void init();

    /**
     * Armazena um arquivo no local padrão.
     * @param file o arquivo a ser armazenado
     * @return o caminho absoluto do arquivo armazenado
     */
    Path store(MultipartFile file);

    /**
     * Armazena um arquivo em um subdiretório específico.
     * @param file   o arquivo a ser armazenado
     * @param subDir o subdiretório onde o arquivo será armazenado
     * @return o caminho absoluto do arquivo armazenado
     */
    Path store(MultipartFile file, String subDir);

    /**
     * Armazena um arquivo com nome personalizado em um subdiretório específico.
     * @param file     o arquivo a ser armazenado
     * @param subDir   o subdiretório onde o arquivo será armazenado
     * @param filename o nome personalizado para o arquivo
     * @return o caminho absoluto do arquivo armazenado
     */
    Path store(MultipartFile file, String subDir, String filename);

    /**
     * Lista todos os arquivos armazenados.
     * @return um Stream contendo os caminhos de todos os arquivos
     */
    Stream<Path> loadAll();

    /**
     * Carrega um arquivo específico pelo nome.
     * @param filename o nome do arquivo a ser carregado
     * @return o caminho do arquivo
     */
    Path load(String filename);

    /**
     * Carrega um arquivo como Resource para download.
     * @param filename o nome do arquivo a ser carregado
     * @return o Resource do arquivo
     */
    Resource loadAsResource(String filename);

    /**
     * Remove todos os arquivos armazenados.
     */
    void deleteAll();

    /**
     * Retorna o caminho raiz do armazenamento.
     * @return o Path do diretório raiz
     */
    Path getRootLocation();

    /**
     * Retorna o caminho do armazenamento de uma miniatura.
     * @return o Path do diretório raiz
     */
    Path createThumbnail(String filename, int width, int height);

    /**
     * Carrega um arquivo como InputStream.
     * Ideal para operações que não precisam de arquivo físico,
     * como envio de e-mails ou processamento em memória.
     *
     * @param path o caminho relativo do arquivo no storage
     * @return InputStream do arquivo
     */
    InputStream loadAsStream(String path);

    void deleteFile(Path path) throws IOException;
}

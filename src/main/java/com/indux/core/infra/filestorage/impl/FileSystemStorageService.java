package com.indux.core.infra.filestorage.impl;

import com.indux.core.infra.compress.ImageProcessor;
import com.indux.core.infra.filestorage.exception.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService extends AbstractStorageService {
    @Autowired
    public FileSystemStorageService(StorageProperties properties, ImageProcessor imageProcessor) {
        super(Paths.get(properties.getLocation()).toAbsolutePath().normalize(), imageProcessor);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    /**
     * Armazena um arquivo no sistema de arquivos. Varia de acordo com a implementação.
     * @param inputStream stream de entrada do arquivo
     * @param targetPath  caminho onde o arquivo será armazenado
     * @throws IOException se houver erro no armazenamento
     */
    @Override
    protected void storeFile(InputStream inputStream, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Carrega um arquivo como Resource.
     * @param file caminho do arquivo
     * @return Resource do arquivo
     * @throws MalformedURLException se a URL do arquivo for inválida
     */
    @Override
    protected Resource loadResource(Path file) throws MalformedURLException {
        return new UrlResource(file.toUri());
    }

    /**
     * Lista todos os arquivos armazenados.
     * @return Stream contendo os caminhos de todos os arquivos
     * @throws StorageException se houver erro ao listar os arquivos
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            return listFiles(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    /**
     * Lista arquivos em um diretório específico.
     * @param dir diretório a ser listado
     * @return Stream de caminhos dos arquivos
     * @throws IOException se houver erro na listagem
     */
    @Override
    protected Stream<Path> listFiles(Path dir) throws IOException {
        return Files.walk(dir, 1)
                .filter(path -> !path.equals(dir))
                .map(dir::relativize);
    }

    /**
     * Remove todos os arquivos armazenados.
     * @throws StorageException se houver erro ao deletar os arquivos
     */
    @Override
    public void deleteAll() {
        try {
            FileSystemUtils.deleteRecursively(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Failed to delete all files", e);
        }
    }



    /**
     * Remove um arquivo específico.
     * @param path caminho do arquivo a ser removido
     * @throws IOException se houver erro na remoção
     */
    @Override
    public void deleteFile(Path path) throws IOException {
        Files.deleteIfExists(path);
    }
}

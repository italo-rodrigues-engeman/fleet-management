package com.indux.core.infra.filestorage.impl;

import com.indux.core.infra.compress.ImageProcessor;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.filestorage.exception.StorageException;
import com.indux.core.infra.filestorage.exception.StorageFileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractStorageService implements StorageService {
    protected final Path rootLocation;
    protected final ImageProcessor imageProcessor;

    /**
     * Construtor base para serviços de armazenamento.
     * @param rootLocation   localização raiz para armazenamento
     * @param imageProcessor componente para compressão e miniaturas
     */
    protected AbstractStorageService(Path rootLocation, ImageProcessor imageProcessor) {
        this.rootLocation = rootLocation;
        this.imageProcessor = imageProcessor;
    }

    @Override
    public InputStream loadAsStream(String path) {
        try {
            Path file = getRootLocation().resolve(path);
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new StorageException("Não foi possível ler o arquivo: " + path, e);
        }
    }

    @Override
    public Path store(MultipartFile file) {
        return store(file, null, null);
    }

    @Override
    public Path store(MultipartFile file, String subDir) {
        return store(file, subDir, null);
    }

    @Override
    public Path store(MultipartFile file, String subDir, String filename) {
        validateFileNotEmpty(file);
        String original = extractOriginalFilename(file);
        String ext = extractExtension(original);
        String resolvedName = determineFilename(original, filename, ext);
        String safeName = sanitizeFilename(resolvedName);

        Path target = buildTargetPath(subDir, safeName);
        validateTargetPath(target);

        try (InputStream in = file.getInputStream()) {
            if (isImage(safeName)) {
                compressAndStore(in, target);
            } else {
                storeFile(in, target);
            }
            return target;
        } catch (IOException e) {
            throw new StorageException("Falha ao armazenar o arquivo " + safeName, e);
        }
    }

    @Override
    public Path createThumbnail(String filename, int width, int height) {
        Path original = load(filename);
        if (!isImage(filename)) {
            throw new StorageException("Não é possível criar miniatura de uma arquivo que não é imagem: " + filename);
        }
        Path thumb = original.getParent().resolve("thumb-" + original.getFileName());
        validateTargetPath(thumb);
        try {
            byte[] data = Files.readAllBytes(original);
            byte[] thumbData = imageProcessor.createThumbnail(data, width, height);
            Files.write(thumb, thumbData);
            return thumb;
        } catch (IOException ex) {
            throw new StorageException("Falha ao criar miniatura para " + filename, ex);
        }
    }

    @Override
    public Path load(String filename) {
        return buildTargetPath(null, filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = loadResource(file);
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Não foi possível ler o arquivo: " + filename);
            }
        } catch (IOException e) {
            throw new StorageFileNotFoundException("Não foi possível ler o arquivo: " + filename, e);
        }
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }

    protected void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("Não é possível armazenar algo vazio.");
        }
    }

    protected String extractOriginalFilename(MultipartFile file) {
        return Objects.requireNonNull(file.getOriginalFilename(), "Nome do arquivo ausente.");
    }

    protected String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 ? filename.substring(dot) : "");
    }

    protected String determineFilename(String original, String requested, String ext) {
        if (requested == null || requested.isBlank()) {
            return StringUtils.cleanPath(original);
        }
        String base = requested.contains(".")
                ? requested.substring(0, requested.lastIndexOf('.'))
                : requested;
        return base + ext;
    }

    protected String sanitizeFilename(String filename) {
        String clean = StringUtils.cleanPath(filename);
        if (clean.contains("..")) {
            throw new StorageException("O nome do arquivo contém uma sequência de caminho inválida: " + clean);
        }
        return clean;
    }

    protected Path buildTargetPath(String subDir, String filename) {
        Path dir = (subDir == null || subDir.isBlank())
                ? rootLocation
                : rootLocation.resolve(subDir).normalize();
        return dir.resolve(filename).normalize();
    }

    protected void validateTargetPath(Path path) {
        if (!path.startsWith(rootLocation)) {
            throw new StorageException("Não é possível armazenar o arquivo fora do local raiz.");
        }
    }

    protected boolean isImage(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".png") || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }

    private void compressAndStore(InputStream in, Path target) throws IOException {
        byte[] data = toByteArray(in);
        byte[] compressed = imageProcessor.compress(data, 0, 0);
        Files.createDirectories(target.getParent());
        Files.write(target, compressed);
    }

    private byte[] toByteArray(InputStream in) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        }
    }

    protected abstract void storeFile(InputStream inputStream, Path targetPath) throws IOException;

    protected abstract Resource loadResource(Path file) throws IOException;

    protected abstract Stream<Path> listFiles(Path dir) throws IOException;

}
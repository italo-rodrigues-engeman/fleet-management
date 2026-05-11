package com.indux.core.infra.compress.impl;

import com.indux.core.infra.compress.ImageProcessor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ThumbnailatorImageProcessor implements ImageProcessor {
    @Override
    public byte[] compress(byte[] imageData, int maxWidth, int maxHeight) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            var builder = Thumbnails.of(new ByteArrayInputStream(imageData));
            if (maxWidth > 0 && maxHeight > 0) {
                builder.size(maxWidth, maxHeight);
            } else {
                builder.scale(1.0);
            }
            builder.outputQuality(0.8)
                    .toOutputStream(bytes);
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao comprimir imagem", e);
        }
    }

    @Override
    public byte[] createThumbnail(byte[] imageData, int width, int height) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(imageData))
                    .size(width, height)
                    .toOutputStream(bytes);
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar thumbnail", e);
        }
    }
}
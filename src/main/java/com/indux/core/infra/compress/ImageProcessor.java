package com.indux.core.infra.compress;

public interface ImageProcessor {
    /**
     * Comprime uma imagem mantendo sua proporção original.
     * A imagem será redimensionada para caber dentro das dimensões máximas
     * especificadas,
     * mantendo a proporção original.
     * @param imageData dados da imagem original em bytes
     * @param maxWidth  largura máxima desejada
     * @param maxHeight altura máxima desejada
     * @return dados da imagem comprimida em bytes
     */
    byte[] compress(byte[] imageData, int maxWidth, int maxHeight);

    /**
     * Cria uma miniatura da imagem com dimensões específicas.
     * A imagem será redimensionada exatamente para as dimensões especificadas, podendo resultar em distorção da proporção original.
     * @param imageData dados da imagem original em bytes
     * @param width     largura desejada da miniatura
     * @param height    altura desejada da miniatura
     * @return dados da miniatura em bytes
     */
    byte[] createThumbnail(byte[] imageData, int width, int height);
}
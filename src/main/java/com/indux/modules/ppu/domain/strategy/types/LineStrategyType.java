package com.indux.modules.ppu.domain.strategy.types;

/**
 * Enum que define os tipos de LineStrategy disponíveis.
 * Cada valor corresponde a uma strategy que calcula quantidade/valor da linha.
 */
public enum LineStrategyType {
    /**
     * Strategy padrão: colaborador vale 1.
     * Quantidade e valor permanecem inalterados.
     */
    UNIQUE,

    /**
     * Strategy para colaborador em <b>dobra</b>: colaborador vale 2.
     * A quantidade efetiva é multiplicada por 2.
     */
    DOUBLE_FOLD,

    /**
     * Strategy para colaborador que vale metade: colaborador vale 1/2.
     * A quantidade efetiva é dividida por 2.
     */
    HALF,

    /**
     * Strategy para redistribuição de valor de disponibilidade.
     * Se o serviço for "disposição", vale 0.
     * O valor (0.7) é atribuído ao serviço pai.
     */
    AVAILABLE_REDISTRIBUTION;

    /**
     * Retorna o nome da strategy correspondente para uso no registry.
     */
    public String getStrategyName() {
        return name();
    }
}

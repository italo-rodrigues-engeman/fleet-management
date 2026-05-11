package com.indux.modules.union_registration.domain.enums;

public enum TipoInstrumento {
    ACT("ACT"),
    CCT("CCT");
    
    private final String valor;
    
    TipoInstrumento(String valor) {
        this.valor = valor;
    }
    
    public String getValor() {
        return valor;
    }
    
    public static TipoInstrumento fromString(String valor) {
        for (TipoInstrumento tipo : TipoInstrumento.values()) {
            if (tipo.valor.equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de instrumento inválido: " + valor);
    }
}


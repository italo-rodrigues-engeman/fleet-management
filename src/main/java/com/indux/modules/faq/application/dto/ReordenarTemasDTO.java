package com.indux.modules.faq.application.dto;

import java.util.List;

public record ReordenarTemasDTO(
        List<TemaOrdem> temas
) {
    
    public record TemaOrdem(
            Long id,
            Integer novaOrdem
    ) {}
}

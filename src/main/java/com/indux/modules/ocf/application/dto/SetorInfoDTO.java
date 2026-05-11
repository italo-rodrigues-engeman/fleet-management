package com.indux.modules.ocf.application.dto;

import com.indux.modules.faq.domain.entities.Setor;

public record SetorInfoDTO(
        Long id,
        String nome,
        Boolean status
) {
    
    public static SetorInfoDTO fromSetor(Setor setor) {
        if (setor == null) {
            return null;
        }
        return new SetorInfoDTO(
                setor.getId(),
                setor.getNome(),
                setor.getStatus()
        );
    }
} 
package com.indux.modules.ppu.application.dtos.item;

import com.mongodb.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record SteelCableDTO(
        @Nullable String id,
        String numero,
        String numeroPPU,
        String nome,
        String descricao,
        String unidadeMedida,
        Double valorItem,
        Double fatorItem,
        List<String> plataformas,
        @Nullable MultipartFile certificado,
        @Nullable String certificadoUri,
        @Nullable Integer totalPrevisto

) {

    public SteelCableDTO copyWith(String certificadoUri){
        return new SteelCableDTO(
                this.id,
                this.numero(),
                this.numeroPPU(),
                this.nome(),
                this.descricao(),
                this.unidadeMedida(),
                this.valorItem(),
                this.fatorItem(),
                this.plataformas(),
                this.certificado(),
                certificadoUri,
                this.totalPrevisto()

        );
    }
}

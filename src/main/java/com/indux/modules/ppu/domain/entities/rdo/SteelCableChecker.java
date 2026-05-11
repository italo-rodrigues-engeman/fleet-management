package com.indux.modules.ppu.domain.entities.rdo;

import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

public record SteelCableChecker(
        @NotNull(message = "O ID do cabo de aço deve ser preenchido.", groups = RDOCreate.class) String id,
        String numero,
        String nome,
        @Nullable  String justificativa,
        String unidadeMedida,
        Double valorItem,
        Double fatorItem,
        @NotNull(message = "Campo de quantidade do cabo de aço não foi preenchido.", groups = RDOCreate.class) Double quantidade,
        @Nullable Integer totalPrevisto,
        String lineID
) {

    public SteelCableChecker copyWithTotalPlanned(

            Integer totalPrevisto
    ) {
        return new SteelCableChecker(
                this.id,
                this.numero,
                this.nome,
                this.justificativa,
                this.unidadeMedida,
                this.valorItem,
                this.fatorItem,
                this.quantidade,
                totalPrevisto != null ? totalPrevisto : this.totalPrevisto,
                this.lineID
        );
    }

    public SteelCableChecker copyWithLineID(String newLineID) {
        return new SteelCableChecker(
                this.id,
                this.numero,
                this.nome,
                this.justificativa,
                this.unidadeMedida,
                this.valorItem,
                this.fatorItem,
                this.quantidade,
                this.totalPrevisto,
                newLineID
        );
    }
}

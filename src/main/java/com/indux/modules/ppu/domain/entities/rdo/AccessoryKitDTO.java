package com.indux.modules.ppu.domain.entities.rdo;

import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AccessoryKitDTO(
        @NotNull(message = "Campo de quantidade do cabo de aço não foi preenchido.", groups = RDOCreate.class) String id,
        String numero,
        String numeroPPU,
        String nome,
        String descricaoModelo,
        String unidadeMedida,
        Double valorItem,
        Double fatorItem,

        @NotNull(message = "A disponibilidade do kit de acessórios não foi preenchido.", groups = RDOCreate.class)  Boolean disponivel,
        @NotNull(message = "O kit de acessórios deve ser avisado se não estiver completo.", groups = RDOCreate.class)  Boolean completo,
        @Nullable  String justificativa,
        List<MeasurementForecast> totalPrevisto,
        @Nullable Integer totalPrevistoRDO,
        Double quantidade,
        String lineID
) {

    public AccessoryKitDTO copyWithTotalPlanned(
            Integer totalPrevistoRDO
    ) {
        return new AccessoryKitDTO(
                this.id,
                this.numero,
                this.numeroPPU,
                this.nome,
                this.descricaoModelo,
                this.unidadeMedida,
                this.valorItem,
                this.fatorItem,
                this.disponivel,
                this.completo,
                this.justificativa,
                this.totalPrevisto,
                totalPrevistoRDO != null ? totalPrevistoRDO : this.totalPrevistoRDO,
                this.quantidade,
                lineID
        );
    }
    public AccessoryKitDTO copyWithID(
            String id
    ) {
        return new AccessoryKitDTO(
                this.id,
                this.numero,
                this.numeroPPU,
                this.nome,
                this.descricaoModelo,
                this.unidadeMedida,
                this.valorItem,
                this.fatorItem,
                this.disponivel,
                this.completo,
                this.justificativa,
                this.totalPrevisto,
               null,
                this.quantidade,
                id
        );
    }

}

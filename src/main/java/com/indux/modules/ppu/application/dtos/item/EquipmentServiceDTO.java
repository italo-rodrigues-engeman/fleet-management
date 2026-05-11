package com.indux.modules.ppu.application.dtos.item;

import com.indux.modules.ppu.domain.entities.rdo.EquipmentChecker;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record EquipmentServiceDTO(
        @NotNull(message = "ID da linha de equipamentos deve ser preenchido.", groups = RDOCreate.class) String id,
        @NotNull(message = "O número da linha de equipamento deve ser preenchido.", groups = EquipmentCreate.class) String numero,
        @NotNull(message = "O número da linha de equipamento deve ser preenchido.", groups = EquipmentCreate.class) String numeroPPU,
        @NotNull(message = "O nome da linha de equipamento deve ser preenchido.", groups = EquipmentCreate.class) String nome,
        @NotNull(message = "A unidade de medida deve ser preenchido.", groups = EquipmentCreate.class) String unidadeMedida,
        @NotNull(message = "O valor deve ser preenchido.", groups = EquipmentCreate.class) Double valor,
        @NotNull(message = "O fator deve ser preenchido.", groups = EquipmentCreate.class) Double fator,
        List<EquipmentDTO> equipamentos,
        @NotNull(message = "Campo de checagem dos equipamentos devem ser preenchidos.", groups = RDOCreate.class) List<EquipmentChecker> equipamentosChecker,
        @Nullable @NotNull(message = "A quantidade prevista deve ser preenchida.", groups = EquipmentCreate.class) List<MeasurementForecast> totalPrevisto,
        String tipoEquipamento,
        String equipamentoId
) {
    public interface EquipmentCreate {}

    public EquipmentServiceDTO copyWithID(String id){
       return new EquipmentServiceDTO(
                this.id,
                this.numero,
                this.numeroPPU,
                this.nome,
                this.unidadeMedida,
                this.valor,
                this.fator,
                this.equipamentos,
                this.equipamentosChecker,
                this.totalPrevisto,
                this.tipoEquipamento,
                id
        );
    }

}

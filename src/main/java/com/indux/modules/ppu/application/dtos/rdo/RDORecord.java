package com.indux.modules.ppu.application.dtos.rdo;


import com.indux.modules.ppu.application.dtos.item.*;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOCreate;
import com.indux.modules.ppu.application.dtos.rdo.itens.RDOEmployees;
import com.indux.modules.ppu.application.dtos.requests.RDOLineRequest;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOJustification;
import com.indux.modules.ppu.domain.entities.rdo.ClientEmployee;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RDORecord(
        String id,
        @Nullable ClientEmployee dadosCliente,
        String plataforma,
        String PPUid,
        LocalDate data,
        @NotNull(message = "O horário do colaborador deve ser preenchido.", groups = RDOCreate.class) List<ServiceItemDTO> servicos,
        List<EquipmentServiceDTO> equipamentos,
        List<AccessoryKitDTO> kitsAcessorios,
        List<SteelCableChecker> cabosDeAcos,
        List<CraneControlDTO> controleGuindaste,
        List<SteelCableControlDTO> controleCaboAco,
        String servicosRealizadosContratada,
        String registrosObservacao,
        List<RDOEmployees> trabalhoDesembarque,
        String platformService,
        @Nullable String justificativa,
        @Nullable CreateRDOJustification justificativaCoordenador,
        String justificativaHoraExtra,
        Long projetoId,
        List<RDOLineRequest> linhasRDO,
        Boolean duplicado
        ) {
    public interface RDOUpdate{}
    public RDORecord copyWithUpdatedItems(
            List<SteelCableChecker> cabosDeAcos,
            List<EquipmentServiceDTO> equipamentos
    ) {
        return new RDORecord(
                this.id,
                this.dadosCliente,
                this.plataforma,
                this.PPUid,
                this.data,
                this.servicos,
                equipamentos,
                this.kitsAcessorios,
                cabosDeAcos,
                this.controleGuindaste,
                this.controleCaboAco,
                this.servicosRealizadosContratada,
                this.registrosObservacao,
                this.trabalhoDesembarque,
                this.platformService,
                this.justificativa,
                this.justificativaCoordenador,
                this.justificativaHoraExtra,
                this.projetoId,
                this.linhasRDO,
                this.duplicado
        );
    }

}

package com.indux.modules.ppu.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.generic.DateRange;
import com.indux.modules.ppu.application.dtos.RDOFrequencyDTO;
import com.indux.modules.ppu.application.dtos.item.*;
import com.indux.modules.ppu.domain.entities.item.*;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.domain.entities.ppu.TotalBalance;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record PPURequest(
        @NotNull(message = "O tipo da PPU deve ser informado.", groups = OnCreate.class) PPUType tipo,
        @NotNull(message = "O ID da regional deve ser informado.", groups = OnCreate.class) Long regionalID,
        @NotNull(message = "O nome da regional deve ser informado.", groups = OnCreate.class) String regionalName,
        @NotNull(message = "ID do cliente é obrigatório.", groups = OnCreate.class) Long clienteID,
        @NotNull(message = "ID do contrato é obrigatório.", groups = OnCreate.class) Integer contratoID,
        @NotEmpty(message = "A lista de plataformas não pode ser vazia.", groups = OnCreate.class) List<String> plataformas,
        Map<String, RDOFrequencyDTO> frequenciasPlataformas,
        @NotNull(message = "A PPU deve conter um responsável", groups = OnCreate.class) String responsavel,
        @NotNull(message = "A PPU deve conter uma data de inicio e uma de finalização", groups = OnCreate.class) DateRange dataPPU,
        @NotNull(message = "Campo obrigatório", groups = OnCreate.class) Boolean vinteQuatroHoras,
        @NotNull(message = "A PPU deve conter um apelido", groups = OnCreate.class) String apelido,
        @NotNull(message = "A PPU deve conter um ID do projeto", groups = OnCreate.class) Long projetoId,
        Double valorSinaleiros,
        Integer qtdSinaleiros,
        List<TeamLeader> caboDeTurma,
        @NotBlank(message = "Uma PPU necessita de descrição.", groups = OnCreate.class) String descricao,
        Boolean samcObrigatorio,
        @JsonProperty("configAuditoria") AuditableConfig auditableConfig,
        MeasurementPeriod periodoMedicao,


        @NotEmpty(message = "Uma PPU deve conter pelo menos um serviço.", groups = ServicePart.class) List<ServiceItemDTO> servicos,
        @NotNull(message = "O horário dos turnos deverão ser informados.", groups = ShiftPart.class) List<ShiftSchedule> horarios,
        List<EquipmentServiceDTO> equipamentos,
        List<SteelCableDTO> cabosAcos,
        List<AccessoryKitDTO> kitAcessorios,
        List<CraneControlDTO> controleGuindaste,
        List<SteelCableControlDTO> controleCaboAco,
        List<MeasurementForecast> totalPrevistoCabosAco,
        List<TotalBalance> saldoTotal,
        List<AvailableTypeRequest> disposicaoTipos,
        List<LineDTO> linhas,
        @NotNull(message = "É obrigatório informar se a PPU possui supervisor a bordo.", groups = OnCreate.class)
        Boolean temSupervisorBordo,
        @NotNull(message = "É obrigatório informar se a PPU permite duplicação de RDO.", groups = OnCreate.class)
        Boolean permiteDuplicarRDO
) {
    public interface OnCreate {
    }

    public interface ShiftPart {}
    public interface ServicePart {}


}

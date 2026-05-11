package com.indux.modules.ppu.infra.mapper.ticket;

import com.indux.modules.ppu.application.dtos.response.ChangeTicketResponseDTO;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ChangeTicketResponseMapper {

    @Mapping(source = "createdAt", target = "criadoEm")
    @Mapping(source = "createdBy", target = "criadoPor")
    @Mapping(source = "reason", target = "motivo")
    @Mapping(source = "fromVersion", target = "versaoOrigem")
    @Mapping(source = "toVersion", target = "versaoDestino")
    @Mapping(source = "actor", target = "ator")
    @Mapping(source = "source", target = "origem")
    @Mapping(source = "item", target = "itens")
    @Mapping(source = "diff", target = "diferencas")
    @Mapping(source = "contract", target = "contrato")
    @Mapping(source = "regionalName", target = "regionalNome")
    @Mapping(source = "approvedAt", target = "aprovadoEm")
    @Mapping(source = "approvedBy", target = "aprovadoPor")
    @Mapping(source = "approverName", target = "nomeAprovador")
    @Mapping(source = "rejectionReason", target = "motivoRejeicao")
    ChangeTicketResponseDTO toResponseDTO(ChangeTicket ticket);
    
    @Mapping(source = "userId", target = "usuarioId")
    @Mapping(source = "name", target = "nome")
    ChangeTicketResponseDTO.AtorDTO mapActor(ChangeTicket.Actor actor);
    
    @Mapping(source = "service", target = "servico")
    ChangeTicketResponseDTO.OrigemDTO mapSource(ChangeTicket.Source source);
    
    @Mapping(source = "itemId", target = "itemId")
    @Mapping(source = "lineType", target = "tipoLinha")
    @Mapping(source = "platform", target = "plataforma")
    @Mapping(source = "quantity", target = "quantidade")
    ChangeTicketResponseDTO.ItemMudancaDTO mapChangeItem(ChangeTicket.ChangeItem item);

    @Mapping(source = "operation", target = "operacao")
    @Mapping(source = "path", target = "caminho")
    @Mapping(source = "from", target = "de")
    @Mapping(source = "to", target = "para")
    ChangeTicketResponseDTO.LogHistoricoDTO mapHistoryLog(ChangeTicket.HistoryLog log);
    
    List<ChangeTicketResponseDTO.ItemMudancaDTO> mapChangeItems(List<ChangeTicket.ChangeItem> items);
    
    List<ChangeTicketResponseDTO.LogHistoricoDTO> mapHistoryLogs(List<ChangeTicket.HistoryLog> logs);
}
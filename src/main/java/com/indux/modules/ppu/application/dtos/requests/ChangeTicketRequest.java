package com.indux.modules.ppu.application.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChangeTicketRequest(
        @NotNull(message ="ID da PPU é obrigatório.") String ppuId,
        @JsonProperty("razao") String reason,
        List<ChangeTicketItemRequest> items
) {

    public record ChangeTicketItemRequest(
            @JsonProperty("itemId") @NotNull(message = "ID do item é obrigatório") String itemId,
            @JsonProperty("tipoLinha") @NotNull(message = "Tipo da linha é obrigatório") String lineType, // SERVICE, EQUIPMENT, STEEL_CABLE, ACCESSORY_KIT
            @JsonProperty("plataforma") String platform,
            @JsonProperty("quantidade") @NotNull(message = "Quantidade é obrigatória") Integer quantity
    ) {}
}

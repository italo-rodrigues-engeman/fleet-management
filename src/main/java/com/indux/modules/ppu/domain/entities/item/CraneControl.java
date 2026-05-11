package com.indux.modules.ppu.domain.entities.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.modules.ppu.application.dtos.item.CraneControlDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Representa o controle de operação dos guindastes na plataforma.
 * Armazenado dentro da PPU.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CraneControl {
    private String id;
    @JsonProperty("localizacao")
    private String location;
    @JsonProperty("ultimaLubrificacao")
    private LocalDateTime lastLubrication;
    @JsonProperty("operacional")
    private Boolean operational;
    @JsonProperty("horimetro")
    private String hourMeter;
    @JsonProperty("checklist")
    private Boolean checklist;
    @JsonProperty("colaboradorChecklist")
    List<SimpleEmployeeDTO> colaboradorChecklist;
    @JsonProperty("inoperancia")
    private String inoperabilityResponsibility;
    @JsonProperty("justificativaInoperancia")
    private String justification;
    @JsonProperty("plataforma")
    private String platform;
    @JsonProperty("sistemas")
    private List<String> systems = Collections.emptyList();

    public static CraneControl fromDTO(CraneControlDTO dto) {
        var parsedDate = LocalDateTime.parse(dto.ultimaLubrificacao(), DateTimeFormatter.ISO_DATE_TIME);
        return new CraneControl(
                Optional.ofNullable(dto.id()).orElse(UUID.randomUUID().toString()),
                dto.localizacao(),
                parsedDate,
                dto.operacional(),
                dto.horimetro(),
                dto.checkList(),
                dto.colaboradorChecklist(),
                dto.responsabilidadeInoperancia(),
                dto.justificativaOperacional(),
                dto.plataforma(),
                dto.sistemas()
        );
    }

}


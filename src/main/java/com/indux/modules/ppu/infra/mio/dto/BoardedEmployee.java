package com.indux.modules.ppu.infra.mio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.modules.ppu.application.dtos.item.BoardedEmployeeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Entidade que representa um funcionário embarcado no sistema.
 * <p>
 * Esta entidade é utilizada no módulo PPU e é responsável por
 * armazenar informações sobre funcionários embarcados em plataformas.
 * <p>
 * A entidade é utilizada internamente no sistema e não deve ser exposta
 * diretamente via API.
 * Para exposição via API, utilize o
 * {@link BoardedEmployeeDTO}.
 * @see BoardedEmployeeDTO
 * @see com.indux.modules.ppu.infra.mio.EmployeeBoardingETL
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardedEmployee {
    @JsonProperty("matricula")
    private String registration;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("cargo")
    private String position;
    @JsonProperty("cargoNome")
    private String positionName;
    @JsonProperty("platform")
    private String platform;
    @JsonProperty("prevEmbarque")
    private LocalDate boardingForecast;
    @JsonProperty("embarque")
    private LocalDate boarding;
    @JsonProperty("desembarque")
    private LocalDate landingForecast;
    @JsonProperty("sispat")
    private String sispat;
    @JsonProperty("contrato")
    private Map<String, Object> contract;
    @JsonProperty("statusColaborador")
    private String status;

    @JsonProperty("RDOid")
    private String rdoId;
    private String filial_HCM;
    @JsonProperty("fimDisposicao") private LocalDate availableEndDate;
    @JsonProperty("inicioDisposicao") private LocalDate availableStartDate;
    @JsonProperty("tipoDisposicao") private String availableType;

    public static BoardedEmployee fromJson(Map<String, Object> json){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        return BoardedEmployee.builder()
                .registration((String) json.get("Matrícula"))
                .name((String) json.get("Nome"))
                .positionName((String) json.get("Função/Cargo"))
                .platform((String) json.get("Destino"))
                .boardingForecast(parseDate(json.get("Prev. de Emb."), formatter))
                .boarding(parseDate(json.get("Embarque Real"), formatter))
                .landingForecast(parseDate(json.get("Desembarque Real") != null ? json.get("Desembarque Real") : json.get("Prev. Desemb."), formatter))
                .filial_HCM((String) json.get("Centro de Custo da RTPE"))
                .build();
    }

    private static LocalDate parseDate(Object value, DateTimeFormatter formatter) {
        if (value instanceof String str && !str.isBlank()) {
            return LocalDate.parse(str, formatter);
        }
        return null;
    }

    public static BoardedEmployee fromSimpleEmployee(SimpleEmployeeDTO dto, BoardedEmployee entity){
        return BoardedEmployee.builder()
                .registration(dto.getRegistration())
                .name(dto.getName())
                .position(dto.getPosition())
                .positionName(dto.getPositionName())
                .sispat(dto.getSispat())
                .platform(entity.getPlatform())
                .boardingForecast(entity.getBoardingForecast())
                .boarding(entity.getBoarding())
                .landingForecast(entity.getLandingForecast())
                .contract(dto.getContract())
                .status(entity.getStatus())
                .filial_HCM(entity.getFilial_HCM())
                .build();
    }


}

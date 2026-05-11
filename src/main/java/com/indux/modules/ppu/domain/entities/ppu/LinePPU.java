package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Classe base abstrata para qualquer item de linha dentro de uma PPU.
 * Contém os campos comuns a todos os tipos de itens, como serviços, materiais,
 * etc.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class LinePPU {
    @Id
    private String id;
    @JsonProperty("numero")
    private String genericNumber;
    @JsonProperty("numeroPPU")
    private String ppuNumber;
    @JsonProperty("nome")
    private String name;
    @JsonProperty("unidadeDeMedida")
    private String unitOfMeasurement;
    @JsonProperty("valor")
    private Double value;
    @JsonProperty("fator")
    private Double factor;

    /**
     * Nova estrutura: lista de configurações de linha auditável por plataforma.
     * Documentos novos <b>DEVEM</b> usam este campo.
     */
    @JsonProperty("linhasAuditaveis")
    private List<AuditableLineConfig> auditableLines;

    /**
     * Campo legado: documentos antigos no MongoDB têm o campo "auditableLine"
     * como String simples. O código lê este campo automaticamente
     * pelo nome do campo Java (não pelo @JsonProperty).
     */
    @JsonIgnore
    private String auditableLine;

    @JsonProperty("plataformas")
    private List<String> platforms;
    @JsonProperty("totalPrevisto")
    private List<MeasurementForecast> measurementForecasts;
    @JsonProperty("ativa")
    private Boolean active;

    public String resolveAuditableLine(String platform) {
        if (auditableLines != null && !auditableLines.isEmpty()) {
            return AuditableLineResolver.resolve(auditableLines, platform);
        }
        return auditableLine;
    }

    public String resolveAuditableLine() {
        return resolveAuditableLine(null);
    }
}

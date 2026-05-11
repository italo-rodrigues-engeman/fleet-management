package com.indux.modules.ppu.application.dtos.response.bm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BMServiceReportItem {
    String id;
    @JsonProperty("numero")
    String number;
    @JsonProperty("nome")
    String name;
    @JsonProperty("qtdPrevista")
    Double qtdExpected;
    @JsonProperty("qtdMedida")
    Double qtdReal;
    @JsonProperty("valorPrevisto")
    BigDecimal valueExpected;
    @JsonProperty("valorMedido")
    BigDecimal valueReal;
    @JsonProperty("porcentagemAproveitamento")
    BigDecimal utilizationPercentage;
    @JsonProperty("valorAproveitamento")
    BigDecimal utilizationValue;
    @JsonProperty("alcancePossivel")
    BigDecimal possibleReach;
}

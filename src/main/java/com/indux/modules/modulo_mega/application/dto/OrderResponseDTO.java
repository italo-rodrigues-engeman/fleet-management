package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderResponseDTO {

    @JsonProperty("numero_pedido")
    private String orderNumber;

    @JsonProperty("situacao")
    private String situation;

    @JsonProperty("status_do_pedido")
    private String orderStatus;

    @JsonProperty("data_pedido")
    private LocalDate orderDate;

    @JsonProperty("valor_total_pedido")
    private BigDecimal totalOrderValue;

    @JsonProperty("solicitacoes")
    private List<SolicitationDTO> solicitations = new ArrayList<>();
}
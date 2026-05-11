package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class SolicitationDTO {

    @JsonProperty("numero_solicitacao")
    private String solicitationNumber;

    @JsonProperty("tipo_pedido")
    private String orderType;

    @JsonProperty("itens")
    // Corrigido: Agora usa o nome correto da classe
    private List<OrderResponseItemDTO> items = new ArrayList<>();
}
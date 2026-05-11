package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class NFResponseDTO {

    @JsonProperty("numero_nf")
    private String noteNumber;

    @JsonProperty("data_nota")
    private LocalDate invoiceDate;

    @JsonProperty("pedidos")
    private List<OrderResponseDTO> pedidos = new ArrayList<>();
}
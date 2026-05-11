package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) 
@JsonPropertyOrder({
    "codigo_regional",
    "nome_regional",
    "dados_de_data_regional",
    "filiais",
    //"estoques",
    // Os campos de BaseStatisticsDTO virão depois, por padrão em ordem de declaração.
    // Se você souber os nomes de BaseStatisticsDTO e precisar de uma ordem específica para ELES,
    // você precisará listá-los aqui também.
})
public class RegionalDetailDTO extends BaseStatisticsDTO {

    @JsonProperty("codigo_regional")
    private String regionalCode;
    
    @JsonProperty("nome_regional")
    private String regionalName;
	
	@JsonProperty("dados_de_data_regional")
	private ItemDetailedDTO.Dates dates;

    @JsonProperty("filiais")
	private List<BranchDetailDTO> branches;
    
    
}
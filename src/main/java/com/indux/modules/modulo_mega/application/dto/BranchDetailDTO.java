package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BranchDetailDTO extends BaseStatisticsDTO {

    @JsonProperty("codigo_filial")
    private String branchId;

    @JsonProperty("nome_filial")
    private String branchName;
}
package com.indux.modules.ppu.domain.entities.rdo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
public class RDOLine {
    private String id;
    @JsonProperty("linhaPai") private String parentId;
    @JsonProperty("numeroPai") private String parentNumber;
    @JsonProperty("nomePai") private String parentName;
    @JsonProperty("quantidade") private Double valueMeasured;
}
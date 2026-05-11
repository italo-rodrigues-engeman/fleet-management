package com.indux.modules.ppu.domain.entities.ppu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SteelCableLine extends LinePPU{
    @JsonProperty("certificateUrl")
    private String certificate;
    @JsonProperty("totalPrevisto")
    private Integer totalPlanned;
}

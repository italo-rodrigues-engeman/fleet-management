package com.indux.modules.ppu.domain.entities.rdo.logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RDOLoggerUser {
    @JsonProperty("nome") private String name;
    @JsonProperty("id") private UUID id;
}

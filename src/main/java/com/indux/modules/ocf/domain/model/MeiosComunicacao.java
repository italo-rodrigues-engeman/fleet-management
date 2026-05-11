package com.indux.modules.ocf.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeiosComunicacao {
    @JsonProperty("email")
    private List<String> email;
    
    @JsonProperty("telefone")
    private List<String> telefone;
}

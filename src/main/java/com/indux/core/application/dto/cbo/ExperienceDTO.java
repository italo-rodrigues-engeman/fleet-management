package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDTO {
    
    @Field("tempo") 
    @JsonProperty("tempo") 
    private String time;
    
    @Field("codigoCboFilho") 
    @JsonProperty("codigoCboFilho") 
    private List<String> codCbo;
    
    @Field("nomeCboFilho") 
    @JsonProperty("nomeCboFilho") 
    private List<String> nameCbo;
}

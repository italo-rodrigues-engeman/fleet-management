package com.indux.core.application.dto.cbo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataLog {
    @Field("nome") @JsonProperty("nome") private String name;
    @Field("data") @JsonProperty("data") private LocalDateTime date;
    @Field("acao") @JsonProperty("acao") private String action;
    @Field("justificativa") @JsonProperty("justificativa") private String justification;
}
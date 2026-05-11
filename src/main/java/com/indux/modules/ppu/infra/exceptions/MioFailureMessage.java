package com.indux.modules.ppu.infra.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.ppu.infra.mio.dto.MioResponse;
import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MioFailureMessage {
    @JsonProperty("mensagem") String message;
    @JsonProperty("matricula") String registration;
    @JsonProperty("nome") String name;
    @JsonProperty("cargo") String position;
    @JsonProperty("statusMio") String statusMio;


    public MioFailureMessage fromMioResponse(String message, String position, MioResponse response){
        return new MioFailureMessage(
                message,
                response.matricula(),
                response.nome(),
                position,
                response.status()
        );
    }

}

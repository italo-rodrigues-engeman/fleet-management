package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public record ModelReturn(
        @JsonProperty("modelo")
        String model,
        @JsonProperty("capacidade")
        String capacity,
        AttachmentEntity manual,
        String link,
        @JsonProperty("foto")
        List<AttachmentEntity> picture,
        @Field("ni_mega_id")
        String niMega
) {
}

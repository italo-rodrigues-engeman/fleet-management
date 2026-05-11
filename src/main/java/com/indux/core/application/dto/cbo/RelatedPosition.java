package com.indux.core.application.dto.cbo;

import org.springframework.data.mongodb.core.mapping.Field;

public record RelatedPosition(
        @Field("cod_cbo") Integer codCBO,
        @Field("nome_cargo_cbo") String nameCBO,
        @Field("nome_cargo_sub") String subCBO
) {
}

package com.indux.modules.modulo_mega.application.dto.groups;

import com.fasterxml.jackson.annotation.JsonProperty;
public record MegaGroupResponse(
        Integer id,
        @JsonProperty("label") String name,
        Integer extendedGroupId,
        Integer identifier,
        @JsonProperty("statusGroup") Boolean statusGroup
) {
    @JsonProperty("status")
    public String status() {
        if (statusGroup == null) return null;
        return statusGroup ? "Ativo" : "Inativo";
    }
}

package com.indux.modules.crm.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.enums.Market;
import com.indux.modules.crm.domain.enums.Modality;
import com.indux.modules.crm.domain.enums.RegistrationCondition;
import com.indux.modules.crm.domain.enums.Sector;

public record CompanyRequest(
        String cnpj,
        @JsonProperty("nome") String name,
        @JsonProperty("mercado") Market market,
        @JsonProperty("setor") Sector sector,
        @JsonProperty("modalidade") Modality modality,
        @JsonProperty("url_portal") String portalUrl,
        @JsonProperty("usuario_portal") String portalUser,
        @JsonProperty("senha_portal") String portalPassword,
        @JsonProperty("observacoes") String details,
        @JsonProperty("condicao_cadastro") RegistrationCondition registrationCondition,
        @JsonProperty("status") Boolean status
) {
}

package com.indux.modules.crm.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.modules.crm.domain.entity.Budget;
import com.indux.modules.crm.domain.entity.CommercialInteractions;
import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.domain.entity.Unit;
import com.indux.modules.crm.domain.enums.RegistrationCondition;

import java.util.List;
import java.util.UUID;

public record CompanyResponse(@JsonProperty("id")
                              String id,
                              @JsonProperty("nome")
                              String name,
                              @JsonProperty("cnpj")
                              String cnpj,
                              @JsonProperty("mercado")
                              String market,
                              @JsonProperty("setor")
                              String sector,
                              @JsonProperty("usuario_responsavel")
                              String responsibleUser,
                              @JsonProperty("url_portal")
                              String portalUrl,
                              @JsonProperty("usuario_portal")
                              String portalUser,
                              @JsonProperty("observacoes")
                              String details,
                              @JsonProperty("condicao_cadastro")
                              RegistrationCondition registrationCondition,
                              @JsonProperty("senha_portal")
                              String portalPassword,
                              @JsonProperty("modalidade")
                              String modality,
                              @JsonProperty("status")
                              Boolean status,
                              @JsonProperty("unidades")
                              List<Unit> units,
                              @JsonProperty("leads")
                              List<Lead> leads,
                              @JsonProperty("interacoes_comerciais")
                              List<CommercialInteractions> commercialInteractions,
                              @JsonProperty("orcamentos")
                              List<Budget> budgets) {

}

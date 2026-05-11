package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxApprovalSolicitationDTO {
    
    @JsonProperty("aplicacao")
    private String aplicacao;
    
    @JsonProperty("servico")
    @JsonAlias({"codigoServico"})
    private String servico;
    
    @JsonProperty("ncm")
    @JsonAlias({"NCM"})
    private String ncm;
    
    @JsonProperty("definicaoFiscal")
    private String definicaoFiscal;
    
    @JsonProperty("codigoSituacaoTributaria")
    private String codigoSituacaoTributaria;
    
    @JsonProperty("codigoTratamentoIcms")
    private String codigoTratamentoIcms;
    
    @JsonProperty("codigoRegraPisCofins")
    private String codigoRegraPisCofins;
}


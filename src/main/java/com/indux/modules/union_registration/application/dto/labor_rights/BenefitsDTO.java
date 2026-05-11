package com.indux.modules.union_registration.application.dto.labor_rights;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BenefitsDTO {
    
    // 5 - Benefícios
    
    // Tipo Alimentação e Refeição
    // CAFÉ DA MANHÃ / DESEJUM
    private BeneficioEstruturadoDTO cafeDaManha;
    
    // ALMOÇO
    private BeneficioEstruturadoDTO almoco;
    
    // LANCHE
    private BeneficioEstruturadoDTO lanche;
    
    // LANCHE PARADA
    private BeneficioEstruturadoDTO lancheParada;
    
    // VALE ALIMENTAÇÃO
    private BeneficioEstruturadoDTO valeAlimentacao;
    
    // VALE ALIMENTAÇÃO PARADA
    private BeneficioEstruturadoDTO valeAlimentacaoParada;
    
    // VALE REFEIÇÃO
    private BeneficioEstruturadoDTO valeRefeicao;
    
    // VALE REFEIÇÃO PARADA
    private BeneficioEstruturadoDTO valeRefeicaoParada;
    
    // CESTA BÁSICA
    private BeneficioEstruturadoDTO cestaBasica;
    
    // CESTA NATALINA
    private BeneficioEstruturadoDTO cestaNatalina;
    
    // Tipo Premiações
    // GRATIFICAÇÃO
    private BeneficioEstruturadoDTO gratificacao;

    // GRATIFICAÇÃO ADCIONAL DE FÉRIAS
    private BeneficioEstruturadoDTO gratificacaoAdicionalFerias;
    
    // GRATIFICAÇÃO/ABONO PARADA
    private BeneficioEstruturadoDTO gratificacaoAbonoParada;
    
    // PLR
    private BeneficioEstruturadoDTO plr;
    
    // PLR PARADA
    private BeneficioEstruturadoDTO plrParada;
    
    // FLASH VIRTUAL
    private BeneficioEstruturadoDTO flashVirtual;
    
    // PRÊMIO DESEMPENHO
    private BeneficioEstruturadoDTO premioDesempenho;
    
    // Tipo Outros Benefícios
    private BeneficioEstruturadoDTO auxilioMoradia;
    private BeneficioEstruturadoDTO reembolsoDespesaViagem;
    private BeneficioEstruturadoDTO ajudaDeCusto;
    private BeneficioEstruturadoDTO auxilioCreche;
    private BeneficioEstruturadoDTO auxilioEducacao;

}



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
public class SalaryFunctionDTO {
    
    // Campos básicos da função
    private String nomeFuncaoAct;
    private String codigoCbo;
    private String nomeFuncaoCbo;
    private String salarioBase; // Salário Base (P0)
    
    // Horas Extras
    private BeneficioEstruturadoDTO horaExtra1; // H. EXTRA 1 - SEG. A SEX
    private BeneficioEstruturadoDTO horaExtra2; // H. EXTRA 2 - SÁBADO
    private BeneficioEstruturadoDTO horaExtra3; // H. EXTRA 3 - DOMINGO / FERIADO
    
    // Adicionais
    private BeneficioEstruturadoDTO adicionalNoturno; // NOTURNO
    private BeneficioEstruturadoDTO adicionalInsalubridade; // INSALUBRIDADE
    private BeneficioEstruturadoDTO adicionalPericulosidade; // PERICULOSIDADE
    private BeneficioEstruturadoDTO adicionalSobreaviso; // SOBREAVISO
    private BeneficioEstruturadoDTO adicionalProntidao; // PRONTIDÃO
    
    // Alimentação e Refeição
    private BeneficioEstruturadoDTO cafeDaManha; // CAFÉ DA MANHÃ / DESEJUM
    private BeneficioEstruturadoDTO almoco; // ALMOÇO
    private BeneficioEstruturadoDTO lanche; // LANCHE
    private BeneficioEstruturadoDTO lancheParada; // LANCHE PARADA
    private BeneficioEstruturadoDTO valeAlimentacao; // VALE ALIMENTAÇÃO
    private BeneficioEstruturadoDTO valeAlimentacaoParada; // VALE ALIMENTAÇÃO PARADA
    private BeneficioEstruturadoDTO valeRefeicao; // VALE REFEIÇÃO
    private BeneficioEstruturadoDTO valeRefeicaoParada; // VALE REFEIÇÃO PARADA
    private BeneficioEstruturadoDTO cestaBasica; // CESTA BÁSICA
    private BeneficioEstruturadoDTO cestaNatalina; // CESTA NATALINA
    
    // Benefícios de Saúde
    private BeneficioEstruturadoDTO planoOdontologico; // PLANO ODONTO
    private BeneficioEstruturadoDTO planoSaude; // PLANO DE SAÚDE
    private BeneficioEstruturadoDTO seguroVida; // SEGURO DE VIDA
    
    // Premiações
    private BeneficioEstruturadoDTO gratificacao; // GRATIFICAÇÃO
    private BeneficioEstruturadoDTO gratificacaoAbonoParada; // GRATIFICAÇÃO/ABONO PARADA
    private BeneficioEstruturadoDTO plr; // PLR
    private BeneficioEstruturadoDTO plrParada; // PLR PARADA
    private BeneficioEstruturadoDTO flashVirtual; // FLASH VIRTUAL
    private BeneficioEstruturadoDTO premioDesempenho; // PRÊMIO DESEMPENHO
    
    // Outros Benefícios
    private BeneficioEstruturadoDTO auxilioMoradia; // AUXÍLIO MORADIA
    private BeneficioEstruturadoDTO ajudaDeCusto; // AJUDA DE CUSTO/ MOBILIZAÇÃO
    private BeneficioEstruturadoDTO reembolsoDespesaViagem; // REEMBOLSO DE DESPESA DE VIAGEM
    
    // Transporte
    private BeneficioEstruturadoDTO valeTransporte; // VALE TRANSPORTE - TRANSPORTE PÚBLICO
    private BeneficioEstruturadoDTO auxilioTransporte; // AUXÍLIO TRANSPORTE
    private BeneficioEstruturadoDTO fretado; // FRETADO
    
    // Anuênio e Contribuições
    private BeneficioEstruturadoDTO anuenio; // ANUÊNIO
    private BeneficioEstruturadoDTO contribuicaoPatronal; // PATRONAL
    private BeneficioEstruturadoDTO contribuicaoPatronalEducativa; // EDUCATIVA
}


package com.indux.modules.request_budgets.application.mapper;

import com.indux.modules.request_budgets.application.dto.BudgetVersionResponseDTO;
import com.indux.modules.request_budgets.domain.model.BudgetVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetVersionMapper {

    private final BudgetComercialMapper budgetComercialMapper;
    private final BudgetEngenhariaMapper budgetEngenhariaMapper;
    private final BudgetSmsMapper budgetSmsMapper;
    private final BudgetOperacaoMapper budgetOperacaoMapper;

    public BudgetVersionResponseDTO toResponseDTO(BudgetVersion version) {
        return BudgetVersionResponseDTO.builder()
                .id(version.getId())
                .budgetId(version.getBudgetId())
                .anexoMd(version.getAnexoMd())
                .mdInfo(version.getMdInfo())
                .anexoPpu(version.getAnexoPpu())
                .ppuInfo(version.getPpuInfo())
                .anexoSms(version.getAnexoSms())
                .smsInfo(version.getSmsInfo())
                .anexoGerais(version.getAnexoGerais())
                .geraisInfo(version.getGeraisInfo())
                .habilitacaoAnexo(version.getHabilitacaoAnexo())
                .habilitacaoInfo(version.getHabilitacaoInfo())
                .tipoProposta(version.getTipoProposta())
                .dataEntrega(version.getDataEntrega())
                .metodoEntrega(version.getMetodoEntrega())
                .propostaLocal(version.getPropostaLocal())
                .cidades(version.getCidades())
                .estados(version.getEstados())
                .dataHoraProposta(version.getDataHoraProposta())
                .valorFinalTotal(version.getValorFinalTotal())
                .ResponsavelProposta(version.getResponsavelProposta())
                .anexoTipo(version.getAnexoTipo())
                .comporvanteProposta(version.getComporvanteProposta())
                .manutencao(version.getManutencao())
                .operacao(version.getOperacao())
                .atividadesDiversas(version.getAtividadesDiversas())
                .construcaoMontagem(version.getConstrucaoMontagem())
                .fabricacao(version.getFabricacao())
                .projetos(version.getProjetos())
                .diversos(version.getDiversos())
                .detalhes(version.getDetalhes())
                .outros(version.getOutros())
                .comissao(version.getComissao())
                .modalidadeConcorrencia(version.getModalidadeConcorrencia())
                .tipoOportunidade(version.getTipoOportunidade())
                .caracteristicasOportunidade(version.getCaracteristicasOportunidade())
                .outroEmail(version.getOutroEmail())
                .portal(version.getPortal())
                .acessoInfo(version.getAcessoInfo())
                .comercial(budgetComercialMapper.toResponseDTOList(version.getComercial()))
                .engenharia(budgetEngenhariaMapper.toResponseDTOList(version.getEngenharia()))
                .sms(budgetSmsMapper.toResponseDTOList(version.getSms()))
                .operacoes(budgetOperacaoMapper.toResponseDTOList(version.getOperacoes()))
                .filtro1(version.getFiltro1())
                .datahoraFiltro1(version.getDatahoraFiltro1())
                .responsavelFiltro1(version.getResponsavelFiltro1())
                .anexoFiltro1(version.getAnexoFiltro1())
                .motivoFiltro1(version.getMotivoFiltro1())
                .responsavelFilrtro1(version.getResponsavelFilrtro1())
                .numeroAc(version.getNumeroAc())
                .oracamentista(version.getOracamentista())
                .dataDesignacao(version.getDataDesignacao())
                .justificativaEngeman(version.getJustificativaEngeman())
                .justificativaSolicitante(version.getJustificativaSolicitante())
                .filtro2(version.getFiltro2())
                .datahoraFiltro2(version.getDatahoraFiltro2())
                .responsavelFiltro2(version.getResponsavelFiltro2())
                .justificativaFiltro2(version.getJustificativaFiltro2())
                .motivoFiltro2(version.getMotivoFiltro2())
                .anexoFiltro2(version.getAnexoFiltro2())
                .dataFiltro2(version.getDataFiltro2())
                .justificativaSolicitanteFiltro2(version.getJustificativaSolicitanteFiltro2())
                .createdAt(version.getCreatedAt())
                .updatedAt(version.getUpdatedAt())
                .createdBy(version.getCreatedBy())
                .updatedBy(version.getUpdatedBy())
                .build();
    }
}




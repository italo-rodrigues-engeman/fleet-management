package com.indux.modules.contracts.application.mapper;

import com.indux.modules.contracts.application.dto.ContractDTO;
import com.indux.modules.contracts.domain.model.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractDTO toDTO(Contract contract) {
        if (contract == null) {
            return null;
        }

        return new ContractDTO(
                contract.getId(),
                contract.getOs(),
                contract.getOsMega(),
                contract.getAnoOs(),
                contract.getCliente(),
                contract.getCodSap(),
                contract.getDataAssinatura(),
                contract.getDataFim(),
                contract.getPorcentagemReajuste(),
                contract.getMesReajuste(),
                contract.getAtivo(),
                contract.getTipoAditivo(),
                contract.getRegional(),
                contract.getGestorInternoContrato(),
                contract.getGestorClienteContrato(),
                contract.getValorContrato(),
                contract.getNomeProjeto(),
                contract.getDataConhecida(),
                contract.getMegaId(),
                contract.getFilialId(),
                contract.getRateioId(),
                contract.getNomeCentroCustos(),
                contract.getDescricaoEscopo(),
                contract.getIdDisciplina(),
                contract.getIdCliente(),
                contract.getGestorInternoCustos(),
                contract.getCoordenadorContrato(),
                contract.getEmailGestor(),
                contract.getEmailCoordenador(),
                contract.getContatoGestor(),
                contract.getContatoCoordenador(),
                contract.getDataInicio(),
                contract.getNome(),
                contract.getClienteId(),
                contract.getPlataformaId(),
                contract.getIcj()
        );
    }
} 
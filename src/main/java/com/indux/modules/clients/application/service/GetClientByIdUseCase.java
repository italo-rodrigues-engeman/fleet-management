package com.indux.modules.clients.application.service;

import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.modules.clients.application.dto.*;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetClientByIdUseCase {
    private final ClientRepository clientRepository;
    private final ContractProjectRepository contractProjectRepository;

    public ClientDTO execute(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        List<ClientFiscalDTO> fiscaisDTO = client.getFiscais().stream()
                .map(fiscal -> new ClientFiscalDTO(
                        fiscal.getId(),
                        fiscal.getNome(),
                        fiscal.getEmail(),
                        fiscal.getTelefone(),
                        fiscal.getCargo(),
                        fiscal.getObservacoes()
                ))
                .collect(Collectors.toList());

        List<ClientBranchDTO> filiaisDTO = client.getFiliais().stream()
                .map(f -> new ClientBranchDTO(
                        f.getCnpj(),
                        f.getNome(),
                        f.getEndereco(),
                        f.getTelefone(),
                        f.getTipoFilial(),
                        f.getRamo(),
                        f.getContatos().stream()
                                .map(c -> new com.indux.modules.clients.application.dto.ContactDTO(c.getNome(), c.getEmail(), c.getTelefone()))
                                .toList()
                ))
                .toList();

        List<IndustrialPlantDTO> plantasDTO = client.getPlantas().stream()
                .map(p -> new IndustrialPlantDTO(
                        p.getId(),
                        p.getTipoPlanta(),
                        p.getEndereco(),
                        p.getLinkMaps(),
                        p.getContatos().stream()
                                .map(c -> new com.indux.modules.clients.application.dto.ContactDTO(c.getNome(), c.getEmail(), c.getTelefone()))
                                .toList(),
                        p.getLinksExternos(),
                        p.getFotos(),
                        p.getObservacoes()
                ))
                .toList();

        List<ContractDTO> contratosDTO = contractProjectRepository.findAllByClientId(id).stream()
                .map(contrato -> new ContractDTO(
                        contrato.getId(),
                        contrato.getRateio(),
                        contrato.getCostCenterName(),
                        contrato.getMegaId(),
                        contrato.getProjectName(),
                        contrato.getContractManager(),
                        contrato.getClient(),
                        contrato.getCodeSap(),
                        contrato.getDataAssinatura(),
                        contrato.getDataFim(),
                        contrato.getPlatforms().stream()
                                .map(p -> new PlatformDTO(p.getId(), p.getNomePlataforma(), p.getSigla()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        return new ClientDTO(
                client.getId(),
                client.getName(),
                client.getCnpj(),
                client.getMercado(),
                client.getRamo(),
                client.getEnderecoPlanta(),
                client.getCidade(),
                client.getEstado(),
                fiscaisDTO,
                client.getStatus(),
                client.getObservacao(),
                client.getDueDiligentes(),
                client.getIcj(),
                client.getLogoMarca(),
                client.getDueDiligenceDoc(),
                client.getTelefones(),
                client.getSite(),
                client.getEmail(),
                client.getIdRamo(),
                filiaisDTO,
                plantasDTO,
                contratosDTO
        );
    }
} 
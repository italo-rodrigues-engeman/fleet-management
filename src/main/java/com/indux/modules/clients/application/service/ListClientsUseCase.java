package com.indux.modules.clients.application.service;

import com.indux.modules.clients.application.dto.ClientDTO;
import com.indux.modules.clients.application.dto.ClientFiscalDTO;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListClientsUseCase {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public Page<ClientDTO> execute(
            String enderecoPlanta,
            String fiscal,
            String cnpj,
            String cidade,
            String estado,
            String tipoCliente,
            com.indux.modules.clients.domain.model.ClientStatus status,
            String name,
            Long clientId,
            Boolean dueDiligentes,
            String ramo,
            Pageable pageable) {

        Page<Client> clients = clientRepository.findAllWithFilters(
                enderecoPlanta,
                fiscal,
                cnpj,
                cidade,
                estado,
                tipoCliente,
                status != null ? status.name() : null,
                name,
                clientId,
                dueDiligentes,
                ramo,
                pageable
        );

        return clients.map(client -> {
            List<ClientFiscalDTO> fiscaisDTO = client.getFiscais().stream()
                    .map(fiscalEntity -> new ClientFiscalDTO(
                            fiscalEntity.getId(),
                            fiscalEntity.getNome(),
                            fiscalEntity.getEmail(),
                            fiscalEntity.getTelefone(),
                            fiscalEntity.getCargo(),
                            fiscalEntity.getObservacoes()
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
                    null, // filiais não carregadas
                    null, // plantas não carregadas
                    null  // contratos não carregados
            );
        });
    }
} 
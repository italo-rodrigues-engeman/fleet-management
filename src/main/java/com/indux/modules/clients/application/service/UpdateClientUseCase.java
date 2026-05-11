package com.indux.modules.clients.application.service;

import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.clients.application.dto.*;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.model.ClientFiscal;
import com.indux.modules.clients.domain.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateClientUseCase {
    private static final Logger logger = LoggerFactory.getLogger(UpdateClientUseCase.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    private final ClientRepository clientRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final StorageService storageService;

    private String uploadImage(MultipartFile file, String subDir, String cnpj) {
        if (file == null) return null;
        
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String original = file.getOriginalFilename();
        String sanitizedCnpj = (cnpj == null ? "" : cnpj.replaceAll("[^0-9]", ""));
        String newName = timestamp + "_" + sanitizedCnpj + "_" + original;
        
        Path stored = storageService.store(file, subDir, newName);
        // Retorna caminho relativo ao rootLocation
        Path root = storageService.getRootLocation();
        return root.relativize(stored).toString().replace('\\', '/');
    }

    @Transactional
    public ClientDTO execute(Long id, UpdateClientRequestDTO request) {
        // Busca o cliente
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Verifica se o CNPJ já existe em outro cliente
        if (!client.getCnpj().equals(request.getCnpj()) && 
            clientRepository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("Já existe um cliente cadastrado com este CNPJ");
        }

        // Atualiza os dados do cliente
        if (request.getName() != null)             client.setName(request.getName());
        if (request.getCnpj() != null)            client.setCnpj(request.getCnpj());
        if (request.getMercado() != null)         client.setMercado(request.getMercado());
        if (request.getEnderecoPlanta() != null)  client.setEnderecoPlanta(request.getEnderecoPlanta());
        if (request.getCidade() != null)          client.setCidade(request.getCidade());
        if (request.getEstado() != null)          client.setEstado(request.getEstado());
        if (request.getRamo() != null)            client.setRamo(request.getRamo());
        if (request.getStatus() != null)          client.setStatus(request.getStatus());
        if (request.getDueDiligentes() != null)   client.setDueDiligentes(request.getDueDiligentes());

        // Upload do logo se fornecido
        String logoUrl = uploadImage(request.getLogo(), "logos", request.getCnpj());
        if (logoUrl != null) {
            client.setLogoMarca(logoUrl);
        }

        // Atualiza o documento de due diligence se fornecido
        if (request.getDueDiligenceDoc() != null) {
            client.setDueDiligenceDoc(request.getDueDiligenceDoc());
            if (request.getDueDiligentes() != null && request.getDueDiligentes()) {
                client.setDataDueDiligentes(LocalDateTime.now());
            }
        }

        if (request.getFiscais() != null) {
            // Substitui lista de fiscais somente se enviada
            client.getFiscais().clear();
            List<ClientFiscal> fiscais = request.getFiscais().stream()
                    .map(dto -> {
                        ClientFiscal fiscal = new ClientFiscal();
                        fiscal.setNome(dto.getNome());
                        fiscal.setEmail(dto.getEmail());
                        fiscal.setTelefone(dto.getTelefone());
                        fiscal.setCargo(dto.getCargo());
                        fiscal.setObservacoes(dto.getObservacoes());
                        fiscal.setClient(client);
                        return fiscal;
                    })
                    .collect(Collectors.toList());
            client.getFiscais().addAll(fiscais);
        }

        // Salva o cliente
        Client savedClient = clientRepository.save(client);

        // Converte para DTO
        List<ClientFiscalDTO> fiscaisDTO = savedClient.getFiscais().stream()
                .map(fiscal -> new ClientFiscalDTO(
                        fiscal.getId(),
                        fiscal.getNome(),
                        fiscal.getEmail(),
                        fiscal.getTelefone(),
                        fiscal.getCargo(),
                        fiscal.getObservacoes()
                ))
                .collect(Collectors.toList());

        List<ClientBranchDTO> filiaisDTO = savedClient.getFiliais().stream()
                .map(f -> new ClientBranchDTO(
                        f.getCnpj(), f.getNome(), f.getEndereco(), f.getTelefone(),
                        f.getTipoFilial(), f.getRamo(),
                        f.getContatos().stream()
                                .map(c -> new com.indux.modules.clients.application.dto.ContactDTO(c.getNome(), c.getEmail(), c.getTelefone()))
                                .toList()
                )).toList();

        List<IndustrialPlantDTO> plantasDTO = savedClient.getPlantas().stream()
                .map(p -> new IndustrialPlantDTO(
                        p.getId(), p.getTipoPlanta(), p.getEndereco(), p.getLinkMaps(),
                        p.getContatos().stream()
                                .map(c -> new com.indux.modules.clients.application.dto.ContactDTO(c.getNome(), c.getEmail(), c.getTelefone()))
                                .toList(),
                        p.getLinksExternos(), p.getFotos(), p.getObservacoes()
                )).toList();

        List<ContractDTO> contratosDTO = contractProjectRepository.findAllByClientId(savedClient.getId()).stream()
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
                savedClient.getId(),
                savedClient.getName(),
                savedClient.getCnpj(),
                savedClient.getMercado(),
                savedClient.getRamo(),
                savedClient.getEnderecoPlanta(),
                savedClient.getCidade(),
                savedClient.getEstado(),
                fiscaisDTO,
                savedClient.getStatus(),
                savedClient.getObservacao(),
                savedClient.getDueDiligentes(),
                savedClient.getIcj(),
                savedClient.getLogoMarca(),
                savedClient.getDueDiligenceDoc(),
                savedClient.getTelefones(),
                savedClient.getSite(),
                savedClient.getEmail(),
                savedClient.getIdRamo(),
                filiaisDTO,
                plantasDTO,
                contratosDTO
        );
    }
} 
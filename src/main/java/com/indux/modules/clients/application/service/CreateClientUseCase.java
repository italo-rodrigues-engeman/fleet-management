package com.indux.modules.clients.application.service;

import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.clients.application.dto.*;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.model.ClientBranch;
import com.indux.modules.clients.domain.model.ClientFiscal;
import com.indux.modules.clients.domain.model.IndustrialPlant;
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
public class CreateClientUseCase {
    private static final Logger logger = LoggerFactory.getLogger(CreateClientUseCase.class);
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
    public ClientDTO execute(CreateClientRequestDTO request) {
        // Verifica se já existe um cliente com o mesmo CNPJ
        if (clientRepository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("Já existe um cliente cadastrado com este CNPJ");
        }

        // Log dos tamanhos dos campos
        logger.info("Verificando tamanho dos campos do cliente:");
        logger.info("Nome: {} caracteres - Valor: {}", request.getName().length(), request.getName());
        logger.info("CNPJ: {} caracteres - Valor: {}", request.getCnpj().length(), request.getCnpj());
        logger.info("Mercado: {} caracteres - Valor: {}", request.getMercado() != null ? request.getMercado().length() : 0, request.getMercado());
        logger.info("Endereço Planta: {} caracteres - Valor: {}", request.getEnderecoPlanta() != null ? request.getEnderecoPlanta().length() : 0, request.getEnderecoPlanta());
        logger.info("Cidade: {} caracteres - Valor: {}", request.getCidade() != null ? request.getCidade().length() : 0, request.getCidade());
        logger.info("Estado: {} caracteres - Valor: {}", request.getEstado() != null ? request.getEstado().length() : 0, request.getEstado());
        logger.info("Ramo: {} caracteres - Valor: {}", request.getRamo() != null ? request.getRamo().length() : 0, request.getRamo());
        logger.info("Telefones: {} caracteres - Valor: {}", request.getTelefones() != null ? request.getTelefones().length() : 0, request.getTelefones());
        logger.info("Site: {} caracteres - Valor: {}", request.getSite() != null ? request.getSite().length() : 0, request.getSite());
        logger.info("Email: {} caracteres - Valor: {}", request.getEmail() != null ? request.getEmail().length() : 0, request.getEmail());
        logger.info("ICJ: {} caracteres - Valor: {}", request.getIcj() != null ? request.getIcj().length() : 0, request.getIcj());
        logger.info("Observação: {} caracteres - Valor: {}", request.getObservacao() != null ? request.getObservacao().length() : 0, request.getObservacao());

        // Upload do logo
        String logoUrl = uploadImage(request.getLogo(), "logos", request.getCnpj());

        // Documento de due diligence
        String dueDiligenceDocUrl = request.getDueDiligenceDoc();

        // Cria o cliente
        Client client = new Client();
        client.setName(request.getName());
        client.setCnpj(request.getCnpj());
        client.setMercado(request.getMercado());
        client.setEnderecoPlanta(request.getEnderecoPlanta());
        client.setCidade(request.getCidade());
        client.setEstado(request.getEstado());
        client.setRamo(request.getRamo());
        client.setStatus(request.getStatus());
        client.setObservacao(request.getObservacao());
        client.setDueDiligentes(request.getDueDiligentes());
        client.setIcj(request.getIcj());
        client.setLogoMarca(logoUrl);
        client.setTelefones(request.getTelefones());
        client.setSite(request.getSite());
        client.setEmail(request.getEmail());
        client.setIdRamo(request.getIdRamo());
        client.setDueDiligenceDoc(dueDiligenceDocUrl);
        if (request.getDueDiligentes() != null && request.getDueDiligentes()) {
            client.setDataDueDiligentes(LocalDateTime.now());
        }

        // Log dos fiscais
        if (request.getFiscais() != null) {
            logger.info("Verificando tamanho dos campos dos fiscais:");
            request.getFiscais().forEach(fiscal -> {
                logger.info("Fiscal - Nome: {} caracteres - Valor: {}", fiscal.getNome().length(), fiscal.getNome());
                logger.info("Fiscal - Email: {} caracteres - Valor: {}", fiscal.getEmail().length(), fiscal.getEmail());
                logger.info("Fiscal - Telefone: {} caracteres - Valor: {}", fiscal.getTelefone().length(), fiscal.getTelefone());
                logger.info("Fiscal - Cargo: {} caracteres - Valor: {}", fiscal.getCargo() != null ? fiscal.getCargo().length() : 0, fiscal.getCargo());
                logger.info("Fiscal - Observações: {} caracteres - Valor: {}", fiscal.getObservacoes() != null ? fiscal.getObservacoes().length() : 0, fiscal.getObservacoes());
            });

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
            client.setFiscais(fiscais);
        }

        // Log das filiais
        if (request.getFiliais() != null) {
            logger.info("Verificando tamanho dos campos das filiais:");
            request.getFiliais().forEach(filial -> {
                logger.info("Filial - CNPJ: {} caracteres - Valor: {}", filial.getCnpj() != null ? filial.getCnpj().length() : 0, filial.getCnpj());
                logger.info("Filial - Nome: {} caracteres - Valor: {}", filial.getNome() != null ? filial.getNome().length() : 0, filial.getNome());
                logger.info("Filial - Endereço: {} caracteres - Valor: {}", filial.getEndereco() != null ? filial.getEndereco().length() : 0, filial.getEndereco());
                logger.info("Filial - Telefone: {} caracteres - Valor: {}", filial.getTelefone() != null ? filial.getTelefone().length() : 0, filial.getTelefone());
                logger.info("Filial - Tipo: {} caracteres - Valor: {}", filial.getTipoFilial() != null ? filial.getTipoFilial().length() : 0, filial.getTipoFilial());
                logger.info("Filial - Ramo: {} caracteres - Valor: {}", filial.getRamo() != null ? filial.getRamo().length() : 0, filial.getRamo());
            });

            List<ClientBranch> filiais = request.getFiliais().stream()
                    .map(dto -> {
                        ClientBranch filial = new ClientBranch();
                        filial.setCnpj(dto.getCnpj());
                        filial.setNome(dto.getNome());
                        filial.setEndereco(dto.getEndereco());
                        filial.setTelefone(dto.getTelefone());
                        filial.setTipoFilial(dto.getTipoFilial());
                        filial.setRamo(dto.getRamo());
                        filial.setClient(client);
                        if (dto.getContatos() != null) {
                            List<com.indux.modules.clients.domain.model.Contact> contatos = dto.getContatos().stream()
                                    .map(c -> new com.indux.modules.clients.domain.model.Contact(
                                            c.getNome(), 
                                            c.getEmail(), 
                                            c.getTelefone()
                                    ))
                                    .collect(Collectors.toList());
                            filial.setContatos(contatos);
                        }
                        return filial;
                    })
                    .collect(Collectors.toList());
            client.setFiliais(filiais);
        }

        // Log das plantas industriais
        if (request.getPlantas() != null) {
            logger.info("Verificando tamanho dos campos das plantas industriais:");
            request.getPlantas().forEach(planta -> {
                logger.info("Planta - Tipo: {} caracteres - Valor: {}", planta.getTipoPlanta() != null ? planta.getTipoPlanta().length() : 0, planta.getTipoPlanta());
                logger.info("Planta - Endereço: {} caracteres - Valor: {}", planta.getEndereco() != null ? planta.getEndereco().length() : 0, planta.getEndereco());
                logger.info("Planta - Link Maps: {} caracteres - Valor: {}", planta.getLinkMaps() != null ? planta.getLinkMaps().length() : 0, planta.getLinkMaps());
                logger.info("Planta - Observações: {} caracteres - Valor: {}", planta.getObservacoes() != null ? planta.getObservacoes().length() : 0, planta.getObservacoes());
            });

            List<IndustrialPlant> plantas = request.getPlantas().stream()
                    .map(dto -> {
                        IndustrialPlant planta = new IndustrialPlant();
                        planta.setTipoPlanta(dto.getTipoPlanta());
                        planta.setEndereco(dto.getEndereco());
                        planta.setLinkMaps(dto.getLinkMaps());
                        if (dto.getContatos() != null) {
                            List<com.indux.modules.clients.domain.model.Contact> contatos = dto.getContatos().stream()
                                    .map(c -> new com.indux.modules.clients.domain.model.Contact(
                                            c.getNome(), 
                                            c.getEmail(), 
                                            c.getTelefone()
                                    ))
                                    .collect(Collectors.toList());
                            planta.setContatos(contatos);
                        }
                        planta.setLinksExternos(dto.getLinksExternos());
                        planta.setObservacoes(dto.getObservacoes());
                        planta.setClient(client);

                        // Upload das fotos da planta
                        if (dto.getFotos() != null) {
                            List<String> fotoUrls = dto.getFotos().stream()
                                    .map(f -> uploadImage(f, "plantas", request.getCnpj()))
                                    .collect(Collectors.toList());
                            planta.setFotos(fotoUrls);
                        }

                        return planta;
                    })
                    .collect(Collectors.toList());
            client.setPlantas(plantas);
        }

        logger.info("Tentando salvar o cliente...");
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

        List<com.indux.modules.clients.application.dto.IndustrialPlantDTO> plantasDTO = savedClient.getPlantas().stream()
                .map(p -> new com.indux.modules.clients.application.dto.IndustrialPlantDTO(
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
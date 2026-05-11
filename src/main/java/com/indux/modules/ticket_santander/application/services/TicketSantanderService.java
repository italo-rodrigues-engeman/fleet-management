package com.indux.modules.ticket_santander.application.services;

import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
// StorageService removido pois não é mais necessário
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.ticket_santander.application.dtos.*;
import com.indux.modules.ticket_santander.domain.entities.mongo.TicketSantanderMongoEntity;
import com.indux.modules.ticket_santander.domain.repositories.mongo.TicketSantanderMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketSantanderService {
    
    private final TicketSantanderMongoRepository ticketSantanderMongoRepository;
    private final AttachmentService attachmentService;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final CargoRepository cargoRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final RegionalRepository regionalRepository;


    public TicketSantanderResponseDTO createTicket(CreateTicketSantanderRequestDTO request) {
        // Verificar se já existe um ticket aberto para este funcionário
        String matricula = request.getMatricula();

        List<TicketSantanderMongoEntity> existingTicketsByMatricula = ticketSantanderMongoRepository.findByMatricula(matricula);
        
        // Combinar as duas listas e remover duplicatas
        List<TicketSantanderMongoEntity> allExistingTickets = new ArrayList<>();
        allExistingTickets.addAll(existingTicketsByMatricula);
        
        // Remover duplicatas baseado no ID
        List<TicketSantanderMongoEntity> uniqueTickets = allExistingTickets.stream()
                .collect(Collectors.toMap(
                    TicketSantanderMongoEntity::getId,
                    ticket -> ticket,
                    (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        
        // Verificar se já existe qualquer ticket para esta matrícula (independente do status)
        if (!uniqueTickets.isEmpty()) {
            TicketSantanderMongoEntity existingTicket = uniqueTickets.get(0); // Pega o primeiro ticket encontrado
            log.warn("Funcionário {} (matrícula: {}) já possui ticket - ID: {}, Status: {} - retornando ticket existente", 
                    request.getFuncionarioId(), matricula, existingTicket.getId(), existingTicket.getStatus());
            TicketSantanderResponseDTO response = convertToResponseDTO(existingTicket);
            // Marcar como ticket existente para o controller identificar
            response.setObservacoes("TICKET_EXISTENTE: " + (response.getObservacoes() != null ? response.getObservacoes() : ""));
            return response;
        }
        
        TicketSantanderMongoEntity entity = TicketSantanderMongoEntity.builder()
                .funcionarioId(request.getFuncionarioId())
                .nomeFuncionario(request.getNomeFuncionario())
                .cpf(request.getCpf())
                .matricula(request.getMatricula())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .tipoTicket(request.getTipoTicket())
                .descricao(request.getDescricao())
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : "NORMAL")
                .categoria(request.getCategoria())
                .numeroAgencia(request.getNumeroAgencia())
                .numeroConta(request.getNumeroConta())
                .carteirinhaAnexos(createAttachmentsFromMultipartFiles(request.getCarteirinhaAnexos()))
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .status("ABERTO")
                .build();
        
        TicketSantanderMongoEntity savedEntity = ticketSantanderMongoRepository.save(entity);
        
        return convertToResponseDTO(savedEntity);
    }
    
    public TicketSantanderResponseDTO createTicketSimple(CreateTicketSantanderSimpleRequestDTO request, UUID funcionarioId) {
        // Validar que pelo menos um anexo foi fornecido
        if (request.getCarteirinhaAnexos() == null || request.getCarteirinhaAnexos().isEmpty() ||
            request.getCarteirinhaAnexos().stream().allMatch(file -> file == null || file.isEmpty())) {
            log.error("Tentativa de criar ticket sem anexos obrigatórios para funcionário: {}", funcionarioId);
            throw new RuntimeException("É obrigatório enviar pelo menos um anexo de carteirinha.");
        }
        
        // Obter informações do funcionário diretamente da tb_funcionarios
        CompleteEmployeeDTO employeeInfo = getEmployeeUseCase.getEmployeeById(funcionarioId);
        
        if (employeeInfo == null) {
            log.error("Funcionário não encontrado com ID: {}", funcionarioId);
            throw new RuntimeException("Funcionário não encontrado com ID: " + funcionarioId + ". Verifique se o ID está correto e se o funcionário existe na tabela tb_funcionarios.");
        }
        
        // Verificar se já existe um ticket aberto para esta matrícula
        String matricula = employeeInfo.getMatricula();
        // Buscar por matrícula
        List<TicketSantanderMongoEntity> existingTicketsByMatricula = ticketSantanderMongoRepository.findByMatricula(matricula);
        
        // Combinar as duas listas e remover duplicatas
        List<TicketSantanderMongoEntity> allExistingTickets = new ArrayList<>();
        allExistingTickets.addAll(existingTicketsByMatricula);
        
        // Remover duplicatas baseado no ID
        List<TicketSantanderMongoEntity> uniqueTickets = allExistingTickets.stream()
                .collect(Collectors.toMap(
                    TicketSantanderMongoEntity::getId,
                    ticket -> ticket,
                    (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());
        
        log.info("Total de tickets únicos encontrados: {} tickets", uniqueTickets.size());
        
        // Log de todos os tickets encontrados
        for (TicketSantanderMongoEntity ticket : uniqueTickets) {
            log.info("Ticket encontrado - ID: {}, Status: {}, Matrícula: {}, FuncionarioId: {}", 
                    ticket.getId(), ticket.getStatus(), ticket.getMatricula(), ticket.getFuncionarioId());
        }
        
        // Verificar se já existe qualquer ticket para esta matrícula (independente do status)
        if (!uniqueTickets.isEmpty()) {
            TicketSantanderMongoEntity existingTicket = uniqueTickets.get(0); // Pega o primeiro ticket encontrado
            log.warn("Funcionário {} (matrícula: {}) já possui ticket - ID: {}, Status: {} - retornando ticket existente", 
                    funcionarioId, matricula, existingTicket.getId(), existingTicket.getStatus());
            TicketSantanderResponseDTO response = convertToResponseDTO(existingTicket);
            // Marcar como ticket existente para o controller identificar
            response.setObservacoes("TICKET_EXISTENTE: " + (response.getObservacoes() != null ? response.getObservacoes() : ""));
            return response;
        }
        
        // Obter informações de regional e contrato
        String regional = employeeInfo.getRegional();
        String contrato = employeeInfo.getNomeProjeto();
        
        // Buscar regional usando a relação correta se não estiver no employeeInfo
        if (regional == null && employeeInfo.getFilial_id() != null) {
            try {
                Long filialId = Long.parseLong(employeeInfo.getFilial_id());
                Optional<Regional> regionalOptional = regionalRepository.findByFilialId(filialId);
                if (regionalOptional.isPresent()) {
                    Regional regionalEntity = regionalOptional.get();
                    regional = regionalEntity.getRegional();
                }
            } catch (Exception e) {
                log.warn("Erro ao buscar regional por filial_id: {}", employeeInfo.getFilial_id(), e);
            }
        }
        
        TicketSantanderMongoEntity entity = TicketSantanderMongoEntity.builder()
                .funcionarioId(employeeInfo.getId())
                .nomeFuncionario(employeeInfo.getNome())
                .cpf(employeeInfo.getCpf())
                .matricula(employeeInfo.getMatricula())
                .email(employeeInfo.getEmail())
                .telefone(employeeInfo.getTelefone())
                .tipoTicket("SANTANDER_CARTEIRINHA")
                .descricao("Solicitação de carteirinha Santander")
                .prioridade("NORMAL")
                .categoria("SANTANDER")
                .numeroAgencia(request.getNumeroAgencia())
                .numeroConta(request.getNumeroConta())
                .contrato(contrato)
                .regional(regional)
                .carteirinhaAnexos(createAttachmentsFromMultipartFiles(request.getCarteirinhaAnexos()))
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .status("ABERTO")
                .build();
        
        TicketSantanderMongoEntity savedEntity = ticketSantanderMongoRepository.save(entity);
        
        return convertToResponseDTO(savedEntity);
    }
    
    public TicketSantanderResponseDTO getTicketById(String id) {
        log.info("Buscando ticket Santander por ID: {}", id);
        
        TicketSantanderMongoEntity entity = ticketSantanderMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));
        
        return convertToResponseDTO(entity);
    }
    
    
    public List<TicketSantanderResponseDTO> getTicketsByFuncionario(UUID funcionarioId) {
        log.info("Buscando tickets Santander por funcionário: {}", funcionarioId);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByFuncionarioId(funcionarioId);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByCpf(String cpf) {
        log.info("Buscando tickets Santander por CPF: {}", maskCpf(cpf));
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByCpf(cpf);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByStatus(String status) {
        log.info("Buscando tickets Santander por status: {}", status);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByStatus(status);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public Page<TicketSantanderResponseDTO> getAllTickets(Pageable pageable, String status) {
        Page<TicketSantanderMongoEntity> entitiesPage;
        
        // Se o status foi fornecido, filtrar por status usando paginação do repositório
        if (status != null && !status.trim().isEmpty()) {
            entitiesPage = ticketSantanderMongoRepository.findByStatus(status, pageable);
        } else {
            // Buscar todos e paginar em memória (mantendo a ordenação atual)
            List<TicketSantanderMongoEntity> allEntities = ticketSantanderMongoRepository.findAll();
            
            List<TicketSantanderMongoEntity> sortedEntities = allEntities.stream()
                    .sorted(Comparator
                            .comparing((TicketSantanderMongoEntity entity) -> !"ABERTO".equals(entity.getStatus()))
                            .thenComparing(TicketSantanderMongoEntity::getDataCriacao, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), sortedEntities.size());
            List<TicketSantanderMongoEntity> pagedEntities = sortedEntities.subList(start, end);
            
            List<TicketSantanderResponseDTO> content = pagedEntities.stream()
                    .map(this::convertToResponseDTO)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(content, pageable, sortedEntities.size());
        }
        
        // Quando há filtro por status, ordenar os resultados
        List<TicketSantanderMongoEntity> sortedContent = entitiesPage.getContent().stream()
                .sorted(Comparator
                        .comparing((TicketSantanderMongoEntity entity) -> !"ABERTO".equals(entity.getStatus()))
                        .thenComparing(TicketSantanderMongoEntity::getDataCriacao, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        
        List<TicketSantanderResponseDTO> content = sortedContent.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(content, pageable, entitiesPage.getTotalElements());
    }
    
    public TicketSantanderResponseDTO updateTicket(String id, UpdateTicketSantanderRequestDTO request) {
        log.info("Atualizando ticket Santander ID: {}", id);
        
        TicketSantanderMongoEntity entity = ticketSantanderMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));
        
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getPrioridade() != null) {
            entity.setPrioridade(request.getPrioridade());
        }
        if (request.getCategoria() != null) {
            entity.setCategoria(request.getCategoria());
        }
        if (request.getDescricao() != null) {
            entity.setDescricao(request.getDescricao());
        }
        
        entity.setDataAtualizacao(LocalDateTime.now());
        TicketSantanderMongoEntity updatedEntity = ticketSantanderMongoRepository.save(entity);
        
        log.info("Ticket Santander atualizado com sucesso - ID: {}", 
                updatedEntity.getId());
        
        return convertToResponseDTO(updatedEntity);
    }
    
    public TicketSantanderResponseDTO approveTicket(String id, ApproveTicketSantanderRequestDTO request) {
        log.info("Aprovando ticket Santander ID: {}", id);
        
        TicketSantanderMongoEntity entity = ticketSantanderMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));
        
        // Verificar se o ticket pode ser aprovado
        if (!"ABERTO".equals(entity.getStatus())) {
            throw new RuntimeException("Apenas tickets com status 'ABERTO' podem ser aprovados. Status atual: " + entity.getStatus());
        }
        
        // Atualizar status e observações
        entity.setStatus("FINALIZADO");
        if (request != null && request.getObservacao() != null && !request.getObservacao().trim().isEmpty()) {
            entity.setObservacoes(request.getObservacao());
        }
        entity.setDataAtualizacao(LocalDateTime.now());
        
        TicketSantanderMongoEntity updatedEntity = ticketSantanderMongoRepository.save(entity);
        
        String observacaoLog = (request != null && request.getObservacao() != null && !request.getObservacao().trim().isEmpty()) 
                ? request.getObservacao() : "Nenhuma observação fornecida";
        log.info("Ticket Santander aprovado com sucesso - ID: {} - Status: {} - Observação: {}", 
                updatedEntity.getId(), updatedEntity.getStatus(), observacaoLog);
        
        return convertToResponseDTO(updatedEntity);
    }
    
    public TicketSantanderResponseDTO rejectTicket(String id, RejectTicketSantanderRequestDTO request) {
        log.info("Rejeitando ticket Santander ID: {}", id);
        
        TicketSantanderMongoEntity entity = ticketSantanderMongoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket não encontrado com ID: " + id));
        
        // Verificar se o ticket pode ser rejeitado
        if (!"ABERTO".equals(entity.getStatus())) {
            throw new RuntimeException("Apenas tickets com status 'ABERTO' podem ser rejeitados. Status atual: " + entity.getStatus());
        }
        
        // Atualizar status e observações
        entity.setStatus("REJEITADO");
        entity.setObservacoes(request.getObservacao());
        entity.setDataAtualizacao(LocalDateTime.now());
        
        TicketSantanderMongoEntity updatedEntity = ticketSantanderMongoRepository.save(entity);
        
        log.info("Ticket Santander rejeitado com sucesso - ID: {} - Status: {} - Observação: {}", 
                updatedEntity.getId(), updatedEntity.getStatus(), request.getObservacao());
        
        return convertToResponseDTO(updatedEntity);
    }
    
    public void deleteTicket(String id) {
        log.info("Deletando ticket Santander ID: {}", id);
        
        if (!ticketSantanderMongoRepository.existsById(id)) {
            throw new RuntimeException("Ticket não encontrado com ID: " + id);
        }
        
        ticketSantanderMongoRepository.deleteById(id);
        
        log.info("Ticket Santander deletado com sucesso: {}", id);
    }
    
    public long countTicketsByStatus(String status) {
        return ticketSantanderMongoRepository.countByStatus(status);
    }
    
    public long countTicketsByFuncionario(UUID funcionarioId) {
        return ticketSantanderMongoRepository.countByFuncionarioId(funcionarioId);
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByAgencia(String numeroAgencia) {
        log.info("Buscando tickets Santander por agência: {}", numeroAgencia);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByNumeroAgencia(numeroAgencia);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByConta(String numeroConta) {
        log.info("Buscando tickets Santander por conta: {}", numeroConta);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByNumeroConta(numeroConta);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByAgenciaAndConta(String numeroAgencia, String numeroConta) {
        log.info("Buscando tickets Santander por agência: {} e conta: {}", numeroAgencia, numeroConta);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByCpfAndAgencia(String cpf, String numeroAgencia) {
        log.info("Buscando tickets Santander por CPF: {} e agência: {}", maskCpf(cpf), numeroAgencia);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByCpfAndNumeroAgencia(cpf, numeroAgencia);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    public List<TicketSantanderResponseDTO> getTicketsByCpfAndConta(String cpf, String numeroConta) {
        log.info("Buscando tickets Santander por CPF: {} e conta: {}", maskCpf(cpf), numeroConta);
        
        List<TicketSantanderMongoEntity> entities = ticketSantanderMongoRepository.findByCpfAndNumeroConta(cpf, numeroConta);
        
        return entities.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private TicketSantanderResponseDTO convertToResponseDTO(TicketSantanderMongoEntity entity) {
        // Buscar informações de cargo e contrato
        String cargo = null;
        String cargoNome = null;
        String contrato = null;
        Integer filialHcm = null;
        
        try {
            // Buscar informações do funcionário
            CompleteEmployeeDTO employeeInfo = getEmployeeUseCase.getEmployeeById(entity.getFuncionarioId());
            
            if (employeeInfo != null) {
                // Buscar cargo por cargo_id primeiro para obter tanto o ID quanto o nome
                if (employeeInfo.getCargo_id() != null) {
                    Optional<Cargo> cargoOptional = cargoRepository.findByIdHcm(employeeInfo.getCargo_id());
                    if (cargoOptional.isPresent()) {
                        Cargo cargoEntity = cargoOptional.get();
                        cargo = cargoEntity.getIdHcm();
                        cargoNome = cargoEntity.getNameTitle();
                    }
                }
                
                // Se não encontrou pelo cargo_id, usar cargoNome diretamente do CompleteEmployeeDTO se disponível
                if (cargoNome == null && employeeInfo.getCargoNome() != null) {
                    cargoNome = employeeInfo.getCargoNome();
                }
                
                // Buscar contrato por rateio_id
                if (employeeInfo.getRateio_id() != null) {
                    Optional<ContractProject> contractOptional = contractProjectRepository.findByRateio(employeeInfo.getRateio_id());
                    if (contractOptional.isPresent()) {
                        ContractProject contractEntity = contractOptional.get();
                        contrato = contractEntity.getProjectName();
                    }
                }
                
                // Se não encontrou o contrato pelo rateio_id, tentar usar o nomeProjeto do CompleteEmployeeDTO
                if (contrato == null && employeeInfo.getNomeProjeto() != null) {
                    contrato = employeeInfo.getNomeProjeto();
                }
                
                // Buscar filial_hcm
                filialHcm = employeeInfo.getFilial_id_hcm();
            }
            
            // Se ainda não encontrou dados e temos a matrícula, tentar buscar por matrícula
            if ((contrato == null || cargoNome == null || filialHcm == null) && entity.getMatricula() != null) {
                try {
                    EmployeeDTO employeeByMatricula = getEmployeeUseCase.getEmployeeByMatricula(entity.getMatricula());
                    if (employeeByMatricula != null) {
                        // Buscar contrato se ainda não encontrou
                        if (contrato == null && employeeByMatricula.getContrato() != null) {
                            contrato = employeeByMatricula.getContrato().getProjectName();
                        }
                        
                        // Buscar cargo se ainda não encontrou - usar cargo_id se disponível
                        if (cargoNome == null) {
                            String cargoIdToSearch = employeeByMatricula.getCargo_id() != null 
                                    ? employeeByMatricula.getCargo_id() 
                                    : employeeByMatricula.getCargo();
                            
                            if (cargoIdToSearch != null) {
                                Optional<Cargo> cargoOptional = cargoRepository.findByIdHcm(cargoIdToSearch);
                                if (cargoOptional.isPresent()) {
                                    Cargo cargoEntity = cargoOptional.get();
                                    cargo = cargoEntity.getIdHcm();
                                    cargoNome = cargoEntity.getNameTitle();
                                }
                            }
                        }
                        
                        // Buscar filialHcm se ainda não encontrou
                        if (filialHcm == null && employeeByMatricula.getFilial_id_hcm() != null) {
                            try {
                                filialHcm = Integer.parseInt(employeeByMatricula.getFilial_id_hcm());
                            } catch (NumberFormatException e) {
                                log.warn("Erro ao converter filial_id_hcm para Integer: {}", employeeByMatricula.getFilial_id_hcm());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Erro ao buscar dados por matrícula {} para ticket: {}", entity.getMatricula(), entity.getId(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar informações de cargo/contrato para ticket: {}", entity.getId(), e);
        }
        
        // Se ainda não encontrou o contrato, usar o que está salvo na entidade como fallback
        if (contrato == null) {
            contrato = entity.getContrato();
        }
        
        return TicketSantanderResponseDTO.builder()
                .id(entity.getId()) // Return complete String ID
                .funcionarioId(entity.getFuncionarioId())
                .nomeFuncionario(entity.getNomeFuncionario())
                .cpf(entity.getCpf())
                .matricula(entity.getMatricula())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .tipoTicket(entity.getTipoTicket())
                .descricao(entity.getDescricao())
                .status(entity.getStatus())
                .prioridade(entity.getPrioridade())
                .categoria(entity.getCategoria())
                .observacoes(entity.getObservacoes())
                .dataCriacao(entity.getDataCriacao())
                .dataAtualizacao(entity.getDataAtualizacao())
                .numeroAgencia(entity.getNumeroAgencia())
                .numeroConta(entity.getNumeroConta())
                .contrato(contrato)
                .regional(entity.getRegional())
                .cargo(cargo)
                .cargoNome(cargoNome)
                .filialHcm(filialHcm)
                .carteirinhaAnexos(entity.getCarteirinhaAnexos())
                .build();
    }
    
    private List<AttachmentEntity> createAttachmentsFromMultipartFiles(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "ticket_santander/carteirinhas");
    }
    
    
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        return "***" + cpf.substring(cpf.length() - 4);
    }
}

package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateLaborContractAddendumRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractAddendumMapper;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class CreateLaborContractAddendumUseCase {

    private final LaborContractRepository laborContractRepository;
    private final LaborContractAddendumRepository addendumRepository;
    private final LaborContractAddendumMapper addendumMapper;
    private final AttachmentService attachmentService;

    @Transactional
    public LaborContractAddendumResponseDTO execute(String contratoTrabalhistaId, 
                                                   @Valid CreateLaborContractAddendumRequestDTO request, 
                                                   String usuarioCriacao) {
        validateBusinessRules(contratoTrabalhistaId, request);
        
        // Calcular próxima sequência
        Integer proximaSequencia = calcularProximaSequencia(contratoTrabalhistaId);
        
        // Processar arquivos anexos
        List<AttachmentEntity> attachments = 
            processAttachments(request.getArquivosAnexos());
        
        // Criar entidade
        LaborContractAddendum addendum = addendumMapper.toEntity(request, contratoTrabalhistaId, usuarioCriacao, attachments);
        addendum.setSequencia(proximaSequencia);
        
        // Criar log de auditoria para criação do aditivo (apenas se usuário foi fornecido)
        if (usuarioCriacao != null && !usuarioCriacao.isEmpty()) {
            try {
                StepLog creationLog = createStepLog(
                    "CRIACAO_ADITIVO_" + request.getTipo().name(),
                    UUID.fromString(usuarioCriacao),
                    "Aditivo de " + request.getTipo().name() + " criado no sistema"
                );
                
                List<StepLog> stepLogs = new ArrayList<>();
                stepLogs.add(creationLog);
                addendum.setStepLog(stepLogs);
            } catch (IllegalArgumentException e) {
                // Se o usuarioCriacao não for um UUID válido, apenas registra o nome
                addendum.setUsuarioCriacao(usuarioCriacao);
            }
        }
        
        // Salvar no banco
        LaborContractAddendum savedAddendum = addendumRepository.save(addendum);
        
        return addendumMapper.toResponseDTO(savedAddendum);
    }
    
    /**
     * Calcula a próxima sequência para o aditivo
     * Busca o maior número de sequência existente e adiciona 1
     * Se não houver aditivos, começa com 1
     */
    private Integer calcularProximaSequencia(String contratoTrabalhistaId) {
        LaborContractAddendum ultimoAditivo = addendumRepository
                .findFirstByContratoTrabalhistaIdOrderBySequenciaDesc(contratoTrabalhistaId);
        
        if (ultimoAditivo == null || ultimoAditivo.getSequencia() == null) {
            return 1;
        }
        
        return ultimoAditivo.getSequencia() + 1;
    }
    
    private void validateBusinessRules(String contratoTrabalhistaId, CreateLaborContractAddendumRequestDTO request) {
        // Validar se o contrato trabalhista existe
        LaborContract contrato = laborContractRepository.findById(contratoTrabalhistaId)
                .orElseThrow(() -> new RuntimeException("ERRO: Contrato trabalhista não encontrado com ID '" + contratoTrabalhistaId + "'."));
        
        // Validar se o contrato está ativo
        if (!"ATIVO".equals(contrato.getStatusRegistro())) {
            throw new RuntimeException("ERRO: Apenas contratos ativos podem ter aditivos.");
        }
        
        // Validação por data removida - a sequência é usada para diferenciar múltiplos aditivos na mesma data
    }
    
    private List<AttachmentEntity> processAttachments(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "labor_contracts/addendums");
    }
    
    private StepLog createStepLog(String name, UUID userId, String observation) {
        return StepLog.builder()
            .id(UUID.randomUUID())
            .name(name)
            .user(userId)
            .created_at(Date.from(Instant.now()))
            .final_at(Date.from(Instant.now()))
            .step(1)
            .observation(observation)
            .build();
    }
}

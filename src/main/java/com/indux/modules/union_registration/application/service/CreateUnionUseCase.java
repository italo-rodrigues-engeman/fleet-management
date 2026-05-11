package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateUnionRequestDTO;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
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
public class CreateUnionUseCase {
    
    private final UnionRepository unionRepository;
    private final UnionMapper unionMapper;
    private final AttachmentService attachmentService;
    
    @Transactional
    public UnionResponseDTO execute(@Valid CreateUnionRequestDTO request, String usuarioCriacao) {
        validateBusinessRules(request);
        
        List<AttachmentEntity> attachments = createAttachmentsFromMultipartFiles(request.getDocumentosAnexos());
        
        Union union = unionMapper.toEntity(request, attachments, null);
        
        // Criar log de auditoria para criação do sindicato (apenas se usuário foi fornecido)
        if (usuarioCriacao != null && !usuarioCriacao.isEmpty()) {
            try {
                StepLog creationLog = createStepLog(
                    "CRIACAO_SINDICATO",
                    UUID.fromString(usuarioCriacao),
                    "Sindicato criado no sistema"
                );
                
                List<StepLog> stepLogs = new ArrayList<>();
                stepLogs.add(creationLog);
                union.setStepLog(stepLogs);
            } catch (IllegalArgumentException e) {
                // Se o usuarioCriacao não for um UUID válido, apenas registra o nome
                union.setUsuarioCriacao(usuarioCriacao);
            }
        }
        
        Union savedUnion = unionRepository.save(union);
        
        return unionMapper.toResponseDTO(savedUnion);
    }
    
    private void validateBusinessRules(CreateUnionRequestDTO request) {
        if (unionRepository.existsByCnpj(request.getCnpj())) {
            throw new RuntimeException("Já existe um sindicato cadastrado com este CNPJ");
        }
    }
    
    private List<AttachmentEntity> createAttachmentsFromMultipartFiles(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "unions/documents");
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

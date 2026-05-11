package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
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
public class CreateLaborContractUseCase {
    
    private final LaborContractRepository laborContractRepository;
    private final UnionRepository unionRepository;
    private final LaborContractMapper laborContractMapper;
    private final AttachmentService attachmentService;
    
    @Transactional
    public LaborContractResponseDTO execute(@Valid CreateLaborContractRequestDTO request, String usuarioCriacao) {
        validateBusinessRules(request);
        
        // Processar arquivos anexos
        List<AttachmentEntity> attachments = processAttachments(request.getArquivosInstrumento());
        
        // Criar entidade
        LaborContract laborContract = laborContractMapper.toEntity(request, attachments, usuarioCriacao);
        
        // Criar log de auditoria para criação do ACT/CCT (apenas se usuário foi fornecido)
        if (usuarioCriacao != null && !usuarioCriacao.isEmpty()) {
            try {
                StepLog creationLog = createStepLog(
                    "CRIACAO_" + request.getTipoInstrumento().name(),
                    UUID.fromString(usuarioCriacao),
                    request.getTipoInstrumento().name() + " criado no sistema"
                );
                
                List<StepLog> stepLogs = new ArrayList<>();
                stepLogs.add(creationLog);
                laborContract.setStepLog(stepLogs);
            } catch (IllegalArgumentException e) {
                // Se o usuarioCriacao não for um UUID válido, apenas registra o nome
                laborContract.setUsuarioCriacao(usuarioCriacao);
            }
        }
        
        // Salvar no banco
        LaborContract savedContract = laborContractRepository.save(laborContract);
        
        // Buscar informações do sindicato para resposta
        Union union = unionRepository.findById(request.getSindicatoTrabalhadoresId()).orElse(null);
        
        return laborContractMapper.toResponseDTOWithUnionInfo(savedContract, union);
    }
    
    private void validateBusinessRules(CreateLaborContractRequestDTO request) {
        // Validar se o sindicato existe
        if (!unionRepository.existsById(request.getSindicatoTrabalhadoresId())) {
            throw new RuntimeException("ERRO: Sindicato não encontrado com ID '" + request.getSindicatoTrabalhadoresId() + "'. Verifique se o sindicato foi cadastrado corretamente.");
        }
        
        // Validar datas
        if (request.getDataInicioVigencia().isAfter(request.getDataFimVigencia())) {
            throw new RuntimeException("ERRO: Data de início da vigência (" + request.getDataInicioVigencia() + ") deve ser anterior à data de fim (" + request.getDataFimVigencia() + ").");
        }
        
        // Validar campos condicionais
        if ("Municipal".equals(request.getAbrangenciaTerritorial()) && 
            (request.getMunicipiosAbrangidos() == null || request.getMunicipiosAbrangidos().isEmpty())) {
            throw new RuntimeException("ERRO: Municípios abrangidos são obrigatórios quando a abrangência territorial é 'Municipal'.");
        }
    }
    
    private List<AttachmentEntity> processAttachments(List<MultipartFile> multipartFiles) {
        return attachmentService.createAttachmentsFromMultipartFiles(multipartFiles, "labor_contracts/documents");
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

package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateUnionRequestDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Validated
public class UpdateUnionUseCase {
    
    private final UnionRepository unionRepository;
    private final UnionMapper unionMapper;
    private final AttachmentService attachmentService;
    
    @Transactional
    public UnionResponseDTO execute(String unionId, @Valid UpdateUnionRequestDTO request, String usuarioAtualizacao) {
        Union existingUnion = unionRepository.findById(unionId)
                .orElseThrow(() -> new UnionNotFoundException("Sindicato não encontrado com ID: " + unionId));
        
        validateBusinessRules(request, existingUnion);
        
        updateUnionFields(existingUnion, request);
        
        if (request.getDocumentosAnexos() != null && !request.getDocumentosAnexos().isEmpty()) {
            List<AttachmentEntity> newAttachments = createAttachmentsFromMultipartFiles(request.getDocumentosAnexos());
            if (existingUnion.getDocumentosAnexos() == null) {
                existingUnion.setDocumentosAnexos(new ArrayList<>());
            }
            existingUnion.getDocumentosAnexos().addAll(newAttachments);
        }
        
        // Adicionar log de auditoria para atualização (apenas se usuário foi fornecido)
        if (usuarioAtualizacao != null && !usuarioAtualizacao.isEmpty()) {
            try {
                StepLog updateLog = createStepLog(
                    "ATUALIZACAO_SINDICATO",
                    UUID.fromString(usuarioAtualizacao),
                    "Sindicato atualizado"
                );
                
                if (existingUnion.getStepLog() == null) {
                    existingUnion.setStepLog(new ArrayList<>());
                }
                existingUnion.getStepLog().add(updateLog);
            } catch (IllegalArgumentException e) {
                // Se não for um UUID válido, apenas registra o nome
            }
        }
        existingUnion.setDataUltimaAtualizacao(LocalDateTime.now());
        existingUnion.setUsuarioUltimaAtualizacao(usuarioAtualizacao);
        
        Union savedUnion = unionRepository.save(existingUnion);
        
        return unionMapper.toResponseDTO(savedUnion);
    }
    
    private void validateBusinessRules(UpdateUnionRequestDTO request, Union existingUnion) {
        // Validar CNPJ único se fornecido e diferente do atual
        if (request.getCnpj() != null && !request.getCnpj().equals(existingUnion.getCnpj())) {
            if (unionRepository.existsByCnpj(request.getCnpj())) {
                throw new RuntimeException("Já existe um sindicato cadastrado com este CNPJ");
            }
        }
    }
    
    private void updateUnionFields(Union union, UpdateUnionRequestDTO request) {
        if (request.getNomeCompletoSindicato() != null) {
            union.setNomeCompletoSindicato(request.getNomeCompletoSindicato());
        }
        if (request.getCnpj() != null) {
            union.setCnpj(request.getCnpj());
        }
        if (request.getCodigoCnes() != null) {
            union.setCodigoCnes(request.getCodigoCnes());
        }
        if (request.getTipo() != null) {
            union.setTipo(request.getTipo());
        }
        if (request.getCategoriaRepresentada() != null) {
            union.setCategoriaRepresentada(request.getCategoriaRepresentada());
        }
        if (request.getAbrangenciaTerritorial() != null) {
            union.setAbrangenciaTerritorial(request.getAbrangenciaTerritorial());
        }
        if (request.getUfSede() != null) {
            union.setUfSede(request.getUfSede());
        }
        if (request.getMunicipioSede() != null) {
            union.setMunicipioSede(request.getMunicipioSede());
        }
        if (request.getUfsAtendidas() != null) {
            union.setUfsAtendidas(request.getUfsAtendidas());
        }
        if (request.getMunicipiosAtendidos() != null) {
            union.setMunicipiosAtendidos(request.getMunicipiosAtendidos());
        }
        if (request.getObservacoesTerritoriais() != null) {
            union.setObservacoesTerritoriais(request.getObservacoesTerritoriais());
        }
        if (request.getLogradouro() != null) {
            union.setLogradouro(request.getLogradouro());
        }
        if (request.getNumero() != null) {
            union.setNumero(request.getNumero());
        }
        if (request.getUf() != null) {
            union.setUf(request.getUf());
        }
        if (request.getCep() != null) {
            union.setCep(request.getCep());
        }
        if (request.getCidade() != null) {
            union.setCidade(request.getCidade());
        }
        if (request.getTelefonePrincipal() != null) {
            union.setTelefonePrincipal(request.getTelefonePrincipal());
        }
        if (request.getEmailInstitucional() != null) {
            union.setEmailInstitucional(request.getEmailInstitucional());
        }
        if (request.getSituacaoMte() != null) {
            union.setSituacaoMte(request.getSituacaoMte());
        }
        if (request.getDataUltimaAtualizacaoMte() != null) {
            union.setDataUltimaAtualizacaoMte(request.getDataUltimaAtualizacaoMte());
        }
        if (request.getPresidenteAtual() != null) {
            union.setPresidenteAtual(request.getPresidenteAtual());
        }
        if (request.getMandatoInicio() != null) {
            union.setMandatoInicio(request.getMandatoInicio());
        }
        if (request.getMandatoFim() != null) {
            union.setMandatoFim(request.getMandatoFim());
        }
        if (request.getObservacoes() != null) {
            union.setObservacoes(request.getObservacoes());
        }
        if (request.getStatusRegistro() != null) {
            union.setStatusRegistro(request.getStatusRegistro());
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

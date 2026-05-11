package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateUnionRequestDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UpdateUnionUseCaseTest {
    
    private UnionRepository unionRepository;
    private UnionMapper unionMapper;
    private AttachmentService attachmentService;
    private UpdateUnionUseCase updateUnionUseCase;
    
    @BeforeEach
    void setUp() {
        unionRepository = mock(UnionRepository.class);
        unionMapper = mock(UnionMapper.class);
        attachmentService = mock(AttachmentService.class);
        updateUnionUseCase = new UpdateUnionUseCase(unionRepository, unionMapper, attachmentService);
    }
    
    @Test
    @DisplayName("Deve atualizar sindicato com sucesso")
    void shouldUpdateUnionSuccessfully() {
        String unionId = "64f8b2c1234567890abcdef1";
        UpdateUnionRequestDTO request = createUpdateRequest();
        Union existingUnion = createExistingUnion();
        UnionResponseDTO expectedResponse = createUnionResponse();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(existingUnion));
        when(unionRepository.existsByCnpj(anyString())).thenReturn(false);
        when(unionRepository.save(any(Union.class))).thenReturn(existingUnion);
        when(unionMapper.toResponseDTO(any(Union.class))).thenReturn(expectedResponse);
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenReturn(List.of(new AttachmentEntity()));
        
        UnionResponseDTO result = updateUnionUseCase.execute(unionId, request, "usuario.teste");
        
        assertNotNull(result);
        assertEquals("Sindicato Atualizado", existingUnion.getNomeCompletoSindicato());
        assertEquals("98.765.432/0001-10", existingUnion.getCnpj());
        
        verify(unionRepository).findById(unionId);
        verify(unionRepository).save(existingUnion);
        verify(unionMapper).toResponseDTO(existingUnion);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe")
    void shouldThrowExceptionWhenUnionNotFound() {
        String unionId = "64f8b2c1234567890abcdef1";
        UpdateUnionRequestDTO request = createUpdateRequest();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> updateUnionUseCase.execute(unionId, request, "usuario.teste"));
        
        assertEquals("Sindicato não encontrado com ID: " + unionId, exception.getMessage());
        verify(unionRepository).findById(unionId);
        verify(unionRepository, never()).save(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        String unionId = "64f8b2c1234567890abcdef1";
        UpdateUnionRequestDTO request = new UpdateUnionRequestDTO();
        request.setNomeCompletoSindicato("Novo Nome");
        request.setTelefonePrincipal("(11) 9999-8888");
        // Outros campos ficam null
        
        Union existingUnion = createExistingUnion();
        String originalCnpj = existingUnion.getCnpj();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(existingUnion));
        when(unionRepository.save(any(Union.class))).thenReturn(existingUnion);
        when(unionMapper.toResponseDTO(any(Union.class))).thenReturn(createUnionResponse());
        
        updateUnionUseCase.execute(unionId, request, "usuario.teste");
        
        // Campos atualizados
        assertEquals("Novo Nome", existingUnion.getNomeCompletoSindicato());
        assertEquals("(11) 9999-8888", existingUnion.getTelefonePrincipal());
        
        // Campos não alterados
        assertEquals(originalCnpj, existingUnion.getCnpj());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNPJ já existe")
    void shouldThrowExceptionWhenCnpjAlreadyExists() {
        String unionId = "64f8b2c1234567890abcdef1";
        UpdateUnionRequestDTO request = new UpdateUnionRequestDTO();
        request.setCnpj("11.222.333/0001-44"); // CNPJ diferente do atual
        
        Union existingUnion = createExistingUnion();
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(existingUnion));
        when(unionRepository.existsByCnpj("11.222.333/0001-44")).thenReturn(true);
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> updateUnionUseCase.execute(unionId, request, "usuario.teste"));
        
        assertEquals("Já existe um sindicato cadastrado com este CNPJ", exception.getMessage());
        verify(unionRepository).existsByCnpj("11.222.333/0001-44");
        verify(unionRepository, never()).save(any(Union.class));
    }
    
    
    @Test
    @DisplayName("Deve adicionar novos documentos aos existentes")
    void shouldAddNewDocumentsToExisting() {
        String unionId = "64f8b2c1234567890abcdef1";
        UpdateUnionRequestDTO request = new UpdateUnionRequestDTO();
        
        MultipartFile mockFile = mock(MultipartFile.class);
        request.setDocumentosAnexos(List.of(mockFile));
        
        Union existingUnion = createExistingUnion();
        int originalDocsCount = existingUnion.getDocumentosAnexos() != null ? 
                               existingUnion.getDocumentosAnexos().size() : 0;
        
        when(unionRepository.findById(unionId)).thenReturn(Optional.of(existingUnion));
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("novo_documento.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        List<AttachmentEntity> mockAttachments = List.of(new AttachmentEntity());
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenReturn(mockAttachments);
        when(unionRepository.save(any(Union.class))).thenReturn(existingUnion);
        when(unionMapper.toResponseDTO(any(Union.class))).thenReturn(createUnionResponse());
        
        updateUnionUseCase.execute(unionId, request, "usuario.teste");
        
        // Verifica se foi adicionado um novo documento
        assertNotNull(existingUnion.getDocumentosAnexos());
        assertEquals(originalDocsCount + 1, existingUnion.getDocumentosAnexos().size());
        
        verify(attachmentService).createAttachmentsFromMultipartFiles(eq(List.of(mockFile)), eq("unions/documents"));
    }
    
    private UpdateUnionRequestDTO createUpdateRequest() {
        UpdateUnionRequestDTO request = new UpdateUnionRequestDTO();
        request.setNomeCompletoSindicato("Sindicato Atualizado");
        request.setCnpj("98.765.432/0001-10");
        request.setTelefonePrincipal("(11) 8888-7777");
        request.setEmailInstitucional("atualizado@sindicato.org.br");
        return request;
    }
    
    private Union createExistingUnion() {
        Union union = new Union();
        union.setId("64f8b2c1234567890abcdef1");
        union.setNomeCompletoSindicato("Sindicato Original");
        union.setCnpj("12.345.678/0001-90");
        union.setTelefonePrincipal("(11) 1234-5678");
        union.setEmailInstitucional("original@sindicato.org.br");
        union.setStatusRegistro("ATIVO");
        return union;
    }
    
    private UnionResponseDTO createUnionResponse() {
        UnionResponseDTO response = new UnionResponseDTO();
        response.setId("64f8b2c1234567890abcdef1");
        response.setNomeCompletoSindicato("Sindicato Atualizado");
        response.setCnpj("98.765.432/0001-10");
        response.setStatusRegistro("ATIVO");
        return response;
    }
}


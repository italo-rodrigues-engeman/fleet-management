package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateUnionRequestDTO;
import com.indux.modules.union_registration.application.dto.UnionResponseDTO;
import com.indux.modules.union_registration.application.mapper.UnionMapper;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CreateUnionUseCaseTest {
    
    private UnionRepository unionRepository;
    private UnionMapper unionMapper;
    private AttachmentService attachmentService;
    private CreateUnionUseCase createUnionUseCase;
    
    @BeforeEach
    void setUp() {
        unionRepository = mock(UnionRepository.class);
        unionMapper = mock(UnionMapper.class);
        attachmentService = mock(AttachmentService.class);
        createUnionUseCase = new CreateUnionUseCase(unionRepository, unionMapper, attachmentService);
    }
    
    @Test
    @DisplayName("Deve cadastrar sindicato com sucesso")
    void shouldCreateUnionSuccessfully() {
        CreateUnionRequestDTO request = createValidUnionRequest();
        Union union = createUnionEntity();
        UnionResponseDTO expectedResponse = createUnionResponse();
        
        when(unionRepository.existsByCnpj(anyString())).thenReturn(false);
        when(unionMapper.toEntity(any(CreateUnionRequestDTO.class), any(List.class), eq(null))).thenReturn(union);
        when(unionRepository.save(any(Union.class))).thenReturn(union);
        when(unionMapper.toResponseDTO(any(Union.class))).thenReturn(expectedResponse);
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenReturn(List.of(new AttachmentEntity()));
        
        UnionResponseDTO result = createUnionUseCase.execute(request, "usuario.teste");
        
        assertNotNull(result);
        assertEquals(expectedResponse.getNomeCompletoSindicato(), result.getNomeCompletoSindicato());
        assertEquals(expectedResponse.getCnpj(), result.getCnpj());
        
        verify(unionRepository).existsByCnpj(request.getCnpj());
        verify(unionRepository).save(any(Union.class));
        verify(unionMapper).toEntity(eq(request), any(List.class), eq(null));
        verify(unionMapper).toResponseDTO(union);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNPJ já existe")
    void shouldThrowExceptionWhenCnpjAlreadyExists() {
        CreateUnionRequestDTO request = createValidUnionRequest();
        
        when(unionRepository.existsByCnpj(request.getCnpj())).thenReturn(true);
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createUnionUseCase.execute(request, "usuario.teste"));
        
        assertEquals("Já existe um sindicato cadastrado com este CNPJ", exception.getMessage());
        verify(unionRepository).existsByCnpj(request.getCnpj());
        verify(unionRepository, never()).save(any(Union.class));
    }
    
    @Test
    @DisplayName("Deve processar anexos quando fornecidos")
    void shouldProcessAttachmentsWhenProvided() {
        CreateUnionRequestDTO request = createValidUnionRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> attachments = List.of(mockFile);
        request.setDocumentosAnexos(attachments);
        
        Union union = createUnionEntity();
        UnionResponseDTO expectedResponse = createUnionResponse();
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("documento.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        when(unionRepository.existsByCnpj(anyString())).thenReturn(false);
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setNome("documento.pdf");
        List<AttachmentEntity> mockAttachments = List.of(attachment);
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenReturn(mockAttachments);
        when(unionMapper.toEntity(any(CreateUnionRequestDTO.class), any(List.class), eq(null))).thenReturn(union);
        when(unionRepository.save(any(Union.class))).thenReturn(union);
        when(unionMapper.toResponseDTO(any(Union.class))).thenReturn(expectedResponse);
        
        UnionResponseDTO result = createUnionUseCase.execute(request, "usuario.teste");
        
        assertNotNull(result);
        verify(attachmentService, times(1)).createAttachmentsFromMultipartFiles(any(), anyString());
        
        ArgumentCaptor<List<AttachmentEntity>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(unionMapper).toEntity(eq(request), attachmentsCaptor.capture(), eq(null));
        
        List<AttachmentEntity> capturedAttachments = attachmentsCaptor.getValue();
        assertFalse(capturedAttachments.isEmpty());
        assertEquals("documento.pdf", capturedAttachments.get(0).getNome());
    }
    
    private CreateUnionRequestDTO createValidUnionRequest() {
        CreateUnionRequestDTO request = new CreateUnionRequestDTO();
        request.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste");
        request.setCnpj("12.345.678/0001-90");
        request.setCodigoCnes("12345");
        request.setTipo("TRABALHADORES");
        request.setCategoriaRepresentada("Categoria de Teste");
        request.setAbrangenciaTerritorial("MUNICIPAL");
        request.setUfSede(List.of("SP"));
        request.setMunicipioSede(List.of("São Paulo"));
        request.setUfsAtendidas(List.of("SP"));
        request.setMunicipiosAtendidos(List.of("São Paulo"));
        request.setObservacoesTerritoriais("Observações de teste");
        request.setLogradouro("Rua de Teste");
        request.setNumero("123");
        request.setUf("SP");
        request.setCep("01234-567");
        request.setCidade("São Paulo");
        request.setTelefonePrincipal("(11) 1234-5678");
        request.setEmailInstitucional("contato@sindicatoteste.com.br");
        request.setSituacaoMte("ATIVO");
        request.setDataUltimaAtualizacaoMte(LocalDate.now());
        request.setPresidenteAtual("Presidente de Teste");
        request.setMandatoInicio(LocalDate.now());
        request.setMandatoFim(LocalDate.now().plusYears(4));
        request.setObservacoes("Observações gerais de teste");
        request.setUsuarioCriacao("usuario.teste");
        request.setStatusRegistro("ATIVO");
        return request;
    }
    
    private Union createUnionEntity() {
        Union union = new Union();
        union.setId("64f8b2c1234567890abcdef1");
        union.setCodeID(12345);
        union.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste");
        union.setCnpj("12.345.678/0001-90");
        union.setStatusRegistro("ATIVO");
        return union;
    }
    
    private UnionResponseDTO createUnionResponse() {
        UnionResponseDTO response = new UnionResponseDTO();
        response.setId("64f8b2c1234567890abcdef1");
        response.setCodeID(12345);
        response.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Teste");
        response.setCnpj("12.345.678/0001-90");
        response.setStatusRegistro("ATIVO");
        return response;
    }
}

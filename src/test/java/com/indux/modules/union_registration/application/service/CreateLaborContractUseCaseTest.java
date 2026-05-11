package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CreateLaborContractUseCaseTest {
    
    private LaborContractRepository laborContractRepository;
    private UnionRepository unionRepository;
    private LaborContractMapper laborContractMapper;
    private AttachmentService attachmentService;
    private CreateLaborContractUseCase createLaborContractUseCase;
    
    @BeforeEach
    void setUp() {
        laborContractRepository = mock(LaborContractRepository.class);
        unionRepository = mock(UnionRepository.class);
        laborContractMapper = mock(LaborContractMapper.class);
        attachmentService = mock(AttachmentService.class);
        createLaborContractUseCase = new CreateLaborContractUseCase(
            laborContractRepository, unionRepository, laborContractMapper, attachmentService);
    }
    
    @Test
    @DisplayName("Deve criar contrato trabalhista com sucesso")
    void shouldCreateLaborContractSuccessfully() {
        CreateLaborContractRequestDTO request = createValidLaborContractRequest();
        Union union = createUnionEntity();
        LaborContract laborContract = createLaborContractEntity();
        LaborContractResponseDTO expectedResponse = createLaborContractResponse();
        
        when(unionRepository.existsById(request.getSindicatoTrabalhadoresId())).thenReturn(true);
        when(unionRepository.findById(request.getSindicatoTrabalhadoresId())).thenReturn(java.util.Optional.of(union));
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString())).thenReturn(List.of());
        when(laborContractMapper.toEntity(any(CreateLaborContractRequestDTO.class), any(List.class), anyString()))
            .thenReturn(laborContract);
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(laborContract);
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(expectedResponse);
        
        LaborContractResponseDTO result = createLaborContractUseCase.execute(request, "usuario.teste");
        
        assertNotNull(result);
        assertEquals(expectedResponse.getTipoInstrumento(), result.getTipoInstrumento());
        assertEquals(expectedResponse.getNumeroIdentificacaoInterno(), result.getNumeroIdentificacaoInterno());
        
        verify(unionRepository).findById(request.getSindicatoTrabalhadoresId());
        verify(laborContractRepository).save(any(LaborContract.class));
        verify(laborContractMapper).toResponseDTOWithUnionInfo(laborContract, union);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando sindicato não existe")
    void shouldThrowExceptionWhenUnionNotFound() {
        CreateLaborContractRequestDTO request = createValidLaborContractRequest();
        
        when(unionRepository.existsById(request.getSindicatoTrabalhadoresId())).thenReturn(false);
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createLaborContractUseCase.execute(request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("ERRO: Sindicato não encontrado"));
        verify(unionRepository).existsById(request.getSindicatoTrabalhadoresId());
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    
    @Test
    @DisplayName("Deve processar anexos quando fornecidos")
    void shouldProcessAttachmentsWhenProvided() {
        CreateLaborContractRequestDTO request = createValidLaborContractRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> attachments = List.of(mockFile);
        request.setArquivosInstrumento(attachments);
        
        Union union = createUnionEntity();
        LaborContract laborContract = createLaborContractEntity();
        LaborContractResponseDTO expectedResponse = createLaborContractResponse();
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("contrato.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        when(unionRepository.existsById(request.getSindicatoTrabalhadoresId())).thenReturn(true);
        when(unionRepository.findById(request.getSindicatoTrabalhadoresId())).thenReturn(java.util.Optional.of(union));
        
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setNome("contrato.pdf");
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenReturn(List.of(attachment));
        
        when(laborContractMapper.toEntity(any(CreateLaborContractRequestDTO.class), any(List.class), anyString()))
            .thenReturn(laborContract);
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(laborContract);
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(expectedResponse);
        
        LaborContractResponseDTO result = createLaborContractUseCase.execute(request, "usuario.teste");
        
        assertNotNull(result);
        verify(attachmentService).createAttachmentsFromMultipartFiles(any(), eq("labor_contracts/documents"));
        
        ArgumentCaptor<List<AttachmentEntity>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(laborContractMapper).toEntity(eq(request), attachmentsCaptor.capture(), eq("usuario.teste"));
        
        List<AttachmentEntity> capturedAttachments = attachmentsCaptor.getValue();
        assertFalse(capturedAttachments.isEmpty());
        assertEquals("contrato.pdf", capturedAttachments.get(0).getNome());
    }
    
    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void shouldValidateRequiredFields() {
        CreateLaborContractRequestDTO request = new CreateLaborContractRequestDTO();
        // Campos obrigatórios não preenchidos
        
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createLaborContractUseCase.execute(request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("ERRO:"));
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve incluir campos de direitos trabalhistas")
    void shouldIncludeLaborRightsFields() {
        CreateLaborContractRequestDTO request = createValidLaborContractRequest();
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.500,00");
        laborRights.setSalary(salary);
        
        BenefitsDTO benefits = new BenefitsDTO();
        benefits.setPremioDesempenho(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("Participação nos lucros")
            .build());
        laborRights.setBenefits(benefits);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        workSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("50% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        
        Union union = createUnionEntity();
        LaborContract laborContract = createLaborContractEntity();
        LaborContractResponseDTO expectedResponse = createLaborContractResponse();
        
        when(unionRepository.existsById(request.getSindicatoTrabalhadoresId())).thenReturn(true);
        when(unionRepository.findById(request.getSindicatoTrabalhadoresId())).thenReturn(java.util.Optional.of(union));
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString())).thenReturn(List.of());
        when(laborContractMapper.toEntity(any(CreateLaborContractRequestDTO.class), any(List.class), anyString()))
            .thenReturn(laborContract);
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(laborContract);
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(expectedResponse);
        
        LaborContractResponseDTO result = createLaborContractUseCase.execute(request, "usuario.teste");
        
        assertNotNull(result);
        
        ArgumentCaptor<CreateLaborContractRequestDTO> requestCaptor = ArgumentCaptor.forClass(CreateLaborContractRequestDTO.class);
        verify(laborContractMapper).toEntity(requestCaptor.capture(), any(List.class), eq("usuario.teste"));
        
        CreateLaborContractRequestDTO capturedRequest = requestCaptor.getValue();
        assertNotNull(capturedRequest.getLaborRights());
        assertNotNull(capturedRequest.getLaborRights().getSalary());
        assertNotNull(capturedRequest.getLaborRights().getBenefits());
        assertNotNull(capturedRequest.getLaborRights().getWorkSchedule());
        
        assertEquals("R$ 1.500,00", capturedRequest.getLaborRights().getSalary().getPisoSalarial());
        assertEquals("Participação nos lucros", capturedRequest.getLaborRights().getBenefits().getPremioDesempenho().getObservacao());
        assertEquals("50% sobre salário", capturedRequest.getLaborRights().getWorkSchedule().getHoraExtra1().getObservacao());
    }
    
    @Test
    @DisplayName("Deve lançar exceção em caso de erro de armazenamento")
    void shouldThrowExceptionOnStorageError() {
        CreateLaborContractRequestDTO request = createValidLaborContractRequest();
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> attachments = List.of(mockFile);
        request.setArquivosInstrumento(attachments);
        
        Union union = createUnionEntity();
        LaborContract laborContract = createLaborContractEntity();
        LaborContractResponseDTO expectedResponse = createLaborContractResponse();
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("contrato.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        
        when(unionRepository.existsById(request.getSindicatoTrabalhadoresId())).thenReturn(true);
        when(unionRepository.findById(request.getSindicatoTrabalhadoresId())).thenReturn(java.util.Optional.of(union));
        when(attachmentService.createAttachmentsFromMultipartFiles(any(), anyString()))
            .thenThrow(new RuntimeException("Storage error"));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createLaborContractUseCase.execute(request, "usuario.teste"));
        
        assertEquals("Storage error", exception.getMessage());
        verify(attachmentService).createAttachmentsFromMultipartFiles(any(), eq("labor_contracts/documents"));
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    private CreateLaborContractRequestDTO createValidLaborContractRequest() {
        CreateLaborContractRequestDTO request = new CreateLaborContractRequestDTO();
        request.setTipoInstrumento(TipoInstrumento.ACT);
        request.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        request.setDataInicioVigencia(LocalDate.now());
        request.setDataFimVigencia(LocalDate.now().plusYears(1));
        request.setDataBase(LocalDate.now());
        request.setAbrangenciaTerritorial("Estadual");
        request.setUfPrincipal(List.of("SP"));
        request.setSubcategoriaCBO("1114");
        request.setEmpresasSignatarias(List.of("Empresa ABC Ltda", "Empresa XYZ S.A."));
        request.setSituacaoMTE("Aprovado");
        // Campos de usuário e status são definidos internamente
        return request;
    }
    
    private Union createUnionEntity() {
        Union union = new Union();
        union.setId("68cd2d492329c37e18b00944");
        union.setNomeCompletoSindicato("Sindicato dos Trabalhadores em Tecnologia");
        union.setCnpj("12.345.678/0001-90");
        union.setStatusRegistro("ATIVO");
        return union;
    }
    
    private LaborContract createLaborContractEntity() {
        LaborContract laborContract = new LaborContract();
        laborContract.setId("contrato123");
        laborContract.setTipoInstrumento(TipoInstrumento.ACT);
        laborContract.setNumeroIdentificacaoInterno("ACT-2024-001");
        laborContract.setNomeInstrumento("Acordo Coletivo de Trabalho 2024");
        laborContract.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        laborContract.setStatusRegistro("ATIVO");
        laborContract.setDataCriacao(LocalDateTime.now());
        return laborContract;
    }
    
    private LaborContractResponseDTO createLaborContractResponse() {
        LaborContractResponseDTO response = new LaborContractResponseDTO();
        response.setId("contrato123");
        response.setTipoInstrumento(TipoInstrumento.ACT);
        response.setNumeroIdentificacaoInterno("ACT-2024-001");
        response.setNomeInstrumento("Acordo Coletivo de Trabalho 2024");
        response.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        response.setNomeSindicato("Sindicato dos Trabalhadores em Tecnologia");
        response.setSiglaSindicato("STT");
        response.setStatusRegistro("ATIVO");
        response.setDataCriacao(LocalDateTime.now());
        return response;
    }
}
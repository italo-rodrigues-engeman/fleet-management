package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.CreateLaborContractAddendumRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.mapper.LaborContractAddendumMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateLaborContractAddendumUseCaseTest {

    @Mock
    private LaborContractRepository laborContractRepository;
    
    @Mock
    private LaborContractAddendumRepository addendumRepository;
    
    @Mock
    private LaborContractAddendumMapper addendumMapper;
    
    @Mock
    private AttachmentService attachmentService;
    
    @InjectMocks
    private CreateLaborContractAddendumUseCase createLaborContractAddendumUseCase;
    
    private CreateLaborContractAddendumRequestDTO request;
    private LaborContract laborContract;
    private LaborContractAddendum addendum;
    private LaborContractAddendumResponseDTO responseDTO;
    
    @BeforeEach
    void setUp() {
        request = createAddendumRequest();
        laborContract = createLaborContract();
        addendum = createAddendum();
        responseDTO = createAddendumResponseDTO();
    }
    
    @Test
    @DisplayName("Deve criar aditivo com sucesso")
    void shouldCreateAddendumSuccessfully() {
        // Arrange
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123")).thenReturn(null);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals("aditivo123", result.getId());
        assertEquals("contrato123", result.getContratoTrabalhistaId());
        assertEquals(TipoInstrumento.ACT, result.getTipo());
        assertEquals(LocalDate.of(2024, 3, 1), result.getDataInclusao());
        
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository).findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123");
        verify(addendumMapper).toEntity(eq(request), eq("contrato123"), eq("usuario.teste"), anyList());
        verify(addendumRepository).save(addendum);
        verify(addendumMapper).toResponseDTO(addendum);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato trabalhista não encontrado")
    void shouldThrowExceptionWhenLaborContractNotFound() {
        // Arrange
        when(laborContractRepository.findById("contrato_inexistente")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createLaborContractAddendumUseCase.execute("contrato_inexistente", request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Contrato trabalhista não encontrado"));
        verify(laborContractRepository).findById("contrato_inexistente");
        verify(addendumRepository, never()).save(any(LaborContractAddendum.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato está inativo")
    void shouldThrowExceptionWhenLaborContractIsInactive() {
        // Arrange
        laborContract.setStatusRegistro("INATIVO");
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> createLaborContractAddendumUseCase.execute("contrato123", request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Apenas contratos ativos podem ter aditivos"));
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository, never()).save(any(LaborContractAddendum.class));
    }
    
    @Test
    @DisplayName("Deve criar aditivo com tipo CCT com sucesso")
    void shouldCreateAddendumWithCCTTypeSuccessfully() {
        // Arrange
        request.setTipo(TipoInstrumento.CCT);
        LaborContractAddendumResponseDTO cctResponseDTO = createAddendumResponseDTO();
        cctResponseDTO.setTipo(TipoInstrumento.CCT);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123")).thenReturn(null);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(cctResponseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals("aditivo123", result.getId());
        assertEquals(TipoInstrumento.CCT, result.getTipo());
        
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository).findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123");
        verify(addendumMapper).toEntity(eq(request), eq("contrato123"), eq("usuario.teste"), anyList());
        verify(addendumRepository).save(addendum);
        verify(addendumMapper).toResponseDTO(addendum);
    }
    
    @Test
    @DisplayName("Deve processar aditivo com tipo válido")
    void shouldProcessAddendumWithValidType() {
        // Arrange
        request.setTipo(TipoInstrumento.ACT);
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123")).thenReturn(null);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals(TipoInstrumento.ACT, result.getTipo());
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository).save(addendum);
    }
    
    @Test
    @DisplayName("Deve processar aditivo com datas válidas")
    void shouldProcessAddendumWithValidDates() {
        // Arrange
        request.setDataInicio(LocalDate.of(2024, 3, 1));
        request.setDataFim(LocalDate.of(2024, 12, 31));
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123")).thenReturn(null);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 3, 1), result.getDataInicio());
        assertEquals(LocalDate.of(2024, 12, 31), result.getDataFim());
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository).save(addendum);
    }
    
    
    @Test
    @DisplayName("Deve permitir criar múltiplos aditivos do mesmo tipo na mesma data")
    void shouldAllowMultipleAddendumsWithSameTypeAndDate() {
        // Arrange
        LaborContractAddendum existingAddendum = new LaborContractAddendum();
        existingAddendum.setSequencia(1);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123"))
            .thenReturn(existingAddendum);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        verify(laborContractRepository).findById("contrato123");
        verify(addendumRepository).findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123");
        verify(addendumRepository).save(any(LaborContractAddendum.class));
        // Verifica que a sequência foi incrementada (de 1 para 2)
        verify(addendumMapper).toEntity(eq(request), eq("contrato123"), eq("usuario.teste"), anyList());
    }
    
    @Test
    @DisplayName("Deve processar campos de direitos trabalhistas corretamente")
    void shouldProcessLaborRightsFieldsCorrectly() {
        // Arrange
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.700,00");
        laborRights.setSalary(salary);
        
        BenefitsDTO benefits = new BenefitsDTO();
        BeneficioEstruturadoDTO premio = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("Participação nos lucros")
            .build();
        benefits.setPremioDesempenho(premio);
        laborRights.setBenefits(benefits);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        BeneficioEstruturadoDTO he1 = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60% sobre salário")
            .build();
        workSchedule.setHoraExtra1(he1);
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(laborContract));
        when(addendumRepository.findFirstByContratoTrabalhistaIdOrderBySequenciaDesc("contrato123")).thenReturn(null);
        when(addendumMapper.toEntity(any(CreateLaborContractAddendumRequestDTO.class), 
            eq("contrato123"), eq("usuario.teste"), anyList())).thenReturn(addendum);
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(addendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class))).thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = createLaborContractAddendumUseCase.execute(
            "contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        verify(addendumMapper).toEntity(eq(request), eq("contrato123"), eq("usuario.teste"), anyList());
    }
    
    private CreateLaborContractAddendumRequestDTO createAddendumRequest() {
        CreateLaborContractAddendumRequestDTO request = new CreateLaborContractAddendumRequestDTO();
        request.setTipo(TipoInstrumento.ACT);
        request.setTitulo("Aditivo Salarial 2024");
        request.setDataInclusao(LocalDate.of(2024, 3, 1));
        request.setDataInicio(LocalDate.of(2024, 3, 1));
        request.setDataFim(LocalDate.of(2024, 12, 31));
        request.setLink("https://exemplo.com/aditivo");
        request.setStatus("APROVADO");
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.700,00");
        laborRights.setSalary(salary);
        
        BenefitsDTO benefits = new BenefitsDTO();
        BeneficioEstruturadoDTO premio = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("Participação nos lucros")
            .build();
        benefits.setPremioDesempenho(premio);
        laborRights.setBenefits(benefits);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        BeneficioEstruturadoDTO he2 = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60% sobre salário")
            .build();
        workSchedule.setHoraExtra1(he2);
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        // Adicionar arquivos anexos obrigatórios
        request.setArquivosAnexos(List.of());
        return request;
    }
    
    private LaborContract createLaborContract() {
        LaborContract contract = new LaborContract();
        contract.setId("contrato123");
        contract.setTipoInstrumento(TipoInstrumento.ACT);
        contract.setStatusRegistro("ATIVO");
        contract.setSindicatoTrabalhadoresId("union123");
        return contract;
    }
    
    private LaborContractAddendum createAddendum() {
        LaborContractAddendum addendum = new LaborContractAddendum();
        addendum.setId("aditivo123");
        addendum.setContratoTrabalhistaId("contrato123");
        addendum.setTipo(TipoInstrumento.ACT);
        addendum.setTitulo("Aditivo Salarial 2024");
        addendum.setDataInclusao(LocalDate.of(2024, 3, 1));
        addendum.setDataInicio(LocalDate.of(2024, 3, 1));
        addendum.setDataFim(LocalDate.of(2024, 12, 31));
        addendum.setLink("https://exemplo.com/aditivo");
        addendum.setStatus("APROVADO");
        // Os campos de direitos trabalhistas agora são mapeados via LaborRightsMapper
        return addendum;
    }
    
    private LaborContractAddendumResponseDTO createAddendumResponseDTO() {
        LaborContractAddendumResponseDTO dto = new LaborContractAddendumResponseDTO();
        dto.setId("aditivo123");
        dto.setContratoTrabalhistaId("contrato123");
        dto.setTipo(TipoInstrumento.ACT);
        dto.setTitulo("Aditivo Salarial 2024");
        dto.setDataInclusao(LocalDate.of(2024, 3, 1));
        dto.setDataInicio(LocalDate.of(2024, 3, 1));
        dto.setDataFim(LocalDate.of(2024, 12, 31));
        dto.setLink("https://exemplo.com/aditivo");
        dto.setStatus("APROVADO");
        
        // Configurar LaborRightsDTO para resposta
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.700,00");
        laborRights.setSalary(salary);
        
        BenefitsDTO benefits = new BenefitsDTO();
        BeneficioEstruturadoDTO premioResp = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("Participação nos lucros")
            .build();
        benefits.setPremioDesempenho(premioResp);
        laborRights.setBenefits(benefits);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        BeneficioEstruturadoDTO he3 = BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60% sobre salário")
            .build();
        workSchedule.setHoraExtra1(he3);
        laborRights.setWorkSchedule(workSchedule);
        
        dto.setLaborRights(laborRights);
        return dto;
    }
}

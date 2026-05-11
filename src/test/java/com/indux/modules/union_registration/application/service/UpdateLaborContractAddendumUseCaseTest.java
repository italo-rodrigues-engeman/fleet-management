package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateLaborContractAddendumRequestDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.mapper.LaborContractAddendumMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import com.indux.modules.union_registration.domain.repository.LaborContractAddendumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateLaborContractAddendumUseCaseTest {

    @Mock
    private LaborContractAddendumRepository addendumRepository;
    
    @Mock
    private LaborContractAddendumMapper addendumMapper;
    
    @Mock
    private AttachmentService attachmentService;
    
    @InjectMocks
    private UpdateLaborContractAddendumUseCase updateLaborContractAddendumUseCase;
    
    private UpdateLaborContractAddendumRequestDTO request;
    private LaborContractAddendum existingAddendum;
    private LaborContractAddendumResponseDTO responseDTO;
    
    @BeforeEach
    void setUp() {
        request = createUpdateRequest();
        existingAddendum = createExistingAddendum();
        responseDTO = createResponseDTO();
    }
    
    @Test
    @DisplayName("Deve atualizar aditivo com sucesso")
    void shouldUpdateAddendumSuccessfully() {
        // Arrange
        when(addendumRepository.findById("addendum123")).thenReturn(Optional.of(existingAddendum));
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(existingAddendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class)))
            .thenReturn(responseDTO);
        
        // Act
        LaborContractAddendumResponseDTO result = updateLaborContractAddendumUseCase.execute("addendum123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals("addendum123", result.getId());
        assertEquals("Aditivo Salarial 2024 - Atualizado", result.getTitulo());
        
        verify(addendumRepository).findById("addendum123");
        verify(addendumRepository).save(existingAddendum);
        verify(addendumMapper).toResponseDTO(existingAddendum);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando aditivo não encontrado")
    void shouldThrowExceptionWhenAddendumNotFound() {
        // Arrange
        when(addendumRepository.findById("addendum_inexistente")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> updateLaborContractAddendumUseCase.execute("addendum_inexistente", request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Aditivo não encontrado"));
        verify(addendumRepository).findById("addendum_inexistente");
        verify(addendumRepository, never()).save(any(LaborContractAddendum.class));
    }
    
    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        // Arrange
        UpdateLaborContractAddendumRequestDTO partialRequest = new UpdateLaborContractAddendumRequestDTO();
        partialRequest.setTitulo("Título Atualizado");
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.800,00");
        laborRights.setSalary(salary);
        partialRequest.setLaborRights(laborRights);
        
        when(addendumRepository.findById("addendum123")).thenReturn(Optional.of(existingAddendum));
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(existingAddendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractAddendumUseCase.execute("addendum123", partialRequest, "usuario.teste");
        
        // Assert
        verify(addendumRepository).save(any(LaborContractAddendum.class));
    }
    
    @Test
    @DisplayName("Deve fazer merge profundo de LaborRights preservando campos não enviados")
    void shouldDeepMergeLaborRightsPreservingExistingFields() {
        // Arrange
        // Configurar aditivo existente com LaborRights completo
        LaborRightsDTO existingLaborRights = new LaborRightsDTO();
        
        SalaryDTO existingSalary = new SalaryDTO();
        existingSalary.setPisoSalarial("R$ 1.700,00");
        existingLaborRights.setSalary(existingSalary);
        
        BenefitsDTO existingBenefits = new BenefitsDTO();
        existingBenefits.setValeAlimentacao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 600,00")
            .build());
        existingBenefits.setValeRefeicao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 35,00 por dia")
            .build());
        existingLaborRights.setBenefits(existingBenefits);
        
        WorkScheduleDTO existingWorkSchedule = new WorkScheduleDTO();
        existingWorkSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60%")
            .build());
        existingWorkSchedule.setBancoHoras("Sim");
        existingLaborRights.setWorkSchedule(existingWorkSchedule);
        
        existingAddendum.setLaborRights(existingLaborRights);
        
        // Configurar request com apenas um campo de salary
        UpdateLaborContractAddendumRequestDTO partialRequest = new UpdateLaborContractAddendumRequestDTO();
        LaborRightsDTO updatedLaborRights = new LaborRightsDTO();
        SalaryDTO updatedSalary = new SalaryDTO();
        updatedSalary.setPisoSalarial("R$ 1.900,00"); // Atualizar apenas pisoSalarial
        updatedLaborRights.setSalary(updatedSalary);
        partialRequest.setLaborRights(updatedLaborRights);
        
        when(addendumRepository.findById("addendum123")).thenReturn(Optional.of(existingAddendum));
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(existingAddendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractAddendumUseCase.execute("addendum123", partialRequest, "usuario.teste");
        
        // Assert
        LaborRightsDTO resultLaborRights = existingAddendum.getLaborRights();
        
        // Verificar que o campo atualizado foi modificado
        assertEquals("R$ 1.900,00", resultLaborRights.getSalary().getPisoSalarial());
        
        // Verificar que o DTO mantém demais campos não enviados (quando existirem)
        
        // Verificar que outros DTOs não foram afetados
        assertNotNull(resultLaborRights.getBenefits());
        assertNotNull(resultLaborRights.getBenefits().getValeAlimentacao());
        assertEquals("R$ 600,00", resultLaborRights.getBenefits().getValeAlimentacao().getObservacao());
        assertNotNull(resultLaborRights.getBenefits().getValeRefeicao());
        assertEquals("R$ 35,00 por dia", resultLaborRights.getBenefits().getValeRefeicao().getObservacao());
        
        assertNotNull(resultLaborRights.getWorkSchedule());
        assertNotNull(resultLaborRights.getWorkSchedule().getHoraExtra1());
        assertEquals("60%", resultLaborRights.getWorkSchedule().getHoraExtra1().getObservacao());
        assertEquals("Sim", resultLaborRights.getWorkSchedule().getBancoHoras());
        
        verify(addendumRepository).save(existingAddendum);
    }
    
    @Test
    @DisplayName("Deve fazer merge de objetos aninhados dentro de LaborRights")
    void shouldMergeNestedObjectsInLaborRights() {
        // Arrange
        LaborRightsDTO existingLaborRights = new LaborRightsDTO();
        BenefitsDTO existingBenefits = new BenefitsDTO();
        existingBenefits.setValeAlimentacao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 600,00")
            .build());
        
        HealthBenefitsDTO existingHealthBenefits = new HealthBenefitsDTO();
        PlanoSaudeOdontologicoDTO existingPlanoSaude = new PlanoSaudeOdontologicoDTO();
        existingPlanoSaude.setExtensividade("Familiar");
        existingPlanoSaude.setCobertura("Quarto Privativo");
        existingPlanoSaude.setAbrangencia("Internacional");
        existingHealthBenefits.setPlanoSaude(existingPlanoSaude);
        existingLaborRights.setBenefits(existingBenefits);
        existingLaborRights.setHealthBenefits(existingHealthBenefits);
        
        existingAddendum.setLaborRights(existingLaborRights);
        
        // Configurar request atualizando apenas um campo do PlanoSaude
        UpdateLaborContractAddendumRequestDTO partialRequest = new UpdateLaborContractAddendumRequestDTO();
        LaborRightsDTO updatedLaborRights = new LaborRightsDTO();
        HealthBenefitsDTO updatedHealthBenefits = new HealthBenefitsDTO();
        PlanoSaudeOdontologicoDTO updatedPlanoSaude = new PlanoSaudeOdontologicoDTO();
        updatedPlanoSaude.setCobertura("Enfermaria"); // Atualizar apenas cobertura
        updatedHealthBenefits.setPlanoSaude(updatedPlanoSaude);
        updatedLaborRights.setHealthBenefits(updatedHealthBenefits);
        partialRequest.setLaborRights(updatedLaborRights);
        
        when(addendumRepository.findById("addendum123")).thenReturn(Optional.of(existingAddendum));
        when(addendumRepository.save(any(LaborContractAddendum.class))).thenReturn(existingAddendum);
        when(addendumMapper.toResponseDTO(any(LaborContractAddendum.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractAddendumUseCase.execute("addendum123", partialRequest, "usuario.teste");
        
        // Assert
        BenefitsDTO resultBenefits = existingAddendum.getLaborRights().getBenefits();
        HealthBenefitsDTO resultHealth = existingAddendum.getLaborRights().getHealthBenefits();
        PlanoSaudeOdontologicoDTO resultPlanoSaude = resultHealth.getPlanoSaude();
        
        // Verificar que o campo atualizado foi modificado
        assertEquals("Enfermaria", resultPlanoSaude.getCobertura());
        
        // Verificar que os outros campos foram preservados
        assertEquals("Familiar", resultPlanoSaude.getExtensividade());
        assertEquals("Internacional", resultPlanoSaude.getAbrangencia());
        
        // Verificar que o campo fora do objeto aninhado foi preservado
        assertEquals("R$ 600,00", resultBenefits.getValeAlimentacao().getObservacao());
        
        verify(addendumRepository).save(existingAddendum);
    }
    
    @Test
    @DisplayName("Deve validar datas de início e fim")
    void shouldValidateStartAndEndDates() {
        // Arrange
        UpdateLaborContractAddendumRequestDTO invalidRequest = new UpdateLaborContractAddendumRequestDTO();
        invalidRequest.setDataInicio(LocalDate.of(2024, 12, 31));
        invalidRequest.setDataFim(LocalDate.of(2024, 1, 1));
        
        when(addendumRepository.findById("addendum123")).thenReturn(Optional.of(existingAddendum));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> updateLaborContractAddendumUseCase.execute("addendum123", invalidRequest, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Data de início da vigência deve ser anterior à data de fim"));
        verify(addendumRepository).findById("addendum123");
        verify(addendumRepository, never()).save(any(LaborContractAddendum.class));
    }
    
    private UpdateLaborContractAddendumRequestDTO createUpdateRequest() {
        UpdateLaborContractAddendumRequestDTO request = new UpdateLaborContractAddendumRequestDTO();
        request.setTipo(TipoInstrumento.ACT);
        request.setTitulo("Aditivo Salarial 2024 - Atualizado");
        request.setDataInicio(LocalDate.of(2024, 4, 1));
        request.setDataFim(LocalDate.of(2024, 12, 31));
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.800,00");
        laborRights.setSalary(salary);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        workSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("70% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        return request;
    }
    
    private LaborContractAddendum createExistingAddendum() {
        LaborContractAddendum addendum = new LaborContractAddendum();
        addendum.setId("addendum123");
        addendum.setTipo(TipoInstrumento.ACT);
        addendum.setTitulo("Aditivo Salarial 2024");
        addendum.setContratoTrabalhistaId("contrato123");
        addendum.setDataInclusao(LocalDate.of(2024, 3, 1));
        addendum.setDataInicio(LocalDate.of(2024, 3, 1));
        addendum.setDataFim(LocalDate.of(2024, 12, 31));
        addendum.setLink("https://exemplo.com/aditivo");
        addendum.setStatus("APROVADO");
        return addendum;
    }
    
    private LaborContractAddendumResponseDTO createResponseDTO() {
        LaborContractAddendumResponseDTO dto = new LaborContractAddendumResponseDTO();
        dto.setId("addendum123");
        dto.setTipo(TipoInstrumento.ACT);
        dto.setTitulo("Aditivo Salarial 2024 - Atualizado");
        dto.setContratoTrabalhistaId("contrato123");
        dto.setDataInclusao(LocalDate.of(2024, 3, 1));
        dto.setDataInicio(LocalDate.of(2024, 4, 1));
        dto.setDataFim(LocalDate.of(2024, 12, 31));
        dto.setLink("https://exemplo.com/aditivo");
        dto.setStatus("APROVADO");
        return dto;
    }
}



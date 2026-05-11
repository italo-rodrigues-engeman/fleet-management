package com.indux.modules.union_registration.application.service;

import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.mapper.LaborContractMapper;
import com.indux.modules.union_registration.application.mapper.LaborRightsMapper;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.Union;
import com.indux.modules.union_registration.domain.repository.LaborContractRepository;
import com.indux.modules.union_registration.domain.repository.UnionRepository;
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
class UpdateLaborContractUseCaseTest {

    @Mock
    private LaborContractRepository laborContractRepository;
    
    @Mock
    private UnionRepository unionRepository;
    
    @Mock
    private LaborContractMapper laborContractMapper;
    
    @Mock
    private AttachmentService attachmentService;
    
    @Mock
    private LaborRightsMapper laborRightsMapper;
    
    @InjectMocks
    private UpdateLaborContractUseCase updateLaborContractUseCase;
    
    private UpdateLaborContractRequestDTO request;
    private LaborContract existingContract;
    private Union union;
    private LaborContractResponseDTO responseDTO;
    
    @BeforeEach
    void setUp() {
        request = createUpdateRequest();
        existingContract = createExistingContract();
        union = createUnion();
        responseDTO = createResponseDTO();
    }
    
    @Test
    @DisplayName("Deve atualizar contrato trabalhista com sucesso")
    void shouldUpdateLaborContractSuccessfully() {
        // Arrange
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(existingContract));
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(existingContract);
        when(unionRepository.findById("union123")).thenReturn(Optional.of(union));
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(responseDTO);
        
        // Act
        LaborContractResponseDTO result = updateLaborContractUseCase.execute("contrato123", request, "usuario.teste");
        
        // Assert
        assertNotNull(result);
        assertEquals("contrato123", result.getId());
        assertEquals("ACT-2024-002", result.getNumeroIdentificacaoInterno());
        
        verify(laborContractRepository).findById("contrato123");
        verify(laborContractRepository).save(existingContract);
        verify(unionRepository).findById("union123");
        verify(laborContractMapper).toResponseDTOWithUnionInfo(existingContract, union);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato não encontrado")
    void shouldThrowExceptionWhenContractNotFound() {
        // Arrange
        when(laborContractRepository.findById("contrato_inexistente")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> updateLaborContractUseCase.execute("contrato_inexistente", request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Contrato trabalhista não encontrado"));
        verify(laborContractRepository).findById("contrato_inexistente");
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando contrato está inativo")
    void shouldThrowExceptionWhenContractIsInactive() {
        // Arrange
        existingContract.setStatusRegistro("INATIVO");
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(existingContract));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> updateLaborContractUseCase.execute("contrato123", request, "usuario.teste"));
        
        assertTrue(exception.getMessage().contains("Apenas contratos ativos podem ser editados"));
        verify(laborContractRepository).findById("contrato123");
        verify(laborContractRepository, never()).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve atualizar apenas campos fornecidos")
    void shouldUpdateOnlyProvidedFields() {
        // Arrange
        UpdateLaborContractRequestDTO partialRequest = new UpdateLaborContractRequestDTO();
        partialRequest.setNomeInstrumento("Nome Atualizado");
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.600,00");
        laborRights.setSalary(salary);
        partialRequest.setLaborRights(laborRights);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(existingContract));
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(existingContract);
        when(unionRepository.findById("union123")).thenReturn(Optional.of(union));
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractUseCase.execute("contrato123", partialRequest, "usuario.teste");
        
        // Assert
        verify(laborContractRepository).save(any(LaborContract.class));
    }
    
    @Test
    @DisplayName("Deve fazer merge profundo de LaborRights preservando campos não enviados")
    void shouldDeepMergeLaborRightsPreservingExistingFields() {
        // Arrange
        // Configurar contrato existente com LaborRights completo
        LaborRightsDTO existingLaborRights = new LaborRightsDTO();
        
        SalaryDTO existingSalary = new SalaryDTO();
        existingSalary.setPisoSalarial("R$ 1.500,00");
        existingLaborRights.setSalary(existingSalary);
        
        BenefitsDTO existingBenefits = new BenefitsDTO();
        existingBenefits.setValeAlimentacao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 500,00")
            .build());
        existingBenefits.setValeRefeicao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 30,00 por dia")
            .build());
        existingLaborRights.setBenefits(existingBenefits);
        
        WorkScheduleDTO existingWorkSchedule = new WorkScheduleDTO();
        existingWorkSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("50%")
            .build());
        existingWorkSchedule.setBancoHoras("Permitido");
        existingLaborRights.setWorkSchedule(existingWorkSchedule);
        
        existingContract.setLaborRights(existingLaborRights);
        
        // Configurar request com apenas um campo de salary
        UpdateLaborContractRequestDTO partialRequest = new UpdateLaborContractRequestDTO();
        LaborRightsDTO updatedLaborRights = new LaborRightsDTO();
        SalaryDTO updatedSalary = new SalaryDTO();
        updatedSalary.setPisoSalarial("R$ 1.600,00"); // Atualizar apenas pisoSalarial
        updatedLaborRights.setSalary(updatedSalary);
        partialRequest.setLaborRights(updatedLaborRights);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(existingContract));
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(existingContract);
        when(unionRepository.findById("union123")).thenReturn(Optional.of(union));
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractUseCase.execute("contrato123", partialRequest, "usuario.teste");
        
        // Assert
        LaborRightsDTO resultLaborRights = existingContract.getLaborRights();
        
        // Verificar que o campo atualizado foi modificado
        assertEquals("R$ 1.600,00", resultLaborRights.getSalary().getPisoSalarial());
        
        // Verificar que o DTO mantém demais campos não enviados (quando existirem)
        
        // Verificar que outros DTOs não foram afetados
        assertNotNull(resultLaborRights.getBenefits());
        assertNotNull(resultLaborRights.getBenefits().getValeAlimentacao());
        assertEquals("R$ 500,00", resultLaborRights.getBenefits().getValeAlimentacao().getObservacao());
        assertNotNull(resultLaborRights.getBenefits().getValeRefeicao());
        assertEquals("R$ 30,00 por dia", resultLaborRights.getBenefits().getValeRefeicao().getObservacao());
        
        assertNotNull(resultLaborRights.getWorkSchedule());
        assertNotNull(resultLaborRights.getWorkSchedule().getHoraExtra1());
        assertEquals("50%", resultLaborRights.getWorkSchedule().getHoraExtra1().getObservacao());
        assertEquals("Permitido", resultLaborRights.getWorkSchedule().getBancoHoras());
        
        verify(laborContractRepository).save(existingContract);
    }
    
    @Test
    @DisplayName("Deve fazer merge de objetos aninhados dentro de LaborRights")
    void shouldMergeNestedObjectsInLaborRights() {
        // Arrange
        LaborRightsDTO existingLaborRights = new LaborRightsDTO();
        BenefitsDTO existingBenefits = new BenefitsDTO();
        existingBenefits.setValeAlimentacao(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("R$ 500,00")
            .build());
        existingLaborRights.setBenefits(existingBenefits);
        HealthBenefitsDTO existingHealthBenefits = new HealthBenefitsDTO();
        PlanoSaudeOdontologicoDTO existingPlanoSaude = new PlanoSaudeOdontologicoDTO();
        existingPlanoSaude.setExtensividade("Dependentes");
        existingPlanoSaude.setCobertura("Quarto Individual");
        existingPlanoSaude.setAbrangencia("Nacional");
        existingHealthBenefits.setPlanoSaude(existingPlanoSaude);
        existingLaborRights.setHealthBenefits(existingHealthBenefits);
        
        existingContract.setLaborRights(existingLaborRights);
        
        // Configurar request atualizando apenas um campo do PlanoSaude
        UpdateLaborContractRequestDTO partialRequest = new UpdateLaborContractRequestDTO();
        LaborRightsDTO updatedLaborRights = new LaborRightsDTO();
        HealthBenefitsDTO updatedHealthBenefits = new HealthBenefitsDTO();
        PlanoSaudeOdontologicoDTO updatedPlanoSaude = new PlanoSaudeOdontologicoDTO();
        updatedPlanoSaude.setCobertura("Quarto Coletivo"); // Atualizar apenas cobertura
        updatedHealthBenefits.setPlanoSaude(updatedPlanoSaude);
        updatedLaborRights.setHealthBenefits(updatedHealthBenefits);
        partialRequest.setLaborRights(updatedLaborRights);
        
        when(laborContractRepository.findById("contrato123")).thenReturn(Optional.of(existingContract));
        when(laborContractRepository.save(any(LaborContract.class))).thenReturn(existingContract);
        when(unionRepository.findById("union123")).thenReturn(Optional.of(union));
        when(laborContractMapper.toResponseDTOWithUnionInfo(any(LaborContract.class), any(Union.class)))
            .thenReturn(responseDTO);
        
        // Act
        updateLaborContractUseCase.execute("contrato123", partialRequest, "usuario.teste");
        
        // Assert
        BenefitsDTO resultBenefits = existingContract.getLaborRights().getBenefits();
        HealthBenefitsDTO resultHealth = existingContract.getLaborRights().getHealthBenefits();
        PlanoSaudeOdontologicoDTO resultPlanoSaude = resultHealth.getPlanoSaude();
        
        // Verificar que o campo atualizado foi modificado
        assertEquals("Quarto Coletivo", resultPlanoSaude.getCobertura());
        
        // Verificar que os outros campos foram preservados
        assertEquals("Dependentes", resultPlanoSaude.getExtensividade());
        assertEquals("Nacional", resultPlanoSaude.getAbrangencia());
        
        // Verificar que o campo fora do objeto aninhado foi preservado
        assertEquals("R$ 500,00", resultBenefits.getValeAlimentacao().getObservacao());
        
        verify(laborContractRepository).save(existingContract);
    }
    
    private UpdateLaborContractRequestDTO createUpdateRequest() {
        UpdateLaborContractRequestDTO request = new UpdateLaborContractRequestDTO();
        request.setTipoInstrumento(TipoInstrumento.ACT);
        request.setNumeroIdentificacaoInterno("ACT-2024-002");
        request.setNomeInstrumento("Acordo Coletivo Atualizado");
        request.setDataInicioVigencia(LocalDate.now().plusDays(1));
        request.setDataFimVigencia(LocalDate.now().plusYears(1));
        
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.600,00");
        laborRights.setSalary(salary);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        workSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        return request;
    }
    
    private LaborContract createExistingContract() {
        LaborContract contract = new LaborContract();
        contract.setId("contrato123");
        contract.setTipoInstrumento(TipoInstrumento.ACT);
        contract.setNumeroIdentificacaoInterno("ACT-2024-001");
        contract.setNomeInstrumento("Acordo Original");
        contract.setSindicatoTrabalhadoresId("union123");
        contract.setStatusRegistro("ATIVO");
        contract.setDataInicioVigencia(LocalDate.now());
        contract.setDataFimVigencia(LocalDate.now().plusYears(1));
        return contract;
    }
    
    private Union createUnion() {
        Union union = new Union();
        union.setId("union123");
        union.setNomeCompletoSindicato("Sindicato Teste");
        return union;
    }
    
    private LaborContractResponseDTO createResponseDTO() {
        LaborContractResponseDTO dto = new LaborContractResponseDTO();
        dto.setId("contrato123");
        dto.setTipoInstrumento(TipoInstrumento.ACT);
        dto.setNumeroIdentificacaoInterno("ACT-2024-002");
        dto.setNomeInstrumento("Acordo Coletivo Atualizado");
        dto.setSindicatoTrabalhadoresId("union123");
        dto.setNomeSindicato("Sindicato Teste");
        dto.setSiglaSindicato("ST");
        return dto;
    }
}

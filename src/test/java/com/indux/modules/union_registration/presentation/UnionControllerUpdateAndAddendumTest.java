package com.indux.modules.union_registration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.application.dto.CreateLaborContractAddendumRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractAddendumResponseDTO;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.dto.UpdateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.*;
import com.indux.modules.union_registration.application.service.*;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnionControllerUpdateAndAddendumTest {
    
    @InjectMocks
    private UnionController unionController;
    
    @Mock
    private CreateUnionUseCase createUnionUseCase;
    
    @Mock
    private DeleteUnionUseCase deleteUnionUseCase;
    
    @Mock
    private GetUnionByIdUseCase getUnionByIdUseCase;
    
    @Mock
    private ListUnionsUseCase listUnionsUseCase;
    
    @Mock
    private UpdateUnionUseCase updateUnionUseCase;
    
    @Mock
    private CreateLaborContractUseCase createLaborContractUseCase;
    
    @Mock
    private CreateLaborContractAddendumUseCase createLaborContractAddendumUseCase;
    
    @Mock
    private UpdateLaborContractUseCase updateLaborContractUseCase;
    
    @Mock
    private DeleteLaborContractUseCase deleteLaborContractUseCase;
    
    @Mock
    private ListLaborContractsUseCase listLaborContractsUseCase;
    
    @Mock
    private Authentication authentication;
    
    @BeforeEach
    void setUp() {
        // Setup básico do mock de autenticação
        when(authentication.getName()).thenReturn("usuario.teste");
    }
    
    @Test
    @DisplayName("Deve atualizar contrato trabalhista com sucesso")
    void shouldUpdateLaborContractSuccessfully() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        UpdateLaborContractRequestDTO requestDTO = createUpdateRequest();
        LaborContractResponseDTO responseDTO = createLaborContractResponse();
        
        when(updateLaborContractUseCase.execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString()))
            .thenReturn(responseDTO);
        
        ResponseEntity<?> response = unionController.updateLaborContract(unionId, contractId, requestDTO, null, null, authentication);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        LaborContractResponseDTO responseBody = (LaborContractResponseDTO) response.getBody();
        assertEquals("contrato123", responseBody.getId());
        assertEquals(TipoInstrumento.ACT, responseBody.getTipoInstrumento());
        
        verify(updateLaborContractUseCase).execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve retornar erro quando atualização falha")
    void shouldReturnErrorWhenUpdateFails() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        UpdateLaborContractRequestDTO requestDTO = createUpdateRequest();
        
        when(updateLaborContractUseCase.execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString()))
            .thenThrow(new RuntimeException("Contrato não encontrado"));
        
        ResponseEntity<?> response = unionController.updateLaborContract(unionId, contractId, requestDTO, null, null, authentication);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        GenericMessage responseBody = (GenericMessage) response.getBody();
        assertEquals("Contrato não encontrado", responseBody.message());
        assertEquals(400, responseBody.status());
        
        verify(updateLaborContractUseCase).execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve criar aditivo de contrato trabalhista com sucesso")
    void shouldCreateLaborContractAddendumSuccessfully() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        CreateLaborContractAddendumRequestDTO requestDTO = createAddendumRequest();
        LaborContractAddendumResponseDTO responseDTO = createAddendumResponse();
        
        when(createLaborContractAddendumUseCase.execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString()))
            .thenReturn(responseDTO);
        
        ResponseEntity<?> response = unionController.createLaborContractAddendum(unionId, contractId, requestDTO, null, authentication);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        LaborContractAddendumResponseDTO responseBody = (LaborContractAddendumResponseDTO) response.getBody();
        assertEquals("aditivo123", responseBody.getId());
        assertEquals("contrato123", responseBody.getContratoTrabalhistaId());
        assertEquals(TipoInstrumento.ACT, responseBody.getTipo());
        
        verify(createLaborContractAddendumUseCase).execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve retornar erro quando criação de aditivo falha")
    void shouldReturnErrorWhenAddendumCreationFails() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        CreateLaborContractAddendumRequestDTO requestDTO = createAddendumRequest();
        
        when(createLaborContractAddendumUseCase.execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString()))
            .thenThrow(new RuntimeException("Contrato trabalhista não encontrado"));
        
        ResponseEntity<?> response = unionController.createLaborContractAddendum(unionId, contractId, requestDTO, null, authentication);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        GenericMessage responseBody = (GenericMessage) response.getBody();
        assertEquals("Contrato trabalhista não encontrado", responseBody.message());
        assertEquals(400, responseBody.status());
        
        verify(createLaborContractAddendumUseCase).execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve retornar erro interno do servidor quando exceção inesperada ocorre")
    void shouldReturnInternalServerErrorForUnexpectedException() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        UpdateLaborContractRequestDTO requestDTO = createUpdateRequest();
        
        when(updateLaborContractUseCase.execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString()))
            .thenThrow(new RuntimeException("Erro inesperado"));
        
        ResponseEntity<?> response = unionController.updateLaborContract(unionId, contractId, requestDTO, null, null, authentication);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        GenericMessage responseBody = (GenericMessage) response.getBody();
        assertEquals("Erro inesperado", responseBody.message());
        assertEquals(400, responseBody.status());
        
        verify(updateLaborContractUseCase).execute(eq(contractId), any(UpdateLaborContractRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve validar campos obrigatórios do aditivo")
    void shouldValidateRequiredAddendumFields() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        CreateLaborContractAddendumRequestDTO requestDTO = createAddendumRequest();
        requestDTO.setTipo(TipoInstrumento.ACT);
        requestDTO.setDataInclusao(LocalDate.of(2024, 3, 1));
        requestDTO.setLink("https://exemplo.com");
        requestDTO.setStatus("APROVADO");
        
        LaborContractAddendumResponseDTO responseDTO = createAddendumResponse();
        
        when(createLaborContractAddendumUseCase.execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString()))
            .thenReturn(responseDTO);
        
        ResponseEntity<?> response = unionController.createLaborContractAddendum(unionId, contractId, requestDTO, null, authentication);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(createLaborContractAddendumUseCase).execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve processar campos de direitos trabalhistas no aditivo")
    void shouldProcessLaborRightsFieldsInAddendum() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        CreateLaborContractAddendumRequestDTO requestDTO = createAddendumRequest();
        // Configurar LaborRightsDTO
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.700,00");
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
            .observacao("60% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        requestDTO.setLaborRights(laborRights);
        
        LaborContractAddendumResponseDTO responseDTO = createAddendumResponse();
        
        when(createLaborContractAddendumUseCase.execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString()))
            .thenReturn(responseDTO);
        
        ResponseEntity<?> response = unionController.createLaborContractAddendum(unionId, contractId, requestDTO, null, authentication);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(createLaborContractAddendumUseCase).execute(eq(contractId), any(CreateLaborContractAddendumRequestDTO.class), anyString());
    }
    
    private UpdateLaborContractRequestDTO createUpdateRequest() {
        UpdateLaborContractRequestDTO request = new UpdateLaborContractRequestDTO();
        request.setTipoInstrumento(TipoInstrumento.ACT);
        request.setNomeInstrumento("Acordo Atualizado");
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
        benefits.setPremioDesempenho(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("Participação nos lucros")
            .build());
        laborRights.setBenefits(benefits);
        
        WorkScheduleDTO workSchedule = new WorkScheduleDTO();
        workSchedule.setHoraExtra1(BeneficioEstruturadoDTO.builder()
            .aplicavel(true)
            .observacao("60% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        request.setLaborRights(laborRights);
        return request;
    }
    
    private LaborContractResponseDTO createLaborContractResponse() {
        LaborContractResponseDTO response = new LaborContractResponseDTO();
        response.setId("contrato123");
        response.setTipoInstrumento(TipoInstrumento.ACT);
        response.setNomeInstrumento("Acordo Atualizado");
        response.setSindicatoTrabalhadoresId("68cd2d492329c37e18b00944");
        // Configurar LaborRightsDTO para resposta
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
        
        response.setLaborRights(laborRights);
        return response;
    }
    
    private LaborContractAddendumResponseDTO createAddendumResponse() {
        LaborContractAddendumResponseDTO response = new LaborContractAddendumResponseDTO();
        response.setId("aditivo123");
        response.setContratoTrabalhistaId("contrato123");
        response.setTipo(TipoInstrumento.ACT);
        response.setTitulo("Aditivo Salarial 2024");
        response.setDataInclusao(LocalDate.of(2024, 3, 1));
        response.setDataInicio(LocalDate.of(2024, 3, 1));
        response.setDataFim(LocalDate.of(2024, 12, 31));
        response.setLink("https://exemplo.com/aditivo");
        response.setStatus("APROVADO");
        // Configurar LaborRightsDTO para resposta
        LaborRightsDTO laborRights = new LaborRightsDTO();
        
        SalaryDTO salary = new SalaryDTO();
        salary.setPisoSalarial("R$ 1.700,00");
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
            .observacao("60% sobre salário")
            .build());
        laborRights.setWorkSchedule(workSchedule);
        
        response.setLaborRights(laborRights);
        return response;
    }
}

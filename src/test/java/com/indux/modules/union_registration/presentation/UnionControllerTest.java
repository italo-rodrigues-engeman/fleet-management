package com.indux.modules.union_registration.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.application.dto.CreateLaborContractRequestDTO;
import com.indux.modules.union_registration.application.dto.LaborContractResponseDTO;
import com.indux.modules.union_registration.application.dto.UnionSimpleResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnionControllerTest {
    
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
    @DisplayName("Deve criar contrato trabalhista com sucesso")
    void shouldCreateLaborContractSuccessfully() {
        String unionId = "68cd2d492329c37e18b00944";
        LaborContractResponseDTO responseDTO = createLaborContractResponse();
        CreateLaborContractRequestDTO requestDTO = createLaborContractRequest();
        
        when(createLaborContractUseCase.execute(any(CreateLaborContractRequestDTO.class), anyString()))
            .thenReturn(responseDTO);
        
        ResponseEntity<?> response = unionController.createLaborContract(unionId, requestDTO, null, null, authentication);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        LaborContractResponseDTO responseBody = (LaborContractResponseDTO) response.getBody();
        assertEquals(TipoInstrumento.ACT, responseBody.getTipoInstrumento());
        assertEquals("ACT-2024-001", responseBody.getNumeroIdentificacaoInterno());
        assertEquals("Acordo Coletivo de Trabalho 2024", responseBody.getNomeInstrumento());
        
        verify(createLaborContractUseCase).execute(any(CreateLaborContractRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve retornar erro quando dados inválidos são enviados")
    void shouldReturnErrorWhenInvalidData() {
        String unionId = "68cd2d492329c37e18b00944";
        CreateLaborContractRequestDTO requestDTO = new CreateLaborContractRequestDTO();
        
        when(createLaborContractUseCase.execute(any(CreateLaborContractRequestDTO.class), anyString()))
            .thenThrow(new RuntimeException("ERRO: Dados obrigatórios não fornecidos"));
        
        ResponseEntity<?> response = unionController.createLaborContract(unionId, requestDTO, null, null, authentication);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        GenericMessage responseBody = (GenericMessage) response.getBody();
        assertEquals("ERRO: Dados obrigatórios não fornecidos", responseBody.message());
        assertEquals(400, responseBody.status());
        
        verify(createLaborContractUseCase).execute(any(CreateLaborContractRequestDTO.class), anyString());
    }
    
    @Test
    @DisplayName("Deve listar contratos trabalhistas com paginação")
    void shouldListLaborContractsWithPagination() {
        String unionId = "68cd2d492329c37e18b00944";
        Pageable pageable = PageRequest.of(0, 20);
        
        LaborContractResponseDTO contract1 = createLaborContractResponse();
        contract1.setId("contrato1");
        LaborContractResponseDTO contract2 = createLaborContractResponse();
        contract2.setId("contrato2");
        contract2.setTipoInstrumento(TipoInstrumento.CCT);
        
        Page<LaborContractResponseDTO> contractsPage = new PageImpl<>(
            Arrays.asList(contract1, contract2), PageRequest.of(0, 20), 2);
        
        when(listLaborContractsUseCase.execute(eq(unionId), any(Pageable.class)))
            .thenReturn(contractsPage);
        
        ResponseEntity<?> response = unionController.listLaborContracts(unionId, null, pageable);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Page<LaborContractResponseDTO> responseBody = (Page<LaborContractResponseDTO>) response.getBody();
        assertEquals(2, responseBody.getContent().size());
        assertEquals(TipoInstrumento.ACT, responseBody.getContent().get(0).getTipoInstrumento());
        assertEquals(TipoInstrumento.CCT, responseBody.getContent().get(1).getTipoInstrumento());
        assertEquals(2, responseBody.getTotalElements());
        assertEquals(1, responseBody.getTotalPages());
        
        verify(listLaborContractsUseCase).execute(eq(unionId), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Deve filtrar contratos por tipo")
    void shouldFilterContractsByType() {
        String unionId = "68cd2d492329c37e18b00944";
        TipoInstrumento tipoInstrumento = TipoInstrumento.ACT;
        Pageable pageable = PageRequest.of(0, 20);
        
        LaborContractResponseDTO contract = createLaborContractResponse();
        Page<LaborContractResponseDTO> contractsPage = new PageImpl<>(
            Arrays.asList(contract), PageRequest.of(0, 20), 1);
        
        when(listLaborContractsUseCase.executeByType(eq(unionId), eq(tipoInstrumento), any(Pageable.class)))
            .thenReturn(contractsPage);
        
        ResponseEntity<?> response = unionController.listLaborContracts(unionId, tipoInstrumento.getValor(), pageable);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Page<LaborContractResponseDTO> responseBody = (Page<LaborContractResponseDTO>) response.getBody();
        assertEquals(1, responseBody.getContent().size());
        assertEquals(TipoInstrumento.ACT, responseBody.getContent().get(0).getTipoInstrumento());
        
        verify(listLaborContractsUseCase).executeByType(eq(unionId), eq(tipoInstrumento), any(Pageable.class));
    }
    
    @Test
    @DisplayName("Deve listar todos os contratos sem paginação")
    void shouldListAllLaborContracts() {
        String unionId = "68cd2d492329c37e18b00944";
        
        LaborContractResponseDTO contract1 = createLaborContractResponse();
        contract1.setId("contrato1");
        LaborContractResponseDTO contract2 = createLaborContractResponse();
        contract2.setId("contrato2");
        contract2.setTipoInstrumento(TipoInstrumento.CCT);
        
        List<LaborContractResponseDTO> contracts = Arrays.asList(contract1, contract2);
        
        when(listLaborContractsUseCase.executeAll(unionId)).thenReturn(contracts);
        
        ResponseEntity<?> response = unionController.listAllLaborContracts(unionId);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        List<LaborContractResponseDTO> responseBody = (List<LaborContractResponseDTO>) response.getBody();
        assertEquals(2, responseBody.size());
        assertEquals(TipoInstrumento.ACT, responseBody.get(0).getTipoInstrumento());
        assertEquals(TipoInstrumento.CCT, responseBody.get(1).getTipoInstrumento());
        
        verify(listLaborContractsUseCase).executeAll(unionId);
    }
    
    @Test
    @DisplayName("Deve excluir contrato trabalhista permanentemente")
    void shouldDeleteLaborContractPermanently() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        
        GenericMessage response = new GenericMessage("Contrato trabalhista excluído com sucesso", 200);
        
        when(deleteLaborContractUseCase.execute(contractId)).thenReturn(response);
        
        ResponseEntity<GenericMessage> result = unionController.deleteLaborContract(unionId, contractId);
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Contrato trabalhista excluído com sucesso", result.getBody().message());
        assertEquals(200, result.getBody().status());
        
        verify(deleteLaborContractUseCase).execute(contractId);
    }
    
    @Test
    @DisplayName("Deve desativar contrato trabalhista")
    void shouldDeactivateLaborContract() {
        String unionId = "68cd2d492329c37e18b00944";
        String contractId = "contrato123";
        
        GenericMessage response = new GenericMessage("Contrato trabalhista desativado com sucesso", 200);
        
        when(deleteLaborContractUseCase.softDelete(contractId)).thenReturn(response);
        
        ResponseEntity<GenericMessage> result = unionController.deactivateLaborContract(unionId, contractId);
        
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Contrato trabalhista desativado com sucesso", result.getBody().message());
        assertEquals(200, result.getBody().status());
        
        verify(deleteLaborContractUseCase).softDelete(contractId);
    }
    
    
    private CreateLaborContractRequestDTO createLaborContractRequest() {
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
        return request;
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
        response.setDataInicioVigencia(LocalDate.now());
        response.setDataFimVigencia(LocalDate.now().plusYears(1));
        response.setStatusRegistro("ATIVO");
        response.setDataCriacao(LocalDateTime.now());
        response.setUsuarioCriacao("usuario.teste");
        
        // Campos de direitos trabalhistas
        // Configurar LaborRightsDTO para resposta
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
        
        response.setLaborRights(laborRights);
        
        return response;
    }
    
    @Test
    @DisplayName("Deve retornar listagem de sindicatos com campo tipo")
    void shouldReturnUnionsListWithTipoField() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 50);
        UnionSimpleResponseDTO union1 = new UnionSimpleResponseDTO(
            "64f8b2c1234567890abcdef1",
            "Sindicato dos Trabalhadores da Construção Civil",
            "Trabalhadores",
            "ATIVO",
            "SP",
            "São Paulo"
        );
        
        UnionSimpleResponseDTO union2 = new UnionSimpleResponseDTO(
            "64f8b2c1234567890abcdef2",
            "Sindicato dos Empregadores da Indústria",
            "Empregadores",
            "ATIVO",
            "RJ",
            "Rio de Janeiro"
        );
        
        List<UnionSimpleResponseDTO> unions = Arrays.asList(union1, union2);
        Page<UnionSimpleResponseDTO> page = new PageImpl<>(unions, pageable, 2);
        
        when(listUnionsUseCase.listAllSimple(pageable)).thenReturn(page);
        
        // Act
        ResponseEntity<Page<UnionSimpleResponseDTO>> response = unionController.listUnions(
            null, null, null, null, null, null, null, null, null, null, null, null, pageable
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Page<UnionSimpleResponseDTO> responseBody = response.getBody();
        assertEquals(2, responseBody.getContent().size());
        
        // Verificar se o campo "tipo" está presente na resposta
        UnionSimpleResponseDTO firstUnion = responseBody.getContent().get(0);
        assertEquals("Trabalhadores", firstUnion.getTipo());
        assertEquals("Sindicato dos Trabalhadores da Construção Civil", firstUnion.getNomeCompletoSindicato());
        
        UnionSimpleResponseDTO secondUnion = responseBody.getContent().get(1);
        assertEquals("Empregadores", secondUnion.getTipo());
        assertEquals("Sindicato dos Empregadores da Indústria", secondUnion.getNomeCompletoSindicato());
        
        verify(listUnionsUseCase).listAllSimple(pageable);
    }
}

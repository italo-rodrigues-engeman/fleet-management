package com.indux.core.application.service.auth;

import com.indux.core.application.dto.auth.SimpleLoginRequestDTO;
import com.indux.core.application.dto.auth.SimpleLoginResponseDTO;
import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleLoginService {
    
    private final EmployeeRepository employeeRepository;
    private final CargoRepository cargoRepository;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final RegionalRepository regionalRepository;
    
    public SimpleLoginResponseDTO authenticate(SimpleLoginRequestDTO loginRequest) {
        try {
            log.info("Tentativa de login simples para CPF: {}", maskCpf(loginRequest.getCpf()));
            
            // Buscar todos os funcionários por CPF
            List<Employee> employees = employeeRepository.findAllByCpf(loginRequest.getCpf());
            
            if (employees.isEmpty()) {
                log.warn("Funcionário não encontrado para CPF: {}", maskCpf(loginRequest.getCpf()));
                return SimpleLoginResponseDTO.failure("Funcionário não encontrado");
            }
            
            // Pegar o funcionário mais recente (última data de admissão)
            // Filtrar funcionários com data de admissão 1900-12-31 (dados antigos/inválidos)
            Employee employee = employees.stream()
                    .filter(emp -> emp.getAdmissionDate() != null && 
                            !emp.getAdmissionDate().equals(java.time.LocalDate.of(1900, 12, 31)))
                    .max(java.util.Comparator.comparing(Employee::getAdmissionDate))
                    .orElse(employees.get(0)); // Se não houver funcionários válidos, pegar o primeiro
            
            log.info("Selecionado funcionário mais recente: {} - {} (Admissão: {})", 
                    employee.getRegistration(), employee.getName(), employee.getAdmissionDate());
            
            // Validar nome completo (comparação case-insensitive)
            if (!isNameMatch(employee.getName(), loginRequest.getNomeCompleto())) {
                log.warn("Nome não confere para CPF: {}", maskCpf(loginRequest.getCpf()));
                return SimpleLoginResponseDTO.failure("Dados de acesso incorretos");
            }
            
            // Validar data de nascimento
            if (!employee.getBirthDate().equals(loginRequest.getDataNascimento())) {
                log.warn("Data de nascimento não confere para CPF: {}", maskCpf(loginRequest.getCpf()));
                return SimpleLoginResponseDTO.failure("Dados de acesso incorretos");
            }
            
            // Buscar informações completas do funcionário
            CompleteEmployeeDTO employeeInfo = getEmployeeUseCase.getEmployeeById(employee.getId());
            
            // Buscar informações de cargo, contrato, centro de custos e regional
            String cargo = null;
            String cargoNome = null;
            String contrato = null;
            String centroCustos = null;
            String regional = null;
            
            try {
                // Buscar cargo por position
                if (employee.getPosition() != null) {
                    Optional<Cargo> cargoOptional = cargoRepository.findByIdHcm(employee.getPosition());
                    if (cargoOptional.isPresent()) {
                        Cargo cargoEntity = cargoOptional.get();
                        cargo = cargoEntity.getIdHcm();
                        cargoNome = cargoEntity.getNameTitle();
                    }
                }
                
                // Usar dados já mapeados do CompleteEmployeeDTO
                if (employeeInfo != null) {
                    contrato = employeeInfo.getNomeProjeto();
                    centroCustos = employeeInfo.getCentro_custos_id();
                }
                
                // Buscar regional usando a relação correta: filial_id -> tb_filial -> tb_regional
                if (employee.getBranch_id() != null) {
                    Optional<Regional> regionalOptional = regionalRepository.findByFilialId(employee.getBranch_id());
                    if (regionalOptional.isPresent()) {
                        Regional regionalEntity = regionalOptional.get();
                        regional = regionalEntity.getRegional();
                    }
                }
                
            } catch (Exception e) {
                log.warn("Erro ao buscar informações de cargo/contrato para funcionário: {}", employee.getRegistration(), e);
            }
            
            log.info("Login simples realizado com sucesso para funcionário: {} - {} (Status: {}) - Cargo: {} - Contrato: {} - Centro Custos: {} - Regional: {}", 
                    employee.getRegistration(), employee.getName(), employee.getStatusEmployee(), cargoNome, contrato, centroCustos, regional);
            String regionalNome = employeeInfo.getHierarchy() != null
                    ? employeeInfo.getHierarchy().getRegionalNome()
                    : "Não informado";

            String contratoNome = employeeInfo.getHierarchy() != null
                    ? employeeInfo.getHierarchy().getContratoNome()
                    : "Não informado";

            return SimpleLoginResponseDTO.success(
                    employee.getId(),
                    employee.getName(),
                    employee.getRegistration(),
                    employee.getCpf(),
                    employee.getStatusEmployee(),
                    cargo,
                    cargoNome,
                    contrato,
                    centroCustos,
                    regional,
                    regionalNome,
                    contratoNome
            );
            
        } catch (Exception e) {
            log.error("Erro durante autenticação simples para CPF: {}", maskCpf(loginRequest.getCpf()), e);
            return SimpleLoginResponseDTO.failure("Erro interno do servidor");
        }
    }
    
    private boolean isNameMatch(String employeeName, String providedName) {
        if (employeeName == null || providedName == null) {
            return false;
        }
        
        // Normalizar nomes: remover espaços extras, converter para maiúsculo
        String normalizedEmployeeName = employeeName.trim().toUpperCase();
        String normalizedProvidedName = providedName.trim().toUpperCase();
        
        return normalizedEmployeeName.equals(normalizedProvidedName);
    }
    
    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 4) {
            return "***";
        }
        return "***" + cpf.substring(cpf.length() - 4);
    }
}

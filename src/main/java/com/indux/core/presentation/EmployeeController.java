package com.indux.core.presentation;

import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.EmployeeFiltersDTO;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.application.service.employee.EmployeeUpdateService;
import com.indux.core.application.service.employee.GetEmployeeFiltersUseCase;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private final GetEmployeeUseCase useCase;
    private final GetEmployeeFiltersUseCase filtersUseCase;
    private final EmployeeUpdateService employeeUpdateService;

    public EmployeeController(GetEmployeeUseCase useCase, GetEmployeeFiltersUseCase filtersUseCase,
            EmployeeUpdateService employeeUpdateService) {
        this.useCase = useCase;
        this.filtersUseCase = filtersUseCase;
        this.employeeUpdateService = employeeUpdateService;
    }

    @GetMapping("/get/{value}")
    public List<EmployeeDTO> search(
            @PathVariable String value,
            @RequestParam(defaultValue = "false") boolean showBank,
            @RequestParam(defaultValue = "false") boolean showFired) {
        return useCase.search(value, showBank, showFired);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompleteEmployeeDTO> getEmployeeById(@PathVariable UUID id) {
        CompleteEmployeeDTO employee = useCase.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/all")
    public Page<EmployeeSummaryDTO> getAllEmployeesSummary(

            @RequestParam(required = false) List<String> matricula,
            @RequestParam(required = false) List<String> nome,
            @RequestParam(required = false) List<String> cidade,
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) List<String> sexo,
            @RequestParam(required = false) List<String> grauInstrucao,
            @RequestParam(required = false) List<String> dataAdmissao,
            @RequestParam(required = false) List<String> dataAdmissaoInicio,
            @RequestParam(required = false) List<String> dataAdmissaoFim,
            @RequestParam(required = false) List<String> dataNascimento,
            @RequestParam(required = false) List<String> dataNascimentoInicio,
            @RequestParam(required = false) List<String> dataNascimentoFim,
            @RequestParam(required = false) List<String> contrato,
            @RequestParam(required = false) List<String> filialId,
            @RequestParam(required = false) List<String> filialIdHcm,
            @RequestParam(required = false) List<String> centroCustosId,
            @RequestParam(required = false) List<String> cargo,
            @RequestParam(defaultValue = "false") boolean showFired,
            @RequestParam(required = false) List<String> situacao,
            @RequestParam(required = false) List<String> dependentes,
            @RequestParam(required = false) List<Long> diretoriaId,
            @RequestParam(required = false) List<Long> superintendenciaId,
            @RequestParam(required = false) List<Long> regionalId,
            @RequestParam(required = false) List<Long> setorId,
            @RequestParam(required = false) List<Long> contratoId,
            @RequestParam(required = false) List<Long> projetoId,
            @RequestParam(required = false) Long tempoTrabalhoMinDias,
            @RequestParam(required = false) Long tempoTrabalhoMaxDias,
            Pageable pageable) {
        List<Integer> dependentesList = null;
        if (dependentes != null && !dependentes.isEmpty()) {
            dependentesList = dependentes.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(Integer::valueOf)
                    .toList();
        }

        return useCase.filterEmployees(
                matricula, nome, cidade, estado, sexo, grauInstrucao,
                dataAdmissao, dataAdmissaoInicio, dataAdmissaoFim, dataNascimento, dataNascimentoInicio,
                dataNascimentoFim, contrato,
                filialId, filialIdHcm, centroCustosId, cargo, showFired, situacao,
                dependentesList,
                diretoriaId, superintendenciaId, regionalId, setorId, contratoId, projetoId,
                tempoTrabalhoMinDias, tempoTrabalhoMaxDias,
                pageable);
    }

    @GetMapping("/filters")
    public ResponseEntity<EmployeeFiltersDTO> getFilters() {
        return ResponseEntity.ok(filtersUseCase.getFilters());
    }

    @GetMapping("/suprimentos")
    public ResponseEntity<Page<EmployeeSummaryDTO>> getEmployeesBySuprimentosContract(
            Pageable pageable,
            @RequestParam(defaultValue = "false") boolean showFired) {

        Page<EmployeeSummaryDTO> employees = useCase.listEmployeesOnlySuprimentos(pageable);

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/update")
    public ResponseEntity<Object> getEmployeeUpdate() {
        try {
            ResponseEntity<Object> response = employeeUpdateService.getEmployeeUpdate();
            return ResponseEntity.ok(response.getBody());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erro ao obter atualizações de funcionários", "message", e.getMessage()));
        }
    }
}

package com.indux.core.application.service.employee;

import com.indux.core.application.dto.generic.EmployeeFiltersDTO;
import com.indux.core.domain.repository.generic.BranchRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetEmployeeFiltersUseCase {

    @PersistenceContext
    private EntityManager em;

    public GetEmployeeFiltersUseCase(EmployeeRepository employeeRepository, BranchRepository branchRepository) {
    }

    public EmployeeFiltersDTO getFilters() {
        // Buscar filiais com funcionários
        String filialQuery = """
                SELECT DISTINCT
                    f.codigo_filial as codigo,
                    f.nome_filial as nome
                FROM
                    tb_filiais f
                INNER JOIN
                    tb_funcionarios func
                    ON func.filial_id = f.codigo_filial
                """;

        List<EmployeeFiltersDTO.FilialDTO> filiais = em.createNativeQuery(filialQuery)
                .getResultList()
                .stream()
                .map(result -> {
                    Object[] row = (Object[]) result;
                    return EmployeeFiltersDTO.FilialDTO.builder()
                            .codigo_filial(((Number) row[0]).longValue())
                            .nome_filial((String) row[1])
                            .build();
                })
                .toList();

        // Buscar situações distintas
        String situacaoQuery = "SELECT DISTINCT situacao FROM tb_funcionarios WHERE situacao IS NOT NULL ORDER BY situacao ASC";
        List<String> situacoes = em.createNativeQuery(situacaoQuery)
                .getResultList()
                .stream()
                .map(Object::toString)
                .toList();

        // Buscar estados distintos
        String estadoQuery = "SELECT DISTINCT estado FROM tb_funcionarios WHERE estado IS NOT NULL ORDER BY estado ASC";
        List<String> estados = em.createNativeQuery(estadoQuery)
                .getResultList()
                .stream()
                .map(Object::toString)
                .toList();

        // Buscar graus de instrução distintos
        String grauInstrucaoQuery = "SELECT DISTINCT grau_instrucao FROM tb_funcionarios WHERE grau_instrucao IS NOT NULL ORDER BY grau_instrucao ASC";
        List<String> grausInstrucao = em.createNativeQuery(grauInstrucaoQuery)
                .getResultList()
                .stream()
                .map(Object::toString)
                .toList();

        // Buscar quantidades de dependentes distintas
        String dependentesQuery = "SELECT DISTINCT qtd_dependentes FROM tb_funcionarios WHERE qtd_dependentes IS NOT NULL ORDER BY qtd_dependentes ASC";
        List<Integer> qtdDependentes = em.createNativeQuery(dependentesQuery)
                .getResultList()
                .stream()
                .map(result -> ((Number) result).intValue())
                .toList();

        return EmployeeFiltersDTO.builder()
                .filiais(filiais)
                .situacoes(situacoes)
                .estados(estados)
                .graus_instrucao(grausInstrucao)
                .qtd_dependentes(qtdDependentes)
                .build();
    }
}
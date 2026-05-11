package com.indux.modules.employee_history.application.service;

import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.modules.employee_history.application.dto.FilterHistory;
import com.indux.modules.employee_history.application.dto.HistoryDTO;
import com.indux.modules.employee_history.domain.entity.History;
import com.indux.modules.employee_history.domain.repository.HistoryRepository;
import com.indux.modules.employee_history.infra.mapper.HistoryMapper;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final HistoryMapper historyMapper;
    private final EmployeeRepository employeeRepository;
    private final OrganizationFilialRepository organizationFilialRepository;
    private final SubordinateService subordinateService;

    public HistoryService(
            HistoryRepository historyRepository, HistoryMapper historyMapper,
            EmployeeRepository employeeRepository,
            OrganizationFilialRepository organizationFilialRepository, SubordinateService subordinateService) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
        this.employeeRepository = employeeRepository;
        this.organizationFilialRepository = organizationFilialRepository;
        this.subordinateService = subordinateService;
    }

    public Page<HistoryDTO> getHistory(FilterHistory filter, Pageable pageable) {
        if (filter != null && filter.getFilialId() == null) {
            getFilialOrganization(filter);
        }
        Page<History> historyEntity = historyRepository.findFilterHistory(filter, pageable);

        List<HistoryDTO> historyDTO = historyEntity.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(historyDTO, pageable, historyEntity.getTotalElements());
    }

    private void getFilialOrganization(FilterHistory filter) {
        if (filter != null && filter.getProjectId() != null) {
            var filiaisEntities = subordinateService.getFiliaisHcmByFilters(null, null, null,
                    null, null, filter.getProjectId());
            List<Integer> filialIds = filiaisEntities.stream()
                    .map(FilialHcmEntity::getFilialId)
                    .collect(Collectors.toList());
            filter.setFilialId(filialIds);
            if (filialIds.isEmpty()) {
                filter.setFilialId(List.of(0));
            }
        } else if (filter != null && filter.getContractId() != null) {
            var filiaisEntities = subordinateService.getFiliaisHcmByFilters(null, null, null,
                    null, filter.getContractId(), null);
            List<Integer> filialIds = filiaisEntities.stream()
                    .map(FilialHcmEntity::getFilialId)
                    .collect(Collectors.toList());
            filter.setFilialId(filialIds);
            if (filialIds.isEmpty()) {
                filter.setFilialId(List.of(0));
            }
        } else if (filter != null && filter.getRegionalId() != null) {
            var filiaisEntities = subordinateService.getFiliaisHcmByFilters(null, null, filter.getRegionalId(),
                    null, null, null);
            List<Integer> filialIds = filiaisEntities.stream()
                    .map(FilialHcmEntity::getFilialId)
                    .collect(Collectors.toList());
            filter.setFilialId(filialIds);
            if (filialIds.isEmpty()) {
                filter.setFilialId(List.of(0));
            }
        }
    }

    private HistoryDTO mapToDTO(History history) {
        String employeeName = null;
        UUID employeeId = null;
        String filialName = null;

        if (history.getRegistration() != null) {
            List<Employee> employees = employeeRepository.findAllByRegistration(history.getRegistration());
            Employee resolved = employees.stream()
                    .filter(emp -> "Trabalhando".equalsIgnoreCase(emp.getStatusEmployee()))
                    .findFirst()
                    .or(() -> employees.stream().findFirst())
                    .orElse(null);
            if (resolved != null) {
                employeeName = resolved.getName();
                employeeId = resolved.getId();
            }
        }

        if (history.getFilialId() != null) {
            filialName = organizationFilialRepository.findById(history.getFilialId().intValue())
                    .map(FilialHcmEntity::getNomeFilial)
                    .orElse(null);
        }

        EmployeeSummaryDTO.HierarchyInfo hierarchyInfo = buildHierarchyInfo(history.getFilialId());

        return historyMapper.toSingleDTO(history, filialName, employeeId, employeeName, hierarchyInfo);
    }

    private EmployeeSummaryDTO.HierarchyInfo buildHierarchyInfo(Long filialId) {
        if (filialId == null) {
            return null;
        }

        List<Map<String, Object>> orgData = organizationFilialRepository
                .findFilialOrganization(List.of(filialId.intValue()), null);

        if (orgData.isEmpty()) {
            return null;
        }

        Map<String, Object> org = orgData.get(0);

        return EmployeeSummaryDTO.HierarchyInfo.builder()
                .projetoId(org.get("idProjeto") != null ? ((Number) org.get("idProjeto")).longValue() : null)
                .projetoNome((String) org.get("nomeProjeto"))
                .contratoId(org.get("idContrato") != null ? ((Number) org.get("idContrato")).longValue() : null)
                .contratoNome((String) org.get("nomeContrato"))
                .setorId(org.get("idSetor") != null ? ((Number) org.get("idSetor")).longValue() : null)
                .setorNome((String) org.get("nomeSetor"))
                .regionalId(org.get("idRegional") != null ? ((Number) org.get("idRegional")).longValue() : null)
                .regionalNome((String) org.get("nomeRegional"))
                .superintendenciaId(
                        org.get("idSuperintendencia") != null
                                ? ((Number) org.get("idSuperintendencia")).longValue()
                                : null)
                .superintendenciaNome((String) org.get("nomeSuperintendencia"))
                .diretoriaId(org.get("idDiretoria") != null ? ((Number) org.get("idDiretoria")).longValue() : null)
                .diretoriaNome((String) org.get("nomeDiretoria"))
                .build();
    }

    public Page<String> getDistinctEvents(Pageable pageable) {
        return historyRepository.findDistinctEvents(pageable);
    }

}

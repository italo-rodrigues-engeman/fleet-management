package com.indux.modules.employee_history.application.service;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeEnrichmentService {

    private final EmployeeRepository employeeRepository;
    private final CargoRepository cargoRepository;
    private final OrganizationFilialRepository organizationFilialRepository;
    private final OrganizationProjectRepository organizationProjectRepository;

    public EmployeeEnrichmentService(EmployeeRepository employeeRepository,
                                     CargoRepository cargoRepository,
                                     OrganizationFilialRepository organizationFilialRepository,
                                     OrganizationProjectRepository organizationProjectRepository) {
        this.employeeRepository = employeeRepository;
        this.cargoRepository = cargoRepository;
        this.organizationFilialRepository = organizationFilialRepository;
        this.organizationProjectRepository = organizationProjectRepository;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class EmployeeInfo {
        private UUID id;
        private String name;
        private String cargoName;
        private String status;
        private Integer filialIdHcm;
    }

    public EmployeeInfo resolveEmployeeInfo(String registration) {
        if (registration == null) return null;

        List<Employee> employees = employeeRepository.findAllByRegistration(registration);
        Employee resolved = employees.stream()
                .filter(emp -> "Trabalhando".equalsIgnoreCase(emp.getStatusEmployee()))
                .findFirst()
                .or(() -> employees.stream().findFirst())
                .orElse(null);

        if (resolved == null) return null;

        String cargoName = null;
        if (resolved.getPosition() != null) {
            cargoName = cargoRepository.findByIdHcm(resolved.getPosition())
                    .map(Cargo::getNameTitle)
                    .orElse(null);
        }

        return EmployeeInfo.builder()
                .id(resolved.getId())
                .name(resolved.getName())
                .cargoName(cargoName)
                .status(resolved.getStatusEmployee())
                .filialIdHcm(resolved.getFilialIdHcm())
                .build();
    }

    public EmployeeSummaryDTO.HierarchyInfo buildHierarchyInfo(Long filialId) {
        if (filialId == null) return null;

        List<Map<String, Object>> orgData = organizationFilialRepository
                .findFilialOrganization(List.of(filialId.intValue()), null);

        if (orgData.isEmpty()) return null;

        Map<String, Object> org = orgData.get(0);

        EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder builder = EmployeeSummaryDTO.HierarchyInfo.builder()
                .projetoId(org.get("idProjeto") != null ? ((Number) org.get("idProjeto")).longValue() : null)
                .projetoNome((String) org.get("nomeProjeto"))
                .contratoId(org.get("idContrato") != null ? ((Number) org.get("idContrato")).longValue() : null)
                .contratoNome((String) org.get("nomeContrato"))
                .setorId(org.get("idSetor") != null ? ((Number) org.get("idSetor")).longValue() : null)
                .setorNome((String) org.get("nomeSetor"))
                .regionalId(org.get("idRegional") != null ? ((Number) org.get("idRegional")).longValue() : null)
                .regionalNome((String) org.get("nomeRegional"))
                .superintendenciaId(org.get("idSuperintendencia") != null
                        ? ((Number) org.get("idSuperintendencia")).longValue()
                        : null)
                .superintendenciaNome((String) org.get("nomeSuperintendencia"))
                .diretoriaId(org.get("idDiretoria") != null ? ((Number) org.get("idDiretoria")).longValue() : null)
                .diretoriaNome((String) org.get("nomeDiretoria"));

        if (org.get("idProjeto") != null) {
            Long projectId = ((Number) org.get("idProjeto")).longValue();
            organizationProjectRepository.findByIdWithDetails(projectId).ifPresent(project -> {
                builder.projectStatus(project.isAtivo());

                if (project.getHcm() != null) {
                    builder.projetoHcmId(project.getHcm().getCcId());
                    builder.projetoCentroCustoHcm(project.getHcm().getCcId());
                }
                if (project.getMega() != null) {
                    builder.projetoMegaId(project.getMega().getCusInReduzido());
                }
                if (project.getFilialMegaId() != null) {
                    builder.projetoFilialMega(project.getFilialMegaId().intValue());
                }
                if (project.getFilial() != null) {
                    List<Integer> filialHcmIds = project.getFilial().stream()
                            .map(FilialHcmEntity::getFilialId)
                            .distinct()
                            .collect(Collectors.toList());
                    if (!filialHcmIds.isEmpty()) {
                        builder.projetoFiliaisHcm(filialHcmIds);
                    }

                    List<Integer> ccMegaIds = project.getFilial().stream()
                            .filter(f -> f.getCcMega() != null)
                            .map(f -> f.getCcMega().getCusInReduzido())
                            .distinct()
                            .collect(Collectors.toList());
                    if (!ccMegaIds.isEmpty()) {
                        builder.projetoCentroCustoMega(ccMegaIds);
                    }
                }
                if (project.getContract() != null) {
                    builder.contratoOs(project.getContract().getOs());
                    enrichHierarchyFromOrg(project.getContract().getSubordinate(), builder);
                } else if (project.getSubordinate() != null) {
                    enrichHierarchyFromOrg(project.getSubordinate(), builder);
                }
            });
        }

        return builder.build();
    }

    public String resolveFilialName(Long filialId) {
        if (filialId == null) return null;
        return organizationFilialRepository.findById(filialId.intValue())
                .map(FilialHcmEntity::getNomeFilial)
                .orElse(null);
    }

    private void enrichHierarchyFromOrg(OrganizationEntity org,
                                        EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder builder) {
        if (org == null) return;

        EmployeeSummaryDTO.ResponsavelInfo responsavel = null;
        if (org.getCollaborator() != null) {
            var emp = org.getCollaborator();
            responsavel = EmployeeSummaryDTO.ResponsavelInfo.builder()
                    .id(emp.getId())
                    .nome(emp.getName())
                    .matricula(emp.getRegistration())
                    .email(emp.getBusinessEmail())
                    .cargo(emp.getPosition())
                    .build();
        }

        switch (org.getType()) {
            case SETOR:
                builder.setorId(org.getId())
                       .setorNome(org.getPosition())
                       .setorSigla(org.getAcronym())
                       .setorResponsavel(responsavel);
                break;
            case REGIONAL:
                builder.regionalId(org.getId())
                       .regionalNome(org.getPosition())
                       .regionalSigla(org.getAcronym())
                       .regionalResponsavel(responsavel);
                break;
            case SUPERINTENDENCIA:
                builder.superintendenciaId(org.getId())
                       .superintendenciaNome(org.getPosition())
                       .superintendenciaSigla(org.getAcronym())
                       .superintendenciaResponsavel(responsavel);
                break;
            case DIRETORIA:
                builder.diretoriaId(org.getId())
                       .diretoriaNome(org.getPosition())
                       .diretoriaSigla(org.getAcronym())
                       .diretoriaResponsavel(responsavel);
                return;
        }

        enrichHierarchyFromOrg(org.getSubordinate(), builder);
    }
}

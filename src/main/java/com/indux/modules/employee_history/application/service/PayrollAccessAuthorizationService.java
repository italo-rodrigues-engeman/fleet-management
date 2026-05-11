package com.indux.modules.employee_history.application.service;

import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.SimpleProjectRepository;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.core.infra.exception.module.ForbiddenModuleAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PayrollAccessAuthorizationService {

    private static final String ACCESS_DENIED_MSG = "Sem autorização para acessar os dados solicitados.";
    private static final String NO_PERMISSION_MSG = "Usuário não possui permissão neste módulo.";

    private final UserService userService;
    private final EmployeeRepository employeeRepository;
    private final SubordinateService subordinateService;
    private final OrganizationRepository organizationRepository;
    private final SimpleProjectRepository projectRepository;

    public PayrollAccessAuthorizationService(UserService userService,
                                             EmployeeRepository employeeRepository,
                                             SubordinateService subordinateService,
                                             OrganizationRepository organizationRepository,
                                             SimpleProjectRepository projectRepository) {
        this.userService = userService;
        this.employeeRepository = employeeRepository;
        this.subordinateService = subordinateService;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
    }

    public ModulePermission findUserPermission(Modulo module, UUID userId) {
        return module.getPermissoes().stream()
                .filter(p -> p.getResponsable().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenModuleAccessException(NO_PERMISSION_MSG));
    }

    public int resolveUserMaxLevel(ModulePermission permission) {
        if (permission.getStepsAllowed() == null || permission.getStepsAllowed().isEmpty()) {
            return -1;
        }
        return permission.getStepsAllowed().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1);
    }

    public Set<Integer> resolveAllowedFiliais(ModulePermission permission) {
        Set<Integer> projetos = permission.getProjetos();
        Set<Integer> regionais = permission.getRegionais();

        boolean hasProjectZero = projetos != null && projetos.contains(0);

        if (hasProjectZero) {
            return resolveRegionalFiliais(regionais);
        }

        List<Long> projetoIds = projetos != null
                ? projetos.stream().map(Integer::longValue).collect(Collectors.toList())
                : List.of();

        Set<FilialHcmEntity> filiais = subordinateService.getFiliaisHcmByFilters(
                null, null, null, null, null, projetoIds.isEmpty() ? null : projetoIds);

        return filiais.stream()
                .map(FilialHcmEntity::getFilialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void validateFilterFiliais(Set<Integer> allowedFiliais, Collection<? extends Number> requestedFiliais) {
        if (requestedFiliais == null || requestedFiliais.isEmpty()) {
            return;
        }
        for (Number requested : requestedFiliais) {
            if (requested != null && !allowedFiliais.contains(requested.intValue())) {
                throw new ForbiddenModuleAccessException(ACCESS_DENIED_MSG);
            }
        }
    }

    public String resolveUserRegistration(UUID userId) {
        Employee employee = userService.getCompleteEmployeeFromUser(userId);
        return employee != null ? employee.getRegistration() : null;
    }

    public <T> List<T> filterSuperiors(List<T> data,
                                       Function<T, String> registrationExtractor,
                                       int userLevel,
                                       String userRegistration,
                                       Modulo module) {
        if (data == null || data.isEmpty() || userLevel == 3) {
            return data;
        }

        int userOrgTypeOrdinal = resolveUserOrgTypeOrdinal(userRegistration);

        Set<String> allRegistrations = data.stream()
                .map(registrationExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Integer> orgTypeByRegistration = resolveOrgTypeBatch(allRegistrations);

        return data.stream()
                .filter(item -> {
                    String registration = registrationExtractor.apply(item);
                    if (registration == null) return true;

                    Integer targetOrgType = orgTypeByRegistration.get(registration);

                    if (targetOrgType == null) {
                        return true;
                    }

                    if (userOrgTypeOrdinal < 0) {
                        return false;
                    }

                    if (registration.equals(userRegistration)) {
                        return true;
                    }

                    return targetOrgType > userOrgTypeOrdinal;
                })
                .toList();
    }

    private int resolveUserOrgTypeOrdinal(String userRegistration) {
        if (userRegistration == null || userRegistration.isBlank()) {
            return -1;
        }

        Set<Employee> employees = employeeRepository.findAllByRegistrationIn(List.of(userRegistration));
        if (employees.isEmpty()) {
            return -1;
        }

        Employee employee = employees.stream()
                .filter(e -> "Trabalhando".equalsIgnoreCase(e.getStatusEmployee()))
                .findFirst()
                .or(() -> employees.stream().findFirst())
                .orElse(null);

        if (employee == null) {
            return -1;
        }

        return organizationRepository.findByCollaboratorIdAndActiveTrue(employee.getId())
                .map(org -> org.getType().ordinal())
                .orElse(-1);
    }

    private Map<String, Integer> resolveOrgTypeBatch(Set<String> registrations) {
        Map<String, Integer> result = new HashMap<>();

        Set<Employee> employees = employeeRepository.findAllByRegistrationIn(List.copyOf(registrations));

        Map<UUID, String> registrationByEmployeeId = new HashMap<>();
        for (Employee emp : employees) {
            if (emp.getRegistration() != null && emp.getId() != null) {
                registrationByEmployeeId.put(emp.getId(), emp.getRegistration());
            }
        }

        if (registrationByEmployeeId.isEmpty()) {
            return result;
        }

        List<OrganizationEntity> orgEntities = organizationRepository
                .findAllByCollaboratorIdIn(List.copyOf(registrationByEmployeeId.keySet()));

        for (OrganizationEntity org : orgEntities) {
            String registration = registrationByEmployeeId.get(org.getCollaborator().getId());
            if (registration != null) {
                result.put(registration, org.getType().ordinal());
            }
        }

        return result;
    }

    private int resolveRegistrationLevel(String registration, Modulo module) {
        List<Employee> employees = employeeRepository.findAllByRegistration(registration);
        if (employees.isEmpty()) {
            return -1;
        }

        Employee employee = employees.stream()
                .filter(e -> "Trabalhando".equalsIgnoreCase(e.getStatusEmployee()))
                .findFirst()
                .or(() -> employees.stream().findFirst())
                .orElse(null);

        if (employee == null || employee.getCpf() == null) {
            return -1;
        }
        User user = userService.getUserByCPF(employee.getCpf());
        if (user == null) {
            return -1;
        }

        return module.getPermissoes().stream()
                .filter(p -> p.getResponsable().equals(user.getId()))
                .findFirst()
                .map(this::resolveUserMaxLevel)
                .orElse(-1);
    }

    private Set<Integer> resolveRegionalFiliais(Set<Integer> regionais) {
        if (regionais == null || regionais.isEmpty()) {
            return Set.of();
        }

        boolean fetchAll = regionais.contains(0);

        Set<FilialHcmEntity> filiais;

        if (fetchAll) {
            List<Long> allProjectIds = projectRepository.findAll().stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toList());

            filiais = subordinateService.getFiliaisHcmByFilters(
                    null, null, null, null, null, allProjectIds);
        } else {
            List<Long> resolvedRegionalIds = regionais.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());

            filiais = subordinateService.getFiliaisHcmByFilters(
                    null, null, resolvedRegionalIds, null, null, null);
        }

        return filiais.stream()
                .map(FilialHcmEntity::getFilialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}

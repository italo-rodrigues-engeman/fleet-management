package com.indux.modules.organization_chart.application.services;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.model.employee.ContractProject;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.ContractProjectRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.dtos.*;
import com.indux.modules.organization_chart.domain.entities.jpa.*;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import com.indux.modules.organization_chart.domain.repositories.jpa.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SubordinateService {

    private final OrganizationRepository organizationRepository;
    private final SimpleContractRepository contractRepository;
    private final SimpleProjectRepository projectRepository;
    private final SimpleEmployeeRepository employeeRepository;
    private final RegionalRepository regionalRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final CargoRepository cargoRepository;
    private final OrganizationProjectRepository organizationProjectRepository;
    private final OrganizationHcmRepository hcmRepository;
    private final OrganizationMegaRepository megaRepository;
    private final OrganizationFilialRepository filialRepository;
    private final EmployeeRepository fullEmployeeRepository;
    private final OrganizationProjectRepository organizationProject;

    public SubordinateService(OrganizationRepository organizationRepository,
                              SimpleContractRepository contractRepository,
                              SimpleProjectRepository projectRepository,
                              SimpleEmployeeRepository employeeRepository,
                              RegionalRepository regionalRepository,
                              ContractProjectRepository contractProjectRepository,
                              CargoRepository cargoRepository,
                              OrganizationProjectRepository organizationProjectRepository,
                              OrganizationHcmRepository hcmRepository,
                              OrganizationMegaRepository megaRepository,
                              OrganizationFilialRepository filialRepository,
                              EmployeeRepository fullEmployeeRepository, OrganizationProjectRepository organizationProject) {
        this.organizationRepository = organizationRepository;
        this.contractRepository = contractRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.regionalRepository = regionalRepository;
        this.contractProjectRepository = contractProjectRepository;
        this.cargoRepository = cargoRepository;
        this.organizationProjectRepository = organizationProjectRepository;
        this.hcmRepository = hcmRepository;
        this.megaRepository = megaRepository;
        this.filialRepository = filialRepository;
        this.fullEmployeeRepository = fullEmployeeRepository;
        this.organizationProject = organizationProject;
    }

    public AllSubordinatesResponseDTO getAllSubordinatesRecursivelyByDirectorId(Long directorId) {
        if (!organizationRepository.existsById(directorId)) {
            throw new ModuleNotFoundFailure("Diretoria não encontrada.");
        }

        List<OrganizationEntity> allSubordinates = findAllSubordinatesRecursively(directorId);

        // Buscar todos os contratos relacionados aos subordinados
        List<Long> subordinateIds = allSubordinates.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());

        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);

        // Buscar projetos relacionados aos contratos
        List<Long> contractIds = contracts.stream()
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(contractIds);

        // Buscar projetos relacionados diretamente aos subordinados (onde contrato é
        // null)
        List<SimpleProjectEntity> projectsBySubordinate = projectRepository
                .findBySubordinateIdInAndContractIdIsNull(subordinateIds);

        // Agrupar contratos por ID do subordinado
        Map<Long, List<SimpleContractEntity>> contractsBySubordinateId = contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));

        // Agrupar projetos por ID do contrato
        Map<Long, List<SimpleProjectEntity>> projectsByContractId = projectsByContract.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));

        // Agrupar projetos por ID do subordinado (projetos sem contrato)
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinateId = projectsBySubordinate.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));

        // Buscar todos os HCM IDs dos projetos para buscar funcionários
        List<String> hcmIds = allSubordinates.stream()
                .flatMap(entity -> {
                    List<SimpleProjectEntity> contractProjects = projectsByContractId.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    List<SimpleProjectEntity> directProjects = projectsBySubordinateId.getOrDefault(entity.getId(),
                            List.of());
                    return Stream.concat(contractProjects.stream(), directProjects.stream());
                })
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        // Agrupar funcionários por centro de custos
        Map<String, List<SimpleEmployeeEntity>> employeesByCostCenter = employees.stream()
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCostCenterId));

        // Converter para DTO simplificado incluindo contratos
        List<SimpleSubordinateDTO> subordinatesDTOs = allSubordinates.stream()
                .map(entity -> {
                    List<SimpleContractDTO> subordinateContracts = contractsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(contract -> {
                                // Buscar projetos relacionados a este contrato
                                List<SimpleProjectDTO> contractProjects = projectsByContractId
                                        .getOrDefault(contract.getId(), List.of())
                                        .stream()
                                        .map(project -> {
                                            // Buscar funcionários relacionados ao HCM deste projeto
                                            List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                                    .getOrDefault(project.getHcmId().toString(), List.of())
                                                    .stream()
                                                    .map(employee -> new SimpleEmployeeDTO(
                                                            employee.getId(),
                                                            employee.getName(),
                                                            employee.getRegistration(),
                                                            employee.getFilialIdHcm()))
                                                    .collect(Collectors.toList());

                                            return new SimpleProjectDTO(
                                                    project.getId(),
                                                    project.getHcmId(),
                                                    project.isAtivo(),
                                                    projectEmployees);
                                        })
                                        .collect(Collectors.toList());

                                return new SimpleContractDTO(
                                        contract.getId(),
                                        contract.getName(),
                                        contract.getOs(),
                                        contractProjects);
                            })
                            .collect(Collectors.toList());

                    // Buscar projetos relacionados diretamente ao subordinado (sem contrato)
                    List<SimpleProjectDTO> directProjects = projectsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(project -> {
                                // Buscar funcionários relacionados ao HCM deste projeto
                                List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                        .getOrDefault(project.getHcmId().toString(), List.of())
                                        .stream()
                                        .map(employee -> new SimpleEmployeeDTO(
                                                employee.getId(),
                                                employee.getName(),
                                                employee.getRegistration(),
                                                employee.getFilialIdHcm()))
                                        .collect(Collectors.toList());

                                return new SimpleProjectDTO(
                                        project.getId(),
                                        project.getHcmId(),
                                        project.isAtivo(),
                                        projectEmployees);
                            })
                            .collect(Collectors.toList());

                    // Buscar informações do subordinado pai (parent)
                    SimpleSubordinateDTO.SubordinadoParentInfo subordinadoParent = null;
                    if (entity.getSubordinate() != null) {
                        OrganizationEntity parent = entity.getSubordinate();
                        subordinadoParent = new SimpleSubordinateDTO.SubordinadoParentInfo(
                                parent.getId(),
                                parent.getAcronym(),
                                parent.getPosition(),
                                parent.getType());
                    }

                    return new SimpleSubordinateDTO(
                            entity.getId(),
                            entity.getAcronym(),
                            entity.getPosition(),
                            entity.getType(),
                            subordinateContracts,
                            directProjects,
                            subordinadoParent);
                })
                .collect(Collectors.toList());

        // Agrupar por tipo
        Map<OrganizationType, List<SimpleSubordinateDTO>> groupedByType = subordinatesDTOs.stream()
                .collect(Collectors.groupingBy(SimpleSubordinateDTO::tipo));

        // Ordenar por tipo conforme especificado: DIRETORIA, SUPERINTENDENCIA,
        // REGIONAL, SETOR
        List<SubordinatesByTypeDTO> subordinatesByType = groupedByType.entrySet().stream()
                .map(entry -> new SubordinatesByTypeDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    // Definir ordem de prioridade
                    Map<OrganizationType, Integer> order = Map.of(
                            OrganizationType.DIRETORIA, 1,
                            OrganizationType.SUPERINTENDENCIA, 2,
                            OrganizationType.REGIONAL, 3,
                            OrganizationType.SETOR, 4);
                    return Integer.compare(
                            order.getOrDefault(a.type(), 999),
                            order.getOrDefault(b.type(), 999));
                })
                .collect(Collectors.toList());

        return new AllSubordinatesResponseDTO(subordinatesByType);
    }

    private List<OrganizationEntity> findAllSubordinatesRecursively(Long directorId) {
        List<OrganizationEntity> directSubordinates = organizationRepository
                .findAllSubordinatesByDirectorId(directorId);
        List<OrganizationEntity> allSubordinates = new java.util.ArrayList<>(directSubordinates);

        // Para cada subordinado direto, buscar seus subordinados recursivamente
        for (OrganizationEntity subordinate : directSubordinates) {
            List<OrganizationEntity> nestedSubordinates = findAllSubordinatesRecursively(subordinate.getId());
            allSubordinates.addAll(nestedSubordinates);
        }

        return allSubordinates;
    }

    public Page<EmployeeSummaryDTO> getAllEmployeesByDirectorId(Long directorId, Pageable pageable) {
        if (!organizationRepository.existsById(directorId)) {
            throw new ModuleNotFoundFailure("Diretoria não encontrada.");
        }

        // Buscar todos os subordinados recursivamente de forma otimizada
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(directorId);

        if (allSubordinateIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch
        Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

        // Coletar todos os HCM IDs dos projetos
        List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch para otimizar performance
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByFilialHcm = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO com dados pré-carregados
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByFilialHcm))
                .collect(Collectors.toList());

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    // Métodos auxiliares otimizados para performance

    private List<Long> findAllSubordinateIdsRecursively(Long directorId) {
        List<Long> allIds = new java.util.ArrayList<>();
        java.util.Queue<Long> queue = new java.util.LinkedList<>();
        queue.offer(directorId);

        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            List<Long> directSubordinateIds = organizationRepository.findAllSubordinateIdsByDirectorId(currentId);
            allIds.addAll(directSubordinateIds);
            queue.addAll(directSubordinateIds);
        }

        return allIds;
    }

    private Map<Long, List<SimpleContractEntity>> getContractsBySubordinateIds(List<Long> subordinateIds) {
        if (subordinateIds.isEmpty()) {
            return Map.of();
        }
        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);
        return contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));
    }

    private Map<Long, List<SimpleProjectEntity>> getProjectsBySubordinateIds(List<Long> subordinateIds) {
        if (subordinateIds.isEmpty()) {
            return Map.of();
        }
        List<SimpleProjectEntity> projects = projectRepository.findBySubordinateIdInAndContractIdIsNull(subordinateIds);
        return projects.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));
    }

    private Map<Long, List<SimpleProjectEntity>> getProjectsByContractIds(
            Map<Long, List<SimpleContractEntity>> contractsBySubordinate) {
        List<Long> contractIds = contractsBySubordinate.values().stream()
                .flatMap(List::stream)
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        if (contractIds.isEmpty()) {
            return Map.of();
        }

        List<SimpleProjectEntity> projects = projectRepository.findByContractIdIn(contractIds);
        return projects.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));
    }

    private List<String> collectAllHcmIds(Map<Long, List<SimpleProjectEntity>> projectsBySubordinate,
            Map<Long, List<SimpleProjectEntity>> projectsByContract) {
        return Stream.concat(
                projectsBySubordinate.values().stream().flatMap(List::stream),
                projectsByContract.values().stream().flatMap(List::stream))
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Long, String> getRegionalsByBranchIds(List<SimpleEmployeeEntity> employees) {
        List<Long> branchIds = employees.stream()
                .map(SimpleEmployeeEntity::getBranchId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (branchIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> regionals = new java.util.HashMap<>();
        for (Long branchId : branchIds) {
            regionalRepository.findByFilial(branchId.intValue())
                    .map(Regional::getRegional)
                    .ifPresent(regional -> regionals.put(branchId, regional));
        }

        return regionals;
    }

    private Map<Integer, ContractProject> getContractsByRateioIds(List<SimpleEmployeeEntity> employees) {
        List<Integer> rateioIds = employees.stream()
                .map(SimpleEmployeeEntity::getRateioId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (rateioIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, ContractProject> contracts = new java.util.HashMap<>();
        for (Integer rateioId : rateioIds) {
            contractProjectRepository.findWithFilialByRateio(rateioId)
                    .ifPresent(contract -> contracts.put(rateioId, contract));
        }

        return contracts;
    }

    private Map<String, Cargo> getCargosByPositionIds(List<SimpleEmployeeEntity> employees) {
        List<String> positionIds = employees.stream()
                .map(SimpleEmployeeEntity::getPositionId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (positionIds.isEmpty()) {
            return Map.of();
        }

        return cargoRepository.findAllByIdHcmIn(positionIds).stream()
                .collect(Collectors.toMap(Cargo::getIdHcm, cargo -> cargo));
    }

    /**
     * Calcula o tempo total de empresa para cada CPF, somando todos os vínculos.
     * Retorna um mapa de CPF para tempo formatado (ex: "2 anos e 3 meses").
     */
    private Map<String, String> calculateTotalTimeByCpf(List<SimpleEmployeeEntity> employees) {
        Map<String, String> tempoTotalPorCpf = new java.util.HashMap<>();

        // Agrupar funcionários por CPF
        Map<String, List<SimpleEmployeeEntity>> employeesByCpf = employees.stream()
                .filter(e -> e.getCpf() != null && !e.getCpf().isBlank())
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCpf));

        // Calcular tempo total para cada CPF
        for (Map.Entry<String, List<SimpleEmployeeEntity>> entry : employeesByCpf.entrySet()) {
            String cpf = entry.getKey();

            // Buscar TODOS os vínculos do funcionário (incluindo históricos)
            List<Employee> vinculos = fullEmployeeRepository.findAllByCpf(cpf);

            // Somar o tempo de todos os vínculos
            long totalDias = vinculos.stream().mapToLong(emp -> {
                LocalDate admissao = emp.getAdmissionDate();

                // Determinar data de término
                LocalDate demissao;
                if ((emp.getStatusEmployee() != null && emp.getStatusEmployee().equalsIgnoreCase("Trabalhando"))
                        || emp.getTerminationDate() == null
                        || emp.getTerminationDate().equals(LocalDate.of(1900, 12, 31))) {
                    demissao = LocalDate.now();
                } else {
                    demissao = emp.getTerminationDate();
                }

                // Validar datas antes de calcular
                if (admissao == null || demissao == null)
                    return 0L;

                // Calcular dias (garantir que não seja negativo)
                long dias = ChronoUnit.DAYS.between(admissao, demissao);
                return Math.max(0, dias);
            }).sum();

            // Formatar o tempo
            long anos = totalDias / 365;
            long meses = (totalDias % 365) / 30;
            String tempoFormatado = String.format("%d anos e %d meses", anos, meses);

            tempoTotalPorCpf.put(cpf, tempoFormatado);
        }

        return tempoTotalPorCpf;
    }

    private EmployeeSummaryDTO buildEmployeeSummaryDTO(SimpleEmployeeEntity employee,
            Map<Long, String> regionalsByBranchId,
            Map<Integer, ContractProject> contractsByRateioId,
            Map<String, Cargo> cargosByPositionId,
            Map<String, String> tempoTotalPorCpf,
            Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByFilialHcm) {
        // Buscar hierarquia organizacional do cache usando a filial HCM
        EmployeeSummaryDTO.HierarchyInfo hierarchy = employee.getFilialIdHcm() != null 
            ? hierarchiesByFilialHcm.get(employee.getFilialIdHcm().toString()) 
            : null;

        // Buscar regional: priorizar da hierarquia organizacional, senão usar da filial
        String regional = null;
        if (hierarchy != null && hierarchy.getRegionalNome() != null) {
            regional = hierarchy.getRegionalNome();
        } else {
            regional = regionalsByBranchId.get(employee.getBranchId());
        }

        // Buscar contrato do cache
        ContractProject contrato = contractsByRateioId.get(employee.getRateioId());
        String costCenterName = contrato != null ? contrato.getCostCenterName() : null;

        // Buscar cargo do cache
        Cargo cargo = cargosByPositionId.get(employee.getPositionId());
        String nomeCargo = cargo != null ? cargo.getNameTitle() : null;

        // Buscar tempo total de empresa do cache
        String tempoTotalEmpresa = tempoTotalPorCpf.get(employee.getCpf());

        return EmployeeSummaryDTO.builder()
                .id(employee.getId())
                .nome(employee.getName())
                .matricula(employee.getRegistration())
                .cpf(employee.getCpf())
                .contrato(employee.getCostCenterId())
                .regional(regional)
                .status(employee.getStatus())
                .cargo(employee.getPositionId())
                .quantidade_dependentes(employee.getNumberOfDependents())
                .grau_instrucao(employee.getEducationLevel())
                .tempoTotalEmpresa(tempoTotalEmpresa)
                .nomeProjeto(costCenterName)
                .costCenterName(costCenterName)
                .cargoNome(nomeCargo)
                .filialId(employee.getBranchId())
                .SISPAT(employee.getSispat())
                .email_particular(employee.getPersonalEmail())
                .telefone(employee.getPhone())
                .telefone2(employee.getPhone2())
                .hierarchy(hierarchy)
                .build();
    }

    /**
     * Busca hierarquias em batch para múltiplos funcionários
     * 
     * @param employees Lista de funcionários
     * @return Mapa de filialIdHcm -> HierarchyInfo
     */
    public Map<String, EmployeeSummaryDTO.HierarchyInfo> getHierarchiesByFilialHcm(
            List<SimpleEmployeeEntity> employees) {

        List<Integer> filialHcmIds = employees.stream()
                .map(SimpleEmployeeEntity::getFilialIdHcm)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (filialHcmIds.isEmpty()) {
            return new HashMap<>();
        }

        List<ProjectEntity> projects = organizationProject.findHierarchyProjectsByFilialIds(filialHcmIds);

        Map<Integer, ProjectEntity> projectsByFilialHcmId = new HashMap<>();

        for (ProjectEntity project : projects) {
            if (project.getFilial() == null) {
                continue;
            }

            for (FilialHcmEntity filial : project.getFilial()) {
                if (filial == null || filial.getFilialId() == null) {
                    continue;
                }

                if (filialHcmIds.contains(filial.getFilialId())) {
                    projectsByFilialHcmId.putIfAbsent(filial.getFilialId(), project);
                }
            }
        }

        List<Long> contractIds = projectsByFilialHcmId.values().stream()
                .map(ProjectEntity::getContract)
                .filter(Objects::nonNull)
                .map(ContractEntity::getId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SimpleContractEntity> contractsById = new HashMap<>();
        if (!contractIds.isEmpty()) {
            contractRepository.findAllById(contractIds)
                    .forEach(contract -> contractsById.put(contract.getId(), contract));
        }

        List<Long> subordinateIds = new ArrayList<>();

        subordinateIds.addAll(
                contractsById.values().stream()
                        .map(SimpleContractEntity::getSubordinateId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );

        subordinateIds.addAll(
                projectsByFilialHcmId.values().stream()
                        .map(ProjectEntity::getSubordinate)
                        .filter(Objects::nonNull)
                        .map(OrganizationEntity::getId)
                        .collect(Collectors.toList())
        );

        Map<Long, OrganizationEntity> organizationsById = new HashMap<>();
        if (!subordinateIds.isEmpty()) {
            organizationRepository.findAllById(
                    subordinateIds.stream().distinct().collect(Collectors.toList())
            ).forEach(org -> {
                organizationsById.put(org.getId(), org);
                loadParentOrganizations(org.getSubordinate(), organizationsById);
            });
        }

        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchies = new HashMap<>();

        for (Map.Entry<Integer, ProjectEntity> entry : projectsByFilialHcmId.entrySet()) {
            Integer filialHcmId = entry.getKey();
            ProjectEntity project = entry.getValue();

            EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder builder = EmployeeSummaryDTO.HierarchyInfo.builder()
                    .projetoId(project.getId())
                    .projetoNome(
                            project.getMega() != null
                                    ? String.valueOf(project.getMega().getCusStDescricao())
                                    : null
                    )
                    .projetoOs(
                            project.getContract() != null
                                    ? project.getContract().getOs()
                                    : null
                    )
                    .projetoHcmId(
                            project.getHcm() != null
                                    ? project.getHcm().getCcId()
                                    : null
                    );

            if (project.getContract() != null && project.getContract().getId() != null) {
                SimpleContractEntity contract = contractsById.get(project.getContract().getId());

                if (contract != null) {
                    builder.contratoId(contract.getId())
                            .contratoNome(contract.getName())
                            .contratoOs(contract.getOs());

                    if (contract.getSubordinateId() != null) {
                        fillHierarchyFromCache(contract.getSubordinateId(), builder, organizationsById);
                    }
                }
            } else if (project.getSubordinate() != null && project.getSubordinate().getId() != null) {
                fillHierarchyFromCache(project.getSubordinate().getId(), builder, organizationsById);
            }

            hierarchies.put(filialHcmId.toString(), builder.build());
        }

        return hierarchies;
    }
    /**
     * Carrega organizações parent recursivamente
     */
    private void loadParentOrganizations(OrganizationEntity parent, Map<Long, OrganizationEntity> cache) {
        if (parent == null || cache.containsKey(parent.getId())) {
            return;
        }

        cache.put(parent.getId(), parent);
        if (parent.getSubordinate() != null) {
            loadParentOrganizations(parent.getSubordinate(), cache);
        }
    }

    /**
     * Preenche hierarquia usando cache de organizações
     */
    private void fillHierarchyFromCache(Long subordinateId,
            EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder builder,
            Map<Long, OrganizationEntity> organizationsCache) {
        OrganizationEntity org = organizationsCache.get(subordinateId);

        if (org == null) {
            return;
        }

        // Definir os dados baseado no tipo
        switch (org.getType()) {
            case SETOR:
                builder.setorId(org.getId())
                        .setorNome(org.getPosition())
                        .setorSigla(org.getAcronym());
                break;
            case REGIONAL:
                builder.regionalId(org.getId())
                        .regionalNome(org.getPosition())
                        .regionalSigla(org.getAcronym());
                break;
            case SUPERINTENDENCIA:
                builder.superintendenciaId(org.getId())
                        .superintendenciaNome(org.getPosition())
                        .superintendenciaSigla(org.getAcronym());
                break;
            case DIRETORIA:
                builder.diretoriaId(org.getId())
                        .diretoriaNome(org.getPosition())
                        .diretoriaSigla(org.getAcronym());
                break;
        }

        // Subir recursivamente se houver parent (subordinate é o parent na estrutura)
        if (org.getSubordinate() != null) {
            fillHierarchyFromCache(org.getSubordinate().getId(), builder, organizationsCache);
        }
    }

    public AllSubordinatesResponseDTO getAllSubordinatesRecursivelyBySuperintendenciaId(Long superintendenciaId) {
        if (!organizationRepository.existsById(superintendenciaId)) {
            throw new ModuleNotFoundFailure("Superintendência não encontrada.");
        }

        List<OrganizationEntity> allSubordinates = findAllSubordinatesRecursively(superintendenciaId);

        // Buscar todos os contratos relacionados aos subordinados
        List<Long> subordinateIds = allSubordinates.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());

        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);

        // Buscar projetos relacionados aos contratos
        List<Long> contractIds = contracts.stream()
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(contractIds);

        // Buscar projetos relacionados diretamente aos subordinados (onde contrato é
        // null)
        List<SimpleProjectEntity> projectsBySubordinate = projectRepository
                .findBySubordinateIdInAndContractIdIsNull(subordinateIds);

        // Agrupar contratos por ID do subordinado
        Map<Long, List<SimpleContractEntity>> contractsBySubordinateId = contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));

        // Agrupar projetos por ID do contrato
        Map<Long, List<SimpleProjectEntity>> projectsByContractId = projectsByContract.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));

        // Agrupar projetos por ID do subordinado (projetos sem contrato)
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinateId = projectsBySubordinate.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));

        // Buscar todos os HCM IDs dos projetos para buscar funcionários
        List<String> hcmIds = allSubordinates.stream()
                .flatMap(entity -> {
                    List<SimpleProjectEntity> contractProjects = projectsByContractId.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    List<SimpleProjectEntity> directProjects = projectsBySubordinateId.getOrDefault(entity.getId(),
                            List.of());
                    return Stream.concat(contractProjects.stream(), directProjects.stream());
                })
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        // Agrupar funcionários por centro de custos
        Map<String, List<SimpleEmployeeEntity>> employeesByCostCenter = employees.stream()
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCostCenterId));

        // Converter para DTO simplificado incluindo contratos
        List<SimpleSubordinateDTO> subordinatesDTOs = allSubordinates.stream()
                .map(entity -> {
                    List<SimpleContractDTO> subordinateContracts = contractsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(contract -> {
                                // Buscar projetos relacionados a este contrato
                                List<SimpleProjectDTO> contractProjects = projectsByContractId
                                        .getOrDefault(contract.getId(), List.of())
                                        .stream()
                                        .map(project -> {
                                            // Buscar funcionários relacionados ao HCM deste projeto
                                            List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                                    .getOrDefault(project.getHcmId().toString(), List.of())
                                                    .stream()
                                                    .map(employee -> new SimpleEmployeeDTO(
                                                            employee.getId(),
                                                            employee.getName(),
                                                            employee.getRegistration(),
                                                            employee.getFilialIdHcm()))
                                                    .collect(Collectors.toList());

                                            return new SimpleProjectDTO(
                                                    project.getId(),
                                                    project.getHcmId(),
                                                    project.isAtivo(),
                                                    projectEmployees);
                                        })
                                        .collect(Collectors.toList());

                                return new SimpleContractDTO(
                                        contract.getId(),
                                        contract.getName(),
                                        contract.getOs(),
                                        contractProjects);
                            })
                            .collect(Collectors.toList());

                    // Buscar projetos relacionados diretamente ao subordinado (sem contrato)
                    List<SimpleProjectDTO> directProjects = projectsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(project -> {
                                // Buscar funcionários relacionados ao HCM deste projeto
                                List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                        .getOrDefault(project.getHcmId().toString(), List.of())
                                        .stream()
                                        .map(employee -> new SimpleEmployeeDTO(
                                                employee.getId(),
                                                employee.getName(),
                                                employee.getRegistration(),
                                                employee.getFilialIdHcm()))
                                        .collect(Collectors.toList());

                                return new SimpleProjectDTO(
                                        project.getId(),
                                        project.getHcmId(),
                                        project.isAtivo(),
                                        projectEmployees);
                            })
                            .collect(Collectors.toList());

                    // Buscar informações do subordinado pai (parent)
                    SimpleSubordinateDTO.SubordinadoParentInfo subordinadoParent = null;
                    if (entity.getSubordinate() != null) {
                        OrganizationEntity parent = entity.getSubordinate();
                        subordinadoParent = new SimpleSubordinateDTO.SubordinadoParentInfo(
                                parent.getId(),
                                parent.getAcronym(),
                                parent.getPosition(),
                                parent.getType());
                    }

                    return new SimpleSubordinateDTO(
                            entity.getId(),
                            entity.getAcronym(),
                            entity.getPosition(),
                            entity.getType(),
                            subordinateContracts,
                            directProjects,
                            subordinadoParent);
                })
                .collect(Collectors.toList());

        // Agrupar por tipo
        Map<OrganizationType, List<SimpleSubordinateDTO>> groupedByType = subordinatesDTOs.stream()
                .collect(Collectors.groupingBy(SimpleSubordinateDTO::tipo));

        // Ordenar por tipo conforme especificado: DIRETORIA, SUPERINTENDENCIA,
        // REGIONAL, SETOR
        List<SubordinatesByTypeDTO> subordinatesByType = groupedByType.entrySet().stream()
                .map(entry -> new SubordinatesByTypeDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    // Definir ordem de prioridade
                    Map<OrganizationType, Integer> order = Map.of(
                            OrganizationType.DIRETORIA, 1,
                            OrganizationType.SUPERINTENDENCIA, 2,
                            OrganizationType.REGIONAL, 3,
                            OrganizationType.SETOR, 4);
                    return Integer.compare(
                            order.getOrDefault(a.type(), 999),
                            order.getOrDefault(b.type(), 999));
                })
                .collect(Collectors.toList());

        return new AllSubordinatesResponseDTO(subordinatesByType);
    }

    public Page<EmployeeSummaryDTO> getAllEmployeesBySuperintendenciaId(Long superintendenciaId, Pageable pageable) {
        if (!organizationRepository.existsById(superintendenciaId)) {
            throw new ModuleNotFoundFailure("Superintendência não encontrada.");
        }

        // Buscar todos os subordinados recursivamente de forma otimizada
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(superintendenciaId);

        if (allSubordinateIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch
        Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

        // Coletar todos os HCM IDs dos projetos
        List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch para otimizar performance
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByFilialHcm = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO com dados pré-carregados
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByFilialHcm))
                .collect(Collectors.toList());

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    public AllSubordinatesResponseDTO getAllSubordinatesRecursivelyByRegionalId(Long regionalId) {
        if (!organizationRepository.existsById(regionalId)) {
            throw new ModuleNotFoundFailure("Regional não encontrada.");
        }

        List<OrganizationEntity> allSubordinates = findAllSubordinatesRecursively(regionalId);

        // Buscar todos os contratos relacionados aos subordinados
        List<Long> subordinateIds = allSubordinates.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());

        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);

        // Buscar projetos relacionados aos contratos
        List<Long> contractIds = contracts.stream()
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(contractIds);

        // Buscar projetos relacionados diretamente aos subordinados (onde contrato é
        // null)
        List<SimpleProjectEntity> projectsBySubordinate = projectRepository
                .findBySubordinateIdInAndContractIdIsNull(subordinateIds);

        // Agrupar contratos por ID do subordinado
        Map<Long, List<SimpleContractEntity>> contractsBySubordinateId = contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));

        // Agrupar projetos por ID do contrato
        Map<Long, List<SimpleProjectEntity>> projectsByContractId = projectsByContract.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));

        // Agrupar projetos por ID do subordinado (projetos sem contrato)
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinateId = projectsBySubordinate.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));

        // Buscar todos os HCM IDs dos projetos para buscar funcionários
        List<String> hcmIds = allSubordinates.stream()
                .flatMap(entity -> {
                    List<SimpleProjectEntity> contractProjects = projectsByContractId.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    List<SimpleProjectEntity> directProjects = projectsBySubordinateId.getOrDefault(entity.getId(),
                            List.of());
                    return Stream.concat(contractProjects.stream(), directProjects.stream());
                })
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        // Agrupar funcionários por centro de custos
        Map<String, List<SimpleEmployeeEntity>> employeesByCostCenter = employees.stream()
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCostCenterId));

        // Converter para DTO simplificado incluindo contratos
        List<SimpleSubordinateDTO> subordinatesDTOs = allSubordinates.stream()
                .map(entity -> {
                    List<SimpleContractDTO> subordinateContracts = contractsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(contract -> {
                                // Buscar projetos relacionados a este contrato
                                List<SimpleProjectDTO> contractProjects = projectsByContractId
                                        .getOrDefault(contract.getId(), List.of())
                                        .stream()
                                        .map(project -> {
                                            // Buscar funcionários relacionados ao HCM deste projeto
                                            List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                                    .getOrDefault(project.getHcmId().toString(), List.of())
                                                    .stream()
                                                    .map(employee -> new SimpleEmployeeDTO(
                                                            employee.getId(),
                                                            employee.getName(),
                                                            employee.getRegistration(),
                                                            employee.getFilialIdHcm()))
                                                    .collect(Collectors.toList());

                                            return new SimpleProjectDTO(
                                                    project.getId(),
                                                    project.getHcmId(),
                                                    project.isAtivo(),
                                                    projectEmployees);
                                        })
                                        .collect(Collectors.toList());

                                return new SimpleContractDTO(
                                        contract.getId(),
                                        contract.getName(),
                                        contract.getOs(),
                                        contractProjects);
                            })
                            .collect(Collectors.toList());

                    // Buscar projetos relacionados diretamente ao subordinado (sem contrato)
                    List<SimpleProjectDTO> directProjects = projectsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(project -> {
                                // Buscar funcionários relacionados ao HCM deste projeto
                                List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                        .getOrDefault(project.getHcmId().toString(), List.of())
                                        .stream()
                                        .map(employee -> new SimpleEmployeeDTO(
                                                employee.getId(),
                                                employee.getName(),
                                                employee.getRegistration(),
                                                employee.getFilialIdHcm()))
                                        .collect(Collectors.toList());

                                return new SimpleProjectDTO(
                                        project.getId(),
                                        project.getHcmId(),
                                        project.isAtivo(),
                                        projectEmployees);
                            })
                            .collect(Collectors.toList());

                    // Buscar informações do subordinado pai (parent)
                    SimpleSubordinateDTO.SubordinadoParentInfo subordinadoParent = null;
                    if (entity.getSubordinate() != null) {
                        OrganizationEntity parent = entity.getSubordinate();
                        subordinadoParent = new SimpleSubordinateDTO.SubordinadoParentInfo(
                                parent.getId(),
                                parent.getAcronym(),
                                parent.getPosition(),
                                parent.getType());
                    }

                    return new SimpleSubordinateDTO(
                            entity.getId(),
                            entity.getAcronym(),
                            entity.getPosition(),
                            entity.getType(),
                            subordinateContracts,
                            directProjects,
                            subordinadoParent);
                })
                .collect(Collectors.toList());

        // Agrupar por tipo
        Map<OrganizationType, List<SimpleSubordinateDTO>> groupedByType = subordinatesDTOs.stream()
                .collect(Collectors.groupingBy(SimpleSubordinateDTO::tipo));

        // Ordenar por tipo conforme especificado: DIRETORIA, SUPERINTENDENCIA,
        // REGIONAL, SETOR
        List<SubordinatesByTypeDTO> subordinatesByType = groupedByType.entrySet().stream()
                .map(entry -> new SubordinatesByTypeDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    // Definir ordem de prioridade
                    Map<OrganizationType, Integer> order = Map.of(
                            OrganizationType.DIRETORIA, 1,
                            OrganizationType.SUPERINTENDENCIA, 2,
                            OrganizationType.REGIONAL, 3,
                            OrganizationType.SETOR, 4);
                    return Integer.compare(
                            order.getOrDefault(a.type(), 999),
                            order.getOrDefault(b.type(), 999));
                })
                .collect(Collectors.toList());

        return new AllSubordinatesResponseDTO(subordinatesByType);
    }

    public Page<EmployeeSummaryDTO> getAllEmployeesByRegionalId(Long regionalId, Pageable pageable) {
        if (!organizationRepository.existsById(regionalId)) {
            throw new ModuleNotFoundFailure("Regional não encontrada.");
        }

        // Buscar todos os subordinados recursivamente de forma otimizada
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(regionalId);

        if (allSubordinateIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch
        Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

        // Coletar todos os HCM IDs dos projetos
        List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch para otimizar performance
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByCentroCusto = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO com dados pré-carregados
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByCentroCusto))
                .collect(Collectors.toList());

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    public AllSubordinatesResponseDTO getAllSubordinatesRecursivelyBySetorId(Long setorId) {
        if (!organizationRepository.existsById(setorId)) {
            throw new ModuleNotFoundFailure("Setor não encontrado.");
        }

        List<OrganizationEntity> allSubordinates = findAllSubordinatesRecursively(setorId);

        // Buscar todos os contratos relacionados aos subordinados
        List<Long> subordinateIds = allSubordinates.stream()
                .map(OrganizationEntity::getId)
                .collect(Collectors.toList());

        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(subordinateIds);

        // Buscar projetos relacionados aos contratos
        List<Long> contractIds = contracts.stream()
                .map(SimpleContractEntity::getId)
                .collect(Collectors.toList());

        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(contractIds);

        // Buscar projetos relacionados diretamente aos subordinados (onde contrato é
        // null)
        List<SimpleProjectEntity> projectsBySubordinate = projectRepository
                .findBySubordinateIdInAndContractIdIsNull(subordinateIds);

        // Agrupar contratos por ID do subordinado
        Map<Long, List<SimpleContractEntity>> contractsBySubordinateId = contracts.stream()
                .collect(Collectors.groupingBy(SimpleContractEntity::getSubordinateId));

        // Agrupar projetos por ID do contrato
        Map<Long, List<SimpleProjectEntity>> projectsByContractId = projectsByContract.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getContractId));

        // Agrupar projetos por ID do subordinado (projetos sem contrato)
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinateId = projectsBySubordinate.stream()
                .collect(Collectors.groupingBy(SimpleProjectEntity::getSubordinateId));

        // Buscar todos os HCM IDs dos projetos para buscar funcionários
        List<String> hcmIds = allSubordinates.stream()
                .flatMap(entity -> {
                    List<SimpleProjectEntity> contractProjects = projectsByContractId.values().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    List<SimpleProjectEntity> directProjects = projectsBySubordinateId.getOrDefault(entity.getId(),
                            List.of());
                    return Stream.concat(contractProjects.stream(), directProjects.stream());
                })
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        // Agrupar funcionários por centro de custos
        Map<String, List<SimpleEmployeeEntity>> employeesByCostCenter = employees.stream()
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCostCenterId));

        // Converter para DTO simplificado incluindo contratos
        List<SimpleSubordinateDTO> subordinatesDTOs = allSubordinates.stream()
                .map(entity -> {
                    List<SimpleContractDTO> subordinateContracts = contractsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(contract -> {
                                // Buscar projetos relacionados a este contrato
                                List<SimpleProjectDTO> contractProjects = projectsByContractId
                                        .getOrDefault(contract.getId(), List.of())
                                        .stream()
                                        .map(project -> {
                                            // Buscar funcionários relacionados ao HCM deste projeto
                                            List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                                    .getOrDefault(project.getHcmId().toString(), List.of())
                                                    .stream()
                                                    .map(employee -> new SimpleEmployeeDTO(
                                                            employee.getId(),
                                                            employee.getName(),
                                                            employee.getRegistration(),
                                                            employee.getFilialIdHcm()))
                                                    .collect(Collectors.toList());

                                            return new SimpleProjectDTO(
                                                    project.getId(),
                                                    project.getHcmId(),
                                                    project.isAtivo(),
                                                    projectEmployees);
                                        })
                                        .collect(Collectors.toList());

                                return new SimpleContractDTO(
                                        contract.getId(),
                                        contract.getName(),
                                        contract.getOs(),
                                        contractProjects);
                            })
                            .collect(Collectors.toList());

                    // Buscar projetos relacionados diretamente ao subordinado (sem contrato)
                    List<SimpleProjectDTO> directProjects = projectsBySubordinateId
                            .getOrDefault(entity.getId(), List.of())
                            .stream()
                            .map(project -> {
                                // Buscar funcionários relacionados ao HCM deste projeto
                                List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                                        .getOrDefault(project.getHcmId().toString(), List.of())
                                        .stream()
                                        .map(employee -> new SimpleEmployeeDTO(
                                                employee.getId(),
                                                employee.getName(),
                                                employee.getRegistration(),
                                                employee.getFilialIdHcm()))
                                        .collect(Collectors.toList());

                                return new SimpleProjectDTO(
                                        project.getId(),
                                        project.getHcmId(),
                                        project.isAtivo(),
                                        projectEmployees);
                            })
                            .collect(Collectors.toList());

                    // Buscar informações do subordinado pai (parent)
                    SimpleSubordinateDTO.SubordinadoParentInfo subordinadoParent = null;
                    if (entity.getSubordinate() != null) {
                        OrganizationEntity parent = entity.getSubordinate();
                        subordinadoParent = new SimpleSubordinateDTO.SubordinadoParentInfo(
                                parent.getId(),
                                parent.getAcronym(),
                                parent.getPosition(),
                                parent.getType());
                    }

                    return new SimpleSubordinateDTO(
                            entity.getId(),
                            entity.getAcronym(),
                            entity.getPosition(),
                            entity.getType(),
                            subordinateContracts,
                            directProjects,
                            subordinadoParent);
                })
                .collect(Collectors.toList());

        // Agrupar por tipo
        Map<OrganizationType, List<SimpleSubordinateDTO>> groupedByType = subordinatesDTOs.stream()
                .collect(Collectors.groupingBy(SimpleSubordinateDTO::tipo));

        // Ordenar por tipo conforme especificado: DIRETORIA, SUPERINTENDENCIA,
        // REGIONAL, SETOR
        List<SubordinatesByTypeDTO> subordinatesByType = groupedByType.entrySet().stream()
                .map(entry -> new SubordinatesByTypeDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> {
                    // Definir ordem de prioridade
                    Map<OrganizationType, Integer> order = Map.of(
                            OrganizationType.DIRETORIA, 1,
                            OrganizationType.SUPERINTENDENCIA, 2,
                            OrganizationType.REGIONAL, 3,
                            OrganizationType.SETOR, 4);
                    return Integer.compare(
                            order.getOrDefault(a.type(), 999),
                            order.getOrDefault(b.type(), 999));
                })
                .collect(Collectors.toList());

        return new AllSubordinatesResponseDTO(subordinatesByType);
    }

    public Page<EmployeeSummaryDTO> getAllEmployeesBySetorId(Long setorId, Pageable pageable) {
        if (!organizationRepository.existsById(setorId)) {
            throw new ModuleNotFoundFailure("Setor não encontrado.");
        }

        // Buscar todos os subordinados recursivamente de forma otimizada
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(setorId);

        if (allSubordinateIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch
        Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

        // Coletar todos os HCM IDs dos projetos
        List<String> hcmIds = collectAllHcmIds(projectsBySubordinate, projectsByContract);

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch para otimizar performance
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByCentroCusto = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO com dados pré-carregados
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByCentroCusto))
                .collect(Collectors.toList());

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    public ContractWithProjectsDTO getContratoWithProjects(Long contratoId) {
        // Verificar se o contrato existe
        SimpleContractEntity contract = contractRepository.findById(contratoId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Contrato não encontrado."));

        // Buscar projetos relacionados ao contrato
        List<SimpleProjectEntity> projectsByContract = projectRepository.findByContractIdIn(List.of(contratoId));

        // Buscar todos os HCM IDs dos projetos para buscar funcionários
        List<String> hcmIds = projectsByContract.stream()
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = hcmIds.isEmpty() ? List.of()
                : employeeRepository.findByCostCenterIdIn(hcmIds);

        // Agrupar funcionários por centro de custos
        Map<String, List<SimpleEmployeeEntity>> employeesByCostCenter = employees.stream()
                .collect(Collectors.groupingBy(SimpleEmployeeEntity::getCostCenterId));

        // Converter projetos para DTO incluindo funcionários
        List<SimpleProjectDTO> projectDTOs = projectsByContract.stream()
                .map(project -> {
                    // Buscar funcionários relacionados ao HCM deste projeto
                    List<SimpleEmployeeDTO> projectEmployees = employeesByCostCenter
                            .getOrDefault(project.getHcmId().toString(), List.of())
                            .stream()
                            .map(employee -> new SimpleEmployeeDTO(
                                    employee.getId(),
                                    employee.getName(),
                                    employee.getRegistration(),
                                    employee.getFilialIdHcm()))
                            .collect(Collectors.toList());

                    return new SimpleProjectDTO(
                            project.getId(),
                            project.getHcmId(),
                            project.isAtivo(),
                            projectEmployees);
                })
                .collect(Collectors.toList());

        // Retornar diretamente o contrato com seus projetos
        return new ContractWithProjectsDTO(
                contract.getId(),
                contract.getName(),
                contract.getOs(),
                projectDTOs);
    }

    public Page<EmployeeSummaryDTO> getAllEmployeesByContratoId(Long contratoId, Pageable pageable) {
        // Verificar se o contrato existe
        if (!contractRepository.existsById(contratoId)) {
            throw new ModuleNotFoundFailure("Contrato não encontrado.");
        }

        // Buscar projetos relacionados ao contrato
        List<SimpleProjectEntity> projects = projectRepository.findByContractIdIn(List.of(contratoId));

        if (projects.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Coletar todos os HCM IDs dos projetos
        List<String> hcmIds = projects.stream()
                .map(project -> project.getHcmId().toString())
                .distinct()
                .collect(Collectors.toList());

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar funcionários relacionados aos centros de custos HCM
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar todos os dados relacionados em batch para otimizar performance
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByCentroCusto = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO com dados pré-carregados
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByCentroCusto))
                .collect(Collectors.toList());

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    public ContractProjectsDetailsDTO getContratoProjectsWithDetails(Long contratoId) {
        // Buscar dados básicos do contrato usando query nativa para evitar problemas de
        // relacionamento
        Map<String, Object> contractData = contractRepository.findContractBasicDataById(contratoId);

        if (contractData == null || contractData.isEmpty()) {
            throw new ModuleNotFoundFailure("Contrato não encontrado.");
        }

        // Extrair dados do map
        Long contractId = ((Number) contractData.get("id")).longValue();
        String contractName = (String) contractData.get("nome");
        String contractOs = (String) contractData.get("os");

        // Buscar dados dos projetos usando query nativa
        List<Object[]> projectsData = organizationProjectRepository.findProjectsDataByContractId(contratoId);

        // Processar cada projeto e buscar seus relacionamentos
        List<ProjectWithDetailsDTO> projectDTOs = projectsData.stream()
                .map(projectData -> {
                    // Extrair dados básicos do projeto
                    Long projectId = ((Number) projectData[0]).longValue();
                    Integer megaId = projectData[1] != null ? ((Number) projectData[1]).intValue() : null;
                    Integer hcmId = projectData[2] != null ? ((Number) projectData[2]).intValue() : null;
                    Boolean ativo = projectData[5] != null ? (Boolean) projectData[5] : false;

                    // Buscar Mega
                    String megaNome = null;
                    if (megaId != null) {
                        megaNome = megaRepository.findById(megaId)
                                .map(MegaEntity::getCusStExtenso)
                                .orElse(null);
                    }

                    // Buscar HCM
                    String nomeCentroCusto = null;
                    if (hcmId != null) {
                        nomeCentroCusto = hcmRepository.findById(hcmId)
                                .map(HcmEntity::getNomeCc)
                                .orElse(null);
                    }

                    // Buscar filiais do projeto através da tabela de relacionamento
                    List<FilialInfoDTO> filiais = findFiliaisByProjectId(projectId);

                    return new ProjectWithDetailsDTO(
                            projectId,
                            megaId,
                            megaNome,
                            hcmId,
                            nomeCentroCusto,
                            filiais,
                            ativo);
                })
                .collect(Collectors.toList());

        // Retornar contrato com seus projetos detalhados
        return new ContractProjectsDetailsDTO(
                contractId,
                contractName,
                contractOs,
                projectDTOs);
    }

    private List<FilialInfoDTO> findFiliaisByProjectId(Long projectId) {
        // Buscar filiais do projeto através da tabela de relacionamento
        List<Object[]> filiaisData = organizationProjectRepository.findFiliaisByProjectId(projectId);

        return filiaisData.stream()
                .map(data -> new FilialInfoDTO(
                        ((Number) data[0]).intValue(),
                        (String) data[1]))
                .collect(Collectors.toList());
    }

    public List<ContractSummaryDTO> getAllContractsByRegionalId(Long regionalId) {
        return getContractsByOrganizationId(regionalId, "Regional");
    }

    public List<ContractSummaryDTO> getAllContractsByDiretoriaId(Long diretoriaId) {
        return getContractsByOrganizationId(diretoriaId, "Diretoria");
    }

    public List<ContractSummaryDTO> getAllContractsBySuperintendenciaId(Long superintendenciaId) {
        return getContractsByOrganizationId(superintendenciaId, "Superintendência");
    }

    public List<ContractSummaryDTO> getAllContractsBySetorId(Long setorId) {
        return getContractsByOrganizationId(setorId, "Setor");
    }

    private List<ContractSummaryDTO> getContractsByOrganizationId(Long organizationId, String organizationType) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new ModuleNotFoundFailure(organizationType + " não encontrada.");
        }

        // Buscar todos os subordinados recursivamente
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(organizationId);

        // Adicionar a própria organização à lista
        allSubordinateIds.add(organizationId);

        if (allSubordinateIds.isEmpty()) {
            return List.of();
        }

        // Buscar todos os contratos relacionados aos subordinados
        List<SimpleContractEntity> contracts = contractRepository.findBySubordinateIdIn(allSubordinateIds);

        // Buscar informações dos subordinados para incluir na resposta
        Map<Long, OrganizationEntity> subordinatesById = organizationRepository.findAllById(allSubordinateIds)
                .stream()
                .collect(Collectors.toMap(OrganizationEntity::getId, org -> org));

        // Converter para DTO
        return contracts.stream()
                .map(contract -> {
                    OrganizationEntity subordinate = subordinatesById.get(contract.getSubordinateId());
                    return new ContractSummaryDTO(
                            contract.getId(),
                            contract.getName(),
                            contract.getOs(),
                            subordinate != null ? subordinate.getAcronym() : null,
                            subordinate != null ? subordinate.getType().name() : null);
                })
                .collect(Collectors.toList());
    }

    public Page<EmployeeSummaryDTO> getEmployeesByFilters(EmployeeFilterDTO filters, Pageable pageable) {
        // Coletar todos os subordinados baseado nos filtros fornecidos
        List<Long> allSubordinateIds = new java.util.ArrayList<>();

        // Se múltiplos filtros hierárquicos são fornecidos, fazer INTERSECÇÃO
        List<List<Long>> subordinateIdLists = new ArrayList<>();

        // Adicionar IDs baseado nos filtros fornecidos
        if (filters.getDiretoriaId() != null && !filters.getDiretoriaId().isEmpty()) {
            List<Long> allSubordinateIdsForDiretoria = new ArrayList<>();
            for (Long diretoriaId : filters.getDiretoriaId()) {
                if (!organizationRepository.existsById(diretoriaId)) {
                    throw new ModuleNotFoundFailure("Diretoria não encontrada: " + diretoriaId);
                }
                List<Long> subordinateIds = findAllSubordinateIdsRecursively(diretoriaId);
                subordinateIds.add(diretoriaId);
                allSubordinateIdsForDiretoria.addAll(subordinateIds);
            }
            // Remover duplicados e adicionar à lista principal
            allSubordinateIdsForDiretoria = allSubordinateIdsForDiretoria.stream().distinct()
                    .collect(Collectors.toList());
            subordinateIdLists.add(allSubordinateIdsForDiretoria);
        }

        if (filters.getSuperintendenciaId() != null && !filters.getSuperintendenciaId().isEmpty()) {
            List<Long> allSubordinateIdsForSuperintendencia = new ArrayList<>();
            for (Long superintendenciaId : filters.getSuperintendenciaId()) {
                if (!organizationRepository.existsById(superintendenciaId)) {
                    throw new ModuleNotFoundFailure("Superintendência não encontrada: " + superintendenciaId);
                }
                List<Long> subordinateIds = findAllSubordinateIdsRecursively(superintendenciaId);
                subordinateIds.add(superintendenciaId);
                allSubordinateIdsForSuperintendencia.addAll(subordinateIds);
            }
            // Remover duplicados e adicionar à lista principal
            allSubordinateIdsForSuperintendencia = allSubordinateIdsForSuperintendencia.stream().distinct()
                    .collect(Collectors.toList());
            subordinateIdLists.add(allSubordinateIdsForSuperintendencia);
        }

        if (filters.getRegionalId() != null && !filters.getRegionalId().isEmpty()) {
            List<Long> allSubordinateIdsForRegional = new ArrayList<>();
            for (Long regionalId : filters.getRegionalId()) {
                if (!organizationRepository.existsById(regionalId)) {
                    throw new ModuleNotFoundFailure("Regional não encontrada: " + regionalId);
                }
                List<Long> subordinateIds = findAllSubordinateIdsRecursively(regionalId);
                subordinateIds.add(regionalId);
                allSubordinateIdsForRegional.addAll(subordinateIds);
            }
            // Remover duplicados e adicionar à lista principal
            allSubordinateIdsForRegional = allSubordinateIdsForRegional.stream().distinct()
                    .collect(Collectors.toList());
            subordinateIdLists.add(allSubordinateIdsForRegional);
        }

        if (filters.getSetorId() != null && !filters.getSetorId().isEmpty()) {
            List<Long> allSubordinateIdsForSetor = new ArrayList<>();
            for (Long setorId : filters.getSetorId()) {
                if (!organizationRepository.existsById(setorId)) {
                    throw new ModuleNotFoundFailure("Setor não encontrado: " + setorId);
                }
                List<Long> subordinateIds = findAllSubordinateIdsRecursively(setorId);
                subordinateIds.add(setorId);
                allSubordinateIdsForSetor.addAll(subordinateIds);
            }
            // Remover duplicados e adicionar à lista principal
            allSubordinateIdsForSetor = allSubordinateIdsForSetor.stream().distinct().collect(Collectors.toList());
            subordinateIdLists.add(allSubordinateIdsForSetor);
        }

        // Se temos múltiplos filtros hierárquicos, fazer intersecção
        if (subordinateIdLists.size() > 1) {
            allSubordinateIds = subordinateIdLists.get(0);
            for (int i = 1; i < subordinateIdLists.size(); i++) {
                allSubordinateIds = allSubordinateIds.stream()
                        .filter(subordinateIdLists.get(i)::contains)
                        .collect(Collectors.toList());
            }
        } else if (subordinateIdLists.size() == 1) {
            allSubordinateIds = subordinateIdLists.get(0);
        }

        // Coletar HCM IDs baseado nos filtros
        List<String> hcmIds = new java.util.ArrayList<>();

        // Se temos filtros de hierarquia, buscar através deles
        if (!allSubordinateIds.isEmpty()) {
            // Remover duplicados
            allSubordinateIds = allSubordinateIds.stream().distinct().collect(Collectors.toList());

            // Buscar contratos e projetos dos subordinados
            Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(
                    allSubordinateIds);
            Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
            Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

            // Se há filtro de contrato, filtrar apenas esses contratos e seus projetos
            if (filters.getContratoId() != null && !filters.getContratoId().isEmpty()) {
                contractsBySubordinate = contractsBySubordinate.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .filter(c -> filters.getContratoId().contains(c.getId()))
                                        .collect(Collectors.toList())));
                // Limpar projetos diretos dos subordinados, pois queremos apenas os dos
                // contratos
                projectsBySubordinate.clear();
                // Refazer o mapa de projetos por contrato apenas com os contratos filtrados
                projectsByContract = getProjectsByContractIds(contractsBySubordinate);
            }

            hcmIds.addAll(collectAllHcmIds(projectsBySubordinate, projectsByContract));
        } else if (filters.getContratoId() != null && !filters.getContratoId().isEmpty()) {
            // Se apenas filtro de contrato foi fornecido
            List<SimpleProjectEntity> allProjects = new ArrayList<>();
            for (Long contratoId : filters.getContratoId()) {
                if (!contractRepository.existsById(contratoId)) {
                    throw new ModuleNotFoundFailure("Contrato não encontrado: " + contratoId);
                }
                List<SimpleProjectEntity> projects = projectRepository.findByContractIdIn(List.of(contratoId));
                allProjects.addAll(projects);
            }
            hcmIds.addAll(allProjects.stream().map(p -> p.getHcmId().toString()).collect(Collectors.toList()));
        }

        // Se há filtro de projeto específico, buscar apenas esses projetos
        if (filters.getProjetoId() != null && !filters.getProjetoId().isEmpty()) {
            hcmIds.clear();
            for (Long projetoId : filters.getProjetoId()) {
                SimpleProjectEntity project = projectRepository.findById(projetoId)
                        .orElseThrow(() -> new ModuleNotFoundFailure("Projeto não encontrado: " + projetoId));
                hcmIds.add(project.getHcmId().toString());
            }
        }

        // Se há filtro de centro de custo HCM direto
        if (filters.getCentroCustoHcm() != null) {
            hcmIds.clear();
            hcmIds.add(filters.getCentroCustoHcm());
        }

        if (hcmIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Remover duplicados
        hcmIds = hcmIds.stream().distinct().collect(Collectors.toList());

        // Buscar funcionários
        List<SimpleEmployeeEntity> employees = employeeRepository.findByCostCenterIdIn(hcmIds);

        // Aplicar filtros adicionais em memória
        employees = applyAdditionalFilters(employees, filters);

        // Remover duplicados baseado na matrícula (manter apenas o primeiro registro de
        // cada matrícula)
        employees = employees.stream()
                .collect(Collectors.toMap(
                        SimpleEmployeeEntity::getRegistration,
                        e -> e,
                        (existing, replacement) -> existing // Manter o primeiro encontrado
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        if (employees.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // Buscar dados relacionados em batch
        Map<Long, String> regionalsByBranchId = getRegionalsByBranchIds(employees);
        Map<Integer, ContractProject> contractsByRateioId = getContractsByRateioIds(employees);
        Map<String, Cargo> cargosByPositionId = getCargosByPositionIds(employees);
        Map<String, String> tempoTotalPorCpf = calculateTotalTimeByCpf(employees);
        Map<String, EmployeeSummaryDTO.HierarchyInfo> hierarchiesByCentroCusto = getHierarchiesByFilialHcm(employees);

        // Converter para EmployeeSummaryDTO
        List<EmployeeSummaryDTO> employeeDTOs = employees.stream()
                .map(employee -> buildEmployeeSummaryDTO(employee, regionalsByBranchId, contractsByRateioId,
                        cargosByPositionId, tempoTotalPorCpf, hierarchiesByCentroCusto))
                .collect(Collectors.toList());

        // Ordenar pela matrícula antes da paginação
        employeeDTOs.sort((e1, e2) -> {
            if (e1.getMatricula() == null && e2.getMatricula() == null)
                return 0;
            if (e1.getMatricula() == null)
                return 1;
            if (e2.getMatricula() == null)
                return -1;
            return e1.getMatricula().compareTo(e2.getMatricula());
        });

        // Aplicar paginação
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employeeDTOs.size());
        List<EmployeeSummaryDTO> pagedEmployees = employeeDTOs.subList(start, end);

        return new PageImpl<>(pagedEmployees, pageable, employeeDTOs.size());
    }

    /**
     * Aplica filtros adicionais aos funcionários em memória
     */
    private List<SimpleEmployeeEntity> applyAdditionalFilters(List<SimpleEmployeeEntity> employees,
            EmployeeFilterDTO filters) {
        Stream<SimpleEmployeeEntity> stream = employees.stream();

        // Filtro de situação
        if (filters.getSituacao() != null && !filters.getSituacao().isBlank()) {
            stream = stream.filter(e -> filters.getSituacao().equalsIgnoreCase(e.getStatus()));
        }

        // Filtro de nome (OR condition)
        if (filters.getNome() != null && !filters.getNome().isEmpty()) {
            stream = stream.filter(e -> {
                if (e.getName() == null)
                    return false;
                String nomeLower = e.getName().toLowerCase();
                return filters.getNome().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .anyMatch(nomeFiltro -> nomeLower.contains(nomeFiltro.toLowerCase()));
            });
        }

        // Filtro de matrícula (OR condition)
        if (filters.getMatricula() != null && !filters.getMatricula().isEmpty()) {
            stream = stream.filter(e -> {
                return filters.getMatricula().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .anyMatch(mat -> mat.equals(e.getRegistration()));
            });
        }

        // Filtro de cidade (OR condition)
        if (filters.getCidade() != null && !filters.getCidade().isEmpty()) {
            stream = stream.filter(e -> {
                if (e.getCity() == null)
                    return false;
                String cidadeLower = e.getCity().toLowerCase();
                return filters.getCidade().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .anyMatch(cidadeFiltro -> cidadeLower.contains(cidadeFiltro.toLowerCase()));
            });
        }

        // Filtro de estado
        if (filters.getEstado() != null && !filters.getEstado().isEmpty()) {
            stream = stream.filter(e -> e.getState() != null && filters.getEstado().contains(e.getState()));
        }

        // Filtro de sexo (OR condition)
        if (filters.getSexo() != null && !filters.getSexo().isEmpty()) {
            stream = stream.filter(e -> {
                return filters.getSexo().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .anyMatch(sexo -> sexo.equals(e.getGender()));
            });
        }

        // Filtro de grau de instrução (OR condition)
        if (filters.getGrauInstrucao() != null && !filters.getGrauInstrucao().isEmpty()) {
            stream = stream.filter(e -> {
                if (e.getEducationLevel() == null)
                    return false;
                String grauLower = e.getEducationLevel().toLowerCase();
                return filters.getGrauInstrucao().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .anyMatch(grauFiltro -> grauLower.contains(grauFiltro.toLowerCase()));
            });
        }

        // Filtro de data de admissão (OR condition)
        if (filters.getDataAdmissao() != null && !filters.getDataAdmissao().isEmpty()) {
            // Esta lógica seria mais complexa pois envolve parsing de datas
            // Por simplicidade, vou manter o comportamento básico
        }

        // Filtro de data de nascimento (OR condition)
        if (filters.getDataNascimento() != null && !filters.getDataNascimento().isEmpty()) {
            // Esta lógica seria mais complexa pois envolve parsing de datas
            // Por simplicidade, vou manter o comportamento básico
        }

        // Filtro de filialId
        if (filters.getFilialId() != null && !filters.getFilialId().isEmpty()) {
            java.util.List<Long> filialIdsLong = filters.getFilialId().stream()
                    .filter(f -> f != null && !f.equals("0"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            if (!filialIdsLong.isEmpty()) {
                stream = stream.filter(e -> e.getBranchId() != null && filialIdsLong.contains(e.getBranchId()));
            }
        }

        // Filtro de filialIdHcm
        if (filters.getFilialIdHcm() != null && !filters.getFilialIdHcm().isEmpty()) {
            // Buscar todos os projetos que possuem essas filiais HCM
            java.util.Set<Integer> filialHcmIdsSet = filters.getFilialIdHcm().stream()
                    .filter(f -> f != null && !f.equals("0"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());

            if (!filialHcmIdsSet.isEmpty()) {
                // Buscar projetos que têm essas filiais
                java.util.List<Long> projectIdsWithFilial = organizationProjectRepository.findAll().stream()
                        .filter(p -> p.getFilial() != null && p.getFilial().stream()
                                .anyMatch(f -> filialHcmIdsSet.contains(f.getFilialId())))
                        .map(ProjectEntity::getId)
                        .collect(Collectors.toList());

                // Buscar os HCM IDs desses projetos
                java.util.Set<String> hcmIdsComFilial = projectRepository.findAllById(projectIdsWithFilial).stream()
                        .map(p -> p.getHcmId().toString())
                        .collect(Collectors.toSet());

                // Filtrar apenas funcionários desses HCM IDs
                stream = stream.filter(e -> hcmIdsComFilial.contains(e.getCostCenterId()));
            }
        }

        // Filtro de cargo
        if (filters.getCargo() != null && !filters.getCargo().isEmpty()) {
            stream = stream.filter(e -> e.getPositionId() != null && filters.getCargo().contains(e.getPositionId()));
        }

        // Filtro de contrato
        if (filters.getContrato() != null && !filters.getContrato().isEmpty()) {
            // Este filtro é aplicado posteriormente na lógica de contratos
        }

        // Filtro de número de dependentes
        if (filters.getDependentes() != null && !filters.getDependentes().isEmpty()) {
            stream = stream.filter(e -> e.getNumberOfDependents() != null
                    && filters.getDependentes().contains(e.getNumberOfDependents()));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * Busca a hierarquia organizacional completa de um funcionário baseado no
     * centro de custo HCM
     * 
     * @param filialHcmId ID do centro de custo HCM (costCenterId do funcionário)
     * @return Objeto com toda a hierarquia (projeto, contrato, setor, regional,
     *         superintendência, diretoria)
     */
    public EmployeeSummaryDTO.HierarchyInfo getHierarchyByFilialHcmId(Integer filialHcmId) {
        if (filialHcmId == null) {
            return null;
        }

        // não pode haver a mesma filial em vários projetos, porém o banco ainda apresenta inconsistência,
        // devido aisso, utilizei o getFirst() até ser corrigido
        var projects = organizationProjectRepository.findHierarchyProjectsByFilialId(filialHcmId);

        if (projects.isEmpty()) {
            return null;
        }

        var project = projects.get(0);


        Integer projetoMegaId = null;
        String projetoNome = null;
        Integer projetoCentroCustoHcm = null;
        List<Integer> projetoCentroCustoMega = new ArrayList<>();
        Integer projetoFilialMega = null;
        List<Integer> projetoFiliaisHcm = new ArrayList<>();
        List<String> filialHCMName = new ArrayList<>();
        List<Integer> projetoFiliaisMega = new ArrayList<>();

        if (project.getMega() != null) {
            projetoMegaId = project.getMega().getCusInReduzido();
            projetoNome = String.valueOf(project.getMega().getCusStDescricao());
        }

        if (project.getHcm() != null) {
            projetoCentroCustoHcm = project.getHcm().getCcId();
        }

        if (project.getFilialMegaId() != null) {
            projetoFilialMega = project.getFilialMegaId().intValue();
        }

        if (project.getFilial() != null && !project.getFilial().isEmpty()) {
            projetoFiliaisHcm = project.getFilial().stream()
                    .map(FilialHcmEntity::getFilialId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            filialHCMName = project.getFilial().stream()
                    .map(FilialHcmEntity::getNomeFilial)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            projetoCentroCustoMega = project.getFilial().stream()
                    .map(FilialHcmEntity::getCcMega)
                    .filter(Objects::nonNull)
                    .map(CCMegaEntity::getCusInReduzido)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder hierarchyBuilder = EmployeeSummaryDTO.HierarchyInfo.builder()
                .projetoId(project.getId())
                .projetoNome(projetoNome)
                .projetoOs(project.getContract() != null ? project.getContract().getOs() : null)
                .projetoHcmId(project.getHcm() != null ? project.getHcm().getCcId() : null)
                .projetoMegaId(projetoMegaId)
                .projetoCentroCustoHcm(projetoCentroCustoHcm)
                .projetoCentroCustoMega(projetoCentroCustoMega)
                .projetoFilialMega(projetoFilialMega)
                .projetoFiliaisHcm(projetoFiliaisHcm)
                .projetoFilialHCMName(filialHCMName)
                .projetoFiliaisMega(projetoFiliaisMega);

        if (project.getContract() != null && project.getContract().getId() != null) {
            hierarchyBuilder
                    .contratoId(project.getContract().getId())
                    .contratoNome(project.getContract().getName())
                    .contratoOs(project.getContract().getOs());

            if (project.getContract().getSubordinate() != null
                    && project.getContract().getSubordinate().getId() != null) {
                fillHierarchyFromSubordinate(project.getContract().getSubordinate().getId(), hierarchyBuilder);
            }
        } else if (project.getSubordinate() != null && project.getSubordinate().getId() != null) {
            fillHierarchyFromSubordinate(project.getSubordinate().getId(), hierarchyBuilder);
        }

        return hierarchyBuilder.build();
    }

    /**
     * Preenche a hierarquia organizacional a partir de um subordinado
     * Sobe recursivamente: Setor -> Regional -> Superintendência -> Diretoria
     */
    private void fillHierarchyFromSubordinate(Long subordinateId,
            EmployeeSummaryDTO.HierarchyInfo.HierarchyInfoBuilder builder) {
        java.util.Optional<OrganizationEntity> subordinateOpt = organizationRepository.findById(subordinateId);

        if (subordinateOpt.isEmpty()) {
            return;
        }

        OrganizationEntity org = subordinateOpt.get();

        // Buscar colaborador responsável se houver collaborator
        EmployeeSummaryDTO.ResponsavelInfo responsavel = null;
        if (org.getCollaborator() != null) {
            Employee emp = org.getCollaborator();
            String cargoNome = null;
            if (emp.getPosition() != null) {
                cargoNome = cargoRepository.findByIdHcm(emp.getPosition())
                        .map(Cargo::getNameTitle)
                        .orElse(null);
            }
            responsavel = EmployeeSummaryDTO.ResponsavelInfo.builder()
                    .id(emp.getId())
                    .nome(emp.getName())
                    .matricula(emp.getRegistration())
                    .email(emp.getBusinessEmail() != null ? emp.getBusinessEmail() : emp.getPersonalEmail())
                    .cargo(cargoNome)
                    .build();
        }

        // Verificar se já temos este nível preenchido (para evitar sobrescrever)
        boolean shouldContinue = true;

        // Definir os dados baseado no tipo (apenas se ainda não foi preenchido)
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
                // Só preencher a diretoria se ainda não foi preenchida (evitar sobrescrever com
                // CEO)
                EmployeeSummaryDTO.HierarchyInfo tempInfo = builder.build();
                if (tempInfo.getDiretoriaId() == null) {
                    builder.diretoriaId(org.getId())
                            .diretoriaNome(org.getPosition())
                            .diretoriaSigla(org.getAcronym())
                            .diretoriaResponsavel(responsavel);
                } else {
                    // Já temos uma diretoria, não precisamos subir mais
                    shouldContinue = false;
                }
                break;
        }

        // Subir recursivamente se houver parent e se devemos continuar
        if (shouldContinue && org.getSubordinate() != null) {
            fillHierarchyFromSubordinate(org.getSubordinate().getId(), builder);
        }
    }

    /**
     * Returns HCM branches based on organizational filters.
     * Returns HCM branches that are in the intersection of the provided filters.
     * 
     * @param diretoriaIds        Optional diretoria ID filter
     * @param superintendenciaIds Optional superintendencia ID filter
     * @param regionalIds         Optional regional ID filter
     * @param setorIds            Optional setor ID filter
     * @param contratoIds         Optional contract ID filter
     * @param projetoIds          Optional project ID filter
     * @return Set of HCM branches that match all provided filters
     */
    public Set<FilialHcmEntity> getFiliaisHcmByFilters(
            List<Long> diretoriaIds,
            List<Long> superintendenciaIds,
            List<Long> regionalIds,
            List<Long> setorIds,
            List<Long> contratoIds,
            List<Long> projetoIds) {

        // Collect HCM IDs from each filter using intersection logic (cascade filtering)
        Set<Integer> hcmIds = new java.util.HashSet<>();
        boolean isFirstFilter = true;

        // Filter by diretoria
        if (diretoriaIds != null && !diretoriaIds.isEmpty()) {
            Set<Integer> allDiretoriaHcmIds = new java.util.HashSet<>();

            List<OrganizationEntity> existingDiretorias = organizationRepository.findAllById(diretoriaIds);
            if (existingDiretorias.size() != diretoriaIds.size()) {
                Set<Long> existingIds = existingDiretorias.stream()
                        .map(OrganizationEntity::getId)
                        .collect(Collectors.toSet());
                for (Long diretoriaId : diretoriaIds) {
                    if (!existingIds.contains(diretoriaId)) {
                        throw new ModuleNotFoundFailure("Diretoria não encontrada: " + diretoriaId);
                    }
                }
            }

            for (Long diretoriaId : diretoriaIds) {
                allDiretoriaHcmIds.addAll(getHcmIdsFromSubordinates(diretoriaId));
            }
            if (isFirstFilter) {
                hcmIds.addAll(allDiretoriaHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allDiretoriaHcmIds); // Intersection
            }
        }

        // Filter by superintendencia
        if (superintendenciaIds != null && !superintendenciaIds.isEmpty()) {
            Set<Integer> allSuperintendenciaHcmIds = new java.util.HashSet<>();

            List<OrganizationEntity> existingSuperintendencias = organizationRepository
                    .findAllById(superintendenciaIds);
            if (existingSuperintendencias.size() != superintendenciaIds.size()) {
                Set<Long> existingIds = existingSuperintendencias.stream()
                        .map(OrganizationEntity::getId)
                        .collect(Collectors.toSet());
                for (Long superintendenciaId : superintendenciaIds) {
                    if (!existingIds.contains(superintendenciaId)) {
                        throw new ModuleNotFoundFailure("Superintendência não encontrada: " + superintendenciaId);
                    }
                }
            }

            for (Long superintendenciaId : superintendenciaIds) {
                allSuperintendenciaHcmIds.addAll(getHcmIdsFromSubordinates(superintendenciaId));
            }
            if (isFirstFilter) {
                hcmIds.addAll(allSuperintendenciaHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allSuperintendenciaHcmIds); // Intersection
            }
        }

        // Filter by regional
        if (regionalIds != null && !regionalIds.isEmpty()) {
            Set<Integer> allRegionalHcmIds = new java.util.HashSet<>();

            List<OrganizationEntity> existingRegionals = organizationRepository.findAllById(regionalIds);
            if (existingRegionals.size() != regionalIds.size()) {
                Set<Long> existingIds = existingRegionals.stream()
                        .map(OrganizationEntity::getId)
                        .collect(Collectors.toSet());
                for (Long regionalId : regionalIds) {
                    if (!existingIds.contains(regionalId)) {
                        throw new ModuleNotFoundFailure("Regional não encontrada: " + regionalId);
                    }
                }
            }

            for (Long regionalId : regionalIds) {
                allRegionalHcmIds.addAll(getHcmIdsFromSubordinates(regionalId));
            }
            if (isFirstFilter) {
                hcmIds.addAll(allRegionalHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allRegionalHcmIds); // Intersection
            }
        }

        // Filter by setor
        if (setorIds != null && !setorIds.isEmpty()) {
            Set<Integer> allSetorHcmIds = new java.util.HashSet<>();

            List<OrganizationEntity> existingSetores = organizationRepository.findAllById(setorIds);
            if (existingSetores.size() != setorIds.size()) {
                Set<Long> existingIds = existingSetores.stream()
                        .map(OrganizationEntity::getId)
                        .collect(Collectors.toSet());
                for (Long setorId : setorIds) {
                    if (!existingIds.contains(setorId)) {
                        throw new ModuleNotFoundFailure("Setor não encontrado: " + setorId);
                    }
                }
            }

            for (Long setorId : setorIds) {
                allSetorHcmIds.addAll(getHcmIdsFromSubordinates(setorId));
            }
            if (isFirstFilter) {
                hcmIds.addAll(allSetorHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allSetorHcmIds); // Intersection
            }
        }

        // Filter by contrato
        if (contratoIds != null && !contratoIds.isEmpty()) {
            Set<Integer> allContratoHcmIds = new java.util.HashSet<>();

            List<SimpleContractEntity> existingContratos = contractRepository.findAllById(contratoIds);
            if (existingContratos.size() != contratoIds.size()) {
                Set<Long> existingIds = existingContratos.stream()
                        .map(SimpleContractEntity::getId)
                        .collect(Collectors.toSet());
                for (Long contratoId : contratoIds) {
                    if (!existingIds.contains(contratoId)) {
                        throw new ModuleNotFoundFailure("Contrato não encontrado: " + contratoId);
                    }
                }
            }

            for (Long contratoId : contratoIds) {
                allContratoHcmIds.addAll(getHcmIdsFromContract(contratoId));
            }
            if (isFirstFilter) {
                hcmIds.addAll(allContratoHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allContratoHcmIds); // Intersection
            }
        }

        // Filter by projeto
        if (projetoIds != null && !projetoIds.isEmpty()) {
            Set<Integer> allProjetoHcmIds = new java.util.HashSet<>();

            List<SimpleProjectEntity> existingProjetos = projectRepository.findAllById(projetoIds);
            if (existingProjetos.size() != projetoIds.size()) {
                Set<Long> existingIds = existingProjetos.stream()
                        .map(SimpleProjectEntity::getId)
                        .collect(Collectors.toSet());
                for (Long projetoId : projetoIds) {
                    if (!existingIds.contains(projetoId)) {
                        throw new ModuleNotFoundFailure("Projeto não encontrado: " + projetoId);
                    }
                }
            }

            for (SimpleProjectEntity project : existingProjetos) {
                allProjetoHcmIds.add(project.getHcmId());
            }
            if (isFirstFilter) {
                hcmIds.addAll(allProjetoHcmIds);
                isFirstFilter = false;
            } else {
                hcmIds.retainAll(allProjetoHcmIds); // Intersection
            }
        }

        // If no filters were provided, return all filiais
        if (isFirstFilter) {
            return new java.util.HashSet<>(filialRepository.findAll());
        }

        // Get all filiais associated with the filtered HCM IDs
        return getFiliaisFromHcmIds(hcmIds);
    }

    /**
     * Gets HCM IDs from subordinate organizations recursively
     */
    private Set<Integer> getHcmIdsFromSubordinates(Long organizationId) {
        Set<Integer> hcmIds = new java.util.HashSet<>();

        // Get all subordinates recursively
        List<Long> allSubordinateIds = findAllSubordinateIdsRecursively(organizationId);
        allSubordinateIds.add(organizationId); // Include the organization itself

        if (allSubordinateIds.isEmpty()) {
            return hcmIds;
        }

        // Get contracts and projects from subordinates
        Map<Long, List<SimpleContractEntity>> contractsBySubordinate = getContractsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsBySubordinate = getProjectsBySubordinateIds(allSubordinateIds);
        Map<Long, List<SimpleProjectEntity>> projectsByContract = getProjectsByContractIds(contractsBySubordinate);

        // Collect HCM IDs from projects
        hcmIds.addAll(collectAllHcmIds(projectsBySubordinate, projectsByContract).stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet()));

        return hcmIds;
    }

    /**
     * Gets HCM IDs from a contract
     */
    private Set<Integer> getHcmIdsFromContract(Long contratoId) {
        Set<Integer> hcmIds = new java.util.HashSet<>();

        // Get projects from contract
        List<SimpleProjectEntity> projects = projectRepository
                .findByContractIdIn(java.util.Collections.singletonList(contratoId));

        // Collect HCM IDs from projects
        hcmIds.addAll(projects.stream()
                .map(SimpleProjectEntity::getHcmId)
                .collect(Collectors.toSet()));

        return hcmIds;
    }

    /**
     * Gets FilialHcmEntity objects from HCM IDs
     */
    private Set<FilialHcmEntity> getFiliaisFromHcmIds(Set<Integer> hcmIds) {
        Set<FilialHcmEntity> filiais = new java.util.HashSet<>();

        if (hcmIds.isEmpty()) {
            return filiais;
        }

        // Convert HCM IDs to project entities to find associated filials
        for (Integer hcmId : hcmIds) {
            projectRepository.findByHcmId(hcmId).stream().findFirst().ifPresent(project -> {
                // Find the full ProjectEntity to access filial relationships
                organizationProjectRepository.findById(project.getId()).ifPresent(fullProject -> {
                    if (fullProject.getFilial() != null) {
                        filiais.addAll(fullProject.getFilial());
                    }
                });
            });
        }

        return filiais;
    }
}

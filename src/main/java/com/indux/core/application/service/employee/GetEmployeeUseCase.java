package com.indux.core.application.service.employee;

import com.indux.core.application.dto.generic.*;
import com.indux.core.domain.model.employee.*;
import com.indux.core.domain.repository.employee.AddressRepository;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import com.indux.core.domain.repository.generic.*;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.organization_chart.application.dtos.EmployeeFilterDTO;
import com.indux.modules.organization_chart.application.services.SubordinateService;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GetEmployeeUseCase {
    private static final Logger logger = LoggerFactory.getLogger(GetEmployeeUseCase.class);
    private final EmployeeRepository repository;
    private final DependentRepository dependentRepository;
    private final RegionalRepository regionalRepository;
    private final BranchRepository branchRepository;
    private final ContractProjectRepository contractProjectRepository;
    private final CargoRepository cargoRepository;
    private final EmployeePositionRepository positionRepository;
    private SubordinateService subordinateService;
    private final AddressService addressService;


    public GetEmployeeUseCase(EmployeeRepository repository, DependentRepository dependentRepository, RegionalRepository regionalRepository, BranchRepository branchRepository, ContractProjectRepository contractProjectRepository, CargoRepository cargoRepository, EmployeePositionRepository positionRepository, AddressService addressService) {
        this.repository = repository;
        this.dependentRepository = dependentRepository;
        this.regionalRepository = regionalRepository;
        this.branchRepository = branchRepository;
        this.contractProjectRepository = contractProjectRepository;
        this.cargoRepository = cargoRepository;
        this.positionRepository = positionRepository;
        this.addressService = addressService;
    }

    @Autowired(required = false)
    public void setSubordinateService(SubordinateService subordinateService) {
        this.subordinateService = subordinateService;
    }

    /**
     * Busca funcionários por filtro (matrícula ou nome) e flags de exibição
     * 
     * @param filter       Nome do colaborador.
     * @param showBank     Se deseja mostrar os dados bancários do colaborador
     * @param showDemitido Se deseja mostrar informações dos funcionários que já
     *                     foram demitidos.
     * @return Lista dos dados dos colaboradores.
     */
    public List<EmployeeDTO> search(String filter,
            boolean showBank,
            boolean showDemitido) {
        List<EmployeeDTO> employees = repository.search(filter, showBank, showDemitido);

        // Adicionar hierarquia organizacional para cada funcionário
        if (subordinateService != null) {
            for (EmployeeDTO employee : employees) {
                // Buscar o costCenterId do funcionário pelo ID
                repository.findById(employee.getId()).ifPresent(emp -> {
                    EmployeeSummaryDTO.HierarchyInfo hierarchy = subordinateService
                            .getHierarchyByFilialHcmId(emp.getFilialIdHcm());
                    employee.setHierarchy(hierarchy);
                });
            }
        }

        return employees;
    }

    /**
     * Retorna o primeiro resultado da busca por matrícula,
     * sempre exibindo dados bancários e excluindo demitidos.
     * 
     * @param registration Numero da matrícula do colaborador.
     * @return Record com as informações do colaborador.
     */
    //TODO: Investigar pq a hierarquia vem errado.
    public EmployeeDTO getEmployeeByMatricula(String registration) {
        EmployeeDTO employee = repository
                .search(registration, true, true)
                .stream()
                .findFirst()
                .orElse(null);

        if (employee != null && subordinateService != null) {
            // Buscar o costCenterId do funcionário pelo ID
            repository.findById(employee.getId()).ifPresent(emp -> {
                EmployeeSummaryDTO.HierarchyInfo hierarchy = subordinateService
                        .getHierarchyByFilialHcmId(emp.getFilialIdHcm());
                employee.setHierarchy(hierarchy);
            });
        }

        return employee;
    }

    /**
     * Retorna todos os dados do colaborador daqueel id.
     * 
     * @param id do colaborador.
     * @return Record com as informações do colaborador.
     */
    public CompleteEmployeeDTO getEmployeeById(UUID id) {
        return repository.findById(id)
                .map(e -> {
                    // Buscar todos os planos ativos (funcionário e dependentes)
                    List<Dependent> planosRaw = dependentRepository.findActivePlansAndDependents(e.getRegistration());

                    // Agrupar por CPF, tipo de plano e código da seguradora
                    List<EmployeePlanDTO> planos = planosRaw.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    d -> d.getCpf() + "_" + d.getTypePlan() + "_" + d.getInsuranceCode(),
                                    java.util.stream.Collectors.collectingAndThen(
                                            java.util.stream.Collectors.toList(),
                                            list -> EmployeePlanDTO.fromEntity(list.get(0)))))
                            .values()
                            .stream()
                            .toList();

                    String gestorInternoContrato = null;
                    String cliente = null;
                    List<String> coordenadoresContrato = new ArrayList<>();
                    if (e.getContract_id() != null) {
                        Optional<ContractProject> contrato = contractProjectRepository
                                .findWithFilialByRateio(e.getContract_id());
                        gestorInternoContrato = contrato.map(ContractProject::getContractManager).orElse(null);
                        cliente = contrato.map(ContractProject::getClient).orElse(null);
                        coordenadoresContrato = contrato.map(ContractProject::getContractCoordinators)
                                .orElse(new ArrayList<>());
                    }

                    String regional = null;
                    String matriculaResponsavelFilial = null;
                    String nomeResponsavelFilial = null;
                    String razaoSocial = null;
                    String endereco = null;
                    String nomeFilial = null;
                    String cnpjFilial = null;
                    String inscricaoMunicipalFilial = null;
                    String municipioFilial = null;

                    if (e.getBranch_id() != null) {
                        Optional<Regional> reg = regionalRepository.findByFilial(e.getBranch_id().intValue());
                        regional = reg.map(Regional::getRegional).orElse(null);
                        Optional<Filial> filial = branchRepository.findById(e.getBranch_id());
                        matriculaResponsavelFilial = filial.map(Filial::getMatricula_responsavel_filial).orElse(null);
                        nomeResponsavelFilial = filial.map(Filial::getNome_responsavel_filial).orElse(null);
                        razaoSocial = filial.map(Filial::getCorporateName).orElse(null);
                        nomeFilial = filial.map(Filial::getBranchName).orElse(null);
                        cnpjFilial = filial.map(Filial::getCnpj).orElse(null);
                        inscricaoMunicipalFilial = filial.map(Filial::getMunicipalRegistration).orElse(null);
                        municipioFilial = filial.map(Filial::getBranchCity).orElse(null);
                    }

                    Address address = addressService.getAddressByRegistrationNo(e.getRegistration());
                    if (address != null) {
                       endereco = address.getType() + " " + address.getStreet() + " - " + address.getNumber() + " " + address.getComplement() + ", " + address.getNeighborhood() + " - " + address.getCep();
                    }

                    // Contar dependentes únicos por CPF
                    int quantidadeDependentes = (int) planos.stream()
                            .filter(p -> "Dependente".equals(p.getTipoFuncionario()))
                            .map(EmployeePlanDTO::getCpf)
                            .distinct()
                            .count();

                    int quantidadeCpfsDependentes = (int) planos.stream()
                            .filter(p -> "Dependente".equals(p.getTipoFuncionario()) && p.getCpf() != null
                                    && !p.getCpf().isBlank())
                            .map(EmployeePlanDTO::getCpf)
                            .distinct()
                            .count();

                    String nomeCargo = null;
                    if (e.getPosition() != null) {
                        nomeCargo = cargoRepository.findByIdHcm(e.getPosition())
                                .map(Cargo::getNameTitle)
                                .orElse(null);
                    }
                    String nomeProjeto = null;
                    if (e.getContract_id() != null) {
                        nomeProjeto = contractProjectRepository.findWithFilialByRateio(e.getContract_id())
                                .map(ContractProject::getCostCenterName)
                                .orElse(null);
                    }

                    // Buscar histórico de trabalhos
                    List<CompleteEmployeeDTO.HistoricoTrabalhoDTO> historicoTrabalhos = repository
                            .findAllByCpf(e.getCpf())
                            .stream()
                            .map(trabalho -> {
                                String cargoHistoricoNome = null;
                                if (trabalho.getPosition() != null) {
                                    cargoHistoricoNome = cargoRepository.findByIdHcm(trabalho.getPosition())
                                            .map(Cargo::getNameTitle)
                                            .orElse(null);
                                }
                                return CompleteEmployeeDTO.HistoricoTrabalhoDTO.builder()
                                        .cpf(trabalho.getCpf())
                                        .matricula(trabalho.getRegistration())
                                        .cargo(trabalho.getPosition())
                                        .cargo_nome(cargoHistoricoNome)
                                        .data_admissao(trabalho.getAdmissionDate())
                                        .data_demissao(trabalho.getTerminationDate())
                                        .telefone(trabalho.getCellphone())
                                        .email_particular(trabalho.getPersonalEmail())
                                        .build();
                            })
                            .toList();

                    // Calcular tempo de empresa com base no último período de trabalho
                    List<Employee> vinculos = repository.findAllByCpf(e.getCpf());
                    Employee ultimoVinculo = vinculos.stream().reduce(null, (melhor, emp) -> {
                        if (melhor == null)
                            return emp;
                        boolean empWorking = "Trabalhando".equalsIgnoreCase(emp.getStatusEmployee());
                        boolean melhorWorking = "Trabalhando".equalsIgnoreCase(melhor.getStatusEmployee());
                        if (empWorking && !melhorWorking)
                            return emp;
                        if (!empWorking && melhorWorking)
                            return melhor;
                        if (melhor.getAdmissionDate() == null)
                            return emp;
                        if (emp.getAdmissionDate() == null)
                            return melhor;
                        return emp.getAdmissionDate().isAfter(melhor.getAdmissionDate()) ? emp : melhor;
                    });
                    long tempoTotalDias = 0L;
                    if (ultimoVinculo != null && ultimoVinculo.getAdmissionDate() != null) {
                        LocalDate admissao = ultimoVinculo.getAdmissionDate();
                        LocalDate demissao = (ultimoVinculo.getStatusEmployee() != null
                                && ultimoVinculo.getStatusEmployee().equalsIgnoreCase("Trabalhando"))
                                || ultimoVinculo.getTerminationDate() == null
                                || ultimoVinculo.getTerminationDate().equals(LocalDate.of(1900, 12, 31))
                                        ? LocalDate.now()
                                        : ultimoVinculo.getTerminationDate();
                        tempoTotalDias = ChronoUnit.DAYS.between(admissao, demissao);
                    }

                    // Formatar o tempo total
                    long anos = tempoTotalDias / 365;
                    long meses = (tempoTotalDias % 365) / 30;
                    String tempoFormatado = String.format("%d anos e %d meses", anos, meses);

                    // Buscar hierarquia organizacional
                    EmployeeSummaryDTO.HierarchyInfo hierarchy = null;
                    if (subordinateService != null && e.getCostCenterId() != null) {
                        hierarchy = subordinateService.getHierarchyByFilialHcmId(e.getFilialIdHcm());
                    }

                    return CompleteEmployeeDTO.fromEntity(e, gestorInternoContrato, regional,
                            quantidadeDependentes, quantidadeCpfsDependentes, matriculaResponsavelFilial,
                            nomeResponsavelFilial, razaoSocial, endereco, nomeCargo, nomeProjeto, historicoTrabalhos,
                            nomeFilial, cnpjFilial, inscricaoMunicipalFilial, municipioFilial,
                            tempoTotalDias, tempoFormatado, cliente, coordenadoresContrato, planos, hierarchy);
                })
                .orElse(null);
    }

    /**
     * Retorna todos os colaboradores.
     * 
     * @param pageable da pagina e quantidade de elementos apresentados.
     * @return Record com as informações de todos os colaboradores.
     */
    public Page<EmployeeSummaryDTO> getAllEmployeesSummary(Pageable pageable, boolean showFired, String nome) {
        Specification<Employee> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!showFired) {
                predicates.add(cb.equal(cb.lower(root.get("statusEmployee")), "trabalhando"));
            }
            if (nome != null && !nome.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + nome.toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Employee> page = repository.findAll(spec, pageable);
        List<Employee> employees = page.getContent();
        // Manter apenas o vínculo com data de admissão mais recente por CPF
        Map<String, Employee> latestByCpf = new HashMap<>();
        for (Employee emp : employees) {
            if (emp.getCpf() == null)
                continue;
            latestByCpf.merge(emp.getCpf(), emp, (oldEmp, newEmp) -> {
                boolean newWorking = "Trabalhando".equalsIgnoreCase(newEmp.getStatusEmployee());
                boolean oldWorking = "Trabalhando".equalsIgnoreCase(oldEmp.getStatusEmployee());

                if (newWorking && !oldWorking)
                    return newEmp;
                if (!newWorking && oldWorking)
                    return oldEmp;

                // Ambos com mesmo tipo de status – escolher o de admissão mais recente
                if (oldEmp.getAdmissionDate() == null)
                    return newEmp;
                if (newEmp.getAdmissionDate() == null)
                    return oldEmp;
                return newEmp.getAdmissionDate().isAfter(oldEmp.getAdmissionDate()) ? newEmp : oldEmp;
            });
        }

        // Substituir lista employees pelo conjunto filtrado
        employees = new java.util.ArrayList<>(latestByCpf.values());

        // Agrupar por CPF para cálculos de tempo total
        Map<String, List<Employee>> employeesByCpf = new HashMap<>();
        for (Employee e : employees) {
            employeesByCpf.computeIfAbsent(e.getCpf(), k -> new ArrayList<>()).add(e);
        }

        // Calcular o tempo de empresa usando apenas o último período de trabalho de
        // cada CPF
        Map<String, Long> totalDiasPorCpf = new HashMap<>();
        for (String cpf : employeesByCpf.keySet()) {
            List<Employee> vinculos = repository.findAllByCpf(cpf);
            Employee ultimoVinculo = vinculos.stream().reduce(null, (melhor, emp) -> {
                if (melhor == null)
                    return emp;
                boolean empWorking = "Trabalhando".equalsIgnoreCase(emp.getStatusEmployee());
                boolean melhorWorking = "Trabalhando".equalsIgnoreCase(melhor.getStatusEmployee());
                if (empWorking && !melhorWorking)
                    return emp;
                if (!empWorking && melhorWorking)
                    return melhor;
                if (melhor.getAdmissionDate() == null)
                    return emp;
                if (emp.getAdmissionDate() == null)
                    return melhor;
                return emp.getAdmissionDate().isAfter(melhor.getAdmissionDate()) ? emp : melhor;
            });
            long totalDias = 0L;
            if (ultimoVinculo != null && ultimoVinculo.getAdmissionDate() != null) {
                LocalDate admissao = ultimoVinculo.getAdmissionDate();
                LocalDate demissao = (ultimoVinculo.getStatusEmployee() != null
                        && ultimoVinculo.getStatusEmployee().equalsIgnoreCase("Trabalhando"))
                        || ultimoVinculo.getTerminationDate() == null
                        || ultimoVinculo.getTerminationDate().equals(LocalDate.of(1900, 12, 31))
                                ? LocalDate.now()
                                : ultimoVinculo.getTerminationDate();
                totalDias = ChronoUnit.DAYS.between(admissao, demissao);
            }
            totalDiasPorCpf.put(cpf, totalDias);
        }

        List<EmployeeSummaryDTO> dtos = employees.stream().map(e -> {
            String regional = null;
            if (e.getBranch_id() != null) {
                regional = regionalRepository.findByFilial(e.getBranch_id().intValue())
                        .map(Regional::getRegional)
                        .orElse(null);
            }

            // Buscar todos os registros (dependentes e do próprio funcionário)
            List<Dependent> todosRegistros = dependentRepository
                    .findByEmployeeRegistrationAndPlanNotContainingAndExclusionMonth(
                            e.getRegistration(), "#Inativo", "1900-12-31 00:00:00");

            // Agrupar registros por plano
            Map<String, List<Dependent>> registrosPorPlano = todosRegistros.stream()
                    .filter(d -> d != null && ("Dependente".equals(d.getTypeEmployee()) ||
                            "Funcionario".equals(d.getTypeEmployee())))
                    .collect(Collectors
                            .groupingBy(d -> d.getTypePlan() + "_" + d.getIdPlan() + "_" + d.getInsuranceCode()));

            // Criar PlanoInfo para cada plano
            List<EmployeeSummaryDTO.PlanoInfo> planosInfo = registrosPorPlano.entrySet().stream()
                    .map(entry -> {
                        Dependent primeiro = entry.getValue().get(0);
                        long qtdDependentes = entry.getValue().stream()
                                .filter(d -> "Dependente".equals(d.getTypeEmployee()))
                                .count();

                        return EmployeeSummaryDTO.PlanoInfo.builder()
                                .tipoPlano(primeiro.getTypePlan())
                                .nomePlano(primeiro.getPlan())
                                .codigoPlano(primeiro.getIdPlan())
                                .nomeSeguradora(primeiro.getNameInsurance())
                                .codigoSeguradora(primeiro.getInsuranceCode())
                                .quantidadeDependentes((int) qtdDependentes)
                                .temPlanoTitular(entry.getValue().stream()
                                        .anyMatch(d -> "Funcionario".equals(d.getTypeEmployee())))
                                .build();
                    })
                    .toList();

            Long totalDiasEmpresa = totalDiasPorCpf.getOrDefault(e.getCpf(), 0L);
            String costCenterNameVar = null;
            if (e.getContract_id() != null) {
                costCenterNameVar = contractProjectRepository.findWithFilialByRateio(e.getContract_id())
                        .map(ContractProject::getCostCenterName)
                        .orElse(null);
            }
            String nomeCargo = null;
            if (e.getPosition() != null) {
                nomeCargo = cargoRepository.findByIdHcm(e.getPosition())
                        .map(Cargo::getNameTitle)
                        .orElse(null);
            }
            return EmployeeSummaryDTO.fromEntity(e, regional, planosInfo, totalDiasEmpresa, costCenterNameVar,
                    nomeCargo);
        }).toList();

        // Ordenar por anos DESC, meses DESC (igual ao exibido no front) e nome ASC
        dtos.sort((dto1, dto2) -> {
            Long dias1 = dto1.getTotalDiasEmpresa();
            Long dias2 = dto2.getTotalDiasEmpresa();
            long anos1 = dias1 != null ? dias1 / 365 : -1L;
            long meses1 = dias1 != null ? (dias1 % 365) / 30 : -1L;
            long anos2 = dias2 != null ? dias2 / 365 : -1L;
            long meses2 = dias2 != null ? (dias2 % 365) / 30 : -1L;
            int cmpAnos = Long.compare(anos2, anos1); // decrescente
            if (cmpAnos != 0)
                return cmpAnos;
            int cmpMeses = Long.compare(meses2, meses1); // decrescente
            if (cmpMeses != 0)
                return cmpMeses;
            if (dto1.getNome() == null && dto2.getNome() == null)
                return 0;
            if (dto1.getNome() == null)
                return 1;
            if (dto2.getNome() == null)
                return -1;
            return dto1.getNome().compareTo(dto2.getNome());
        });

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public Page<EmployeeSummaryDTO> listEmployeesOnlySuprimentos(Pageable pageable) {
         final Long SUPRIMENTOS_DIRETORIA = 1L;
        var employees = subordinateService.getAllEmployeesByDirectorId(SUPRIMENTOS_DIRETORIA, pageable);
        return employees;
    }

    public Page<EmployeeSummaryDTO> filterEmployees(
            List<String> matricula,
            List<String> nome,
            List<String> cidade,
            List<String> estado,
            List<String> sexo,
            List<String> grauInstrucao,
            List<String> dataAdmissao,
            List<String> dataAdmissaoInicio,
            List<String> dataAdmissaoFim,
            List<String> dataNascimento,
            List<String> dataNascimentoInicio,
            List<String> dataNascimentoFim,
            List<String> contrato,
            List<String> filialId,
            List<String> filialIdHcm,
            List<String> centroCustosId,
            List<String> cargo,
            boolean showFired,
            List<String> situacao,
            List<Integer> dependentes,
            List<Long> diretoriaId,
            List<Long> superintendenciaId,
            List<Long> regionalId,
            List<Long> setorId,
            List<Long> contratoId,
            List<Long> projetoId,
            Long tempoTrabalhoMinDias,
            Long tempoTrabalhoMaxDias,
            Pageable pageable) {
        EmployeeFilter filter = new EmployeeFilter(
                matricula, nome, cidade, estado, sexo, grauInstrucao,
                dataAdmissao, dataAdmissaoInicio, dataAdmissaoFim, dataNascimento,
                dataNascimentoInicio, dataNascimentoFim,
                contrato, filialId, filialIdHcm, centroCustosId, cargo, Boolean.valueOf(showFired), situacao,
                dependentes,
                diretoriaId, superintendenciaId, regionalId, setorId, contratoId, projetoId,
                tempoTrabalhoMinDias, tempoTrabalhoMaxDias);
        return filterEmployeesWithFilter(filter, pageable);
    }

    public Page<EmployeeSummaryDTO> filterEmployeesWithFilter(
            EmployeeFilter filter,
            Pageable pageable) {
        // Se houver filtros organizacionais e o SubordinateService está disponível,
        // usar sua lógica
        // Mas somente se tivermos exatamente um ID por tipo OU nenhum filtro
        // organizacional
        if (subordinateService != null &&
                hasOrganizationalFilters(filter)) {

            // Converter lista de situação em uma única string (pegar o primeiro valor)
            String situacaoFiltro = null;
            if (filter.situacao() != null && !filter.situacao().isEmpty()) {
                situacaoFiltro = filter.situacao().get(0);
            }

            EmployeeFilterDTO orgFilter = new EmployeeFilterDTO();

            // Filtros organizacionais
            orgFilter.setDiretoriaId(filter.diretoriaId());
            orgFilter.setSuperintendenciaId(filter.superintendenciaId());
            orgFilter.setRegionalId(filter.regionalId());
            orgFilter.setSetorId(filter.setorId());
            orgFilter.setContratoId(filter.contratoId());
            orgFilter.setProjetoId(filter.projetoId());
            orgFilter.setCentroCustoHcm(null);

            // Filtros de funcionário
            orgFilter.setSituacao(situacaoFiltro);
            orgFilter.setNome(filter.nome());
            orgFilter.setMatricula(filter.matricula());
            orgFilter.setCidade(filter.cidade());
            orgFilter.setEstado(filter.estado());
            orgFilter.setSexo(filter.sexo());
            orgFilter.setGrauInstrucao(filter.grauInstrucao());
            orgFilter.setDataAdmissao(filter.dataAdmissao());
            orgFilter.setDataAdmissaoInicio(filter.dataAdmissaoInicio());
            orgFilter.setDataAdmissaoFim(filter.dataAdmissaoFim());
            orgFilter.setDataNascimento(filter.dataNascimento());
            orgFilter.setContrato(filter.contrato());
            orgFilter.setFilialId(filter.filialId());
            orgFilter.setFilialIdHcm(filter.filialIdHcm());
            orgFilter.setCentroCustosId(filter.centroCustosId());
            orgFilter.setCargo(filter.cargo());
            orgFilter.setShowFired(filter.showFired());
            orgFilter.setDependentes(filter.dependentes());

            return subordinateService.getEmployeesByFilters(orgFilter, pageable);
        }

        // Se temos múltiplos IDs organizacionais ou o SubordinateService não está
        // disponível,
        // fazemos a filtragem diretamente aqui com condições OR
        return filterEmployeesDirectly(filter, pageable);
    }

    public SimpleEmployeeDTO mapperDTO(EmployeeDTO dto) {

        return new SimpleEmployeeDTO(
                dto.getMatricula(),
                dto.getName(),
                dto.getCargo(),
                dto.getCargo(),
                dto.getSispat(),
                dto.getContrato().toDTO());
    }

    public EmployeeDTO getActiveEmployeeByCPF(String cpf) {
        return repository.findByCpfAndStatus(cpf).stream().findFirst()
                .orElseThrow(() -> new NotFoundEmployee("Colaborador não encontrado ou sem status de trabalhando."));
    }

    public List<SimpleEmployeeDTO> findByRegistrations(List<String> registrations) {

        Set<Employee> employees = repository.findAllByRegistrationIn(registrations);

        Set<String> positionIds = employees.stream()
                .map(Employee::getPosition)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> positionIdToName = positionRepository.findAllByIdHCMIn(positionIds).stream()
                .collect(Collectors.toMap(
                        EmployeePosition::getIdHCM,
                        EmployeePosition::getName));

        Set<Integer> contractIds = employees.stream()
                .map(Employee::getContract_id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, ContractProject> contractsByRateio = contractProjectRepository.findAllByRateioIn(contractIds)
                .stream()
                .collect(Collectors.toMap(ContractProject::getRateio, Function.identity()));

        return employees.stream()
                .map(e -> {
                    String positionId = e.getPosition();
                    String positionName = positionIdToName.get(positionId);
                    ContractProject contract = contractsByRateio.get(e.getContract_id());
                    return new SimpleEmployeeDTO(
                            e.getRegistration(),
                            e.getName(),
                            e.getPosition(),
                            positionName,
                            e.getSISPAT(),
                            contract != null ? contract.toDTO() : null);
                })
                .collect(Collectors.toList());
    }

    public Employee getCompleteActiveEmployeeByCPF(String cpf) {
        return repository.findByCpfAndStatusEmployee(cpf, "Trabalhando");
    }

    public CompleteEmployeeDTO getMobileEmployeeByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new NotFoundEmployee("Colaborador não encontrado com matrícula ativa.");
        }

        Employee employee = repository.findAllByCpf(cpf).stream()
                .filter(this::isEligibleMobileEmployee)
                .max(employeeSelectionComparator())
                .orElseThrow(() -> new NotFoundEmployee("Colaborador não encontrado com matrícula ativa."));

        return Optional.ofNullable(getEmployeeById(employee.getId()))
                .orElseThrow(() -> new NotFoundEmployee("Colaborador não encontrado com matrícula ativa."));
    }

    private boolean isEligibleMobileEmployee(Employee employee) {
        String normalizedStatus = normalizeStatus(employee.getStatusEmployee());
        return !normalizedStatus.contains("DEMIT");
    }

    private Comparator<Employee> employeeSelectionComparator() {
        return Comparator
                .comparing(this::isActiveMobileEmployee)
                .thenComparing(employee -> Optional.ofNullable(employee.getAdmissionDate()).orElse(LocalDate.MIN));
    }

    private boolean isActiveMobileEmployee(Employee employee) {
        return normalizeStatus(employee.getStatusEmployee()).contains("TRABALH");
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }

        return Normalizer.normalize(status, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase();
    }

    private boolean hasOrganizationalFilters(EmployeeFilter filter) {
        return (filter.diretoriaId() != null && !filter.diretoriaId().isEmpty()) ||
                (filter.superintendenciaId() != null && !filter.superintendenciaId().isEmpty()) ||
                (filter.regionalId() != null && !filter.regionalId().isEmpty()) ||
                (filter.setorId() != null && !filter.setorId().isEmpty()) ||
                (filter.contratoId() != null && !filter.contratoId().isEmpty()) ||
                (filter.projetoId() != null && !filter.projetoId().isEmpty());
    }

    private Page<EmployeeSummaryDTO> filterEmployeesDirectly(EmployeeFilter filter, Pageable pageable) {
        Specification<Employee> spec = (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Aplicar filtro de situação apenas se especificado
            if (filter.situacao() != null && !filter.situacao().isEmpty()) {
                predicates.add(root.get("statusEmployee").in(filter.situacao()));
            }

            // Filtros básicos com OR condition
            addOrPredicate(predicates, filter.matricula(), root, cb, "registration");
            addOrPredicateLike(predicates, filter.nome(), root, cb, "name");
            addOrPredicateLike(predicates, filter.cidade(), root, cb, "city");
            addOrPredicateLike(predicates, filter.cargo(), root, cb, "position");

            if (filter.estado() != null && !filter.estado().isEmpty()) {
                predicates.add(root.get("state").in(filter.estado()));
            }

            addOrPredicate(predicates, filter.sexo(), root, cb, "gender");
            addOrPredicateLike(predicates, filter.grauInstrucao(), root, cb, "educationLevel");

            // Filtros de data
            addDateRangePredicate(predicates, filter.dataAdmissaoInicio(), filter.dataAdmissaoFim(), root, cb,
                    "admissionDate");
            addDatePredicates(predicates, filter.dataAdmissao(), root, cb, "admissionDate");
            addDatePredicates(predicates, filter.dataNascimento(), root, cb, "birthDate");
            // Range de data de nascimento: inicio, fim ou ambos
            addDateRangePredicate(predicates, filter.dataNascimentoInicio(), filter.dataNascimentoFim(), root, cb,
                    "birthDate");

            // Filtros numéricos
            addNumericInPredicate(predicates, filter.filialId(), root, cb, "branch_id", Long.class);
            addNumericInPredicate(predicates, filter.filialIdHcm(), root, cb, "filialIdHcm", Integer.class);

            if (filter.centroCustosId() != null && !filter.centroCustosId().isEmpty()) {
                List<String> centroCustosIds = filter.centroCustosId().stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank() && !s.equals("0"))
                        .toList();
                if (!centroCustosIds.isEmpty()) {
                    predicates.add(root.get("costCenterId").in(centroCustosIds));
                }
            }

            // Filtro de quantidade de dependentes
            if (filter.dependentes() != null && !filter.dependentes().isEmpty()) {
                predicates.add(root.get("numberOfDependents").in(filter.dependentes()));
            }

            // Filtro de tempo de trabalho baseado na data de admissão
            // minDias: admissionDate <= hoje - minDias (entrou há pelo menos X dias)
            // maxDias: admissionDate >= hoje - maxDias (entrou há no máximo X dias)
            if (filter.tempoTrabalhoMinDias() != null) {
                LocalDate limiteMin = LocalDate.now().minusDays(filter.tempoTrabalhoMinDias());
                predicates.add(cb.lessThanOrEqualTo(root.get("admissionDate"), limiteMin));
            }
            if (filter.tempoTrabalhoMaxDias() != null) {
                LocalDate limiteMax = LocalDate.now().minusDays(filter.tempoTrabalhoMaxDias());
                predicates.add(cb.greaterThanOrEqualTo(root.get("admissionDate"), limiteMax));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Employee> employees;
        long totalElements;

        boolean filtrarPorContratoNome = filter.contrato() != null && !filter.contrato().isEmpty();

        boolean precisaFiltrarNome = filtrarPorContratoNome;

        List<String> filtrosContratoLower = filtrarPorContratoNome ? filter.contrato().stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList() : List.of();

        if (precisaFiltrarNome) {
            // Buscar todos que satisfazem os demais filtros
            employees = repository.findAll(spec);

            // Filtrar por nome do cargo (contains, case-insensitive)
            employees = employees.stream()
                    .filter(e -> {

                        boolean contratoOk = true;
                        if (filtrarPorContratoNome) {
                            contratoOk = e.getContract_id() != null
                                    && contractProjectRepository.findByRateio(e.getContract_id())
                                            .map(cp -> {
                                                String costName = cp.getCostCenterName() != null
                                                        ? cp.getCostCenterName().toLowerCase()
                                                        : "";
                                                String projName = cp.getProjectName() != null
                                                        ? cp.getProjectName().toLowerCase()
                                                        : "";
                                                return filtrosContratoLower.stream()
                                                        .anyMatch(f -> costName.contains(f) || projName.contains(f));
                                            })
                                            .orElse(false);
                        }

                        return contratoOk;
                    })
                    .toList();

            // Manter apenas o vínculo mais recente por CPF
            java.util.Map<String, Employee> latestByCpfInMem = new java.util.HashMap<>();
            for (Employee emp : employees) {
                if (emp.getCpf() == null)
                    continue;
                latestByCpfInMem.merge(emp.getCpf(), emp, (oldEmp, newEmp) -> {
                    boolean newWorking = "Trabalhando".equalsIgnoreCase(newEmp.getStatusEmployee());
                    boolean oldWorking = "Trabalhando".equalsIgnoreCase(oldEmp.getStatusEmployee());

                    if (newWorking && !oldWorking)
                        return newEmp;
                    if (!newWorking && oldWorking)
                        return oldEmp;

                    // Ambos com mesmo tipo de status – escolher o de admissão mais recente
                    if (oldEmp.getAdmissionDate() == null)
                        return newEmp;
                    if (newEmp.getAdmissionDate() == null)
                        return oldEmp;
                    return newEmp.getAdmissionDate().isAfter(oldEmp.getAdmissionDate()) ? newEmp : oldEmp;
                });
            }

            employees = new java.util.ArrayList<>(latestByCpfInMem.values());
            totalElements = employees.size();

            // Ordenar por tempo de empresa (decrescente) e depois pelo nome (crescente)
            employees.sort((e1, e2) -> {
                LocalDate adm1 = e1.getAdmissionDate();
                LocalDate adm2 = e2.getAdmissionDate();
                LocalDate ref = LocalDate.now();
                long dias1 = adm1 != null ? ChronoUnit.DAYS.between(adm1, ref) : -1L;
                long dias2 = adm2 != null ? ChronoUnit.DAYS.between(adm2, ref) : -1L;
                long anos1 = dias1 >= 0 ? dias1 / 365 : -1L;
                long meses1 = dias1 >= 0 ? (dias1 % 365) / 30 : -1L;
                long anos2 = dias2 >= 0 ? dias2 / 365 : -1L;
                long meses2 = dias2 >= 0 ? (dias2 % 365) / 30 : -1L;
                int cmpAnos = Long.compare(anos2, anos1); // decrescente
                if (cmpAnos != 0)
                    return cmpAnos;
                int cmpMeses = Long.compare(meses2, meses1); // decrescente
                if (cmpMeses != 0)
                    return cmpMeses;
                if (e1.getName() == null && e2.getName() == null)
                    return 0;
                if (e1.getName() == null)
                    return 1;
                if (e2.getName() == null)
                    return -1;
                return e1.getName().compareTo(e2.getName());
            });

            // Aplicar paginação manual após deduplicação e ordenação
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), employees.size());
            if (start > end) {
                employees = java.util.Collections.emptyList();
            } else {
                employees = employees.subList(start, end);
            }
        } else {
            // Buscar todos os registros que satisfazem o Specification
            employees = repository.findAll(spec);

            // Deduplicar – manter "Trabalhando" preferencialmente e admissão mais recente
            java.util.Map<String, Employee> latestByCpfGlobal = new java.util.HashMap<>();
            for (Employee emp : employees) {
                if (emp.getCpf() == null)
                    continue;
                latestByCpfGlobal.merge(emp.getCpf(), emp, (oldEmp, newEmp) -> {
                    boolean newWorking = "Trabalhando".equalsIgnoreCase(newEmp.getStatusEmployee());
                    boolean oldWorking = "Trabalhando".equalsIgnoreCase(oldEmp.getStatusEmployee());

                    if (newWorking && !oldWorking)
                        return newEmp;
                    if (!newWorking && oldWorking)
                        return oldEmp;

                    if (oldEmp.getAdmissionDate() == null)
                        return newEmp;
                    if (newEmp.getAdmissionDate() == null)
                        return oldEmp;
                    return newEmp.getAdmissionDate().isAfter(oldEmp.getAdmissionDate()) ? newEmp : oldEmp;
                });
            }

            employees = new java.util.ArrayList<>(latestByCpfGlobal.values());
            totalElements = employees.size();

            // Ordenar por tempo de empresa (decrescente) e depois pelo nome (crescente)
            employees.sort((e1, e2) -> {
                LocalDate adm1 = e1.getAdmissionDate();
                LocalDate adm2 = e2.getAdmissionDate();
                LocalDate ref = LocalDate.now();
                long dias1 = adm1 != null ? ChronoUnit.DAYS.between(adm1, ref) : -1L;
                long dias2 = adm2 != null ? ChronoUnit.DAYS.between(adm2, ref) : -1L;
                long anos1 = dias1 >= 0 ? dias1 / 365 : -1L;
                long meses1 = dias1 >= 0 ? (dias1 % 365) / 30 : -1L;
                long anos2 = dias2 >= 0 ? dias2 / 365 : -1L;
                long meses2 = dias2 >= 0 ? (dias2 % 365) / 30 : -1L;
                int cmpAnos = Long.compare(anos2, anos1); // decrescente
                if (cmpAnos != 0)
                    return cmpAnos;
                int cmpMeses = Long.compare(meses2, meses1); // decrescente
                if (cmpMeses != 0)
                    return cmpMeses;
                if (e1.getName() == null && e2.getName() == null)
                    return 0;
                if (e1.getName() == null)
                    return 1;
                if (e2.getName() == null)
                    return -1;
                return e1.getName().compareTo(e2.getName());
            });

            // Aplicar paginação manual após ordenação
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), employees.size());
            if (start > end) {
                employees = java.util.Collections.emptyList();
            } else {
                employees = employees.subList(start, end);
            }
        }

        // Agrupar por CPF
        Map<String, List<Employee>> employeesByCpf = new HashMap<>();
        for (Employee e : employees) {
            employeesByCpf.computeIfAbsent(e.getCpf(), k -> new ArrayList<>()).add(e);
        }

        // Calcular o tempo de empresa usando apenas o último período de trabalho de
        // cada CPF
        Map<String, Long> totalDiasPorCpf = new HashMap<>();
        for (String cpf : employeesByCpf.keySet()) {
            List<Employee> vinculos = repository.findAllByCpf(cpf);
            Employee ultimoVinculo = vinculos.stream().reduce(null, (melhor, emp) -> {
                if (melhor == null)
                    return emp;
                boolean empWorking = "Trabalhando".equalsIgnoreCase(emp.getStatusEmployee());
                boolean melhorWorking = "Trabalhando".equalsIgnoreCase(melhor.getStatusEmployee());
                if (empWorking && !melhorWorking)
                    return emp;
                if (!empWorking && melhorWorking)
                    return melhor;
                if (melhor.getAdmissionDate() == null)
                    return emp;
                if (emp.getAdmissionDate() == null)
                    return melhor;
                return emp.getAdmissionDate().isAfter(melhor.getAdmissionDate()) ? emp : melhor;
            });
            long totalDias = 0L;
            if (ultimoVinculo != null && ultimoVinculo.getAdmissionDate() != null) {
                LocalDate admissao = ultimoVinculo.getAdmissionDate();
                LocalDate demissao = (ultimoVinculo.getStatusEmployee() != null
                        && ultimoVinculo.getStatusEmployee().equalsIgnoreCase("Trabalhando"))
                        || ultimoVinculo.getTerminationDate() == null
                        || ultimoVinculo.getTerminationDate().equals(LocalDate.of(1900, 12, 31))
                                ? LocalDate.now()
                                : ultimoVinculo.getTerminationDate();
                totalDias = ChronoUnit.DAYS.between(admissao, demissao);
            }
            totalDiasPorCpf.put(cpf, totalDias);
        }

        // Buscar dependentes e planos para cada funcionário
        Map<String, List<EmployeeSummaryDTO.PlanoInfo>> planosPorMatricula = new HashMap<>();
        for (Employee e : employees) {
            // Buscar todos os registros (dependentes e do próprio funcionário)
            List<Dependent> todosRegistros = dependentRepository
                    .findByEmployeeRegistrationAndPlanNotContainingAndExclusionMonth(
                            e.getRegistration(), "#Inativo", "1900-12-31 00:00:00");

            // Agrupar registros por plano
            Map<String, List<Dependent>> registrosPorPlano = todosRegistros.stream()
                    .filter(d -> d != null && ("Dependente".equals(d.getTypeEmployee()) ||
                            "Funcionario".equals(d.getTypeEmployee())))
                    .collect(Collectors
                            .groupingBy(d -> d.getTypePlan() + "_" + d.getIdPlan() + "_" + d.getInsuranceCode()));

            // Criar PlanoInfo para cada plano
            List<EmployeeSummaryDTO.PlanoInfo> planosInfo = registrosPorPlano.entrySet().stream()
                    .map(entry -> {
                        Dependent primeiro = entry.getValue().get(0);
                        long qtdDependentes = entry.getValue().stream()
                                .filter(d -> "Dependente".equals(d.getTypeEmployee()))
                                .count();

                        return EmployeeSummaryDTO.PlanoInfo.builder()
                                .tipoPlano(primeiro.getTypePlan())
                                .nomePlano(primeiro.getPlan())
                                .codigoPlano(primeiro.getIdPlan())
                                .nomeSeguradora(primeiro.getNameInsurance())
                                .codigoSeguradora(primeiro.getInsuranceCode())
                                .quantidadeDependentes((int) qtdDependentes)
                                .temPlanoTitular(entry.getValue().stream()
                                        .anyMatch(d -> "Funcionario".equals(d.getTypeEmployee())))
                                .build();
                    })
                    .toList();

            planosPorMatricula.put(e.getRegistration(), planosInfo);
        }

        // Converter para DTOs
        List<EmployeeSummaryDTO> dtos = new ArrayList<>();
        for (Employee e : employees) {
            String regional = null;
            if (e.getBranch_id() != null) {
                regional = regionalRepository.findByFilial(e.getBranch_id().intValue())
                        .map(Regional::getRegional)
                        .orElse(null);
            }

            ContractProject contrato = e.getContract_id() != null
                    ? contractProjectRepository.findWithFilialByRateio(e.getContract_id()).orElse(null)
                    : null;

            String costCenterNameVar = contrato != null ? contrato.getCostCenterName() : null;

            Cargo cargo = e.getPosition() != null
                    ? cargoRepository.findByIdHcm(e.getPosition()).orElse(null)
                    : null;

            String nomeCargo = cargo != null ? cargo.getNameTitle() : null;

            List<EmployeeSummaryDTO.PlanoInfo> planos = planosPorMatricula.get(e.getRegistration());

            // Buscar hierarquia organizacional simplificada
            EmployeeSummaryDTO.HierarchyInfo hierarchy = null;
            if (subordinateService != null && e.getCostCenterId() != null) {
                hierarchy = subordinateService.getHierarchyByFilialHcmId(e.getFilialIdHcm());
            }

            // Priorizar regional da hierarquia organizacional
            String regionalFinal = regional;
            if (hierarchy != null && hierarchy.getRegionalNome() != null) {
                regionalFinal = hierarchy.getRegionalNome();
            }

            EmployeeSummaryDTO dto = EmployeeSummaryDTO.fromEntity(
                    e,
                    regionalFinal,
                    planos,
                    totalDiasPorCpf.get(e.getCpf()),
                    costCenterNameVar,
                    nomeCargo);
            dto.setHierarchy(hierarchy);
            dtos.add(dto);
        }

        // Ordenar por anos DESC, meses DESC (igual ao exibido no front) e nome ASC
        dtos.sort((dto1, dto2) -> {
            Long dias1 = dto1.getTotalDiasEmpresa();
            Long dias2 = dto2.getTotalDiasEmpresa();
            long anos1 = dias1 != null ? dias1 / 365 : -1L;
            long meses1 = dias1 != null ? (dias1 % 365) / 30 : -1L;
            long anos2 = dias2 != null ? dias2 / 365 : -1L;
            long meses2 = dias2 != null ? (dias2 % 365) / 30 : -1L;
            int cmpAnos = Long.compare(anos2, anos1); // decrescente
            if (cmpAnos != 0)
                return cmpAnos;
            int cmpMeses = Long.compare(meses2, meses1); // decrescente
            if (cmpMeses != 0)
                return cmpMeses;
            if (dto1.getNome() == null && dto2.getNome() == null)
                return 0;
            if (dto1.getNome() == null)
                return 1;
            if (dto2.getNome() == null)
                return -1;
            return dto1.getNome().compareTo(dto2.getNome());
        });

        return new PageImpl<>(dtos, pageable, totalElements);
    }

    private <T> void addOrPredicate(List<Predicate> predicates, List<T> values, Root<Employee> root, CriteriaBuilder cb,
            String fieldName) {
        if (values != null && !values.isEmpty()) {
            List<Predicate> orPredicates = values.stream()
                    .filter(Objects::nonNull)
                    .map(value -> cb.equal(root.get(fieldName), value))
                    .collect(Collectors.toList());
            if (!orPredicates.isEmpty()) {
                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }
        }
    }

    private void addOrPredicateLike(List<Predicate> predicates, List<String> values, Root<Employee> root,
            CriteriaBuilder cb, String fieldName) {
        if (values != null && !values.isEmpty()) {
            List<Predicate> orPredicates = values.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .map(value -> cb.like(cb.lower(root.get(fieldName)), "%" + value.toLowerCase() + "%"))
                    .collect(Collectors.toList());
            if (!orPredicates.isEmpty()) {
                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }
        }
    }

    /**
     * Filtro de range de data:
     * - Ambos preenchidos: BETWEEN inicio e fim
     * - Só inicio: >= inicio
     * - Só fim: <= fim
     */
    private void addDateRangePredicate(List<Predicate> predicates, List<String> startDate, List<String> endDate,
            Root<Employee> root, CriteriaBuilder cb, String fieldName) {
        boolean hasStart = startDate != null && !startDate.isEmpty();
        boolean hasEnd = endDate != null && !endDate.isEmpty();
        if (!hasStart && !hasEnd)
            return;
        try {
            if (hasStart && hasEnd) {
                LocalDate inicio = LocalDate.parse(startDate.get(0));
                LocalDate fim = LocalDate.parse(endDate.get(0));
                predicates.add(cb.between(root.get(fieldName), inicio, fim));
            } else if (hasStart) {
                LocalDate inicio = LocalDate.parse(startDate.get(0));
                predicates.add(cb.greaterThanOrEqualTo(root.get(fieldName), inicio));
            } else {
                LocalDate fim = LocalDate.parse(endDate.get(0));
                predicates.add(cb.lessThanOrEqualTo(root.get(fieldName), fim));
            }
        } catch (Exception ignored) {
            // Ignorar se não conseguir fazer parse
        }
    }

    private void addDatePredicates(List<Predicate> predicates, List<String> dates, Root<Employee> root,
            CriteriaBuilder cb, String fieldName) {
        if (dates != null && !dates.isEmpty()) {
            List<Predicate> datePredicates = dates.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .map(date -> {
                        Predicate p1 = cb.like(
                                cb.function("TO_CHAR", String.class, root.get(fieldName), cb.literal("YYYY-MM-DD")),
                                "%" + date + "%");
                        Predicate p2 = cb.like(
                                cb.function("TO_CHAR", String.class, root.get(fieldName), cb.literal("MM-DD")),
                                "%" + date + "%");
                        Predicate p3 = cb.like(
                                cb.function("TO_CHAR", String.class, root.get(fieldName), cb.literal("DD-MM")),
                                "%" + date + "%");
                        Predicate p4 = cb.like(
                                cb.function("TO_CHAR", String.class, root.get(fieldName), cb.literal("YYYY")),
                                "%" + date + "%");
                        Predicate p5 = cb.like(
                                cb.function("TO_CHAR", String.class, root.get(fieldName), cb.literal("DD-MM-YYYY")),
                                "%" + date + "%");
                        return cb.or(p1, p2, p3, p4, p5);
                    })
                    .collect(Collectors.toList());
            if (!datePredicates.isEmpty()) {
                predicates.add(cb.or(datePredicates.toArray(new Predicate[0])));
            }
        }
    }

    private <T extends Number> void addNumericInPredicate(List<Predicate> predicates, List<String> values,
            Root<Employee> root, CriteriaBuilder cb, String fieldName, Class<T> numberClass) {
        if (values != null && !values.isEmpty()) {
            List<T> numericValues = values.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank() && !s.equals("0"))
                    .map(s -> {
                        try {
                            if (numberClass == Long.class) {
                                return numberClass.cast(Long.valueOf(s));
                            } else if (numberClass == Integer.class) {
                                return numberClass.cast(Integer.valueOf(s));
                            }
                            return null;
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!numericValues.isEmpty()) {
                predicates.add(root.get(fieldName).in(numericValues));
            }
        }
    }

}

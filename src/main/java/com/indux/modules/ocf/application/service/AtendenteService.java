package com.indux.modules.ocf.application.service;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.modules.faq.domain.repository.SetorRepository;
import com.indux.modules.ocf.application.dto.AtendenteDTO;
import com.indux.modules.ocf.application.dto.AtendentePatchDTO;
import com.indux.modules.ocf.application.dto.SetorInfoDTO;
import com.indux.modules.ocf.domain.model.Atendente;
import com.indux.modules.ocf.domain.repository.AtendenteRepository;
import com.indux.modules.organization_chart.application.dtos.ProjectFilter;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity_.project;

@Service
public class AtendenteService {

    private final AtendenteRepository repository;
    private final EmployeeRepository employeeRepository;
    private final RegionalRepository regionalRepository;
    private final SetorRepository setorRepository;
    private final com.indux.core.domain.repository.generic.BranchRepository branchRepository;
    private final com.indux.modules.contracts.domain.repository.ContractRepository contractRepository;
    private final JdbcTemplate jdbcTemplate;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final ProjectService projectService;
    private final SubordinateService subordinateService;

    public AtendenteService(AtendenteRepository repository, EmployeeRepository employeeRepository,
            RegionalRepository regionalRepository, SetorRepository setorRepository,
            com.indux.core.domain.repository.generic.BranchRepository branchRepository,
            com.indux.modules.contracts.domain.repository.ContractRepository contractRepository,
            JdbcTemplate jdbcTemplate, GetEmployeeUseCase getEmployeeUseCase, ProjectService projectService,
            SubordinateService subordinateService) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.regionalRepository = regionalRepository;
        this.setorRepository = setorRepository;
        this.branchRepository = branchRepository;
        this.contractRepository = contractRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.projectService = projectService;
        this.subordinateService = subordinateService;
    }

    public Atendente findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado com id: " + id));
    }

    public Atendente save(AtendenteDTO atendenteDTO) {
        return repository.save(Atendente.fromDTO(atendenteDTO));
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Atendente não encontrado com id: " + id);
        }
        repository.deleteById(id);
    }

    public Atendente update(Long id, AtendenteDTO atendenteDTO) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Atendente não encontrado com id: " + id);
        }
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado com id: " + id));

        entity.setEmail(atendenteDTO.email());
        entity.setMatriculaRHlocal(atendenteDTO.matriculaRHlocal());
        entity.setMatriculaRHmatriz(atendenteDTO.matriculaRHmatriz());
        entity.setSetor(atendenteDTO.setor());
        entity.setSenha(atendenteDTO.senha());
        // Converter lista de regionais para JSON string
        entity.setRegional(convertListToJson(atendenteDTO.regional()));
        // Converter lista de contratos para JSON string
        entity.setContrato(convertListToJson(atendenteDTO.contrato()));

        return repository.save(entity);
    }

    public Atendente updatePartial(Long id, AtendentePatchDTO patchDTO) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Atendente não encontrado com id: " + id);
        }
        var entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Atendente não encontrado com id: " + id));

        // Atualizar apenas os campos que foram fornecidos
        patchDTO.email().ifPresent(entity::setEmail);
        patchDTO.matriculaRHlocal().ifPresent(entity::setMatriculaRHlocal);
        patchDTO.matriculaRHmatriz().ifPresent(entity::setMatriculaRHmatriz);
        patchDTO.setor().ifPresent(entity::setSetor);
        patchDTO.senha().ifPresent(entity::setSenha);

        // Atualizar listas apenas se fornecidas
        patchDTO.regional().ifPresent(regional -> entity.setRegional(convertListToJson(regional)));
        patchDTO.contrato().ifPresent(contrato -> entity.setContrato(convertListToJson(contrato)));

        return repository.save(entity);
    }

    public List<Atendente> findAll() {
        return repository.findAll();
    }

    public void create(Atendente atendente) {
        // Não há validação específica necessária para os novos campos
        repository.save(atendente);
    }

    public Optional<Atendente> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public List<Atendente> findByRegional(String regional) {
        return repository.findByRegional(regional);
    }

    public List<Atendente> findByContrato(String contrato) {
        return repository.findByContrato(contrato);
    }

    public List<Atendente> findByRegionalAndContrato(String regional, String contrato) {
        return repository.findByRegionalAndContrato(regional, contrato);
    }

    // Métodos para carregar dados relacionados
    public Atendente carregarDadosRelacionados(Atendente atendente) {
        if (atendente.getMatricula() != null && !atendente.getMatricula().trim().isEmpty()) {
            // Resolver filial e regional via hierarquia organizacional (fonte mais confiável)
            EmployeeDTO employeeDTO = getEmployeeUseCase.getEmployeeByMatricula(atendente.getMatricula());
            if (employeeDTO != null) {
                atendente.setFilialNome(employeeDTO.getFilial_nome());
                if (employeeDTO.getHierarchy() != null
                        && employeeDTO.getHierarchy().getRegionalNome() != null) {
                    atendente.setRegionalFromFilialNome(employeeDTO.getHierarchy().getRegionalNome());
                }
            }

            // Carregar Employee completo para resolução de contrato e projectId
            List<Employee> funcionarios = employeeRepository.findAllByRegistration(atendente.getMatricula());
            if (!funcionarios.isEmpty()) {
                atendente.setFuncionario(funcionarios.getFirst());
            }
        }

        if (atendente.getSetor() != null) {
            setorRepository.findById(atendente.getSetor()).ifPresent(atendente::setSetorEntity);
        }
        List<String> regionalIds = atendente.getRegionaisAsList();
        if (!regionalIds.isEmpty()) {
            List<Long> ids = regionalIds.stream()
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .map(Long::parseLong)
                    .toList();
            if (!ids.isEmpty()) {
                List<Regional> regionais = ids.stream()
                        .map(regionalRepository::findByIdWithFiliais)
                        .filter(java.util.Optional::isPresent)
                        .map(java.util.Optional::get)
                        .toList();
                atendente.setRegionais(regionais);
            }
        }
        if (atendente.getFuncionario() != null) {
            Integer rateioId = atendente.getFuncionario().getContract_id();
            Long filialId = atendente.getFuncionario().getBranch_id();
            var filialHcm = atendente.getFuncionario().getFilialIdHcm();
            if (rateioId != null) {
                var contratoOpt = contractRepository.findAll().stream()
                        .filter(c -> c.getRateioId() != null && c.getRateioId().intValue() == rateioId)
                        .findFirst();
                contratoOpt.ifPresent(c -> atendente.setContractNomeCentroCustos(c.getNomeCentroCustos()));
            }
            // Resolver nome da filial e a regional a que pertence
            if (filialId != null) {
                branchRepository.findById(filialId).ifPresent(f -> {
                    atendente.setFilialNome(f.getBranchName());
                    if (f.getRegional() != null) {
                        atendente.setRegionalFromFilialNome(f.getRegional().getRegional());
                    }
                });
            }

            if (filialHcm != null) {
                var hierarchy = subordinateService.getHierarchyByFilialHcmId(filialHcm);
                if (hierarchy != null && hierarchy.getRegionalNome() != null) {
                    atendente.setRegionalFromFilialNome(hierarchy.getRegionalNome());
                }

                var filter = ProjectFilter.builder().filial(List.of(filialHcm)).build();
                var projectId = projectService.getAllProjects(filter, PageRequest.of(0, 1));
                if (projectId != null && projectId.hasContent()) {
                    var project = projectId.getContent().getFirst();
                    atendente.setProjectId(project.getId());
                }
            }
        }

        return atendente;
    }

    public List<Atendente> carregarDadosRelacionados(List<Atendente> atendentes) {
        return atendentes.stream()
                .map(this::carregarDadosRelacionados)
                .toList();
    }

    public GenericMessage registrarAtendente(AtendenteDTO dto) {
        try {
            save(dto);
            return new GenericMessage("Atendente registrado com sucesso", 201);
        } catch (Exception e) {
            return new GenericMessage("Erro ao registrar atendente: " + e.getMessage(), 400);
        }
    }

    public GenericMessage atualizarAtendente(String id, AtendenteDTO dto) {
        try {
            update(Long.parseLong(id), dto);
            return new GenericMessage("Atendente atualizado com sucesso", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao atualizar atendente: " + e.getMessage(), 400);
        }
    }

    public GenericMessage atualizarAtendenteParcial(String id, AtendentePatchDTO patchDTO) {
        try {
            updatePartial(Long.parseLong(id), patchDTO);
            return new GenericMessage("Atendente atualizado parcialmente com sucesso", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao atualizar atendente: " + e.getMessage(), 400);
        }
    }

    public GenericMessage atualizarNomeAtendente(String id, String nome) {
        try {
            Long atendenteId = Long.parseLong(id);
            var entity = repository.findById(atendenteId)
                    .orElseThrow(() -> new RuntimeException("Atendente não encontrado com id: " + id));
            entity.setNome(nome);
            repository.save(entity);
            return new GenericMessage("Nome do atendente atualizado com sucesso", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao atualizar nome do atendente: " + e.getMessage(), 400);
        }
    }

    public GenericMessage atualizarNomeEMatricula(String id, String nome, String matricula) {
        try {
            Long atendenteId = Long.parseLong(id);
            var entity = repository.findById(atendenteId)
                    .orElseThrow(() -> new RuntimeException("Atendente não encontrado com id: " + id));
            entity.setNome(nome);
            entity.setMatricula(matricula);
            repository.save(entity);
            return new GenericMessage("Nome e matrícula do atendente atualizados com sucesso", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao atualizar nome e matrícula do atendente: " + e.getMessage(), 400);
        }
    }

    public GenericMessage deletarAtendente(String id) {
        try {
            deleteById(Long.parseLong(id));
            return new GenericMessage("Atendente deletado com sucesso", 200);
        } catch (Exception e) {
            return new GenericMessage("Erro ao deletar atendente: " + e.getMessage(), 400);
        }
    }

    public List<SetorInfoDTO> listarSetores() {
        return setorRepository.findByStatus(true)
                .stream()
                .map(SetorInfoDTO::fromSetor)
                .toList();
    }

    private String convertListToJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        return "[" + String.join(",", list.stream().map(String::valueOf).toList()) + "]";
    }

    /**
     * Busca informações do time e atendentes por contrato
     */
    public com.indux.modules.ocf.application.dto.TeamInfoByContractDTO getTeamInfoByContract(String contrato) {
        // Buscar atendentes vinculados ao contrato via times
        List<Atendente> atendentes = repository.findByContrato(contrato);

        if (atendentes.isEmpty()) {
            return new com.indux.modules.ocf.application.dto.TeamInfoByContractDTO(
                    null,
                    "Nenhum time encontrado",
                    List.of());
        }

        // Carregar regionais para todos os atendentes
        loadRegionaisForAtendentes(atendentes);

        // Buscar informações do time através do primeiro atendente
        Long teamId = getTeamIdByContract(contrato);
        String teamName = getTeamNameByContract(contrato);

        // Converter atendentes para DTO com regionais
        List<com.indux.modules.ocf.application.dto.AtendenteWithRegionalDTO> atendentesDTO = atendentes.stream()
                .map(this::convertToAtendenteWithRegionalDTO)
                .toList();

        return new com.indux.modules.ocf.application.dto.TeamInfoByContractDTO(
                teamId,
                teamName,
                atendentesDTO);
    }

    /**
     * Carrega as regionais para todos os atendentes
     */
    private void loadRegionaisForAtendentes(List<Atendente> atendentes) {
        for (Atendente atendente : atendentes) {
            List<String> regionais = getRegionaisFromAtendente(atendente);
            // Criar lista de objetos Regional para o atendente
            List<Regional> regionaisObjects = regionais.stream()
                    .map(nome -> {
                        Regional regional = new Regional();
                        regional.setRegional(nome);
                        return regional;
                    })
                    .toList();
            atendente.setRegionais(regionaisObjects);
        }
    }

    /**
     * Busca regionais do atendente usando a matrícula diretamente
     */
    private List<String> getRegionaisFromDatabase(Long atendenteId) {
        try {
            // Buscar matrícula do atendente
            String sqlMatricula = "SELECT matricula FROM tb_atendentes WHERE id = ?";
            String matricula = jdbcTemplate.queryForObject(sqlMatricula, String.class, atendenteId);

            if (matricula == null || matricula.trim().isEmpty()) {
                return List.of("Matriz"); // Fallback
            }

            // Buscar regional diretamente da tb_funcionarios -> tb_filiais -> tb_regional
            String sqlRegional = "SELECT DISTINCT r.regional " +
                    "FROM tb_funcionarios f " +
                    "JOIN tb_filiais fil ON fil.id = f.filial_id " +
                    "JOIN tb_regional r ON r.id = fil.regional_id " +
                    "WHERE f.matricula = ?";

            List<String> regionais = jdbcTemplate.queryForList(sqlRegional, String.class, matricula);

            if (!regionais.isEmpty()) {
                return regionais;
            }

            // Fallback: retornar "Matriz"
            return List.of("Matriz");

        } catch (Exception e) {
            return List.of("Matriz"); // Fallback
        }
    }

    /**
     * Converte Atendente para DTO com regionais e filial
     */
    private com.indux.modules.ocf.application.dto.AtendenteWithRegionalDTO convertToAtendenteWithRegionalDTO(
            Atendente atendente) {
        // Buscar filial e regional em uma única consulta usando a matrícula
        FilialRegionalInfo info = getFilialERegionalFromMatricula(atendente.getMatricula());

        // Obter a regional principal (primeira da lista ou "Matriz" como fallback)
        String regionalPrincipal = info.regionais.isEmpty() ? "Matriz" : info.regionais.get(0);

        return new com.indux.modules.ocf.application.dto.AtendenteWithRegionalDTO(
                atendente.getId(),
                atendente.getEmail(),
                atendente.getNome(),
                atendente.getMatricula(),
                atendente.getAccountId(),
                info.filial,
                regionalPrincipal,
                info.regionais);
    }

    /**
     * Classe interna para armazenar informações de filial e regional
     */
    private static class FilialRegionalInfo {
        final String filial;
        final List<String> regionais;

        FilialRegionalInfo(String filial, List<String> regionais) {
            this.filial = filial;
            this.regionais = regionais;
        }
    }

    /**
     * Busca filial e regional usando EmployeeDTO pela matrícula
     */
    private FilialRegionalInfo getFilialERegionalFromMatricula(String matricula) {
        try {
            if (matricula == null || matricula.trim().isEmpty()) {
                return new FilialRegionalInfo(null, List.of("Matriz"));
            }

            // Buscar informações do funcionário usando o EmployeeDTO
            EmployeeDTO employeeDTO = getEmployeeUseCase.getEmployeeByMatricula(matricula);

            if (employeeDTO != null) {
                String filial = employeeDTO.getFilial_nome();
                Long filialId = employeeDTO.getFilial_id();

                List<String> regionais = new java.util.ArrayList<>();

                // Se tiver filial_id, buscar a regional diretamente da tb_regional
                if (filialId != null) {
                    // Buscar regional pelo filial_id na tb_regional
                    String regional = getRegionalByFilialId(filialId);
                    if (regional != null && !regional.trim().isEmpty()) {
                        regionais.add(regional);
                    } else {
                        // Se não encontrar regional específica, usar "Matriz"
                        regionais.add("Matriz");
                    }
                } else {
                    // Se não tiver filial_id, usar "Matriz" como fallback
                    regionais.add("Matriz");
                }

                return new FilialRegionalInfo(filial, regionais);
            }

            // Fallback se não encontrar o funcionário
            return new FilialRegionalInfo(null, List.of("Matriz"));

        } catch (Exception e) {
            return new FilialRegionalInfo(null, List.of("Matriz"));
        }
    }

    /**
     * Busca a regional pela filial na tb_regional
     */
    private String getRegionalByFilialName(String filialNome) {
        try {
            if (filialNome == null || filialNome.trim().isEmpty()) {
                return "Matriz";
            }

            // Buscar regional da filial na tb_regional
            String sql = "SELECT r.regional " +
                    "FROM tb_filiais f " +
                    "JOIN tb_regional r ON r.id = f.regional_id " +
                    "WHERE f.nome = ? " +
                    "LIMIT 1";

            String regional = jdbcTemplate.queryForObject(sql, String.class, filialNome);

            // Se encontrar regional, retornar; senão retornar "Matriz"
            return regional != null && !regional.trim().isEmpty() ? regional : "Matriz";

        } catch (Exception e) {
            // Em caso de erro, retornar "Matriz" como fallback
            return "Matriz";
        }
    }

    /**
     * Busca a regional pelo filial_id usando a mesma lógica do endpoint
     * /api/regional/all
     */
    private String getRegionalByFilialId(Long filialId) {
        try {
            if (filialId == null) {
                return "Matriz";
            }

            // Usar a mesma lógica do endpoint /api/regional/all
            // Buscar regional usando o RegionalRepository.findByFilialId
            Optional<Regional> regionalOpt = regionalRepository.findByFilialId(filialId);

            if (regionalOpt.isPresent()) {
                Regional regional = regionalOpt.get();
                return regional.getRegional();
            } else {
                return "Matriz";
            }

        } catch (Exception e) {
            // Em caso de erro, retornar "Matriz" como fallback
            return "Matriz";
        }
    }

    /**
     * Busca a filial do atendente usando EmployeeDTO pela matrícula
     */
    private String getFilialFromAtendente(Atendente atendente) {
        try {
            String matricula = atendente.getMatricula();

            if (matricula == null || matricula.trim().isEmpty()) {
                return null;
            }

            // Buscar informações do funcionário usando o EmployeeDTO
            EmployeeDTO employeeDTO = getEmployeeUseCase.getEmployeeByMatricula(matricula);

            if (employeeDTO != null) {
                return employeeDTO.getFilial_nome();
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca regionais do atendente
     */
    private List<String> getRegionaisFromAtendente(Atendente atendente) {
        // Primeiro, tentar usar as regionais já carregadas no objeto
        if (atendente.getRegionais() != null && !atendente.getRegionais().isEmpty()) {
            return atendente.getRegionais().stream()
                    .map(Regional::getRegional)
                    .toList();
        }

        // Se não tiver regionais carregadas, tentar buscar do JSON e converter IDs para
        // nomes
        if (atendente.getRegional() != null && !atendente.getRegional().isEmpty()) {
            try {
                // Parse do JSON simples para extrair IDs das regionais
                String regionalJson = atendente.getRegional();
                if (regionalJson.startsWith("[") && regionalJson.endsWith("]")) {
                    String content = regionalJson.substring(1, regionalJson.length() - 1);
                    if (!content.trim().isEmpty()) {
                        String[] regionalIds = content.split(",");
                        List<String> regionais = convertRegionalIdsToNames(regionalIds);
                        if (!regionais.isEmpty()) {
                            return regionais;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignorar erro de parsing
            }
        }

        // Como último recurso, buscar diretamente do banco de dados
        return getRegionaisFromDatabase(atendente.getId());
    }

    /**
     * Converte IDs das regionais para nomes
     */
    private List<String> convertRegionalIdsToNames(String[] regionalIds) {
        try {
            List<String> regionalNames = new java.util.ArrayList<>();

            for (String idStr : regionalIds) {
                String cleanId = idStr.trim();
                if (!cleanId.isEmpty()) {
                    try {
                        Long regionalId = Long.parseLong(cleanId);
                        String regionalName = getRegionalNameById(regionalId);
                        if (regionalName != null) {
                            regionalNames.add(regionalName);
                        }
                    } catch (NumberFormatException e) {
                        // Se não for um número, adicionar como está (pode ser um nome)
                        regionalNames.add(cleanId);
                    }
                }
            }

            return regionalNames;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Busca o nome da regional pelo ID
     */
    private String getRegionalNameById(Long regionalId) {
        try {
            String sql = "SELECT regional FROM tb_regional WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, regionalId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca o ID do time por contrato
     */
    private Long getTeamIdByContract(String contrato) {
        try {
            String sql = """
                    SELECT DISTINCT t.time_id
                    FROM tb_times t
                    JOIN tb_times_contratos tc ON tc.time_id = t.time_id
                    WHERE tc.contrato_id = CAST(? AS BIGINT)
                    LIMIT 1
                    """;

            return jdbcTemplate.queryForObject(sql, Long.class, contrato);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca o nome do time por contrato
     */
    private String getTeamNameByContract(String contrato) {
        try {
            String sql = """
                    SELECT DISTINCT t.nome_tipe
                    FROM tb_times t
                    JOIN tb_times_contratos tc ON tc.time_id = t.time_id
                    WHERE tc.contrato_id = CAST(? AS BIGINT)
                    LIMIT 1
                    """;

            return jdbcTemplate.queryForObject(sql, String.class, contrato);
        } catch (Exception e) {
            return "Time não encontrado";
        }
    }
}
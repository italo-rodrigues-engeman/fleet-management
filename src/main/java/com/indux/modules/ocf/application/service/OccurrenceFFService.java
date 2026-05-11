package com.indux.modules.ocf.application.service;


import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.occurrence.AbstractOccurrenceService;
import com.indux.core.domain.service.occurrence.log.OccurrenceLogService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.core.infra.notifcation.whatsapp.WhatsappSender;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.ocf.application.dto.*;
import com.indux.modules.ocf.domain.model.*;
import com.indux.modules.ocf.domain.model.log.FinanceOccurrenceLog;
import com.indux.modules.ocf.domain.model.log.IdCodeProjection;
import com.indux.modules.ocf.domain.repository.DatabaseSequenceFFRepository;
import com.indux.modules.ocf.domain.repository.ItemReclamadoRepository;
import com.indux.modules.ocf.domain.repository.OcorrenciaFFRepository;
import com.indux.modules.ocf.domain.repository.TicketAlodpRepository;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepository;
import com.mongodb.lang.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Service

@Transactional

public class OccurrenceFFService extends AbstractOccurrenceService<OcorrenciaFF> {

    private static final Logger logger = LoggerFactory.getLogger(OccurrenceFFService.class);

    private final OcorrenciaFFRepository repository;

    private final ModuleManagementService moduleService;

    private final GetEmployeeUseCase getEmployee;

    private final UserService userService;

    private final DatabaseSequenceFFRepository sequenceRepository;

    private final ModuleResponseMapper moduleResponseMapper;

    private final OccurrenceLogService<FinanceOccurrenceLog> logService;

    private final ItemReclamadoRepository itemReclamadoRepository;

    private final TicketAlodpRepository ticketAlodpRepository;

    private final AtendenteService atendenteService;

    private final JdbcTemplate jdbcTemplate;

    private final TimeService timeService;

    private final WhatsappSender whatsappSender;

    private final DiretoriaFilterService diretoriaFilterService;

    private final OrganizationProjectRepository organizationProjectRepository;

    @Value("${module.ocf.id}")
    private String modulo_id;

    private final AttachmentService attachmentService;

    public OccurrenceFFService(OcorrenciaFFRepository repository, StorageService storageService, NotificationService notification, CustomMailSender mailSender, ModuleManagementService moduleService, GetEmployeeUseCase getEmployee, UserService userService, DatabaseSequenceFFRepository sequenceRepository, MongoTemplate mongo, ModuleResponseMapper moduleResponseMapper, OccurrenceLogService<FinanceOccurrenceLog> logService, ItemReclamadoRepository itemReclamadoRepository, TicketAlodpRepository ticketAlodpRepository, AtendenteService atendenteService, JdbcTemplate jdbcTemplate, TimeService timeService, WhatsappSender whatsappSender, DiretoriaFilterService diretoriaFilterService, OrganizationProjectRepository organizationProjectRepository, AttachmentService attachmentService) {

        super(storageService, notification, mailSender, userService, mongo);

        this.repository = repository;

        this.moduleService = moduleService;

        this.getEmployee = getEmployee;

        this.userService = userService;


        this.sequenceRepository = sequenceRepository;

        this.moduleResponseMapper = moduleResponseMapper;

        this.logService = logService;

        this.itemReclamadoRepository = itemReclamadoRepository;

        this.ticketAlodpRepository = ticketAlodpRepository;

        this.atendenteService = atendenteService;

        this.jdbcTemplate = jdbcTemplate;

        this.timeService = timeService;

        this.whatsappSender = whatsappSender;

        this.diretoriaFilterService = diretoriaFilterService;

        this.organizationProjectRepository = organizationProjectRepository;

        this.attachmentService = attachmentService;
    }


    @Override

    protected OcorrenciaFF getOccurrenceInternal(String id) {

        OcorrenciaFF occurrence = repository.findById(id)

                .orElseThrow(() -> new ModuleNotFoundFailure("Ocorrência não encontrada."));


        List<ItemReclamado> itemsReclamados = itemReclamadoRepository.findByOcorrenciaId(id);

        occurrence.setItemsreclamados(itemsReclamados);


        return occurrence;

    }


    public OcorrenciaFF saveOccurrence(OcorrenciaFF occurrence) {

        occurrence.setDataLog(Date.from(Instant.now()));

        setOrderFields(occurrence);

        return repository.save(occurrence);

    }


    @Override

    protected void updateOccurrenceAfterReview(OcorrenciaFF occurrence, Object dto, String userId, boolean isEdit) {

        OccurrenceFFDTO occurrenceDTO = (OccurrenceFFDTO) dto;

        occurrence.setOrigem(occurrenceDTO.origem());

        occurrence.setData_Ocorrencia(occurrenceDTO.data_ocorrencia());

        occurrence.setObservacaoAnalista1(occurrenceDTO.observacaoAnalista1());

        occurrence.setMotivoAnalista(occurrenceDTO.motivoAnalista());

        occurrence.setAprovacaoAnalista1(occurrenceDTO.aprovacaoAnalista1());

        occurrence.setPrioridade(occurrenceDTO.prioridade());

        occurrence.setTipoAtendimento(occurrenceDTO.tipoAtendimento());


        // Atualizar novos campos se fornecidos

        if (occurrenceDTO.dataAtendimento().isPresent()) {

            occurrence.setDataAtendimento(occurrenceDTO.dataAtendimento().get());

        }

        if (occurrenceDTO.dataFim().isPresent()) {

            occurrence.setDataFim(occurrenceDTO.dataFim().get());

        }

        if (occurrenceDTO.atendenteRHlocal().isPresent()) {

            occurrence.setAtendenteRHlocal(occurrenceDTO.atendenteRHlocal().get());

        }

        if (occurrenceDTO.atendenteRHmatriz().isPresent()) {

            occurrence.setAtendenteRHmatriz(occurrenceDTO.atendenteRHmatriz().get());

        }

        if (occurrenceDTO.competencia().isPresent()) {

            occurrence.setCompetencia(occurrenceDTO.competencia().get());

        }

        if (occurrenceDTO.conversaChat().isPresent()) {

            occurrence.setConversaChat(occurrenceDTO.conversaChat().get());

        }

        if (occurrenceDTO.descricaoOcorrencia().isPresent()) {

            occurrence.setDescricaoOcorrencia(occurrenceDTO.descricaoOcorrencia().get());

        }

        if (occurrenceDTO.respostaEmpregado().isPresent()) {

            occurrence.setRespostaEmpregado(occurrenceDTO.respostaEmpregado().get());

        }

        if (occurrenceDTO.justificativaOcorrencia().isPresent()) {

            occurrence.setJustificativaOcorrencia(occurrenceDTO.justificativaOcorrencia().get());

        }

        if (occurrenceDTO.meiosComunicacao().isPresent()) {

            occurrence.setMeiosComunicacao(occurrenceDTO.meiosComunicacao().get());

        }


        if (occurrenceDTO.extratoColaborador().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(occurrenceDTO.extratoColaborador().get(), "ocf/extratos", occurrence.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 1);

            occurrence.setExtratoColaborador(metadata);

        }


        if (occurrenceDTO.anexoTicket().isPresent()) {

            FileMetadata anexoTicketMetadata = attachmentService.storeFile(occurrenceDTO.anexoTicket().get(), "ocf/anexos_ticket", occurrence.getId() + "_ticket_" + System.currentTimeMillis() + "_" + occurrenceDTO.anexoTicket().get().getOriginalFilename(), 1);

            occurrence.setAnexoTicket(anexoTicketMetadata);

        }


        if (occurrenceDTO.bancoDadosErrado().isPresent() && occurrenceDTO.bancoDadosErrado().get()) {

            occurrence.setBancoDadosErrado(true);

            occurrence.setDadosCorretos(occurrenceDTO.dadosCorretos());

        }


        StepLog correctionLog = StepLog.builder()

                .id(UUID.randomUUID())

                .name(isEdit ? "Edição das informações" : "Revisão finalizada")

                .group(occurrenceDTO.grupoResponsavel().map(UUID::fromString).orElse(null))

                .created_at(new Date())

                .step(occurrence.getCurrentStep())

                .user(UUID.fromString(userId))

                .final_at(new Date())

                .observation(occurrenceDTO.observacao())

                .build();


        if (!isEdit) {

            occurrence.setStatus(DocumentStatus.ABERTO);


            // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

            String occurrenceCode = String.valueOf(occurrence.getCodeID());

            Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

            String moduleName = module.getName();

//            notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep() + 1, occurrenceCode, moduleName);


            occurrence.setCurrentStep(2);

        }

        occurrence.getStepLog().add(correctionLog);

        saveOccurrence(occurrence);

    }


    @Override

    public GenericMessage createOccurrence(Object dto, String userId) throws InterruptedException {

        CreateOccurrenceResponseDTO result = createOccurrenceWithDetails(dto, userId);

        return new GenericMessage(result.message(), result.status());

    }


    public CreateOccurrenceResponseDTO createOccurrenceWithDetails(Object dto, String userId) throws InterruptedException {
        OccurrenceFFDTO request = (OccurrenceFFDTO) dto;

        if (request.numeroDoProtocolo() != null && !request.numeroDoProtocolo().isBlank()) {
            if (repository.existsByNumeroDoProtocolo(request.numeroDoProtocolo())) {
                throw new ModuleFailure("Já existe uma ocorrência criada para o protocolo: " + request.numeroDoProtocolo() + ". Use o endpoint de movimentação do ticket.");

            }
        }


        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        EmployeeDTO employee = getEmployee.getEmployeeByMatricula(request.matriculaReclamante());


        if (request.email() != null && !request.email().isBlank()) {
            employee.setEmail_comercial(request.email());
        }


        if (request.telefone() != null && !request.telefone().isBlank()) {
            employee.setTelefone(request.telefone());
        }

        SimpleUser applicant = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));


        OcorrenciaFF occurrence = new OcorrenciaFF();
        StepLog log = createStepLog(
                UUID.fromString(userId),
                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome)
                        .orElse("Início da Solicitação"),
                1,
                request.observacao(),
                null
        );

        var sequenciaID = new DatabaseSequenceFF();
        DatabaseSequenceFF sequence = sequenceRepository.save(sequenciaID);


        // Criação e adição das informações na Ocorrência

        // Todo: poderia ser um builder, talvez fosse menos verboso.

        occurrence.setSolicitante(applicant);
        occurrence.setColaborador(employee);
        occurrence.setModulo_id(module.getId());
        occurrence.setOrigem(request.origem());
        occurrence.setDescricao(request.observacao());
        occurrence.setRegionalId(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                        ? employee.getHierarchy().getRegionalId().intValue()
                        : null
        );
        occurrence.setRegionalNome(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                        ? employee.getHierarchy().getRegionalNome()
                        : ""
        );
        occurrence.setProjectId(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                        ? employee.getHierarchy().getProjetoId().intValue()
                        : 0
        );
        occurrence.setProjectName(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                        ? employee.getHierarchy().getProjetoNome()
                        : ""
        );
        occurrence.setData_Ocorrencia(request.data_ocorrencia());
        occurrence.setContratoId(employee.getContrato().getRateio());
        occurrence.setStatus(DocumentStatus.ABERTO);

        // Definir etapa inicial baseada no tipo de fluxo

        String tipoFluxo = request.tipoFluxo() != null && !request.tipoFluxo().isBlank() ? request.tipoFluxo() : "financeiro";
        int etapaInicial = "financeiro".equalsIgnoreCase(tipoFluxo) ? 2 : 3;
        occurrence.setCurrentStep(etapaInicial);
        occurrence.setTipoFluxo(tipoFluxo);
        occurrence.setCreated_at(Date.from(Instant.now()));
        occurrence.setDadosBancarios(employee.getDadosBancarios());
        occurrence.setStepLog(List.of(log));
        occurrence.setCodeID(sequence.getId());
        occurrence.setSituacao(DocumentStatus.ANDAMENTO);
        occurrence.setCausas(request.causa());

        boolean dadosErrados = request.bancoDadosErrado().isPresent() ? request.bancoDadosErrado().get() : false;

        if ("financeiro".equalsIgnoreCase(tipoFluxo)) {
            occurrence.setDadosCorretos(request.dadosCorretos());
            occurrence.setBancoDadosErrado(true);
            occurrence.setDescricao("Erro no cadastro da conta bancária.");

        } else {
            occurrence.setDescricao(request.observacao());
        }


        occurrence.setNumeroDoProtocolo(request.numeroDoProtocolo());
        occurrence.setTelefone(request.telefone());
        occurrence.setTelefoneDeContato(request.telefoneDeContato());
        occurrence.setTipoDeBeneficio(request.tipoDeBeneficio());
        occurrence.setMotivo(request.motivo());
        occurrence.setValorContestado(request.valorContestado());
        occurrence.setValorPagarDescontar(request.valorPagarDescontar());
        occurrence.setAprovacaoGestor(request.aprovacaoGestor() != null ? request.aprovacaoGestor() : false);
        occurrence.setObservacaoAnalista1(request.observacaoAnalista1());
        occurrence.setMotivoAnalista(request.motivoAnalista());
        occurrence.setAprovacaoAnalista1(request.aprovacaoAnalista1() != null ? request.aprovacaoAnalista1() : false);
        occurrence.setPrioridade(request.prioridade());
        occurrence.setTipoAtendimento(request.tipoAtendimento());
        occurrence.setDataAtendimento(request.dataAtendimento().orElse(null));
        occurrence.setDataFim(request.dataFim().orElse(null));
        occurrence.setAtendenteRHlocal(request.atendenteRHlocal().orElse(null));
        occurrence.setAtendenteRHmatriz(request.atendenteRHmatriz().orElse(null));
        occurrence.setCompetencia(request.competencia().orElse(null));
        occurrence.setConversaChat(request.conversaChat().orElse(null));
        occurrence.setDescricaoOcorrencia(request.descricaoOcorrencia().orElse(null));
        occurrence.setRespostaEmpregado(request.respostaEmpregado().orElse(null));
        occurrence.setJustificativaOcorrencia(request.justificativaOcorrencia().orElse(null));
        occurrence.setPertinente(request.pertinente().orElse(null));
        occurrence.setTemperatura(request.temperatura().orElse(null));
        if (request.meiosComunicacao().isPresent()) {
            occurrence.setMeiosComunicacao(request.meiosComunicacao().get());
        }


        OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);

        sequence.setDocumentId(occurrenceSaved.getId());

        sequenceRepository.save(sequence);


        // Processar extratoColaborador se presente

        if (request.extratoColaborador().isPresent()) {

            FileMetadata metadata = storeExtrato(request, occurrence);

            occurrenceSaved.setExtratoColaborador(metadata);

        }


        // Processar anexoTicket se presente

        if (request.anexoTicket().isPresent()) {

            FileMetadata anexoTicketMetadata = storeFile(request.anexoTicket().get(), "ocf/anexos_ticket",

                    occurrenceSaved.getId() + "_ticket_" + System.currentTimeMillis() + "_" +

                            request.anexoTicket().get().getOriginalFilename(), 1);

            occurrenceSaved.setAnexoTicket(anexoTicketMetadata);

        }


        saveOccurrence(occurrenceSaved);


        // Processar itens reclamados

        if (request.itemsreclamados() != null && !request.itemsreclamados().isEmpty()) {

            processItemsReclamados(request.itemsreclamados(), occurrenceSaved.getId());

        }


        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());

        String moduleName = module.getName();

        // Notificar baseado na etapa inicial definida pelo tipo de fluxo
        var regional = employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                ? employee.getHierarchy().getRegionalId().intValue() : null;
        notifyNextStep(module, regional, etapaInicial, occurrenceCode, moduleName);


        return new CreateOccurrenceResponseDTO("Ocorrência criada com sucesso.", 201, occurrenceSaved.getId(), occurrenceSaved.getCodeID());

    }


    public GenericMessage createDerivedOccurrence(Object dto, String userId) throws InterruptedException {

        CreateOccurrenceResponseDTO result = createDerivedOccurrenceWithDetails(dto, userId);

        return new GenericMessage(result.message(), result.status());

    }


    public CreateOccurrenceResponseDTO createDerivedOccurrenceWithDetails(Object dto, String userId) throws InterruptedException {

        OccurrenceFFDTO request = (OccurrenceFFDTO) dto;


        // Safety: evitar duplicidade por número de protocolo (ex.: chamadas incorretas do frontend)

        if (request.numeroDoProtocolo() != null && !request.numeroDoProtocolo().isBlank()) {

            if (repository.existsByNumeroDoProtocolo(request.numeroDoProtocolo())) {

                throw new ModuleFailure("Já existe uma ocorrência criada para o protocolo: " + request.numeroDoProtocolo() + ". Use o endpoint de movimentação do ticket.");

            }

        }


        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        EmployeeDTO employee = getEmployee.getEmployeeByMatricula(request.matriculaReclamante());

        // Dados do colaborador obtidos

        // Sobrescrever o e-mail do colaborador com o valor enviado pelo frontend, se presente

        if (request.email() != null && !request.email().isBlank()) {

            employee.setEmail_comercial(request.email());

        }

        // Sobrescrever o telefone do colaborador com o valor enviado pelo frontend, se presente

        if (request.telefone() != null && !request.telefone().isBlank()) {

            employee.setTelefone(request.telefone());

        }

        SimpleUser applicant = userService.getUserById(userId)

                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));


        OcorrenciaFF occurrence = new OcorrenciaFF();

        StepLog log = createStepLog(

                UUID.fromString(userId),

                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome)

                        .orElse("Início da Solicitação"),

                1,

                request.observacao(),

                null

        );

        var sequenciaID = new DatabaseSequenceFF();

        DatabaseSequenceFF sequence = sequenceRepository.save(sequenciaID);


        // Criação e adição das informações na Ocorrência Derivada

        occurrence.setSolicitante(applicant);

        occurrence.setColaborador(employee);

        occurrence.setModulo_id(module.getId());

        occurrence.setOrigem(request.origem());

        occurrence.setDescricao(request.observacao());

        occurrence.setRegionalId(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                        ? employee.getHierarchy().getRegionalId().intValue()
                        : null
        );
        occurrence.setRegionalNome(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                        ? employee.getHierarchy().getRegionalNome()
                        : ""
        );
        occurrence.setProjectId(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                        ? employee.getHierarchy().getProjetoId().intValue()
                        : 0
        );
        occurrence.setProjectName(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                        ? employee.getHierarchy().getProjetoNome()
                        : ""
        );

        occurrence.setData_Ocorrencia(request.data_ocorrencia());

        occurrence.setContratoId(employee.getContrato().getRateio());


        // DIFERENÇA PRINCIPAL: Manter na etapa 1 com status PENDENTE

        occurrence.setStatus(DocumentStatus.PENDENTE);

        occurrence.setCurrentStep(1); // Sempre etapa 1

        occurrence.setTipoFluxo(request.tipoFluxo() != null && !request.tipoFluxo().isBlank() ? request.tipoFluxo() : "financeiro");


        occurrence.setCreated_at(Date.from(Instant.now()));

        occurrence.setDadosBancarios(employee.getDadosBancarios());

        occurrence.setStepLog(List.of(log));

        occurrence.setCodeID(sequence.getId());

        occurrence.setSituacao(DocumentStatus.ANDAMENTO);


        // Sempre salve a causa enviada pelo usuário

        occurrence.setCausas(request.causa());


        // Executar bloco financeiro apenas se for tipo financeiro

        String tipoFluxo = occurrence.getTipoFluxo();

        if ("financeiro".equalsIgnoreCase(tipoFluxo)) {

            occurrence.setDadosCorretos(request.dadosCorretos());

            occurrence.setBancoDadosErrado(true);

            occurrence.setDescricao("Erro no cadastro da conta bancária.");

        } else {

            // Para outros tipos de fluxo, manter a descrição original

            occurrence.setDescricao(request.observacao());

        }


        // Novos campos adicionados

        occurrence.setNumeroDoProtocolo(request.numeroDoProtocolo());

        occurrence.setTelefone(request.telefone());

        occurrence.setTelefoneDeContato(request.telefoneDeContato());

        occurrence.setTipoDeBeneficio(request.tipoDeBeneficio());

        occurrence.setMotivo(request.motivo());

        occurrence.setValorContestado(request.valorContestado());

        occurrence.setValorPagarDescontar(request.valorPagarDescontar());

        occurrence.setAprovacaoGestor(request.aprovacaoGestor() != null ? request.aprovacaoGestor() : false);

        occurrence.setObservacaoAnalista1(request.observacaoAnalista1());

        occurrence.setMotivoAnalista(request.motivoAnalista());

        occurrence.setAprovacaoAnalista1(request.aprovacaoAnalista1() != null ? request.aprovacaoAnalista1() : false);

        occurrence.setPrioridade(request.prioridade());

        occurrence.setTipoAtendimento(request.tipoAtendimento());


        // Novos campos adicionados

        occurrence.setDataAtendimento(request.dataAtendimento().orElse(null));

        occurrence.setDataFim(request.dataFim().orElse(null));

        occurrence.setAtendenteRHlocal(request.atendenteRHlocal().orElse(null));

        occurrence.setAtendenteRHmatriz(request.atendenteRHmatriz().orElse(null));

        occurrence.setCompetencia(request.competencia().orElse(null));

        occurrence.setConversaChat(request.conversaChat().orElse(null));

        occurrence.setDescricaoOcorrencia(request.descricaoOcorrencia().orElse(null));

        occurrence.setRespostaEmpregado(request.respostaEmpregado().orElse(null));

        occurrence.setJustificativaOcorrencia(request.justificativaOcorrencia().orElse(null));

        occurrence.setTemperatura(request.temperatura().orElse(null));


        OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);

        sequence.setDocumentId(occurrenceSaved.getId());

        sequenceRepository.save(sequence);


        // Processar extratoColaborador se presente

        if (request.extratoColaborador().isPresent()) {

            FileMetadata metadata = storeExtrato(request, occurrence);

            occurrenceSaved.setExtratoColaborador(metadata);

        }


        // Processar anexoTicket se presente

        if (request.anexoTicket().isPresent()) {

            FileMetadata anexoTicketMetadata = storeFile(request.anexoTicket().get(), "ocf/anexos_ticket",

                    occurrenceSaved.getId() + "_ticket_" + System.currentTimeMillis() + "_" +

                            request.anexoTicket().get().getOriginalFilename(), 1);

            occurrenceSaved.setAnexoTicket(anexoTicketMetadata);

        }


        saveOccurrence(occurrenceSaved);


        // Processar itens reclamados

        if (request.itemsreclamados() != null && !request.itemsreclamados().isEmpty()) {

            processItemsReclamados(request.itemsreclamados(), occurrenceSaved.getId());

        }


        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());

        String moduleName = module.getName(); // Utilizar o nome do módulo obtido dinamicamente

        String etapaNome = getEtapaNome(module, 1); // Obter nome da etapa 1

        // Notificar etapa 1 (já que a ocorrência fica na etapa 1)

        var regional = employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                ? employee.getHierarchy().getRegionalId().intValue() : null;
        notifyAnyStep(module, regional, 1,

                "Nova ocorrência criada",

                "Uma nova ocorrência pendente foi criada e está aguardando análise na " + etapaNome,

                occurrenceCode, moduleName);


        return new CreateOccurrenceResponseDTO("Ocorrência pendente criada com sucesso.", 201, occurrenceSaved.getId(), occurrenceSaved.getCodeID());

    }


    /**
     * Obtém o nome da etapa através do ID da etapa no módulo
     */

    private String getEtapaNome(Modulo module, int etapaId) {

        return module.getConfigEtapas().stream()

                .filter(etapa -> etapa.getEtapa() == etapaId)

                .findFirst()

                .map(StepModule::getNome)

                .orElse("Etapa " + etapaId);

    }


    public CreateFinalizedOccurrenceResponseDTO createFinalizedOccurrence(OccurrenceFFDTO request, String userId, List<String> canais, String respostaEmpregado) throws InterruptedException {

        if (request.numeroDoProtocolo() != null && !request.numeroDoProtocolo().isBlank()) {

            if (repository.existsByNumeroDoProtocolo(request.numeroDoProtocolo())) {

                throw new ModuleFailure("Já existe uma ocorrência criada para o protocolo: " + request.numeroDoProtocolo() + ".");

            }

        }


        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        EmployeeDTO employee = getEmployee.getEmployeeByMatricula(request.matriculaReclamante());

        if (request.email() != null && !request.email().isBlank()) {

            employee.setEmail_comercial(request.email());

        }

        if (request.telefone() != null && !request.telefone().isBlank()) {

            employee.setTelefone(request.telefone());

        }


        SimpleUser applicant = userService.getUserById(userId)

                .orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));


        var sequenciaID = new DatabaseSequenceFF();

        DatabaseSequenceFF sequence = sequenceRepository.save(sequenciaID);


        OcorrenciaFF occurrence = new OcorrenciaFF();

        occurrence.setSolicitante(applicant);

        occurrence.setColaborador(employee);

        occurrence.setModulo_id(module.getId());

        occurrence.setOrigem(request.origem());

        occurrence.setDescricao(request.observacao());

        occurrence.setData_Ocorrencia(request.data_ocorrencia());

        occurrence.setRegionalId(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                        ? employee.getHierarchy().getRegionalId().intValue()
                        : null
        );
        occurrence.setRegionalNome(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                        ? employee.getHierarchy().getRegionalNome()
                        : ""
        );
        occurrence.setProjectId(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                        ? employee.getHierarchy().getProjetoId().intValue()
                        : 0
        );
        occurrence.setProjectName(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                        ? employee.getHierarchy().getProjetoNome()
                        : ""
        );

        occurrence.setContratoId(employee.getContrato().getRateio());

        occurrence.setStatus(DocumentStatus.APROVADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        occurrence.setCreated_at(Date.from(Instant.now()));

        occurrence.setFinal_date(new Date());

        occurrence.setDataFim(LocalDateTime.now());

        occurrence.setDadosBancarios(employee.getDadosBancarios());

        occurrence.setCodeID(sequence.getId());

        occurrence.setNumeroDoProtocolo(request.numeroDoProtocolo());

        occurrence.setCausas(request.causa());

        occurrence.setTelefone(request.telefone());

        occurrence.setTelefoneDeContato(request.telefoneDeContato());

        occurrence.setTipoDeBeneficio(request.tipoDeBeneficio());

        occurrence.setMotivo(request.motivo());

        occurrence.setValorContestado(request.valorContestado());

        occurrence.setValorPagarDescontar(request.valorPagarDescontar());

        occurrence.setTipoFluxo(request.tipoFluxo());

        occurrence.setAprovacaoGestor(Boolean.TRUE.equals(request.aprovacaoGestor()));

        occurrence.setObservacaoAnalista1(request.observacaoAnalista1());

        occurrence.setMotivoAnalista(request.motivoAnalista());

        occurrence.setAprovacaoAnalista1(Boolean.TRUE.equals(request.aprovacaoAnalista1()));

        occurrence.setPrioridade(request.prioridade());

        occurrence.setTipoAtendimento(request.tipoAtendimento());

        occurrence.setDataAtendimento(request.dataAtendimento().orElse(null));

        occurrence.setAtendenteRHlocal(request.atendenteRHlocal().orElse(null));

        occurrence.setAtendenteRHmatriz(request.atendenteRHmatriz().orElse(null));

        occurrence.setCompetencia(request.competencia().orElse(null));

        occurrence.setConversaChat(request.conversaChat().orElse(null));

        occurrence.setDescricaoOcorrencia(request.descricaoOcorrencia().orElse(null));

        occurrence.setRespostaEmpregado(request.respostaEmpregado().orElse(null));

        occurrence.setJustificativaOcorrencia(request.justificativaOcorrencia().orElse(null));

        occurrence.setPertinente(request.pertinente().orElse(null));


        // Mapear meiosComunicacao se fornecido

        if (request.meiosComunicacao().isPresent()) {

            occurrence.setMeiosComunicacao(request.meiosComunicacao().get());

        }


        StepLog log = createStepLog(

                UUID.fromString(userId),

                "Finalização",

                0,

                request.observacao(),

                request.grupoResponsavel().orElse(null)

        );

        occurrence.setStepLog(new ArrayList<>());

        addLog(occurrence, log);


        OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);

        sequence.setDocumentId(occurrenceSaved.getId());

        sequenceRepository.save(sequence);


        if (request.extratoColaborador().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(request.extratoColaborador().get(), "ocf/extratos", occurrenceSaved.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 1);

            occurrenceSaved.setExtratoColaborador(metadata);

        }

        if (request.comprovanteDePagamento().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(request.comprovanteDePagamento().get(), "ocf/comprovantes", occurrenceSaved.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

            occurrenceSaved.setComprovantePagamento(metadata);

        }

        if (request.anexoTicket().isPresent()) {

            FileMetadata anexoTicketMetadata = attachmentService.storeFile(request.anexoTicket().get(), "ocf/anexos_ticket", occurrenceSaved.getId() + "_ticket_" + System.currentTimeMillis() + "_" + request.anexoTicket().get().getOriginalFilename(), 1);

            occurrenceSaved.setAnexoTicket(anexoTicketMetadata);

        }

        if (request.itemsreclamados() != null && !request.itemsreclamados().isEmpty()) {

            processItemsReclamados(request.itemsreclamados(), occurrenceSaved.getId());

        }


        saveOccurrence(occurrenceSaved);

        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());


        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());

        String moduleName = module.getName();

        notifyApplicant(occurrenceSaved.getStepLog().stream().findFirst().get(),

                "Sua solicitação foi finalizada.",

                "<br><br>\n\n        Sua ocorrencia no " + moduleName + " foi aprovada e finalizada na criação.<br><br>\n",

                occurrenceCode,

                moduleName);


        // Notificação de colaborador removida - usar CollaboratorNotificationService


        return new CreateFinalizedOccurrenceResponseDTO(

                "Ocorrência criada, aprovada e finalizada com sucesso.",

                201,

                occurrenceSaved.getId(),

                occurrenceSaved.getCodeID()

        );

    }

    @Override

    public GenericMessage moveToNextStep(String occurrenceId, Object dto, String userId) {

        return null;

    }


    @Override

    public GenericMessage requestReview(String occurrenceId, Object dto, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);

        if (occurrence.getCurrentStep() <= 1) {

            String primeiraEtapaNome = getEtapaNome(moduleService.getModuleByID(UUID.fromString(modulo_id)), 1);

            throw new ModuleFailure("Não é possível voltar além da " + primeiraEtapaNome + ".");

        }


        occurrence.setStatus(DocumentStatus.REVISÃO);

        occurrence.setSituacao(DocumentStatus.ANDAMENTO);

        var log = createStepLog(UUID.fromString(userId),

                "Solicitação de Revisão", occurrence.getCurrentStep(), ((OccurrenceFFDTO) dto).observacao(), null);

        addLog(occurrence, log);


        // Regra: se não for financeiro e estiver na etapa 3, voltar para a etapa 1

        String tipoFluxo = occurrence.getTipoFluxo();

        boolean isFinanceiro = tipoFluxo != null && tipoFluxo.equalsIgnoreCase("financeiro");


        // Dados para notificação

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        if (!isFinanceiro && occurrence.getCurrentStep() == 3) {

            // Notificar etapa 1 diretamente

            String etapaNome = getEtapaNome(module, 1);

            var regional = occurrence.getColaborador().getHierarchy() != null && occurrence.getColaborador().getHierarchy().getRegionalId() != null
                    ? occurrence.getColaborador().getHierarchy().getRegionalId().intValue() : null;
            notifyAnyStep(module, regional, 1, "Solicitação de Revisão", "Ocorrência enviada para revisão na " + etapaNome + ".", occurrenceCode, moduleName);

            occurrence.setCurrentStep(1);

        } else {

            // Comportamento padrão: voltar uma etapa e notificar a etapa anterior

            notifyPreviousStep(occurrence.getStepLog().getLast(), occurrence.getCurrentStep(), occurrenceCode, moduleName);

            occurrence.setCurrentStep(occurrence.getCurrentStep() - 1);

        }


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        return new GenericMessage("Revisão solicitada.", 200);

    }


    @Override

    public GenericMessage editAfterReview(String occurrenceId, Object dto, String userId, boolean isEdit) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);


        if (occurrence == null) {

            throw new ModuleNotFoundFailure("Ocorrência não encontrada.");

        }


        if (!isEdit && !DocumentStatus.REVISÃO.equals(occurrence.getStatus())) {

            throw new ModuleFailure(

                    "Só é possível editar ocorrências que estão em revisão.");

        }


        updateOccurrenceAfterReview(occurrence, dto, userId, isEdit);


        // Quando edit=true, consideramos que os dados foram corrigidos e a ocorrência deve seguir o fluxo normal

        if (isEdit) {

            // Reabrir e enviar para a etapa correta conforme o tipoFluxo

            String tipoFluxo = occurrence.getTipoFluxo();

            boolean isFinanceiro = tipoFluxo != null && tipoFluxo.equalsIgnoreCase("financeiro");

            int proximaEtapa = isFinanceiro ? 2 : 3;

            occurrence.setStatus(DocumentStatus.ABERTO);

            occurrence.setSituacao(DocumentStatus.ANDAMENTO);

            occurrence.setCurrentStep(proximaEtapa);


            // Notificar próximo responsável

            String occurrenceCode = String.valueOf(occurrence.getCodeID());

            Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

            String moduleName = module.getName();
            var regional = occurrence.getColaborador().getHierarchy() != null && occurrence.getColaborador().getHierarchy().getRegionalId() != null
                    ? occurrence.getColaborador().getHierarchy().getRegionalId().intValue() : null;
            notifyNextStep(module, regional, proximaEtapa, occurrenceCode, moduleName);

        }


        saveOccurrence(occurrence);


        return new GenericMessage("Ocorrência editada com sucesso após revisão.", 200);

    }


    @Override

    public GenericMessage rejectOccurrence(String occurrenceId, @Nullable Object dto, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);

        if (occurrence.getStatus() == DocumentStatus.APROVADO)

            throw new ModuleFailure("Não é possível rejeitar uma solicitação já finalizada.");


        occurrence.setStatus(DocumentStatus.REJEITADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        occurrence.setFinal_date(Date.from(Instant.now()));

        var log = createStepLog(UUID.fromString(userId),


                "Ocorrência Rejeitada", occurrence.getCurrentStep(), dto != null ? ((OccurrenceFFDTO) dto).observacao() : "", dto != null ? ((OccurrenceFFDTO) dto).grupoResponsavel().orElse(null) : null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(),

                "Sua solicitação foi negada.",

                "<br><br>\n" +

                        "\n" +

                        "        A sua " + moduleName + " foi negada.<br><br>\n" +

                        "\n" +

                        "        Sua ocorrência foi negada! Acesse o sistema para mais detalhes.<br><br>\n",

                occurrenceCode,

                moduleName);


        return new GenericMessage("Ocorrência rejeitada com sucesso.", 200);

    }


    @Override

    public GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId) {

        return finalizeOccurrence(occurrenceId, dto, userId, null, null);

    }


    public GenericMessage finalizeOccurrence(String occurrenceId, Object dto, String userId, List<String> canais, String respostaEmpregado) {

        OccurrenceFFDTO request = (OccurrenceFFDTO) dto;

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);


        // Processar comprovante de pagamento se fornecido (agora opcional)

        if (request.comprovanteDePagamento().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(request.comprovanteDePagamento().get(), "ocf/comprovantes", occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

            occurrence.setComprovantePagamento(metadata);

        }

        SimpleUser user = userService.getUserById(userId)

                .orElseThrow(() -> new ModuleBadRequest("Usuário atual não foi encontrado no nosso sistema."));


        occurrence.setStatus(DocumentStatus.APROVADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        occurrence.setFinal_date(new Date());

        // Garantir o preenchimento de dataFim com o momento da aprovação

        occurrence.setDataFim(LocalDateTime.now());


        // Processar todos os campos do FormData

        occurrence.setCausas(request.causa());

        occurrence.setDescricao(request.observacao());

        occurrence.setPrioridade(request.prioridade());

        occurrence.setTipoFluxo(request.tipoFluxo());

        occurrence.setTipoAtendimento(request.tipoAtendimento());


        // Processar campos que estavam faltando

        occurrence.setMotivo(request.motivo());

        occurrence.setTipoDeBeneficio(request.tipoDeBeneficio());


        // Tratar data_ocorrencia que pode estar chegando como null

        if (request.data_ocorrencia() != null) {

            occurrence.setData_Ocorrencia(request.data_ocorrencia());

        }


        // Processar campos opcionais

        request.descricaoOcorrencia().ifPresent(occurrence::setDescricaoOcorrencia);

        request.atendenteRHlocal().ifPresent(occurrence::setAtendenteRHlocal);

        request.atendenteRHmatriz().ifPresent(occurrence::setAtendenteRHmatriz);

        request.competencia().ifPresent(occurrence::setCompetencia);

        request.dataAtendimento().ifPresent(occurrence::setDataAtendimento);

        request.dataFim().ifPresent(occurrence::setDataFim);

        request.conversaChat().ifPresent(occurrence::setConversaChat);

        request.respostaEmpregado().ifPresent(occurrence::setRespostaEmpregado);

        request.justificativaOcorrencia().ifPresent(occurrence::setJustificativaOcorrencia);

        request.pertinente().ifPresent(occurrence::setPertinente);


        // Mapear meiosComunicacao se fornecido

        if (request.meiosComunicacao().isPresent()) {

            occurrence.setMeiosComunicacao(request.meiosComunicacao().get());

        }


        if (request.extratoColaborador().isPresent()) {

            FileMetadata extratoMetadata = attachmentService.storeFile(request.extratoColaborador().get(), "ocf/extratos", occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 1);

            occurrence.setExtratoColaborador(extratoMetadata);

        }


        if (request.anexoTicket().isPresent()) {

            FileMetadata anexoTicketMetadata = attachmentService.storeFile(request.anexoTicket().get(), "ocf/anexos_ticket", occurrenceId + "_ticket_" + System.currentTimeMillis() + "_" + request.anexoTicket().get().getOriginalFilename(), 1);

            occurrence.setAnexoTicket(anexoTicketMetadata);

        }


        // Processar itens reclamados se fornecidos

        if (request.itemsreclamados() != null && !request.itemsreclamados().isEmpty()) {

            processItemsReclamados(request.itemsreclamados(), occurrence.getId());

        }

        var log = createStepLog(UUID.fromString(userId),

                "Finalização", 0, ((OccurrenceFFDTO) dto).observacao(), ((OccurrenceFFDTO) dto).grupoResponsavel().orElse(null));

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Atualizar status do chatwoot na tabela tb_ticket_alodp usando JdbcTemplate

        try {

            Long codeID = occurrence.getCodeID();

            if (codeID != null) {

                // Usar JdbcTemplate diretamente para evitar triggers problemáticos

                String sql = "UPDATE tb_ticket_alodp SET status_chatwoot = ? WHERE id_ocorrencia = ?";

                int updatedRows = jdbcTemplate.update(sql, 1, codeID);

                if (updatedRows > 0) {

                } else {

                }

            }

        } catch (Exception e) {

            // Não falha a operação principal se houver erro na atualização do chatwoot

        }


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(),

                "Sua solicitação foi finalizada.",

                "<br><br>\n" +

                        "\n" +

                        "        Sua " + moduleName + " foi aprovada.<br><br>\n" +

                        "\n" +

                        "        Sua ocorrência foi aprovada.<br><br>\n",

                occurrenceCode,

                moduleName);


        // Notificação de colaborador removida - usar CollaboratorNotificationService


        return new GenericMessage("Ocorrência finalizada com sucesso.", 200);

    }


    // Método removido - usar CollaboratorNotificationService


    /**
     * Faz o parsing dos canais de notificação, tratando tanto array quanto string única
     */

    private List<String> parseCanaisNotificacao(List<String> canais) {

        if (canais == null || canais.isEmpty()) {

            return new ArrayList<>();

        }


        List<String> canaisProcessados = new ArrayList<>();


        for (String canal : canais) {

            if (canal == null || canal.trim().isEmpty()) {

                continue;

            }


            // Se o canal contém vírgulas ou colchetes, é uma string que representa um array

            if (canal.contains(",") || canal.contains("[") || canal.contains("]")) {

                // Remove colchetes e quebra por vírgulas

                String canalLimpo = canal.replaceAll("[\\[\\]]", "").trim();

                String[] canaisArray = canalLimpo.split(",");


                for (String c : canaisArray) {

                    String canalProcessado = c.trim().replaceAll("\"", "");

                    if (!canalProcessado.isEmpty()) {

                        canaisProcessados.add(canalProcessado);

                    }

                }

            } else {

                // Canal simples, adiciona diretamente

                canaisProcessados.add(canal.trim());

            }

        }


        return canaisProcessados;

    }


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    /**
     * Envia notificação de resposta para o colaborador usando os dados do formulário
     * e os canais especificados

     */

    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    /**
     * Formata o telefone para o padrão internacional do WhatsApp (55 + DDD + número)
     */

    private String formatarTelefoneParaWhatsApp(String telefone) {

        if (telefone == null || telefone.trim().isEmpty()) {

            return telefone;

        }


        // Remove todos os caracteres não numéricos

        String apenasNumeros = telefone.replaceAll("[^0-9]", "");


        // Se já começa com 55, retorna como está

        if (apenasNumeros.startsWith("55")) {

            return apenasNumeros;

        }


        // Se tem 11 dígitos (DDD + 9 dígitos), adiciona 55

        if (apenasNumeros.length() == 11) {

            return "55" + apenasNumeros;

        }


        // Se tem 10 dígitos (DDD + 8 dígitos), adiciona 55

        if (apenasNumeros.length() == 10) {

            return "55" + apenasNumeros;

        }


        // Para outros casos, adiciona 55 no início

        return "55" + apenasNumeros;

    }


    /**
     * Método público para obter ocorrência por ID (para uso no controller)
     */

    public OcorrenciaFF getOccurrenceById(String id) {

        return getOccurrenceInternal(id);

    }


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    // Método removido - usar CollaboratorNotificationService


    /**
     * Envia notificação padrão por email (sobrecarga para compatibilidade)
     */

    // Método removido - usar CollaboratorNotificationService
    private void sendStandardEmailNotification_REMOVED(String email, OcorrenciaFF occurrence) {

        try {

            String nomeColaborador = occurrence.getColaborador() != null ? occurrence.getColaborador().getName() : "Colaborador";

            String subject = "Ocorrência Encaminhada - #" + occurrence.getCodeID();


            // Mensagem padrão para email

            String mensagemPadrao = String.format(

                    "ENGEMAN INFORMA:<br/><br/>" +

                            "Olá, <strong>%s</strong>!<br/><br/>" +

                            "Seu chamado '<strong>%s</strong>', aberto no SAC Engeman em '<strong>%s</strong>' está sendo encaminhado para análise da equipe da matriz da Engeman, que tem mais recursos e condições para avaliar com precisão.<br/><br/>" +

                            "Pedimos sua paciência e compreensão.<br/><br/>" +

                            "Estamos fazendo o nosso melhor para resolver sua questão o quanto antes.<br/><br/>" +

                            "Se precisar, pode continuar entrando em contato por aqui.",

                    nomeColaborador,

                    occurrence.getCodeID(),

                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            );


            // Layout HTML para email (sem DOCTYPE pois será processado pelo template)

            String body = String.format(

                    "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;'>" +

                            "<div style='background: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>" +

                            "<h1 style='margin: 0 0 10px 0;'>📋 Ocorrência Encaminhada</h1>" +

                            "<p style='margin: 0; font-size: 18px;'>Protocolo: <strong>#%d</strong></p>" +

                            "</div>" +

                            "<div style='background: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px;'>" +

                            "<div style='background: white; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #007bff;'>" +

                            "<h3 style='margin: 0 0 15px 0; color: #007bff;'>📋 Informações da Ocorrência</h3>" +

                            "<p style='margin: 5px 0;'><strong>Protocolo:</strong> #%d</p>" +

                            "<p style='margin: 5px 0;'><strong>Data de Abertura:</strong> %s</p>" +

                            "<p style='margin: 5px 0;'><strong>Status:</strong> Encaminhada para Matriz</p>" +

                            "</div>" +

                            "<div style='background: #e9ecef; padding: 15px; border-radius: 5px; margin: 15px 0; font-style: italic;'>" +

                            "%s" +

                            "</div>" +

                            "<div style='text-align: center; margin-top: 20px; color: #6c757d; font-size: 12px;'>" +

                            "<p>Esta notificação foi gerada automaticamente pelo sistema Indux.</p>" +

                            "</div>" +

                            "</div>" +

                            "</div>",

                    occurrence.getCodeID(),

                    occurrence.getCodeID(),

                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                    mensagemPadrao

            );


            mailSender.sendGenericEmail(email, subject, body, null);

        } catch (Exception e) {

        }

    }


    /**
     * Envia notificação padrão por WhatsApp
     */

    // Método removido - usar CollaboratorNotificationService
    private void sendStandardWhatsAppNotification_REMOVED(String telefone, String nome, OcorrenciaFF occurrence) {

        try {

            // Mensagem padrão para WhatsApp

            String mensagemPadrao = String.format(

                    "ENGEMAN INFORMA:\n" +

                            "Olá, %s!\n\n" +

                            "Seu chamado '%s', aberto no SAC Engeman em '%s' está sendo encaminhado para análise da equipe da matriz da Engeman, que tem mais recursos e condições para avaliar com precisão.\n\n" +

                            "Pedimos sua paciência e compreensão.\n\n" +

                            "Estamos fazendo o nosso melhor para resolver sua questão o quanto antes.\n\n" +

                            "Se precisar, pode continuar entrando em contato por aqui.",

                    nome,

                    occurrence.getCodeID(),

                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            );


            // Layout de texto simples para WhatsApp

            String message = String.format(

                    "📋 *Ocorrência Encaminhada*\n\n" +

                            "%s\n\n" +

                            "%s\n\n" +

                            "Atenciosamente,\n" +

                            "Equipe de Atendimento",

                    occurrence.getCodeID(),

                    mensagemPadrao,

                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),

                    mensagemPadrao

            );


            String telefoneFormatado = formatarTelefoneParaWhatsApp(telefone);


            whatsappSender.sendMessage(telefoneFormatado, message);

        } catch (Exception e) {

        }

    }


    /**
     * Envia notificação padrão por SMS
     */

    // Método removido - usar CollaboratorNotificationService
    private void sendStandardSMSNotification_REMOVED(String telefone, String nome, OcorrenciaFF occurrence) {

        try {

            // Mensagem padrão para SMS

            String mensagemPadrao = String.format(

                    "ENGEMAN INFORMA: Olá, %s! Seu chamado '%s', aberto no SAC Engeman em '%s' está sendo encaminhado para análise da equipe da matriz da Engeman, que tem mais recursos e condições para avaliar com precisão. Pedimos sua paciência e compreensão. Estamos fazendo o nosso melhor para resolver sua questão o quanto antes. Se precisar, pode continuar entrando em contato por aqui.",

                    nome,

                    occurrence.getCodeID(),

                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            );


            String message = String.format(

                    "%s Protocolo: #%d - Equipe de Atendimento",

                    mensagemPadrao, occurrence.getCodeID()

            );


            // Aqui você pode integrar com o serviço de SMS

        } catch (Exception e) {

        }

    }


    // Método removido - usar CollaboratorNotificationService


    /**
     * Envia notificação de resposta por SMS para o colaborador
     */

    // Método removido - usar CollaboratorNotificationService
    private void sendEmployeeResponseSMSNotification_REMOVED(String telefone, String nome, String resposta, OcorrenciaFF occurrence) {

        try {

            String message = String.format(

                    "Olá %s! Sua ocorrência #%d foi finalizada. Resposta: %s. Protocolo: #%d",

                    nome, occurrence.getCodeID(), resposta, occurrence.getCodeID()

            );


            // Aqui você pode integrar com o serviço de SMS

        } catch (Exception e) {

        }

    }


    @Override

    public OcorrenciaFF getOccurrence(String id) {

        OcorrenciaFF occurrence = getOccurrenceInternal(id);

        return occurrence;

    }


    @Override

    public Page<OcorrenciaFF> listOccurrencesByUser(UUID userId, Pageable pageable) {

        Pageable sortedPageable = PageRequest.of(

                pageable.getPageNumber(),

                pageable.getPageSize(),

                Sort.by(

                        Sort.Order.asc("statusOrder"),

                        Sort.Order.asc("situacaoOrder")));


        return repository.findBySolicitanteId(userId, sortedPageable);

    }


    @Override

    public Page<OcorrenciaFF> listOccurrencesAllowed(UUID userId, Pageable pageable) {

        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));

        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);


        Set<Integer> etapas = module.permissoesUsuario().etapasPermitidas();

        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();

        Set<Integer> projetos = module.permissoesUsuario().projetosPermitidos();


        List<DocumentStatus> rejectStatus = List.of(

                DocumentStatus.REJEITADO,

                DocumentStatus.APROVADO);


        boolean allRegionais = regionais.contains(0);

        boolean allProjetos = projetos.contains(0);


        Pageable sortedPageable = PageRequest.of(

                pageable.getPageNumber(),

                pageable.getPageSize(),

                Sort.by(

                        Sort.Order.asc("statusOrder"),

                        Sort.Order.asc("situacaoOrder")));


        Page<OcorrenciaFF> result;

        if (allRegionais && allProjetos) {

            result = repository.findByCurrentStepInAndStatusNotIn(

                    etapas, rejectStatus, sortedPageable);

        } else if (allRegionais) {

            result = repository.findByCurrentStepInAndProjectIdInAndStatusNotIn(

                    etapas, projetos, rejectStatus, sortedPageable);

        } else if (allProjetos) {

            result = repository.findByCurrentStepInAndRegionalIdInAndStatusNotIn(

                    etapas, regionais, rejectStatus, sortedPageable);

        } else {

            result = repository.findByCurrentStepInAndRegionalIdInAndProjectIdInAndStatusNotIn(

                    etapas, regionais, projetos, rejectStatus, sortedPageable);

        }


        // Popular teamId e teamName para cada ocorrência

        result.getContent().forEach(this::populateTeamId);


        return result;

    }


    @Override

    public Page<OcorrenciaFF> listAll(Pageable pageable) {

        return repository.findAll(pageable);

    }


    @Override

    public Page<OcorrenciaFF> listAllAllowed(UUID userId, Pageable pageable) {

        Modulo modulo = moduleService.getModuleByID(UUID.fromString(modulo_id));

        ModuleResponseDTO module = moduleResponseMapper.toDTO(modulo, userId, false, false);


        Set<Integer> regionais = module.permissoesUsuario().regionaisPermitidas();

        Set<Integer> projetos = module.permissoesUsuario().projetosPermitidos();


        boolean allRegionais = regionais.contains(0);

        boolean allProjetos = projetos.contains(0);


        Pageable sortedPageable = PageRequest.of(

                pageable.getPageNumber(),

                pageable.getPageSize(),

                Sort.by(

                        Sort.Order.asc("statusOrder"),

                        Sort.Order.asc("situacaoOrder")));


        Page<OcorrenciaFF> result;

        if (allRegionais && allProjetos) {

            result = repository.findAll(sortedPageable);

        } else if (allRegionais) {

            result = repository.findByProjectIdIn(projetos, sortedPageable);

        } else if (allProjetos) {

            result = repository.findByRegionalIdIn(regionais, sortedPageable);

        } else {

            result = repository.findByRegionalIdInAndProjectIdIn(regionais, projetos, sortedPageable);

        }


        // Popular teamId para cada ocorrência

        result.getContent().forEach(this::populateTeamId);


        return result;

    }


    /**
     * Popula o teamId e teamName da ocorrência baseado no contrato
     */

    private void populateTeamId(OcorrenciaFF occurrence) {

        try {

            if (occurrence.getColaborador() != null &&

                    occurrence.getColaborador().getContrato() != null &&

                    occurrence.getColaborador().getContrato().getRateio() != null) {


                String contrato = occurrence.getColaborador().getContrato().getRateio().toString();

                Long teamId = getTeamIdByContract(contrato);

                String teamName = getTeamNameByContract(contrato);

                occurrence.setTeamId(teamId);

                occurrence.setTeamName(teamName);

            }

        } catch (Exception e) {

            // Em caso de erro, deixar teamId e teamName como null

            occurrence.setTeamId(null);

            occurrence.setTeamName(null);

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

            return null;

        }

    }


    @Override

    public Page<OcorrenciaFF> searchFilter(Object filter, boolean filterGlobal, String user, Pageable pageable) {

        var filterResolved = (OccurrenceFilter) filter;

        Page<OcorrenciaFF> result;


        // Criar um filtro sem teamIds para a query inicial

        OccurrenceFilter filterWithoutTeamIds = new OccurrenceFilter(

                filterResolved.statuses(),

                filterResolved.situations(),

                filterResolved.currentSteps(),

                filterResolved.createdFrom(),

                filterResolved.createdTo(),

                filterResolved.id(),

                filterResolved.codeId(),

                filterResolved.competencias(),

                filterResolved.regionaisIds(),

                filterResolved.projectsIds(),

                filterResolved.requesterName(),

                filterResolved.stepLogUserId(),

                filterResolved.complainantId(),

                filterResolved.origin(),

                filterResolved.type(),

                filterResolved.tipoFluxo(),

                null, // Remover teamIds da query inicial

                filterResolved.diretoriaIds(),

                filterResolved.superintendenciaIds(),

                filterResolved.regionalIds(),

                filterResolved.setorIds(),

                filterResolved.contratoIds(),

                filterResolved.projetoIds(),

                filterResolved.filialHcmIds()

        );


        // Verificar se há filtros hierárquicos para usar o método apropriado

        boolean hasHierarchicalFilters = (filterResolved.diretoriaIds() != null && !filterResolved.diretoriaIds().isEmpty()) || (filterResolved.superintendenciaIds() != null && !filterResolved.superintendenciaIds().isEmpty()) || (filterResolved.regionalIds() != null && !filterResolved.regionalIds().isEmpty()) || (filterResolved.setorIds() != null && !filterResolved.setorIds().isEmpty()) || (filterResolved.contratoIds() != null && !filterResolved.contratoIds().isEmpty()) || (filterResolved.projetoIds() != null && !filterResolved.projetoIds().isEmpty()) || (filterResolved.filialHcmIds() != null && !filterResolved.filialHcmIds().isEmpty());

        if (hasHierarchicalFilters) {
            // Usar o método com suporte a filtros hierárquicos
            // Para filtros hierárquicos que precisam converter para centro_custos_id
            Set<String> hcmIds = new HashSet<>();
            boolean firstFilter = true;

            // Coletar HCM IDs apenas de filtros que precisam conversão (não incluindo filialHcmIds)
            if (filterResolved.diretoriaIds() != null && !filterResolved.diretoriaIds().isEmpty()) {
                Set<String> diretoriaHcmIds = diretoriaFilterService.getHcmIdsByDiretorias(filterResolved.diretoriaIds());
                if (firstFilter) {
                    hcmIds.addAll(diretoriaHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(diretoriaHcmIds);
                }
            }

            if (filterResolved.superintendenciaIds() != null && !filterResolved.superintendenciaIds().isEmpty()) {
                Set<String> superintendenciaHcmIds = diretoriaFilterService.getHcmIdsBySuperintendencias(filterResolved.superintendenciaIds());
                if (firstFilter) {
                    hcmIds.addAll(superintendenciaHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(superintendenciaHcmIds);
                }
            }

            if (filterResolved.regionalIds() != null && !filterResolved.regionalIds().isEmpty()) {
                Set<String> regionalHcmIds = diretoriaFilterService.getHcmIdsByRegionais(filterResolved.regionalIds());
                if (firstFilter) {
                    hcmIds.addAll(regionalHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(regionalHcmIds);
                }
            }

            if (filterResolved.setorIds() != null && !filterResolved.setorIds().isEmpty()) {
                Set<String> setorHcmIds = diretoriaFilterService.getHcmIdsBySetores(filterResolved.setorIds());
                if (firstFilter) {
                    hcmIds.addAll(setorHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(setorHcmIds);
                }
            }

            if (filterResolved.contratoIds() != null && !filterResolved.contratoIds().isEmpty()) {
                Set<String> contratoHcmIds = diretoriaFilterService.getHcmIdsByContratos(filterResolved.contratoIds());
                if (firstFilter) {
                    hcmIds.addAll(contratoHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(contratoHcmIds);
                }
            }

            if (filterResolved.projetoIds() != null && !filterResolved.projetoIds().isEmpty()) {
                Set<String> projetoHcmIds = diretoriaFilterService.getHcmIdsByProjetos(filterResolved.projetoIds());
                if (firstFilter) {
                    hcmIds.addAll(projetoHcmIds);
                    firstFilter = false;
                } else {
                    // Apply cascade filtering - intersection with existing HCM IDs
                    hcmIds.retainAll(projetoHcmIds);
                }
            }

            // Handle filialHcmIds separately as it's a direct filter, not requiring conversion
            // Convert to list to maintain compatibility with existing code
            List<String> hcmIdsList = new ArrayList<>(hcmIds);

            if (filterGlobal) {
                result = repository.findByFilterWithHierarchicalFilters(filterWithoutTeamIds, pageable, hcmIdsList);
            } else {
                Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
                UUID userID = UUID.fromString(user);
                List<ModulePermission> userPerms = module.getPermissoes().stream().filter(p -> userID.equals(p.getResponsable())).toList();
                List<Integer> regionalAllowed = userPerms.stream().flatMap(p -> p.getRegionais().stream()).distinct().toList();
                List<Integer> projectsAllowed = userPerms.stream().flatMap(p -> p.getProjetos().stream()).distinct().toList();
                List<Integer> stepAllowed = userPerms.stream().flatMap(p -> p.getStepsAllowed().stream()).distinct().toList();
                OccurrenceFilter dto = filterWithoutTeamIds.withRegionalAndProject(regionalAllowed, projectsAllowed, stepAllowed);
                result = repository.findByFilterWithHierarchicalFilters(dto, pageable, hcmIdsList);
            }
        } else {
            // Usar o método normal sem filtros hierárquicos
            if (filterGlobal) {
                result = repository.findByFilter(filterWithoutTeamIds, pageable);
            } else {
                Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
                UUID userID = UUID.fromString(user);
                List<ModulePermission> userPerms = module.getPermissoes().stream().filter(p -> userID.equals(p.getResponsable())).toList();
                List<Integer> regionalAllowed = userPerms.stream().flatMap(p -> p.getRegionais().stream()).distinct().toList();
                List<Integer> projectsAllowed = userPerms.stream().flatMap(p -> p.getProjetos().stream()).distinct().toList();
                List<Integer> stepAllowed = userPerms.stream().flatMap(p -> p.getStepsAllowed().stream()).distinct().toList();
                OccurrenceFilter dto = filterWithoutTeamIds.withRegionalAndProject(regionalAllowed, projectsAllowed, stepAllowed);
                result = repository.findByFilter(dto, pageable);
            }
        }


        // Popular teamId e teamName para cada ocorrência

        result.getContent().forEach(this::populateTeamId);


        // Aplicar filtro por teamIds após popular os dados

        if (filterResolved.teamIds() != null && !filterResolved.teamIds().isEmpty()) {

            List<OcorrenciaFF> filteredContent = result.getContent().stream()

                    .filter(occurrence -> occurrence.getTeamId() != null &&

                            filterResolved.teamIds().contains(occurrence.getTeamId()))

                    .toList();


            // Criar nova página com conteúdo filtrado

            result = new PageImpl<>(filteredContent, pageable, filteredContent.size());

        }


        return result;

    }


    @Override

    public void batchUpdateStatus(BatchStatusDTO ids, UUID user) {

        StepLog logger = createStepLog(

                user,

                "Rejeição em grupo",

                0,

                ids.observacao() != null ? ids.observacao()

                        : "Rejeição de " + ids.itens().size() + " itens sem observação.",

                null);

        List<IdCodeProjection> results = repository.findByIdIn(ids.itens());


        Map<String, Long> codeMap = results.stream()

                .collect(Collectors.toMap(IdCodeProjection::getId, IdCodeProjection::getCodeID));

        for (String id : ids.itens()) {

            Long codeID = codeMap.get(id);

            createOccurrenceLog(logger, id, codeID);

        }


        batchUpdateStatusGlobal(ids.itens(), logger, DocumentStatus.REJEITADO, OcorrenciaFF.class);

    }


    private FileMetadata storeExtrato(OccurrenceFFDTO request, OcorrenciaFF occurrence) {

        String filename = occurrence.getId() + " - " + Instant.now()

                .truncatedTo(ChronoUnit.SECONDS)

                .toString().replace(":", "-");


        return attachmentService.storeFile(request.extratoColaborador().get(), "ocf/extratos", filename, 1);

    }


    private void createOccurrenceLog(StepLog log, String occurrenceID, Long code) {

        var occurrenceLog = new FinanceOccurrenceLog(

                occurrenceID,

                code

        );


        occurrenceLog.setId(UUID.randomUUID());

        occurrenceLog.setCreated_at(log.getCreated_at());

        occurrenceLog.setUser(log.getUser());

        occurrenceLog.setName(log.getName());

        occurrenceLog.setGroup(log.getGroup());

        occurrenceLog.setObservation(log.getObservation());

        occurrenceLog.setFinal_at(log.getFinal_at());


        logService.save(occurrenceLog);

    }


    public GenericMessage approveAnalyst(String occurrenceId, AnalystApprovalDTO approvalDTO, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);


        // Definir aprovação do analista

        occurrence.setAprovacaoAnalista(true);


        // Salvar observação do analista se fornecida

        if (approvalDTO.observacaoAnalista().isPresent()) {

            occurrence.setObservacaoAnalista(approvalDTO.observacaoAnalista().get());

        }


        // Processar evidência do analista se fornecida

        if (approvalDTO.evidenciaAnalista().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(approvalDTO.evidenciaAnalista().get(), "ocf/evidencias", occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 3);

            occurrence.setEvidenciaAnalista(metadata);

        }


        // Finalizar a ocorrência

        occurrence.setStatus(DocumentStatus.APROVADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        occurrence.setFinal_date(new Date());


        // Criar log da aprovação

        var log = createStepLog(UUID.fromString(userId),

                "Aprovação do Analista", 0,

                approvalDTO.observacaoAnalista().orElse("Aprovação do analista realizada"),

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(),

                "Sua solicitação foi aprovada pelo analista.",

                "<br><br>\n" +

                        "\n" +

                        "        Sua " + moduleName + " foi aprovada pelo analista.<br><br>\n" +

                        "\n" +

                        "        Sua ocorrência foi aprovada e finalizada.<br><br>\n",

                occurrenceCode,

                moduleName);


        // NOTIFICAÇÃO INDEPENDENTE: Enviar notificação de resposta para o colaborador

        // Esta notificação é completamente independente da notificação existente que envia para o solicitante

        if (approvalDTO.respostaEmpregado().isPresent() && !approvalDTO.respostaEmpregado().get().trim().isEmpty()) {

            String email = approvalDTO.email().orElse(occurrence.getColaborador().getEmail_comercial());

            String telefone = approvalDTO.telefone().orElse(occurrence.getColaborador().getTelefone());


            // Usar canais fornecidos explicitamente no DTO (não determinados automaticamente)

            List<String> canais = approvalDTO.canais().orElse(new ArrayList<>());


            // Notificação de colaborador removida - usar CollaboratorNotificationService

        }


        return new GenericMessage("Aprovação do analista realizada com sucesso. Ocorrência finalizada.", 200);

    }


    public GenericMessage approveManager(String occurrenceId, ManagerApprovalDTO approvalDTO, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);


        // Processar comprovante de pagamento se fornecido

        FileMetadata metadata = null;

        if (approvalDTO.comprovanteDePagamento().isPresent()) {

            metadata = attachmentService.storeFile(approvalDTO.comprovanteDePagamento().get(), "ocf/comprovantes", occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

        }


        SimpleUser user = userService.getUserById(userId)

                .orElseThrow(() -> new ModuleBadRequest("Usuário atual não foi encontrado no nosso sistema."));


        // Finalizar a ocorrência (mesma lógica do finalizeOccurrence)

        occurrence.setStatus(DocumentStatus.APROVADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        if (metadata != null) {

            occurrence.setComprovantePagamento(metadata);

        }

        occurrence.setFinal_date(new Date());

        // Garantir dataFim no momento da aprovação do gestor

        occurrence.setDataFim(LocalDateTime.now());


        // Salvar a string recebida em causas

        if (approvalDTO.causa().isPresent()) {

            occurrence.setCausas(approvalDTO.causa().get());

        }


        // Salvar observação do gestor se fornecida

        if (approvalDTO.observacaoGestor().isPresent()) {

            occurrence.setObservacaoGestor(approvalDTO.observacaoGestor().get());

        }

        // Salvar novos campos de pagamento se fornecidos

        approvalDTO.valorPagar().ifPresent(occurrence::setValorPagar);

        approvalDTO.competenciaPagar().ifPresent(occurrence::setCompetenciaPagar);

        approvalDTO.dataPagamento().ifPresent(occurrence::setDataPagamento);


        // Criar log da finalização

        var log = createStepLog(UUID.fromString(userId),

                "Finalização pelo Gestor", 0,

                approvalDTO.observacao().orElse("Ocorrência finalizada pelo gestor"),

                approvalDTO.grupoResponsavel().orElse(null));

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Atualizar status do chatwoot na tabela tb_ticket_alodp usando JdbcTemplate

        try {

            Long codeID = occurrence.getCodeID();

            if (codeID != null) {

                // Usar JdbcTemplate diretamente para evitar triggers problemáticos

                String sql = "UPDATE tb_ticket_alodp SET status_chatwoot = ? WHERE id_ocorrencia = ?";

                int updatedRows = jdbcTemplate.update(sql, 1, codeID);

                if (updatedRows > 0) {

                } else {

                }

            }

        } catch (Exception e) {

            // Não falha a operação principal se houver erro na atualização do chatwoot

        }


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(),

                "Sua solicitação foi finalizada.",

                "<br><br>\n" +

                        "\n" +

                        "        Sua " + moduleName + " foi aprovada.<br><br>\n" +

                        "\n" +

                        "        Sua ocorrência foi aprovada.<br><br>\n",

                occurrenceCode,

                moduleName);


        // NOTIFICAÇÃO INDEPENDENTE: Enviar notificação de resposta para o colaborador

        // Esta notificação é completamente independente da notificação existente que envia para o solicitante

        if (approvalDTO.respostaEmpregado().isPresent() && !approvalDTO.respostaEmpregado().get().trim().isEmpty()) {

            String email = approvalDTO.email().orElse(occurrence.getColaborador().getEmail_comercial());

            String telefone = approvalDTO.telefone().orElse(occurrence.getColaborador().getTelefone());


            // Usar canais fornecidos explicitamente no DTO (não determinados automaticamente)

            List<String> canais = approvalDTO.canais().orElse(new ArrayList<>());


            // Notificação de colaborador removida - usar CollaboratorNotificationService

        }


        return new GenericMessage("Ocorrência finalizada com sucesso pelo gestor.", 200);

    }


    public GenericMessage completeOccurrence(String occurrenceId, CompleteOccurrenceDTO completeDTO, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);


        // Verificar se a ocorrência já foi aprovada

        if (occurrence.getStatus() != DocumentStatus.APROVADO) {

            throw new ModuleFailure("Apenas ocorrências aprovadas podem ser concluídas.");

        }


        // Finalizar a ocorrência com status FINALIZADO

        occurrence.setStatus(DocumentStatus.FINALIZADO);

        occurrence.setSituacao(DocumentStatus.FINALIZADO);

        occurrence.setCurrentStep(0);

        occurrence.setFinal_date(new Date());

        // Garantir o preenchimento de dataFim com o momento da conclusão

        occurrence.setDataFim(LocalDateTime.now());


        // Salvar observação de finalização se fornecida

        if (completeDTO.observacaoFinalizacao().isPresent()) {

            occurrence.setObservacaoAnalista(completeDTO.observacaoFinalizacao().get());

        }


        // Criar log da finalização

        var log = createStepLog(UUID.fromString(userId),

                "Finalização da Ocorrência", 0,

                completeDTO.observacaoFinalizacao().orElse("Ocorrência finalizada com sucesso"),

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Aplicando método melhorado - incluindo código da ocorrência e nome do módulo

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String moduleName = module.getName();

        notifyApplicant(occurrence.getStepLog().stream().findFirst().get(),

                "Sua ocorrência foi concluída.",

                "<br><br>\n" +

                        "\n" +

                        "        Sua " + moduleName + " foi concluída.<br><br>\n" +

                        "\n" +

                        "        Sua ocorrência foi finalizada e concluída com sucesso.<br><br>\n",

                occurrenceCode,

                moduleName);


        return new GenericMessage("Ocorrência concluída com sucesso.", 200);

    }


    public GenericMessage approveAnalyst1(String occurrenceId, Analyst1ApprovalDTO approvalDTO, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);

        checkStatus(occurrence);


        // Definir aprovação do analista1

        occurrence.setAprovacaoAnalista1(approvalDTO.aprovacaoAnalista1().orElse(false));

        occurrence.setCurrentStep(4);


        // Salvar observação do analista1 se fornecida

        if (approvalDTO.observacaoAnalista1().isPresent()) {

            occurrence.setObservacaoAnalista1(approvalDTO.observacaoAnalista1().get());

        }


        // Salvar motivo do analista se fornecido

        if (approvalDTO.motivoAnalista().isPresent()) {

            occurrence.setMotivoAnalista(approvalDTO.motivoAnalista().get());

        }


        // Processar evidência do analista se fornecida

        if (approvalDTO.evidenciaAnalista().isPresent()) {

            String filenameEvidencia = occurrenceId + "_evidencia_analista1_" + System.currentTimeMillis() + "_" +

                    approvalDTO.evidenciaAnalista().get().getOriginalFilename();

            FileMetadata evidenciaMetadata = attachmentService.storeFile(approvalDTO.evidenciaAnalista().get(), "ocf/evidencias_analista", filenameEvidencia, 4);

            occurrence.setEvidenciaAnalista(evidenciaMetadata);

        }


        // Processar itens reclamados se fornecidos

        if (approvalDTO.itemsreclamados() != null && !approvalDTO.itemsreclamados().isEmpty()) {

            processAnalyst1ItemsReclamados(approvalDTO.itemsreclamados(), occurrenceId);

        }


        // Criar log da aprovação

        var log = createStepLog(UUID.fromString(userId),

                "Aprovação do Analista 1", 4,

                approvalDTO.observacaoAnalista1().orElse("Aprovação do analista 1 realizada"),

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // NOTIFICAÇÃO INDEPENDENTE: Enviar notificação de resposta para o colaborador

        // Esta notificação é completamente independente da notificação existente que envia para o solicitante

        if (approvalDTO.respostaEmpregado().isPresent() && !approvalDTO.respostaEmpregado().get().trim().isEmpty()) {

            String email = approvalDTO.email().orElse(occurrence.getColaborador().getEmail_comercial());

            String telefone = approvalDTO.telefone().orElse(occurrence.getColaborador().getTelefone());


            // Usar canais fornecidos explicitamente no DTO (não determinados automaticamente)

            List<String> canais = approvalDTO.canais().orElse(new ArrayList<>());


            // Notificação de colaborador removida - usar CollaboratorNotificationService

        }


        return new GenericMessage("Aprovação do analista 1 realizada com sucesso.", 200);

    }


    /**
     * Processa os itens reclamados da ocorrência
     */

    private void processItemsReclamados(List<ItemReclamadoDTO> itemsReclamados, String ocorrenciaId) {

        for (ItemReclamadoDTO itemDTO : itemsReclamados) {

            FileMetadata anexoInicialMetadata = null;

            FileMetadata anexoAdicionalMetadata = null;


            // Processar anexo inicial se presente

            if (itemDTO.anexoInicial().isPresent()) {

                String filenameInicial = ocorrenciaId + "_item_inicial_" + System.currentTimeMillis() + "_" +

                        itemDTO.anexoInicial().get().getOriginalFilename();

                anexoInicialMetadata = attachmentService.storeFile(itemDTO.anexoInicial().get(), "ocf/itens_reclamados", filenameInicial, 1);

            }


            // Processar anexo adicional se presente

            if (itemDTO.anexoAdicional().isPresent()) {

                String filenameAdicional = ocorrenciaId + "_item_adicional_" + System.currentTimeMillis() + "_" +

                        itemDTO.anexoAdicional().get().getOriginalFilename();

                anexoAdicionalMetadata = attachmentService.storeFile(itemDTO.anexoAdicional().get(), "ocf/itens_reclamados", filenameAdicional, 2);

            }


            // Criar entidade do item reclamado com todos os campos

            ItemReclamado itemReclamado = new ItemReclamado(

                    itemDTO.descricao(),

                    itemDTO.quantidadeReclamada(),

                    itemDTO.valor(),

                    itemDTO.justificativa().orElse(null),

                    itemDTO.observacao().orElse(null),

                    itemDTO.respostaColab().orElse(null),

                    anexoInicialMetadata,

                    anexoAdicionalMetadata,

                    ocorrenciaId

            );


            // Salvar item reclamado

            itemReclamadoRepository.save(itemReclamado);

        }

    }


    /**
     * Processa os itens reclamados atualizados pelo analista 1
     */

    public void processAnalyst1ItemsReclamados(List<ItemReclamadoAnalyst1DTO> itemsReclamados, String ocorrenciaId) {

        for (ItemReclamadoAnalyst1DTO itemDTO : itemsReclamados) {

            // Buscar o item reclamado existente

            ItemReclamado itemReclamado = itemReclamadoRepository.findById(itemDTO.id())

                    .orElseThrow(() -> new ModuleNotFoundFailure("Item reclamado não encontrado: " + itemDTO.id()));


            // Verificar se o item pertence à ocorrência

            if (!itemReclamado.getOcorrenciaId().equals(ocorrenciaId)) {

                throw new ModuleFailure("Item reclamado não pertence à ocorrência.");

            }


            // Atualizar campos se fornecidos

            if (itemDTO.justificativa().isPresent()) {

                itemReclamado.setJustificativa(itemDTO.justificativa().get());

            }


            if (itemDTO.observacao().isPresent()) {

                itemReclamado.setObservacao(itemDTO.observacao().get());

            }


            if (itemDTO.respostaColab().isPresent()) {

                itemReclamado.setRespostaColab(itemDTO.respostaColab().get());

            }


            // Processar anexo adicional se fornecido

            if (itemDTO.anexoAdicional().isPresent()) {

                String filenameAdicional = ocorrenciaId + "_item_adicional_" + System.currentTimeMillis() + "_" +

                        itemDTO.anexoAdicional().get().getOriginalFilename();

                FileMetadata anexoAdicionalMetadata = attachmentService.storeFile(itemDTO.anexoAdicional().get(), "ocf/itens_reclamados", filenameAdicional, 2);

                itemReclamado.setAnexoAdicional(anexoAdicionalMetadata);

            }


            // Salvar item reclamado atualizado

            itemReclamadoRepository.save(itemReclamado);

        }

    }


    public GenericMessage updateOccurrenceFields(String occurrenceId, UpdateOccurrenceDTO updateDTO, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);


        // Atualizar apenas os campos permitidos

        updateDTO.tipoFluxo().ifPresent(occurrence::setTipoFluxo);

        updateDTO.competencia().ifPresent(occurrence::setCompetencia);

        // conversaChat não pertence a UpdateOccurrenceDTO (apenas ao fluxo de ticket)

        updateDTO.prioridade().ifPresent(occurrence::setPrioridade);

        updateDTO.atendenteRHMatriz().ifPresent(valor -> {

            occurrence.setAtendenteRHmatriz(valor);

            try {

                // Determinar o e-mail comercial do atendente (não do colaborador)

                String destinatario = null;


                try {

                    var atendenteOpt = atendenteService.findByEmail(valor);

                    if (atendenteOpt.isPresent()) {

                        var atendente = atendenteOpt.get();

                        if (atendente.getMatricula() != null && !atendente.getMatricula().isBlank()) {

                            var dto = getEmployee.getEmployeeByMatricula(atendente.getMatricula());

                            if (dto != null && dto.getEmail_comercial() != null && !dto.getEmail_comercial().isBlank()) {

                                destinatario = dto.getEmail_comercial();

                            }

                        }

                        if (destinatario == null) {

                            // fallback para o e-mail cadastrado do atendente

                            destinatario = atendente.getEmail();

                        }

                    } else if (valor.contains("@")) {

                        // Se o valor já for um e-mail, usar diretamente

                        destinatario = valor;

                    }

                } catch (Exception ignored2) {
                }


                // Se veio e-mail explicitamente no corpo, priorizar

                if (updateDTO.emailAtendenteRHMatriz().isPresent() && updateDTO.emailAtendenteRHMatriz().get() != null && !updateDTO.emailAtendenteRHMatriz().get().isBlank()) {

                    destinatario = updateDTO.emailAtendenteRHMatriz().get();

                }


                if (destinatario != null && !destinatario.isBlank()) {

                    String occurrenceCode = String.valueOf(occurrence.getCodeID());

                    Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

                    String moduleName = module.getName();

                    String subject = "[" + moduleName + "] Novo atendimento atribuído - Ocorrência " + occurrenceCode;

                    String body = "Você foi atribuído como atendente RH Matriz na ocorrência " + occurrenceCode +

                            " do módulo " + moduleName + ".";

                    mailSender.sendGenericEmail(destinatario, subject, body, null);

                } else {

                }

            } catch (Exception ignored) {
            }

        });


        // Criar log de atualização

        var log = createStepLog(UUID.fromString(userId),

                "Atualização de Campos", occurrence.getCurrentStep(),

                "Campos da ocorrência atualizados: " + getUpdatedFieldsLog(updateDTO),

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        return new GenericMessage("Campos da ocorrência atualizados com sucesso.", 200);

    }


    private String getUpdatedFieldsLog(UpdateOccurrenceDTO updateDTO) {

        StringBuilder fields = new StringBuilder();


        if (updateDTO.tipoFluxo().isPresent()) {

            fields.append("tipoFluxo, ");

        }

        if (updateDTO.competencia().isPresent()) {

            fields.append("competencia, ");

        }

        if (updateDTO.prioridade().isPresent()) {

            fields.append("prioridade, ");

        }

        if (updateDTO.atendenteRHMatriz().isPresent()) {

            fields.append("atendenteRHMatriz, ");

        }


        return fields.length() > 0 ? fields.substring(0, fields.length() - 2) : "nenhum";

    }


    public CreateOccurrenceFromTicketResponseDTO createOccurrenceFromTicket(CreateOccurrenceFromTicketDTO ticketDTO, String userId) throws InterruptedException {
        var ticketOptional = ticketAlodpRepository.findByIdOcorrencia(ticketDTO.id_ocorrencia());
        TicketAlodp ticket = ticketOptional.orElseThrow(() -> new ModuleNotFoundFailure("Ticket não encontrado com id_ocorrencia: " + ticketDTO.id_ocorrencia()));
        boolean alreadyExists = repository.existsByNumeroDoProtocolo(ticket.getProtocolo().toString());

        if (alreadyExists)
            throw new ModuleFailure("Já existe uma ocorrência criada para o ticket com protocolo: " + ticket.getProtocolo());

        var employees = getEmployee.search(ticket.getColaboradorNome(), false, true);

        var employee = employees.stream()
                .max(Comparator.comparing(EmployeeDTO::getMatricula))
                .orElse(null);


        SimpleUser applicant;

        try {
            applicant = userService.getUserById(userId).orElseThrow(() -> new NotFoundEmployee("Solicitante não encontrado."));
        } catch (IllegalArgumentException e) {
            throw new NotFoundEmployee("ID do usuário inválido: " + userId);
        } catch (Exception e) {
            throw e;
        }
        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));
        OcorrenciaFF occurrence = new OcorrenciaFF();
        occurrence.setSolicitante(applicant);
        occurrence.setColaborador(employee);
        occurrence.setModulo_id(module.getId());
        occurrence.setOrigem(ticketDTO.origem());
        occurrence.setDescricao("Ocorrência criada a partir do ticket ALO DP: " + ticket.getProtocolo());
        occurrence.setRegionalId(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                        ? employee.getHierarchy().getRegionalId().intValue()
                        : null
        );
        occurrence.setRegionalNome(
                employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                        ? employee.getHierarchy().getRegionalNome()
                        : ""
        );
        occurrence.setProjectId(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                        ? employee.getHierarchy().getProjetoId().intValue()
                        : 0
        );
        occurrence.setProjectName(
                employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                        ? employee.getHierarchy().getProjetoNome()
                        : ""
        );
        if (ticket.getDataHoraInicial() != null) {
            occurrence.setData_Ocorrencia(ticket.getDataHoraInicial().toLocalDate());
            occurrence.setDataAtendimento(ticket.getDataHoraInicial());
        } else {
            occurrence.setData_Ocorrencia(ticketDTO.dataInicial().orElse(LocalDate.now()));
        }

        occurrence.setContratoId(employee.getContrato().getRateio());
        occurrence.setStatus(DocumentStatus.ABERTO);
        occurrence.setCurrentStep(1);
        occurrence.setCreated_at(Date.from(Instant.now()));
        occurrence.setDadosBancarios(employee.getDadosBancarios());
        occurrence.setCodeID(ticketDTO.id_ocorrencia()); // Usar o id_ocorrencia como codeID
        occurrence.setSituacao(DocumentStatus.ANDAMENTO);
        occurrence.setNumeroDoProtocolo(ticket.getProtocolo().toString());
        occurrence.setTelefone(employee.getTelefone());
        occurrence.setTelefoneDeContato(ticketDTO.telefoneDeContato().orElse(null));
        if (ticket.getColaboradorNome() != null) {
            employee.setName(ticket.getColaboradorNome());
        }
        if (ticket.getColaboradorEmail() != null) {
            employee.setEmail_comercial(ticket.getColaboradorEmail());
        }
        if (ticket.getPrioridade() != null) {
            occurrence.setPrioridade(ticket.getPrioridade());
        } else if (ticketDTO.prioridade().isPresent()) {
            occurrence.setPrioridade(ticketDTO.prioridade().get());
        }
        if (ticket.getEtapa() != null) {
            occurrence.setDescricao("Etapa do ticket: " + ticket.getEtapa());
        }
        if (ticket.getNotaAtendimento() != null) {
            occurrence.setDescricaoOcorrencia(ticket.getNotaAtendimento());
        }
        if (ticket.getIdAssunto() != null) {

            occurrence.setCausas("Assunto ID: " + ticket.getIdAssunto());

        }


        // Criar log inicial

        StepLog log = createStepLog(

                UUID.fromString(userId),

                module.getConfigEtapas().stream().filter(e -> e.getEtapa() == 1).findFirst().map(StepModule::getNome)

                        .orElse("Início da Solicitação"),

                1,

                "Ocorrência criada a partir do ticket ALO DP: " + ticket.getProtocolo(),

                null

        );


        occurrence.setStepLog(List.of(log));


        // Salvar ocorrência

        OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);


        // Atualizar o document_id na tb_sequence_ff onde id = id_ocorrencia

        sequenceRepository.updateDocumentId(ticketDTO.id_ocorrencia(), occurrenceSaved.getId());


        // Criar log da ocorrência

        createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());


        // Notificar usuários com permissão na etapa 1

        String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID()); // Agora é o protocolo

        String moduleName = module.getName();

        var regional = employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                ? employee.getHierarchy().getRegionalId().intValue() : null;

        notifyAnyStep(module, regional, 1,

                "Nova ocorrência criada a partir de ticket",

                "Uma nova ocorrência foi criada a partir de um ticket ALO DP e está aguardando sua análise",

                occurrenceCode, moduleName);


        Long timeId = null;
        timeId = timeService.findTimeIdByRateio(occurrenceSaved.getProjectId());


        return new CreateOccurrenceFromTicketResponseDTO("Ocorrência criada com sucesso a partir do ticket ALO DP.", 201, timeId);

    }


    public GenericMessage updateOccurrenceFromTicketAndMoveToNextStep(UpdateOccurrenceFromTicketDTO updateDTO, String userId) throws InterruptedException {

        OcorrenciaFF occurrence = repository.findById(updateDTO.occurrenceId())
                .orElseThrow(() -> new ModuleNotFoundFailure("Ocorrência não encontrada com ID: " + updateDTO.occurrenceId()));

        if (occurrence.getNumeroDoProtocolo() == null || occurrence.getNumeroDoProtocolo().isEmpty()) {
            // Não falhar mais, apenas avisar
        }

        checkStatus(occurrence);

        updateDTO.descricao().ifPresent(occurrence::setDescricao);
        updateDTO.descricaoOcorrencia().ifPresent(occurrence::setDescricaoOcorrencia);
        updateDTO.respostaEmpregado().ifPresent(occurrence::setRespostaEmpregado);
        updateDTO.justificativaOcorrencia().ifPresent(occurrence::setJustificativaOcorrencia);
        updateDTO.causas().ifPresent(occurrence::setCausas);
        updateDTO.prioridade().ifPresent(occurrence::setPrioridade);
        updateDTO.tipoFluxo().ifPresent(occurrence::setTipoFluxo);
        updateDTO.tipoAtendimento().ifPresent(occurrence::setTipoAtendimento);
        updateDTO.motivo().ifPresent(occurrence::setMotivo);
        updateDTO.tipoBeneficio().ifPresent(occurrence::setTipoDeBeneficio);
        updateDTO.competencia().ifPresent(occurrence::setCompetencia);
        updateDTO.temperatura().ifPresent(occurrence::setTemperatura);
        updateDTO.conversaChat().ifPresent(occurrence::setConversaChat);
        updateDTO.atendenteRHMatriz().ifPresent(occurrence::setAtendenteRHmatriz);
        updateDTO.atendenteRHlocal().ifPresent(occurrence::setAtendenteRHlocal);

        // updateDTO.emailPessoal().ifPresent(occurrence::setEmailPessoal); // Campo não existe na classe OcorrenciaFF

        // updateDTO.emailComercial().ifPresent(occurrence::setEmailComercial); // Campo não existe na classe OcorrenciaFF

        // updateDTO.telefone2().ifPresent(occurrence::setTelefone2); // Campo não existe na classe OcorrenciaFF


        // Processar arquivos

        if (updateDTO.extratoColaborador().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(updateDTO.extratoColaborador().get(), "ocf/extratos", occurrence.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 1);

            occurrence.setExtratoColaborador(metadata);

        }

        if (updateDTO.comprovanteDePagamento().isPresent()) {

            FileMetadata metadata = attachmentService.storeFile(updateDTO.comprovanteDePagamento().get(), "ocf/comprovantes", occurrence.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

            occurrence.setComprovantePagamento(metadata);

        }

        if (updateDTO.anexoTicket().isPresent()) {

            FileMetadata metadata = storeFile(updateDTO.anexoTicket().get(), "ocf/anexos",

                    occurrence.getId() + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 3);

            occurrence.setAnexoTicket(metadata);

        }


        // Processar meios de comunicação se fornecidos

        updateDTO.meiosComunicacao().ifPresent(occurrence::setMeiosComunicacao);


        // Processar itens reclamados se fornecidos

        if (updateDTO.itemsreclamados() != null && !updateDTO.itemsreclamados().isEmpty()) {

            processItemsReclamados(updateDTO.itemsreclamados(), occurrence.getId());

        }


        // Salvar ocorrência atualizada

        OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);


        // Determinar próxima etapa baseado no tipoFluxo

        String tipoFluxo = updateDTO.tipoFluxo().orElse(occurrence.getTipoFluxo());

        int proximaEtapa;


        if ("financeiro".equalsIgnoreCase(tipoFluxo)) {

            // Fluxo financeiro vai para o Gestor (etapa 2)

            proximaEtapa = 2;

            occurrence.setCurrentStep(2);

            occurrence.setStatus(DocumentStatus.ABERTO);

            occurrence.setSituacao(DocumentStatus.ANDAMENTO);

        } else {

            // Outros fluxos (Benefícios, etc.) vão para o Analista 1 (etapa 3)

            proximaEtapa = 3;

            occurrence.setCurrentStep(3);

            occurrence.setStatus(DocumentStatus.ABERTO);

            occurrence.setSituacao(DocumentStatus.ANDAMENTO);

        }


        // Criar log da atualização

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String etapaNome = getEtapaNome(module, proximaEtapa);

        var log = createStepLog(UUID.fromString(userId),

                "Atualização via Ticket", proximaEtapa,

                "Ocorrência atualizada e movida para " + etapaNome + " via ticket ALO DP",

                null);

        addLog(occurrence, log);


        // Salvar ocorrência com nova etapa

        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Notificar próximo responsável

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        String moduleName = module.getName();

        var regional = occurrence.getColaborador().getHierarchy() != null && occurrence.getColaborador().getHierarchy().getRegionalId() != null
                ? occurrence.getColaborador().getHierarchy().getRegionalId().intValue() : null;
        notifyNextStep(module, regional, proximaEtapa, occurrenceCode, moduleName);


        return new GenericMessage("Ocorrência atualizada e movida para a " + etapaNome + " com sucesso.", 200);

    }


    /**
     * Método para passar ocorrências de tickets para a próxima etapa
     * <p>
     * Similar ao editAfterReview mas sem verificação de revisão
     */

    public GenericMessage moveTicketOccurrenceToNextStep(String occurrenceId, Object dto, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);


        if (occurrence == null) {

            throw new ModuleNotFoundFailure("Ocorrência não encontrada.");

        }


        // Verificar se a ocorrência foi criada a partir de um ticket

        if (occurrence.getNumeroDoProtocolo() == null || occurrence.getNumeroDoProtocolo().isEmpty()) {

            throw new ModuleFailure("Esta ocorrência não foi criada a partir de um ticket ALO DP.");

        }


        // Verificar se a ocorrência está em status válido para atualização

        if (occurrence.getStatus() == DocumentStatus.APROVADO) {

            throw new ModuleFailure("Não é possível atualizar uma ocorrência já finalizada.");

        }


        if (occurrence.getStatus() == DocumentStatus.REJEITADO) {

            throw new ModuleFailure("Não é possível atualizar uma ocorrência rejeitada.");

        }


        // Atualizar a ocorrência com os dados recebidos SEM efeitos colaterais de revisão

        OccurrenceFFDTO body = (OccurrenceFFDTO) dto;

        // Campos simples (não opcionais na DTO)

        if (body.observacao() != null) occurrence.setDescricao(body.observacao());

        if (body.causa() != null) occurrence.setCausas(body.causa());

        if (body.prioridade() != null) occurrence.setPrioridade(body.prioridade());

        if (body.tipoAtendimento() != null) occurrence.setTipoAtendimento(body.tipoAtendimento());

        if (body.tipoFluxo() != null) occurrence.setTipoFluxo(body.tipoFluxo());

        if (body.motivo() != null) occurrence.setMotivo(body.motivo());

        if (body.tipoDeBeneficio() != null) occurrence.setTipoDeBeneficio(body.tipoDeBeneficio());

        if (body.valorContestado() != null) occurrence.setValorContestado(body.valorContestado());

        if (body.valorPagarDescontar() != null) occurrence.setValorPagarDescontar(body.valorPagarDescontar());

        if (body.data_ocorrencia() != null) occurrence.setData_Ocorrencia(body.data_ocorrencia());

        if (body.numeroDoProtocolo() != null) occurrence.setNumeroDoProtocolo(body.numeroDoProtocolo());

        if (body.telefone() != null) occurrence.setTelefone(body.telefone());

        if (body.telefoneDeContato() != null) occurrence.setTelefoneDeContato(body.telefoneDeContato());


        // Campos opcionais

        body.dataAtendimento().ifPresent(occurrence::setDataAtendimento);

        body.dataFim().ifPresent(occurrence::setDataFim);

        body.atendenteRHlocal().ifPresent(occurrence::setAtendenteRHlocal);

        body.atendenteRHmatriz().ifPresent(occurrence::setAtendenteRHmatriz);

        body.competencia().ifPresent(occurrence::setCompetencia);

        body.conversaChat().ifPresent(occurrence::setConversaChat);

        body.descricaoOcorrencia().ifPresent(occurrence::setDescricaoOcorrencia);

        body.respostaEmpregado().ifPresent(occurrence::setRespostaEmpregado);

        body.justificativaOcorrencia().ifPresent(occurrence::setJustificativaOcorrencia);

        body.meiosComunicacao().ifPresent(occurrence::setMeiosComunicacao);


        // Arquivos opcionais

        if (body.extratoColaborador().isPresent()) {

            FileMetadata metadata = storeFile(body.extratoColaborador().get(), "ocf/extratos",

                    occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 1);

            occurrence.setExtratoColaborador(metadata);

        }

        if (body.comprovanteDePagamento().isPresent()) {

            FileMetadata metadata = storeFile(body.comprovanteDePagamento().get(), "ocf/comprovantes",

                    occurrenceId + " - " + Instant.now().truncatedTo(ChronoUnit.SECONDS).toString().replace(":", "-"), 2);

            occurrence.setComprovantePagamento(metadata);

        }

        if (body.anexoTicket().isPresent()) {

            FileMetadata metadata = storeFile(body.anexoTicket().get(), "ocf/anexos_ticket",

                    occurrenceId + "_ticket_" + System.currentTimeMillis() + "_" + body.anexoTicket().get().getOriginalFilename(), 1);

            occurrence.setAnexoTicket(metadata);

        }


        // Itens reclamados (se enviados nesta etapa)

        if (body.itemsreclamados() != null && !body.itemsreclamados().isEmpty()) {

            processItemsReclamados(body.itemsreclamados(), occurrence.getId());

        }


        // Determinar próxima etapa baseado no tipoFluxo

        String tipoFluxo = body.tipoFluxo() != null ? body.tipoFluxo() : occurrence.getTipoFluxo();

        int proximaEtapa;


        if ("financeiro".equalsIgnoreCase(tipoFluxo)) {

            // Fluxo financeiro vai para o Gestor (etapa 2)

            proximaEtapa = 2;

            occurrence.setCurrentStep(2);

        } else {

            // Outros fluxos (Benefícios, etc.) vão para o Analista 1 (etapa 3)

            proximaEtapa = 3;

            occurrence.setCurrentStep(3);

        }


        occurrence.setStatus(DocumentStatus.ABERTO);

        occurrence.setSituacao(DocumentStatus.ANDAMENTO);


        // Criar log da movimentação

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        String etapaNome = getEtapaNome(module, proximaEtapa);

        var log = createStepLog(UUID.fromString(userId),

                "Movimentação via Ticket", proximaEtapa,

                "Ocorrência movida para " + etapaNome + " via ticket ALO DP",

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        // Notificar próximo responsável

        String occurrenceCode = String.valueOf(occurrence.getCodeID());

        String moduleName = module.getName();

        var regional = occurrence.getColaborador().getHierarchy() != null && occurrence.getColaborador().getHierarchy().getRegionalId() != null
                ? occurrence.getColaborador().getHierarchy().getRegionalId().intValue() : null;
        notifyNextStep(module, regional, proximaEtapa, occurrenceCode, moduleName);


        return new GenericMessage("Ocorrência movida para a " + etapaNome + " com sucesso.", 200);

    }


    private OccurrenceFFDTO convertToOccurrenceFFDTO(UpdateOccurrenceFromTicketDTO updateDTO) {

        return new OccurrenceFFDTO(

                "11603", // matriculaReclamante (placeholder)

                Optional.empty(), // grupoResponsavel

                "Observação da atualização", // observacao

                Origem.ALO_DP, // origem

                updateDTO.extratoColaborador(),

                updateDTO.comprovanteDePagamento(),

                updateDTO.causas().orElse(""), // causa

                Optional.empty(), // bancoDadosErrado

                null, // dadosCorretos

                LocalDate.now(), // data_ocorrencia

                "", // numeroDoProtocolo

                updateDTO.telefone2().orElse(""), // telefone

                "", // telefoneDeContato

                updateDTO.emailPessoal().orElse(""), // email

                "", // tipoDeBeneficio

                "", // motivo

                "", // valorContestado

                "", // valorPagarDescontar

                updateDTO.tipoFluxo().orElse(""), // tipoFluxo

                false, // aprovacaoGestor

                "", // observacaoAnalista1

                "", // motivoAnalista

                false, // aprovacaoAnalista1

                updateDTO.prioridade().orElse(""), // prioridade

                "", // tipoAtendimento

                updateDTO.itemsreclamados(), // itemsreclamados

                Optional.empty(), // dataAtendimento

                Optional.empty(), // dataFim

                updateDTO.atendenteRHlocal(), // atendenteRHlocal

                updateDTO.atendenteRHMatriz(), // atendenteRHmatriz

                updateDTO.competencia(), // competencia

                Optional.empty(), // conversaChat

                updateDTO.descricaoOcorrencia(), // descricaoOcorrencia

                Optional.empty(), // respostaEmpregado

                Optional.empty(), // justificativaOcorrencia

                updateDTO.anexoTicket(), // anexoTicket

                Optional.empty(), // canais

                Optional.empty(), // meiosComunicacao

                Optional.empty(), // pertinente

                updateDTO.temperatura() // temperatura

        );

    }


    /**
     * Altera o status de uma ocorrência para pendente (ABERTO)
     *
     * @param occurrenceId ID da ocorrência
     * @param userId       ID do usuário que está alterando o status
     * @return GenericMessage com resultado da operação
     */

    public GenericMessage setOccurrenceStatusToPending(String occurrenceId, String userId) {

        OcorrenciaFF occurrence = getOccurrenceInternal(occurrenceId);


        if (occurrence == null) {

            throw new ModuleNotFoundFailure("Ocorrência não encontrada.");

        }


        // Verificar se a ocorrência pode ser alterada para pendente

        if (occurrence.getStatus() == DocumentStatus.FINALIZADO) {

            throw new ModuleFailure("Não é possível alterar o status de uma ocorrência já finalizada.");

        }


        if (occurrence.getStatus() == DocumentStatus.REJEITADO) {

            throw new ModuleFailure("Não é possível alterar o status de uma ocorrência rejeitada.");

        }


        // Alterar status para pendente

        occurrence.setStatus(DocumentStatus.PENDENTE);

        occurrence.setSituacao(DocumentStatus.ANDAMENTO);


        // Criar log da alteração de status

        var log = createStepLog(UUID.fromString(userId),

                "Status alterado para Pendente", occurrence.getCurrentStep(),

                "Status da ocorrência alterado para pendente",

                null);

        addLog(occurrence, log);


        saveOccurrence(occurrence);

        createOccurrenceLog(log, occurrence.getId(), occurrence.getCodeID());


        return new GenericMessage("Status da ocorrência alterado para pendente com sucesso.", 200);

    }


    /**
     * Altera o status de múltiplas ocorrências para pendente (ABERTO) em lote
     *
     * @param occurrenceIds Lista de IDs das ocorrências
     * @param userId        ID do usuário que está alterando o status
     * @return GenericMessage com resultado da operação
     */

    public GenericMessage setMultipleOccurrencesStatusToPending(List<String> occurrenceIds, String userId) {

        int successCount = 0;

        int errorCount = 0;

        StringBuilder errors = new StringBuilder();


        for (String occurrenceId : occurrenceIds) {

            try {

                setOccurrenceStatusToPending(occurrenceId, userId);

                successCount++;

            } catch (Exception e) {

                errorCount++;

                errors.append("Ocorrência ").append(occurrenceId).append(": ").append(e.getMessage()).append("; ");

            }

        }


        String message = String.format("Processamento concluído. %d ocorrências alteradas com sucesso, %d erros.",

                successCount, errorCount);


        if (errorCount > 0) {

            message += " Erros: " + errors.toString();

        }


        return new GenericMessage(message, 200);

    }

    /**
     * Cria múltiplas ocorrências em lote para um array de matrículas.
     * Todas as ocorrências são criadas na etapa 3 com tipoFluxo "Folha".
     *
     * @param request DTO contendo array de matrículas, justificativa e tipoAtendimento
     * @param userId  ID do usuário que está criando as ocorrências
     * @param jwt     Token JWT para obter dados do usuário caso não encontre no banco
     * @return DTO de resposta com informações sobre as ocorrências criadas e erros
     */
    public CreateOccurrencesBatchResponseDTO createOccurrencesBatch(
            CreateOccurrencesBatchDTO request,
            String userId,
            JwtAuthenticationToken jwt) throws InterruptedException {

        Modulo module = moduleService.getModuleByID(UUID.fromString(modulo_id));

        // Tentar buscar o usuário do banco, se não encontrar, criar a partir do token JWT
        SimpleUser applicant = userService.getUserById(userId)
                .orElseGet(() -> {
                    // Se não encontrar no banco, criar um SimpleUser a partir dos claims do token
                    logger.warn("Usuário não encontrado no banco para ID: {}. Criando SimpleUser a partir do token JWT.", userId);

                    String nome = "Usuário do Sistema";
                    String email = null;

                    if (jwt != null && jwt.getToken() != null) {
                        var claims = jwt.getToken().getClaims();
                        nome = (String) claims.getOrDefault("name", nome);
                        email = (String) claims.getOrDefault("email", null);
                    }

                    return SimpleUser.builder()
                            .id(UUID.fromString(userId))
                            .nome(nome)
                            .email(email)
                            .isPj(false)
                            .telefone(null)
                            .imagemUrl(null)
                            .miniaturaUrl(null)
                            .desativado(false)
                            .permissao(null)
                            .criadoEm(null)
                            .ultimoLogin(null)
                            .primeiroAcesso(null)
                            .atualizadoEm(null)
                            .perfil(null)
                            .build();
                });

        List<CreateOccurrencesBatchResponseDTO.OccurrenceCreatedInfo> ocorrenciasCriadas = new ArrayList<>();
        List<CreateOccurrencesBatchResponseDTO.ErroCriacao> erros = new ArrayList<>();

        for (String matricula : request.matriculas()) {
            try {
                // Buscar dados do funcionário pela matrícula
                EmployeeDTO employee = getEmployee.getEmployeeByMatricula(matricula);

                if (employee == null) {
                    erros.add(new CreateOccurrencesBatchResponseDTO.ErroCriacao(
                            matricula,
                            "Funcionário não encontrado para a matrícula: " + matricula
                    ));
                    continue;
                }

                // Criar ocorrência na etapa 3
                OcorrenciaFF occurrence = new OcorrenciaFF();

                // Criar StepLog para etapa 3
                String etapaNome = module.getConfigEtapas().stream()
                        .filter(e -> e.getEtapa() == 3)
                        .findFirst()
                        .map(StepModule::getNome)
                        .orElse("Análise do Analista 1");

                StepLog log = createStepLog(
                        UUID.fromString(userId),
                        etapaNome,
                        3,
                        request.justificativaOcorrencia(),
                        null
                );

                // Criar sequência
                var sequenciaID = new DatabaseSequenceFF();
                DatabaseSequenceFF sequence = sequenceRepository.save(sequenciaID);

                // Preencher dados básicos da ocorrência
                occurrence.setSolicitante(applicant);
                occurrence.setColaborador(employee);
                occurrence.setModulo_id(module.getId());
                occurrence.setOrigem(Origem.KOGNI_RDO_ONLINE);
                occurrence.setDescricao(request.justificativaOcorrencia());
                occurrence.setDescricaoOcorrencia("RDO editado após o fechamento da folha correspondente.");

                occurrence.setRegionalId(
                        employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                                ? employee.getHierarchy().getRegionalId().intValue()
                                : null
                );
                occurrence.setRegionalNome(
                        employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                                ? employee.getHierarchy().getRegionalNome()
                                : ""
                );
                occurrence.setProjectId(
                        employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                                ? employee.getHierarchy().getProjetoId().intValue()
                                : 0
                );
                occurrence.setProjectName(
                        employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                                ? employee.getHierarchy().getProjetoNome()
                                : ""
                );
                occurrence.setData_Ocorrencia(LocalDate.now());

                if (employee.getContrato() != null && employee.getContrato().getRateio() != null) {
                    occurrence.setContratoId(employee.getContrato().getRateio());
                }

                occurrence.setStatus(DocumentStatus.ABERTO);
                occurrence.setCurrentStep(3); // Etapa 3
                occurrence.setTipoFluxo("Folha");
                occurrence.setTipoAtendimento(request.tipoAtendimento());
                occurrence.setJustificativaOcorrencia(request.justificativaOcorrencia());
                occurrence.setCompetencia(request.competencia());
                occurrence.setCreated_at(Date.from(Instant.now()));
                occurrence.setDadosBancarios(employee.getDadosBancarios());
                occurrence.setStepLog(List.of(log));
                occurrence.setCodeID(sequence.getId());
                occurrence.setSituacao(DocumentStatus.ANDAMENTO);

                // Salvar ocorrência
                OcorrenciaFF occurrenceSaved = saveOccurrence(occurrence);

                // Atualizar sequência com o ID do documento
                sequence.setDocumentId(occurrenceSaved.getId());
                sequenceRepository.save(sequence);

                // Criar log da ocorrência
                createOccurrenceLog(log, occurrenceSaved.getId(), occurrenceSaved.getCodeID());

                // Notificar próxima etapa
                String occurrenceCode = String.valueOf(occurrenceSaved.getCodeID());
                String moduleName = module.getName();
                var regional = employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                        ? employee.getHierarchy().getRegionalId().intValue() : null;
                notifyNextStep(module, regional, 3, occurrenceCode, moduleName);

                ocorrenciasCriadas.add(new CreateOccurrencesBatchResponseDTO.OccurrenceCreatedInfo(
                        matricula,
                        occurrenceSaved.getId(),
                        occurrenceSaved.getCodeID()
                ));

            } catch (Exception e) {
                erros.add(new CreateOccurrencesBatchResponseDTO.ErroCriacao(
                        matricula,
                        "Erro ao criar ocorrência: " + e.getMessage()
                ));
                logger.error("Erro ao criar ocorrência para matrícula {}: {}", matricula, e.getMessage(), e);
            }
        }

        int totalCriadas = ocorrenciasCriadas.size();
        int totalErros = erros.size();
        String message = String.format("Processamento concluído: %d ocorrência(s) criada(s), %d erro(s).",
                totalCriadas, totalErros);

        return new CreateOccurrencesBatchResponseDTO(
                message,
                201,
                totalCriadas,
                totalErros,
                ocorrenciasCriadas,
                erros
        );
    }


}


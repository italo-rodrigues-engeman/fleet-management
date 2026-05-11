package com.indux.modules.ocf.presentation;

import com.indux.core.application.dto.generic.BatchStatusDTO;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.domain.service.occurrence.OccurrenceService;
import com.indux.modules.ocf.application.dto.*;
import com.indux.modules.ocf.application.service.*;
import com.indux.modules.ocf.domain.model.*;
import com.indux.modules.ocf.domain.repository.ItemReclamadoRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/solicitacoes/ocf")
public class FinanceOccurrenceController {
    private static final Logger logger = LoggerFactory.getLogger(FinanceOccurrenceController.class);

    private final OccurrenceService<OcorrenciaFF> service;
    private final FinanceOccurrenceTypeService typeService;
    private final ItemReclamadoRepository itemReclamadoRepository;
    private final AtendenteService atendenteService;
    private final FinanceAttendanceTypeService attendanceTypeService;
    private final FinanceComplaintReasonService complaintReasonService;
    private final NotificationScheduleService notificationScheduleService;
    private final com.indux.modules.ocf.infra.NotificationScheduleScheduler notificationScheduleScheduler;
    private final com.indux.core.infra.config.NotificationConfig notificationConfig;
    private final com.indux.modules.ocf.application.service.CollaboratorNotificationService collaboratorNotificationService;
    private final com.indux.core.domain.service.module.ModuleManagementService moduleService;
    private final com.indux.modules.ocf.application.service.OcfExportService exportService;
    private final AlodpTempCleanupService alodpTempCleanupService;
    private final DiretoriaFilterService diretoriaFilterService;

    public FinanceOccurrenceController(OccurrenceService<OcorrenciaFF> service,
            FinanceOccurrenceTypeService typeService, ItemReclamadoRepository itemReclamadoRepository,
            AtendenteService atendenteService, FinanceAttendanceTypeService attendanceTypeService,
            FinanceComplaintReasonService complaintReasonService,
            NotificationScheduleService notificationScheduleService,
            com.indux.modules.ocf.infra.NotificationScheduleScheduler notificationScheduleScheduler,
            com.indux.core.infra.config.NotificationConfig notificationConfig,
            com.indux.modules.ocf.application.service.CollaboratorNotificationService collaboratorNotificationService,
            com.indux.core.domain.service.module.ModuleManagementService moduleService,
            com.indux.modules.ocf.application.service.OcfExportService exportService,
            AlodpTempCleanupService alodpTempCleanupService, DiretoriaFilterService diretoriaFilterService) {
        this.service = service;
        this.typeService = typeService;
        this.itemReclamadoRepository = itemReclamadoRepository;
        this.atendenteService = atendenteService;
        this.attendanceTypeService = attendanceTypeService;
        this.complaintReasonService = complaintReasonService;
        this.notificationScheduleService = notificationScheduleService;
        this.notificationScheduleScheduler = notificationScheduleScheduler;
        this.notificationConfig = notificationConfig;
        this.collaboratorNotificationService = collaboratorNotificationService;
        this.moduleService = moduleService;
        this.exportService = exportService;
        this.alodpTempCleanupService = alodpTempCleanupService;
        this.diretoriaFilterService = diretoriaFilterService;
    }

    @PostMapping("/")
    public ResponseEntity<CreateOccurrenceResponseDTO> createFirstOccurrence(
            @ModelAttribute OccurrenceFFDTO solicitacao,
            @RequestParam(value = "meiosComunicacao.email", required = false) List<String> emailsMeiosComunicacao,
            @RequestParam(value = "meiosComunicacao.telefone", required = false) List<String> telefonesMeiosComunicacao,
            JwtAuthenticationToken jwt) throws IOException, InterruptedException {

        // Criar MeiosComunicacao manualmente se não estiver presente no DTO
        OccurrenceFFDTO solicitacaoComMeiosComunicacao = mapearMeiosComunicacaoDoFormData(solicitacao,
                emailsMeiosComunicacao, telefonesMeiosComunicacao);

        if (solicitacaoComMeiosComunicacao.meiosComunicacao().isPresent()) {
            MeiosComunicacao meiosMapeados = solicitacaoComMeiosComunicacao.meiosComunicacao().get();
        } else {
        }

        CreateOccurrenceResponseDTO result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .createOccurrenceWithDetails(solicitacaoComMeiosComunicacao, jwt.getName());
        if (notificationConfig.isEnabled()) {
            try {
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(result.occurrenceId());
                if (occurrence != null && occurrence.getColaborador() != null) {
                    List<String> canais = solicitacaoComMeiosComunicacao.canais().orElse(new ArrayList<>());
                    if (occurrence.getMeiosComunicacao() != null) {

                        List<String> canaisValidos = new ArrayList<>();
                        List<String> canaisInvalidos = new ArrayList<>();

                        for (String canal : canais) {
                            if ("EMAIL".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getEmail() != null &&
                                        !occurrence.getMeiosComunicacao().getEmail().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum email disponível)");
                                }
                            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getTelefone() != null &&
                                        !occurrence.getMeiosComunicacao().getTelefone().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum telefone disponível)");
                                }
                            } else {
                                canaisValidos.add(canal); // Outros canais passam sem validação específica
                            }
                        }
                        canais = canaisValidos;
                    }

                    if (!canais.isEmpty()) {
                        collaboratorNotificationService
                                .sendStandardNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais);
                    } else {
                        logger.warn(
                                "Nenhum canal de comunicação especificado para ocorrência #{} - notificação não enviada",
                                occurrence.getCodeID());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Mapeia os campos meiosComunicacao do formdata para o DTO
     */
    private OccurrenceFFDTO mapearMeiosComunicacaoDoFormData(OccurrenceFFDTO solicitacao,
            List<String> emailsMeiosComunicacao, List<String> telefonesMeiosComunicacao) {
        List<String> emails = new ArrayList<>();
        List<String> telefones = new ArrayList<>();
        if (emailsMeiosComunicacao != null && !emailsMeiosComunicacao.isEmpty()) {
            emails.addAll(emailsMeiosComunicacao);
        }
        if (telefonesMeiosComunicacao != null && !telefonesMeiosComunicacao.isEmpty()) {
            telefones.addAll(telefonesMeiosComunicacao);
        }
        if (emails.isEmpty() && solicitacao.email() != null && !solicitacao.email().trim().isEmpty()) {
            emails.add(solicitacao.email());

        }

        com.indux.modules.ocf.domain.model.MeiosComunicacao meiosComunicacao = null;
        boolean temDadosFormData = (emailsMeiosComunicacao != null && !emailsMeiosComunicacao.isEmpty()) ||
                (telefonesMeiosComunicacao != null && !telefonesMeiosComunicacao.isEmpty());
        boolean temDadosFallback = (!emails.isEmpty()); // Removido telefones da condição de fallback
        boolean temPeloMenosUmMeio = (!emails.isEmpty()) || (!telefones.isEmpty());

        if (!temPeloMenosUmMeio) {
            String mensagemErro = "É obrigatório informar pelo menos um email OU um telefone nos meiosComunicacao. " +
                    "Envie 'meiosComunicacao.email' e/ou 'meiosComunicacao.telefone' no formdata.";
            throw new com.indux.core.infra.exception.module.ModuleFailure(mensagemErro);
        }

        if (temDadosFormData || temDadosFallback) {
            // Criar com telefones vazios se não foram enviados
            List<String> telefonesParaCriar = telefones.isEmpty() ? null : telefones;
            meiosComunicacao = new com.indux.modules.ocf.domain.model.MeiosComunicacao(emails, telefonesParaCriar);
        }
        Optional<com.indux.modules.ocf.domain.model.MeiosComunicacao> meiosComunicacaoOptional = Optional
                .ofNullable(meiosComunicacao);

        OccurrenceFFDTO resultado = new OccurrenceFFDTO(
                solicitacao.matriculaReclamante(),
                solicitacao.grupoResponsavel(),
                solicitacao.observacao(),
                solicitacao.origem(),
                solicitacao.extratoColaborador(),
                solicitacao.comprovanteDePagamento(),
                solicitacao.causa(),
                solicitacao.bancoDadosErrado(),
                solicitacao.dadosCorretos(),
                solicitacao.data_ocorrencia(),
                solicitacao.numeroDoProtocolo(),
                solicitacao.telefone(),
                solicitacao.telefoneDeContato(),
                solicitacao.email(),
                solicitacao.tipoDeBeneficio(),
                solicitacao.motivo(),
                solicitacao.valorContestado(),
                solicitacao.valorPagarDescontar(),
                solicitacao.tipoFluxo(),
                solicitacao.aprovacaoGestor(),
                solicitacao.observacaoAnalista1(),
                solicitacao.motivoAnalista(),
                solicitacao.aprovacaoAnalista1(),
                solicitacao.prioridade(),
                solicitacao.tipoAtendimento(),
                solicitacao.itemsreclamados(),
                solicitacao.dataAtendimento(),
                solicitacao.dataFim(),
                solicitacao.atendenteRHlocal(),
                solicitacao.atendenteRHmatriz(),
                solicitacao.competencia(),
                solicitacao.conversaChat(),
                solicitacao.descricaoOcorrencia(),
                solicitacao.respostaEmpregado(),
                solicitacao.justificativaOcorrencia(),
                solicitacao.anexoTicket(),
                solicitacao.canais(),
                meiosComunicacaoOptional,
                solicitacao.pertinente(),
                solicitacao.temperatura());
        return resultado;
    }

    @PostMapping("/pendente")
    public ResponseEntity<CreateOccurrenceResponseDTO> createDerivedOccurrence(
            @ModelAttribute OccurrenceFFDTO solicitacao, JwtAuthenticationToken jwt)
            throws IOException, InterruptedException {
        CreateOccurrenceResponseDTO result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .createDerivedOccurrenceWithDetails(solicitacao, jwt.getName());

        // Enviar notificação para o colaborador apenas se canais foram especificados
        // (se habilitado)
        if (notificationConfig.isEnabled()) {
            try {
                // Buscar a ocorrência real criada
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(result.occurrenceId());
                if (occurrence != null && occurrence.getColaborador() != null && solicitacao.canais().isPresent()) {
                    List<String> canais = solicitacao.canais().get();

                    if (!canais.isEmpty()) {
                        collaboratorNotificationService
                                .sendStandardNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais);
                    }
                } else if (occurrence != null && !solicitacao.canais().isPresent()) {
                    logger.warn(
                            "Nenhum canal de comunicação especificado para ocorrência #{} - notificação não enviada",
                            occurrence.getCodeID());
                }
            } catch (Exception e) {
                // Não falha a operação principal se a notificação falhar
                logger.error("Erro ao enviar notificação para colaborador: {}", e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(value = "/create-finalized", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.indux.modules.ocf.application.dto.CreateFinalizedOccurrenceResponseDTO> createFinalizedOccurrence(
            @ModelAttribute OccurrenceFFDTO solicitacao,
            @RequestParam(required = false) List<String> canais,
            @RequestParam(required = false) String respostaEmpregado,
            JwtAuthenticationToken jwt) throws IOException, InterruptedException {
        OccurrenceFFDTO solicitacaoComMeiosComunicacao = solicitacao;
        com.indux.modules.ocf.application.dto.CreateFinalizedOccurrenceResponseDTO result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .createFinalizedOccurrence(solicitacaoComMeiosComunicacao, jwt.getName(), canais, respostaEmpregado);

        // Enviar notificação para o colaborador baseada no meiosComunicacao e canais do
        // formdata
        if (notificationConfig.isEnabled()) {
            try {

                // Buscar a ocorrência real criada
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(result.id());

                if (occurrence != null && occurrence.getColaborador() != null) {
                    List<String> canaisParaNotificacao = canais != null && !canais.isEmpty() ? canais
                            : new ArrayList<>();

                    // Validar se os canais solicitados são compatíveis com os meiosComunicacao
                    // disponíveis
                    if (occurrence.getMeiosComunicacao() != null) {
                        List<String> canaisValidos = new ArrayList<>();
                        List<String> canaisInvalidos = new ArrayList<>();

                        for (String canal : canaisParaNotificacao) {
                            if ("EMAIL".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getEmail() != null &&
                                        !occurrence.getMeiosComunicacao().getEmail().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum email disponível)");
                                }
                            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getTelefone() != null &&
                                        !occurrence.getMeiosComunicacao().getTelefone().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum telefone disponível)");
                                }
                            } else {
                                canaisValidos.add(canal); // Outros canais passam sem validação específica
                            }
                        }
                        canaisParaNotificacao = canaisValidos;
                    }

                    if (!canaisParaNotificacao.isEmpty()) {
                        // Verificar se a ocorrência é pertinente para definir o texto da notificação
                        String respostaEmpregadoParaNotificacao;
                        if (occurrence.getPertinente() != null && !occurrence.getPertinente()) {
                            // Ocorrência NÃO PROCEDENTE - usar texto específico
                            String nomeColaborador = occurrence.getColaborador() != null
                                    ? occurrence.getColaborador().getName()
                                    : "Colaborador";
                            String numeroTicket = occurrence.getCodeID() != null ? occurrence.getCodeID().toString()
                                    : "0000";
                            String dataAbertura = occurrence.getData_Ocorrencia() != null
                                    ? occurrence.getData_Ocorrencia().format(
                                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                                    : "00/00/00";
                            String respostaEquipe = respostaEmpregado != null && !respostaEmpregado.trim().isEmpty()
                                    ? respostaEmpregado
                                    : "Análise concluída pela equipe de RH.";
                            enviarNotificacaoNaoProcedente(occurrence, canaisParaNotificacao, nomeColaborador,
                                    numeroTicket, dataAbertura, respostaEquipe);
                            return ResponseEntity.status(HttpStatus.CREATED).body(result); // Retornar resposta sem
                                                                                           // continuar com notificação
                                                                                           // padrão
                        } else if (occurrence.getPertinente() != null && occurrence.getPertinente()) {
                            String nomeColaborador = occurrence.getColaborador() != null
                                    ? occurrence.getColaborador().getName()
                                    : "Colaborador";
                            String numeroTicket = occurrence.getCodeID() != null ? occurrence.getCodeID().toString()
                                    : "0000";
                            String dataAbertura = occurrence.getData_Ocorrencia() != null
                                    ? occurrence.getData_Ocorrencia().format(
                                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                                    : "00/00/00";
                            String respostaEquipe = respostaEmpregado != null && !respostaEmpregado.trim().isEmpty()
                                    ? respostaEmpregado
                                    : "Análise concluída pela equipe de RH.";
                            enviarNotificacaoProcedente(occurrence, canaisParaNotificacao, nomeColaborador,
                                    numeroTicket, dataAbertura, respostaEquipe);
                            return ResponseEntity.status(HttpStatus.CREATED).body(result); // Retornar resposta sem
                                                                                           // continuar com notificação
                                                                                           // padrão
                        } else {
                            // Sem informação de pertinência - usar texto padrão
                            respostaEmpregadoParaNotificacao = respostaEmpregado != null ? respostaEmpregado
                                    : "Ocorrência finalizada com sucesso.";
                            collaboratorNotificationService.sendResponseNotificationToCollaboratorWithMeiosComunicacao(
                                    occurrence, canaisParaNotificacao, respostaEmpregadoParaNotificacao);

                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/batch/status")
    public ResponseEntity<GenericMessage> recuseBatchItens(@RequestBody BatchStatusDTO body,
            JwtAuthenticationToken jwt) {
        service.batchUpdateStatus(body, UUID.fromString(jwt.getName()));
        return ResponseEntity.ok(new GenericMessage("Ocorrências Rejeitadas com sucesso.", 200));
    }

    @GetMapping("/get/me")
    public ResponseEntity<Page<OcorrenciaFF>> getMyOccurrence(JwtAuthenticationToken jwt, Pageable pageable) {
        return ResponseEntity.ok(service.listOccurrencesByUser(UUID.fromString(jwt.getName()), pageable));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<OcorrenciaFF> getOccurrence(@PathVariable String id) {
        return ResponseEntity.ok(service.getOccurrence(id));
    }

    @GetMapping("/get/{id}/itemsreclamados")
    public ResponseEntity<List<ItemReclamado>> getItemsReclamados(@PathVariable String id) {
        List<ItemReclamado> items = itemReclamadoRepository.findByOcorrenciaId(id);
        return ResponseEntity.ok(items);
    }

    // todo: filtragem de itens genéricos
    @GetMapping("/all")
    public ResponseEntity<?> listCurrentOccurrence(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable,
            @RequestParam(required = false, name = "full", defaultValue = "false") boolean full,
            @RequestParam(required = false, name = "export") String export) {

        // Se export foi solicitado, retornar arquivo ao invés de JSON
        if (export != null && !export.isEmpty()) {
            try {
                boolean hasPermission = jwt.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                                "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

                // Criar filtro vazio (sem filtros específicos)
                OccurrenceFilter filter = new OccurrenceFilter(
                        null, // statuses
                        null, // situations
                        null, // currentSteps
                        null, // createdFrom
                        null, // createdTo
                        null, // id
                        null, // codeId
                        null, // competencias
                        null, // branchIds
                        null, // contractIds
                        null, // requesterName
                        null, // stepLogUserId
                        null, // complainantId
                        null, // origin
                        null, // type
                        null, // tipoFluxo
                        null, // teamIds
                        null, // diretoriaIds
                        null, // superintendenciaIds
                        null, // regionalIds
                        null, // setorIds
                        null, // contratoIds
                        null, // projetoIds
                        null // filialHcmIds
                );

                byte[] data;
                String filename;
                String contentType;

                switch (export.toLowerCase()) {
                    case "pdf":
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.pdf";
                        contentType = "application/pdf";
                        break;
                    case "csv":
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.csv";
                        contentType = "text/csv; charset=UTF-8";
                        break;
                    case "excel":
                    default:
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        break;
                }

                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .body(data);

            } catch (Exception e) {
                logger.error("Erro ao exportar ocorrências no formato {}: {}", export, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Comportamento normal - retornar JSON paginado
        if (full)
            return ResponseEntity.ok(service.listAllAllowed(UUID.fromString(jwt.getName()), pageable));
        return ResponseEntity.ok(service.listOccurrencesAllowed(UUID.fromString(jwt.getName()), pageable));
    }

    @PutMapping("/aprove/{id}")
    public ResponseEntity<GenericMessage> aproveStepTwo(
            @PathVariable String id,
            @ModelAttribute OccurrenceFFDTO dto,
            @RequestParam(required = false) List<String> canais,
            @RequestParam(required = false) String respostaEmpregado,
            @RequestParam(value = "meiosComunicacao.email", required = false) List<String> emailsMeiosComunicacao,
            @RequestParam(value = "meiosComunicacao.telefone", required = false) List<String> telefonesMeiosComunicacao,
            JwtAuthenticationToken jwt) throws IOException {
        OccurrenceFFDTO dtoComMeiosComunicacao = mapearMeiosComunicacaoDoFormData(dto, emailsMeiosComunicacao,
                telefonesMeiosComunicacao);
        GenericMessage message = ((OccurrenceFFService) service).finalizeOccurrence(id, dtoComMeiosComunicacao,
                jwt.getName(), canais, respostaEmpregado);

        // Limpar logs da tabela alodp_temp baseado no CPF do colaborador
        try {
            var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                    .getOccurrenceById(id);
            if (occurrence != null && occurrence.getColaborador() != null) {
                String cpfColaborador = occurrence.getColaborador().getCpf();
                if (cpfColaborador != null && !cpfColaborador.trim().isEmpty()) {
                    long logsRemovidos = alodpTempCleanupService.cleanupLogsByCpf(cpfColaborador);
                    logger.info("Limpeza de logs alodp_temp concluída para CPF {}: {} registros removidos",
                            cpfColaborador, logsRemovidos);
                } else {
                    logger.warn("CPF do colaborador não encontrado para limpeza de logs alodp_temp na ocorrência {}",
                            id);
                }
            } else {
                logger.warn("Ocorrência ou colaborador não encontrado para limpeza de logs alodp_temp na ocorrência {}",
                        id);
            }
        } catch (Exception e) {
            logger.error("Erro ao executar limpeza de logs alodp_temp para ocorrência {}: {}", id, e.getMessage(), e);
        }

        if (notificationConfig.isEnabled()) {
            logger.info("=== INICIANDO PROCESSO DE NOTIFICAÇÃO DE APROVAÇÃO - Endpoint /aprove/{} ===", id);
            try {
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(id);
                logger.info("Ocorrência encontrada: {}",
                        occurrence != null ? "SIM - #" + occurrence.getCodeID() : "NÃO");

                if (occurrence != null && occurrence.getColaborador() != null) {
                    logger.info("Colaborador: {} (ID: {})",
                            occurrence.getColaborador().getName(),
                            occurrence.getColaborador().getId());

                    // Verificar se a ocorrência tem meiosComunicacao configurado (PEGAR DO BANCO DE
                    // DADOS)
                    if (occurrence.getMeiosComunicacao() != null) {
                        logger.info("MeiosComunicacao disponíveis - Emails: {}, Telefones: {}",
                                occurrence.getMeiosComunicacao().getEmail() != null
                                        ? occurrence.getMeiosComunicacao().getEmail().size()
                                        : 0,
                                occurrence.getMeiosComunicacao().getTelefone() != null
                                        ? occurrence.getMeiosComunicacao().getTelefone().size()
                                        : 0);

                        // Usar apenas os canais enviados pelo frontend
                        List<String> canaisParaNotificacao = canais != null && !canais.isEmpty() ? canais
                                : new ArrayList<>();

                        // Validar se os canais solicitados são compatíveis com os meiosComunicacao
                        // disponíveis
                        List<String> canaisValidos = new ArrayList<>();
                        List<String> canaisInvalidos = new ArrayList<>();

                        for (String canal : canaisParaNotificacao) {
                            if ("EMAIL".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getEmail() != null &&
                                        !occurrence.getMeiosComunicacao().getEmail().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum email disponível)");
                                }
                            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getTelefone() != null &&
                                        !occurrence.getMeiosComunicacao().getTelefone().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum telefone disponível)");
                                }
                            } else {
                                canaisValidos.add(canal); // Outros canais passam sem validação específica
                            }
                        }

                        // Usar apenas os canais válidos
                        canaisParaNotificacao = canaisValidos;

                        if (!canaisParaNotificacao.isEmpty()) {
                            // Verificar se pertinente está definido na ocorrência
                            Boolean pertinente = occurrence.getPertinente();
                            String nomeColaborador = occurrence.getColaborador().getName();
                            String numeroTicket = occurrence.getCodeID().toString();
                            String dataAbertura = occurrence.getData_Ocorrencia() != null
                                    ? occurrence.getData_Ocorrencia()
                                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    : "Data não disponível";
                            String respostaEquipe = respostaEmpregado != null ? respostaEmpregado
                                    : "Ocorrência aprovada com sucesso.";

                            if (pertinente != null) {
                                if (pertinente.booleanValue() == false) {

                                    enviarNotificacaoNaoProcedente(occurrence, canaisParaNotificacao, nomeColaborador,
                                            numeroTicket, dataAbertura, respostaEquipe);
                                } else {
                                    enviarNotificacaoProcedente(occurrence, canaisParaNotificacao, nomeColaborador,
                                            numeroTicket, dataAbertura, respostaEquipe);
                                }
                            } else {

                                enviarNotificacaoProcedente(occurrence, canaisParaNotificacao, nomeColaborador,
                                        numeroTicket, dataAbertura, respostaEquipe);
                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/review/{id}")
    public ResponseEntity<GenericMessage> requestReview(@PathVariable String id, @RequestBody OccurrenceFFDTO body,
            JwtAuthenticationToken jwt) throws MessagingException {
        return ResponseEntity.ok(service.requestReview(id, body, jwt.getName()));
    }

    @PutMapping("/review/{id}")
    public ResponseEntity<GenericMessage> sendDataReviewed(@PathVariable String id,
            @ModelAttribute OccurrenceFFDTO body, JwtAuthenticationToken jwt,
            @RequestParam(required = false, name = "edit", defaultValue = "false") boolean edit) throws IOException {
        return ResponseEntity.ok(service.editAfterReview(id, body, jwt.getName(), edit));
    }

    @PutMapping(value = "/recuse/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericMessage> recuseOccurrence(@PathVariable String id,
            @ModelAttribute Analyst1ApprovalDTO dto, JwtAuthenticationToken jwt) throws MessagingException {

        // Converter Analyst1ApprovalDTO para OccurrenceFFDTO para compatibilidade com o
        // service
        OccurrenceFFDTO occurrenceDTO = convertAnalyst1ApprovalToOccurrenceFFDTO(dto);

        // Processar itens reclamados se fornecidos (mesmo comportamento do
        // approveAnalyst1)
        if (dto != null && dto.itemsreclamados() != null && !dto.itemsreclamados().isEmpty()) {
            for (int i = 0; i < dto.itemsreclamados().size(); i++) {
                ItemReclamadoAnalyst1DTO item = dto.itemsreclamados().get(i);
            }

            // Processar itens reclamados usando o mesmo método do approveAnalyst1
            ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                    .processAnalyst1ItemsReclamados(dto.itemsreclamados(), id);
        }

        GenericMessage message = service.rejectOccurrence(id, occurrenceDTO, jwt.getName());

        if (notificationConfig.isEnabled()) {
            try {
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(id);

                if (occurrence != null && occurrence.getColaborador() != null && dto != null) {

                    // Verificar se a ocorrência tem meiosComunicacao configurado (PEGAR DO BANCO DE
                    // DADOS)
                    if (occurrence.getMeiosComunicacao() != null) {
                        // Usar apenas os canais enviados pelo frontend
                        List<String> canais = dto.canais().orElse(new ArrayList<>());
                        // CORREÇÃO: Spring Boot está fazendo split na vírgula, então precisamos juntar
                        // os itens
                        if (!canais.isEmpty() && canais.get(0) instanceof String) {
                            String primeiroItem = canais.get(0);
                            if (primeiroItem.startsWith("[") && !primeiroItem.endsWith("]") && canais.size() > 1) {
                                String segundoItem = canais.get(1);
                                String jsonCompleto = primeiroItem + "," + segundoItem;
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                List<String> canaisParsed = mapper.readValue(jsonCompleto, List.class);
                                canais = canaisParsed;

                            } else if (primeiroItem.startsWith("[") && primeiroItem.endsWith("]")) {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                List<String> canaisParsed = mapper.readValue(primeiroItem, List.class);
                                canais = canaisParsed;
                            }
                        }

                        if (dto.canais().isPresent()) {
                            for (int i = 0; i < dto.canais().get().size(); i++) {
                                Object item = dto.canais().get().get(i);
                                if (item instanceof List) {
                                    List<?> subList = (List<?>) item;
                                    for (int j = 0; j < subList.size(); j++) {
                                    }
                                }
                            }
                        }
                        // CORREÇÃO: Se os canais estão aninhados, achatar o array
                        List<String> canaisCorrigidos = new ArrayList<>();

                        for (int i = 0; i < canais.size(); i++) {
                            Object item = canais.get(i);

                            if (item instanceof List) {
                                // Se é uma lista, adicionar todos os itens
                                List<?> subList = (List<?>) item;
                                for (int j = 0; j < subList.size(); j++) {
                                    Object subItem = subList.get(j);
                                    if (subItem instanceof String) {
                                        canaisCorrigidos.add((String) subItem);
                                    }
                                }
                            } else if (item instanceof String) {
                                // Se é uma string, adicionar diretamente
                                canaisCorrigidos.add((String) item);
                            } else {
                            }
                        }

                        // Usar os canais corrigidos
                        canais = canaisCorrigidos;

                        // Validar se os canais solicitados são compatíveis com os meiosComunicacao
                        // disponíveis
                        List<String> canaisValidos = new ArrayList<>();
                        List<String> canaisInvalidos = new ArrayList<>();

                        for (String canal : canais) {
                            if ("EMAIL".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getEmail() != null &&
                                        !occurrence.getMeiosComunicacao().getEmail().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum email disponível)");
                                }
                            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getTelefone() != null &&
                                        !occurrence.getMeiosComunicacao().getTelefone().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum telefone disponível)");
                                }
                            } else {
                                canaisValidos.add(canal); // Outros canais passam sem validação específica
                            }
                        }

                        if (!canaisInvalidos.isEmpty()) {
                        }

                        // Usar apenas os canais válidos
                        canais = canaisValidos;

                        if (!canais.isEmpty()) {
                            // Extrair respostaColab dos itens reclamados
                            String respostaColab = extractRespostaColabFromItems(occurrence);
                            if (respostaColab != null && !respostaColab.trim().isEmpty()) {
                            }

                            // Sempre usar respostaColab para notificação de recusa
                            String mensagemRecusa = respostaColab != null && !respostaColab.trim().isEmpty()
                                    ? respostaColab
                                    : "Ocorrência recusada - sem resposta específica";
                            collaboratorNotificationService.sendRejectionNotificationToCollaboratorWithMeiosComunicacao(
                                    occurrence, canais, mensagemRecusa);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        return ResponseEntity.ok(message);
    }

    @PutMapping("/aprovacao-gestor/{id}")
    public ResponseEntity<GenericMessage> aprovarGestor(@PathVariable String id,
            @ModelAttribute ManagerApprovalDTO approvalDTO, JwtAuthenticationToken jwt) {
        // Debug: logs visíveis no terminal
        if (approvalDTO != null) {
        }

        GenericMessage result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .approveManager(id, approvalDTO, jwt.getName());

        // Enviar notificação personalizada para o colaborador usando meiosComunicacao
        // da ocorrência (se habilitado)

        if (notificationConfig.isEnabled()) {
            try {
                var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                        .getOccurrenceById(id);

                if (occurrence != null && occurrence.getColaborador() != null) {

                    // Verificar se a ocorrência tem meiosComunicacao configurado (PEGAR DO BANCO DE
                    // DADOS)
                    if (occurrence.getMeiosComunicacao() != null) {

                        // Usar apenas os canais enviados pelo frontend
                        List<String> canais = approvalDTO.canais().orElse(new ArrayList<>());
                        if (!canais.isEmpty()) {
                        }

                        // CORREÇÃO ESPECÍFICA: Frontend envia canais como JSON string via
                        // formData.append('canais', JSON.stringify(canais))

                        // CORREÇÃO: Spring Boot está fazendo split na vírgula, então precisamos juntar
                        // os itens
                        if (!canais.isEmpty() && canais.get(0) instanceof String) {
                            String primeiroItem = canais.get(0);

                            // Se o primeiro item começa com [ mas não termina com ], juntar com o próximo
                            if (primeiroItem.startsWith("[") && !primeiroItem.endsWith("]") && canais.size() > 1) {
                                String segundoItem = canais.get(1);

                                // Juntar os itens para formar o JSON completo
                                String jsonCompleto = primeiroItem + "," + segundoItem;

                                try {
                                    // Fazer parse do JSON string
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    List<String> canaisParsed = mapper.readValue(jsonCompleto, List.class);
                                    canais = canaisParsed;
                                } catch (Exception e) {
                                    // Manter canais originais se der erro
                                }
                            }
                            // Se já está completo, fazer parse normal
                            else if (primeiroItem.startsWith("[") && primeiroItem.endsWith("]")) {
                                try {
                                    // Fazer parse do JSON string
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    List<String> canaisParsed = mapper.readValue(primeiroItem, List.class);
                                    canais = canaisParsed;
                                } catch (Exception e) {
                                    // Manter canais originais se der erro
                                }
                            }
                        }

                        if (approvalDTO.canais().isPresent()) {
                            for (int i = 0; i < approvalDTO.canais().get().size(); i++) {
                                Object item = approvalDTO.canais().get().get(i);
                                if (item instanceof List) {
                                    List<?> subList = (List<?>) item;
                                    for (int j = 0; j < subList.size(); j++) {
                                    }
                                }
                            }
                        }
                        // CORREÇÃO: Se os canais estão aninhados, achatar o array
                        List<String> canaisCorrigidos = new ArrayList<>();

                        for (int i = 0; i < canais.size(); i++) {
                            Object item = canais.get(i);

                            if (item instanceof List) {
                                // Se é uma lista, adicionar todos os itens
                                List<?> subList = (List<?>) item;
                                for (int j = 0; j < subList.size(); j++) {
                                    Object subItem = subList.get(j);
                                    if (subItem instanceof String) {
                                        canaisCorrigidos.add((String) subItem);
                                    }
                                }
                            } else if (item instanceof String) {
                                // Se é uma string, adicionar diretamente
                                canaisCorrigidos.add((String) item);
                            } else {
                            }
                        }

                        // Usar os canais corrigidos
                        canais = canaisCorrigidos;

                        // Validar se os canais solicitados são compatíveis com os meiosComunicacao
                        // disponíveis
                        List<String> canaisValidos = new ArrayList<>();
                        List<String> canaisInvalidos = new ArrayList<>();

                        for (String canal : canais) {
                            if ("EMAIL".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getEmail() != null &&
                                        !occurrence.getMeiosComunicacao().getEmail().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum email disponível)");
                                }
                            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                                if (occurrence.getMeiosComunicacao().getTelefone() != null &&
                                        !occurrence.getMeiosComunicacao().getTelefone().isEmpty()) {
                                    canaisValidos.add(canal);
                                } else {
                                    canaisInvalidos.add(canal + " (nenhum telefone disponível)");
                                }
                            } else {
                                canaisValidos.add(canal); // Outros canais passam sem validação específica
                            }
                        }

                        if (!canaisInvalidos.isEmpty()) {
                        }

                        // Usar apenas os canais válidos
                        canais = canaisValidos;

                        if (!canais.isEmpty()) {

                            // Extrair respostaColab dos itens reclamados (mesma lógica do recuse/{id})
                            String respostaColab = extractRespostaColabFromItems(occurrence);
                            if (respostaColab != null && !respostaColab.trim().isEmpty()) {
                            }

                            // Preparar dados para notificação procedente
                            String nomeColaborador = occurrence.getColaborador().getName();
                            String numeroTicket = occurrence.getCodeID().toString();
                            String dataAbertura = occurrence.getData_Ocorrencia() != null
                                    ? occurrence.getData_Ocorrencia()
                                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                    : "Data não disponível";
                            String respostaEquipe = respostaColab != null && !respostaColab.trim().isEmpty()
                                    ? respostaColab
                                    : "Ocorrência aprovada pelo gestor com sucesso.";

                            // Usar o mesmo método de notificação procedente do create-finalized
                            enviarNotificacaoProcedente(occurrence, canais, nomeColaborador, numeroTicket, dataAbertura,
                                    respostaEquipe);

                        } else {
                            logger.warn(
                                    "Nenhum canal de comunicação especificado para aprovação do gestor - Ocorrência: #{} - notificação não enviada",
                                    occurrence.getCodeID());
                        }
                    } else {
                    }
                } else {
                }
            } catch (Exception e) {
                // Não falha a operação principal se a notificação falhar
                e.printStackTrace();
            } finally {
            }
        } else {
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping(value = "/aprovacao-analista/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericMessage> aprovarAnalista(@PathVariable String id,
            @ModelAttribute AnalystApprovalDTO approvalDTO, JwtAuthenticationToken jwt) {
        GenericMessage result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .approveAnalyst(id, approvalDTO, jwt.getName());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/aprovacao-analista1/{id}")
    public ResponseEntity<GenericMessage> aprovarAnalista1(@PathVariable String id,
            @ModelAttribute Analyst1ApprovalDTO approvalDTO, JwtAuthenticationToken jwt) {
        GenericMessage result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .approveAnalyst1(id, approvalDTO, jwt.getName());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/concluir/{id}")
    public ResponseEntity<GenericMessage> concluirOcorrencia(@PathVariable String id,
            @RequestBody(required = false) CompleteOccurrenceDTO completeDTO, JwtAuthenticationToken jwt) {
        return ResponseEntity.ok(((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .completeOccurrence(id, completeDTO, jwt.getName()));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage> atualizarCamposOcorrencia(@PathVariable String id,
            @RequestBody UpdateOccurrenceDTO updateDTO, JwtAuthenticationToken jwt) {
        return ResponseEntity.ok(((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .updateOccurrenceFields(id, updateDTO, jwt.getName()));
    }

    @GetMapping("/filter")
    public ResponseEntity<?> searchGet(
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false) List<String> situacao,
            @RequestParam(required = false) List<Integer> currentStep,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name = "codeId") Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false) List<Integer> filiais,
            @RequestParam(required = false) List<Integer> contratos,
            @RequestParam(required = false) String nomeSolicitante,
            @RequestParam(required = false) String stepLogUserId,
            @RequestParam(required = false) String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "diretoriaIds") List<Long> diretoriaIds,
            @RequestParam(required = false, name = "superintendenciaIds") List<Long> superintendenciaIds,
            @RequestParam(required = false, name = "regionalIds") List<Long> regionalIds,
            @RequestParam(required = false, name = "setorIds") List<Long> setorIds,
            @RequestParam(required = false, name = "contratoIds") List<Long> contratoIds,
            @RequestParam(required = false, name = "projetoIds") List<Long> projetoIds,
            @RequestParam(required = false, name = "filialHcmIds") List<Long> filialHcmIds,
            @RequestParam(required = false, name = "export") String export,
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable) {

        boolean hasPermission = jwt.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                        "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

        OccurrenceFilter filter = new OccurrenceFilter(
                statuses, situacao, currentStep,
                createdFrom, createdTo, id,
                codeId,
                competencias,
                filiais, contratos,
                nomeSolicitante, stepLogUserId,
                (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds,
                diretoriaIds, superintendenciaIds, regionalIds, setorIds, contratoIds, projetoIds, filialHcmIds);

        // Se export foi solicitado, retornar arquivo ao invés de JSON
        if (export != null && !export.isEmpty()) {
            try {
                byte[] data;
                String filename;
                String contentType;

                switch (export.toLowerCase()) {
                    case "pdf":
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.pdf";
                        contentType = "application/pdf";
                        break;
                    case "csv":
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.csv";
                        contentType = "text/csv; charset=UTF-8";
                        break;
                    case "excel":
                    default:
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf.xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        break;
                }

                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .body(data);

            } catch (Exception e) {
                logger.error("Erro ao exportar ocorrências filtradas no formato {}: {}", export, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Comportamento normal - retornar JSON paginado
        return ResponseEntity.ok(
                service.searchFilter(filter, hasPermission, jwt.getName(), pageable));
    }

    // ------------ FinanceType Endpoints ------------//
    @GetMapping("/type/full")
    public ResponseEntity<List<FinanceOccurrenceComplaintType>> fetchAllComplaintType() {
        return ResponseEntity.ok(typeService.fetchAllComplaintType());
    }

    @PostMapping("/type")
    public ResponseEntity<GenericMessage> createComplaintType(@RequestBody FinanceOccurrenceTypeRecord dto) {
        typeService.createComplaintType(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericMessage("Tipo de reclamação criado com sucesso", 201));
    }

    @PostMapping("/type/occurrence")
    public ResponseEntity<GenericMessage> createOccurrenceType(@RequestBody FinanceOccurrenceTypeRecord dto) {
        typeService.createOccurrenceType(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericMessage("Tipo de ocorrência criada com sucesso", 201));
    }

    // Novo endpoint: criação de tipos de atendimento, um ou vários, opcionalmente
    // associados a um tipo de reclamação
    @PostMapping("/type/attendance")
    public ResponseEntity<GenericMessage> createAttendanceTypes(@RequestBody AttendanceTypeCreateRequest dto) {
        var created = attendanceTypeService.createAttendanceTypes(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericMessage("Tipos de atendimento criados: " + created.size(), 201));
    }

    // Novo endpoint: criação de motivos relacionados a reclamações (um ou vários)
    @PostMapping("/type/reason")
    public ResponseEntity<GenericMessage> createComplaintReasons(@RequestBody ComplaintReasonCreateRequest dto) {
        var created = complaintReasonService.createReasons(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericMessage("Motivos criados: " + created.size(), 201));
    }

    @GetMapping("/type/reason")
    public ResponseEntity<List<com.indux.modules.ocf.domain.model.FinanceComplaintReason>> listComplaintReasons() {
        return ResponseEntity.ok(complaintReasonService.findAll());
    }

    @DeleteMapping("/type/occurrence/{id}")
    public ResponseEntity<GenericMessage> deleteOccurrenceType(@PathVariable Long id) {
        typeService.deleteOccurrenceType(id);
        return ResponseEntity.ok(new GenericMessage("Tipo de ocorrência excluída com sucesso", 200));
    }

    @DeleteMapping("/type/{id}")
    public ResponseEntity<GenericMessage> deleteComplaintType(@PathVariable Long id) {
        typeService.deleteComplaintType(id);
        return ResponseEntity.ok(new GenericMessage("Tipo de reclamação excluída com sucesso", 200));
    }

    // ------------ Atendentes Endpoints ------------//
    @PostMapping("/atendentes")
    public ResponseEntity<GenericMessage> registrarAtendente(@RequestBody AtendenteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atendenteService.registrarAtendente(dto));
    }

    @GetMapping("/atendentes")
    public ResponseEntity<List<AtendenteListDTO>> listarTodosAtendentes() {
        List<Atendente> atendentes = atendenteService.findAll();
        List<Atendente> ordenados = atendentes.stream()
                .sorted(java.util.Comparator.comparing(
                        Atendente::getAccountId,
                        java.util.Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<AtendenteListDTO> payload = ordenados.stream()
                .map(a -> new AtendenteListDTO(
                        a.getId(),
                        a.getAccountId(),
                        a.getEmail(),
                        a.getNome(),
                        a.getMatricula(),
                        a.getSetor(),
                        a.getRegionaisAsList()))
                .toList();
        return ResponseEntity.ok(payload);
    }

    // Versão detalhada com nome da regional e suas filiais (comportamento antigo)
    @GetMapping("/atendentes/full")
    public ResponseEntity<List<AtendenteResponseDTO>> listarAtendentesComRegionais() {
        List<Atendente> atendentes = atendenteService.findAll();
        List<Atendente> atendentesComDados = atendenteService.carregarDadosRelacionados(atendentes);
        List<Atendente> ordenados = atendentesComDados.stream()
                .sorted(java.util.Comparator.comparing(
                        Atendente::getAccountId,
                        java.util.Comparator.nullsLast(Integer::compareTo)))
                .toList();
        return ResponseEntity.ok(
                ordenados.stream().map(AtendenteResponseDTO::fromEntity).toList());
    }

    @GetMapping("/atendentes/rh-matriz")
    public ResponseEntity<List<AtendenteResponseDTO>> listarAtendentesRHMatriz() {
        List<Atendente> atendentes = atendenteService.findAll();
        List<Atendente> atendentesComDados = atendenteService.carregarDadosRelacionados(atendentes);
        List<AtendenteResponseDTO> dtos = atendentesComDados.stream()
                .map(AtendenteResponseDTO::fromEntity)
                .filter(dto -> {
                    boolean isFilial265 = dto.funcionario() != null
                            && dto.funcionario().filialIdHcm() != null
                            && dto.funcionario().filialIdHcm().equals(265);
                    return isFilial265;
                })
                .sorted(Comparator.comparing(
                        AtendenteResponseDTO::accountId,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/atendentes/email/{emailChat}")
    public ResponseEntity<AtendenteResponseDTO> buscarAtendentePorEmail(@PathVariable String emailChat) {
        return atendenteService.findByEmail(emailChat)
                .map(atendenteService::carregarDadosRelacionados)
                .map(AtendenteResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/atendentes/regional/{regional}")
    public ResponseEntity<List<AtendenteResponseDTO>> buscarAtendentesPorRegional(@PathVariable String regional) {
        List<Atendente> atendentes = atendenteService.findByRegional(regional);
        List<Atendente> atendentesComDados = atendenteService.carregarDadosRelacionados(atendentes);
        List<Atendente> atendentesOrdenados = atendentesComDados.stream()
                .sorted(java.util.Comparator.comparing(
                        Atendente::getAccountId,
                        java.util.Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<AtendenteResponseDTO> atendentesDTO = atendentesOrdenados.stream()
                .map(AtendenteResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(atendentesDTO);
    }

    @GetMapping("/atendentes/contrato/{contrato}")
    public ResponseEntity<List<AtendenteResponseDTO>> buscarAtendentesPorContrato(@PathVariable Integer contrato) {
        List<Atendente> atendentes = atendenteService.findByContrato(contrato.toString());
        List<Atendente> atendentesComDados = atendenteService.carregarDadosRelacionados(atendentes);
        List<Atendente> atendentesOrdenados = atendentesComDados.stream()
                .sorted(java.util.Comparator.comparing(
                        Atendente::getAccountId,
                        java.util.Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<AtendenteResponseDTO> atendentesDTO = atendentesOrdenados.stream()
                .map(AtendenteResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(atendentesDTO);
    }

    @GetMapping("/atendentes/regional/{regional}/contrato/{contrato}")
    public ResponseEntity<List<AtendenteResponseDTO>> buscarAtendentesPorRegionalEContrato(
            @PathVariable String regional,
            @PathVariable String contrato) {
        // Remover chaves se existirem
        String contratoLimpo = contrato.replaceAll("[{}]", "");
        List<Atendente> atendentes = atendenteService.findByRegionalAndContrato(regional, contratoLimpo);
        List<Atendente> atendentesComDados = atendenteService.carregarDadosRelacionados(atendentes);
        List<Atendente> atendentesOrdenados = atendentesComDados.stream()
                .sorted(java.util.Comparator.comparing(
                        Atendente::getAccountId,
                        java.util.Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<AtendenteResponseDTO> atendentesDTO = atendentesOrdenados.stream()
                .map(AtendenteResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(atendentesDTO);
    }

    @PutMapping("/atendentes/{id}")
    public ResponseEntity<GenericMessage> atualizarAtendente(@PathVariable String id, @RequestBody AtendenteDTO dto) {
        return ResponseEntity.ok(atendenteService.atualizarAtendente(id, dto));
    }

    @PatchMapping("/atendentes/{id}")
    public ResponseEntity<GenericMessage> atualizarAtendenteParcial(@PathVariable String id,
            @RequestBody AtendentePatchDTO patchDTO) {
        return ResponseEntity.ok(atendenteService.atualizarAtendenteParcial(id, patchDTO));
    }

    @PatchMapping("/atendentes/{id}/nome")
    public ResponseEntity<GenericMessage> atualizarNomeAtendente(
            @PathVariable String id,
            @RequestBody UpdateAtendenteNomeDTO body) {
        return ResponseEntity.ok(atendenteService.atualizarNomeAtendente(id, body.nome()));
    }

    @PatchMapping("/atendentes/{id}/nome-matricula")
    public ResponseEntity<GenericMessage> atualizarNomeEMatriculaAtendente(
            @PathVariable String id,
            @RequestBody UpdateAtendenteNomeMatriculaDTO body) {
        return ResponseEntity.ok(atendenteService.atualizarNomeEMatricula(id, body.nome(), body.matricula()));
    }

    @DeleteMapping("/atendentes/{id}")
    public ResponseEntity<GenericMessage> deletarAtendente(@PathVariable String id) {
        return ResponseEntity.ok(atendenteService.deletarAtendente(id));
    }

    @GetMapping("/setores")
    public ResponseEntity<List<SetorInfoDTO>> listarSetores() {
        return ResponseEntity.ok(atendenteService.listarSetores());
    }

    @PostMapping("/ticket/create-occurrence")
    public ResponseEntity<CreateOccurrenceFromTicketResponseDTO> criarOcorrenciaFromTicket(
            @RequestBody CreateOccurrenceFromTicketDTO ticketDTO, JwtAuthenticationToken jwt) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(((OccurrenceFFService) service).createOccurrenceFromTicket(ticketDTO, jwt.getName()));
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CreateOccurrenceFromTicketResponseDTO(
                            "Erro interno ao criar ocorrência: " + e.getMessage(), 500, null));
        }
    }

    @PostMapping("/batch/create-occurrences")
    public ResponseEntity<CreateOccurrencesBatchResponseDTO> criarOcorrenciasEmLote(
            @RequestBody CreateOccurrencesBatchDTO request,
            JwtAuthenticationToken jwt) {
        try {
            // Obter userId do token
            String userId = jwt.getName();

            // Se não conseguir obter do token, tentar obter dos claims
            if (userId == null || userId.isEmpty()) {
                Object subject = jwt.getToken().getClaims().get("sub");
                if (subject != null) {
                    userId = subject.toString();
                } else {
                    throw new com.indux.core.infra.exception.user.NotFoundEmployee(
                            "Não foi possível identificar o usuário do token.");
                }
            }

            CreateOccurrencesBatchResponseDTO response = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                    .createOccurrencesBatch(request, userId, jwt);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InterruptedException e) {
            logger.error("Erro ao criar ocorrências em lote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CreateOccurrencesBatchResponseDTO(
                            "Erro interno ao criar ocorrências: " + e.getMessage(),
                            500,
                            0,
                            0,
                            new ArrayList<>(),
                            new ArrayList<>()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar ocorrências em lote: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CreateOccurrencesBatchResponseDTO(
                            "Erro inesperado ao criar ocorrências: " + e.getMessage(),
                            500,
                            0,
                            0,
                            new ArrayList<>(),
                            new ArrayList<>()));
        }
    }

    @PutMapping(value = "/ticket/update-and-next-step", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericMessage> atualizarOcorrenciaFromTicketAndMoveToNextStep(
            @ModelAttribute UpdateOccurrenceFromTicketDTO updateDTO,
            JwtAuthenticationToken jwt,
            jakarta.servlet.http.HttpServletRequest request) {
        // Fallback para variações de nome de campo vindas do front
        String tipoBeneficioParam = firstNonEmpty(request.getParameter("tipoBeneficio"),
                request.getParameter("tipoDeBeneficio"));
        String atendenteRHMatrizParam = firstNonEmpty(request.getParameter("atendenteRHMatriz"),
                request.getParameter("atendenteRHmatriz"));
        String conversaChatParam = firstNonEmpty(request.getParameter("conversaChat"),
                request.getParameter("conversa_chat"));

        UpdateOccurrenceFromTicketDTO effectiveDTO = updateDTO;
        if ((tipoBeneficioParam != null && (updateDTO.tipoBeneficio().isEmpty()))
                || (atendenteRHMatrizParam != null && (updateDTO.atendenteRHMatriz().isEmpty()))
                || (conversaChatParam != null && (updateDTO.conversaChat().isEmpty()))) {
            effectiveDTO = new UpdateOccurrenceFromTicketDTO(
                    updateDTO.occurrenceId(),
                    updateDTO.descricao(),
                    updateDTO.descricaoOcorrencia(),
                    updateDTO.respostaEmpregado(),
                    updateDTO.justificativaOcorrencia(),
                    updateDTO.causas(),
                    updateDTO.prioridade(),
                    updateDTO.tipoFluxo(),
                    updateDTO.tipoAtendimento(),
                    updateDTO.motivo(),
                    java.util.Optional.ofNullable(tipoBeneficioParam).or(() -> updateDTO.tipoBeneficio()),
                    updateDTO.competencia(),
                    java.util.Optional.ofNullable(conversaChatParam).or(() -> updateDTO.conversaChat()),
                    updateDTO.pertinente(),
                    java.util.Optional.ofNullable(atendenteRHMatrizParam).or(() -> updateDTO.atendenteRHMatriz()),
                    updateDTO.atendenteRHlocal(),
                    updateDTO.emailPessoal(),
                    updateDTO.emailComercial(),
                    updateDTO.telefone2(),
                    updateDTO.extratoColaborador(),
                    updateDTO.comprovanteDePagamento(),
                    updateDTO.anexoTicket(),
                    updateDTO.itemsreclamados(),
                    updateDTO.canais(),
                    updateDTO.meiosComunicacao(),
                    updateDTO.temperatura());
        }

        try {
            GenericMessage result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                    .updateOccurrenceFromTicketAndMoveToNextStep(effectiveDTO, jwt.getName());

            // Enviar notificação para o colaborador usando canais do frontend (se
            // habilitado)
            if (notificationConfig.isEnabled()) {
                try {
                    // Buscar a ocorrência para obter dados do colaborador
                    var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                            .getOccurrenceById(effectiveDTO.occurrenceId());
                    if (occurrence != null && occurrence.getColaborador() != null
                            && occurrence.getMeiosComunicacao() != null) {
                        // Usar canais enviados pelo frontend
                        List<String> canais = effectiveDTO.canais().orElse(new ArrayList<>());

                        if (!canais.isEmpty()) {
                            collaboratorNotificationService
                                    .sendStandardNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais);
                        } else {
                            logger.warn(
                                    "Nenhum canal de comunicação especificado para atualização do ticket - Ocorrência: #{}",
                                    occurrence.getCodeID());
                        }
                    }
                } catch (Exception e) {
                    // Não falha a operação principal se a notificação falhar
                }
            }

            return ResponseEntity.ok(result);
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno ao atualizar ocorrência: " + e.getMessage(), 500));
        }
    }

    // Alias compatível com a rota esperada no front
    @PutMapping(value = "/ticket/move-to-next-step/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericMessage> moverParaProximoPassoAlias(
            @PathVariable("id") String id,
            @ModelAttribute UpdateOccurrenceFromTicketDTO body,
            JwtAuthenticationToken jwt,
            jakarta.servlet.http.HttpServletRequest request) {

        try {
            // Fallback para variações de nome de campo vindas do front
            String tipoBeneficioParam = firstNonEmpty(request.getParameter("tipoBeneficio"),
                    request.getParameter("tipoDeBeneficio"));
            String atendenteRHMatrizParam = firstNonEmpty(request.getParameter("atendenteRHMatriz"),
                    request.getParameter("atendenteRHmatriz"));
            String conversaChatParam = firstNonEmpty(request.getParameter("conversaChat"),
                    request.getParameter("conversa_chat"));
            // Forçar o occurrenceId a partir do path
            UpdateOccurrenceFromTicketDTO dto = new UpdateOccurrenceFromTicketDTO(
                    id,
                    body.descricao(),
                    body.descricaoOcorrencia(),
                    body.respostaEmpregado(),
                    body.justificativaOcorrencia(),
                    body.causas(),
                    body.prioridade(),
                    body.tipoFluxo(),
                    body.tipoAtendimento(),
                    body.motivo(),
                    java.util.Optional.ofNullable(tipoBeneficioParam).or(() -> body.tipoBeneficio()),
                    body.competencia(),
                    java.util.Optional.ofNullable(conversaChatParam).or(() -> body.conversaChat()),
                    body.pertinente(),
                    java.util.Optional.ofNullable(atendenteRHMatrizParam).or(() -> body.atendenteRHMatriz()),
                    body.atendenteRHlocal(),
                    body.emailPessoal(),
                    body.emailComercial(),
                    body.telefone2(),
                    body.extratoColaborador(),
                    body.comprovanteDePagamento(),
                    body.anexoTicket(),
                    body.itemsreclamados(),
                    body.canais(),
                    body.meiosComunicacao(),
                    body.temperatura());
            GenericMessage result = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                    .updateOccurrenceFromTicketAndMoveToNextStep(dto, jwt.getName());

            // Enviar notificação para o colaborador usando canais do frontend (se
            // habilitado)
            if (notificationConfig.isEnabled()) {
                try {
                    // Buscar a ocorrência para obter dados do colaborador
                    var occurrence = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                            .getOccurrenceById(id);
                    if (occurrence != null && occurrence.getColaborador() != null
                            && occurrence.getMeiosComunicacao() != null) {
                        // Usar canais enviados pelo frontend
                        List<String> canais = dto.canais().orElse(new ArrayList<>());

                        if (!canais.isEmpty()) {
                            // Sempre usar meiosComunicacao para notificações de colaboradores
                            collaboratorNotificationService
                                    .sendStandardNotificationToCollaboratorWithMeiosComunicacao(occurrence, canais);
                        } else {
                            logger.warn(
                                    "Nenhum canal de comunicação especificado para movimento do ticket - Ocorrência: #{}",
                                    occurrence.getCodeID());
                        }
                    }
                } catch (Exception e) {
                    // Erro silencioso - não falha a operação principal se a notificação falhar
                }
            }

            return ResponseEntity.ok(result);
        } catch (InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno ao atualizar ocorrência: " + e.getMessage(), 500));
        }
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.isBlank())
            return a;
        if (b != null && !b.isBlank())
            return b;
        return null;
    }

    /**
     * Converte Analyst1ApprovalDTO para OccurrenceFFDTO para compatibilidade com o
     * service
     */
    private OccurrenceFFDTO convertAnalyst1ApprovalToOccurrenceFFDTO(Analyst1ApprovalDTO dto) {
        // Criar MeiosComunicacao se email ou telefone estiverem presentes
        Optional<MeiosComunicacao> meiosComunicacao = Optional.empty();
        if (dto.email().isPresent() || dto.telefone().isPresent()) {
            List<String> emails = dto.email().isPresent() ? List.of(dto.email().get()) : new ArrayList<>();
            List<String> telefones = dto.telefone().isPresent() ? List.of(dto.telefone().get()) : new ArrayList<>();
            meiosComunicacao = Optional.of(new MeiosComunicacao(emails, telefones));
        }

        // Converter itemsreclamados de Analyst1 para o formato esperado
        List<com.indux.modules.ocf.application.dto.ItemReclamadoDTO> itemsReclamados = new ArrayList<>();
        if (dto.itemsreclamados() != null) {
            for (ItemReclamadoAnalyst1DTO item : dto.itemsreclamados()) {
                itemsReclamados.add(new com.indux.modules.ocf.application.dto.ItemReclamadoDTO(
                        "Item Reclamado", // descricao - valor padrão já que não está disponível no Analyst1
                        null, // quantidadeReclamada - não disponível no Analyst1
                        null, // valor - não disponível no Analyst1
                        item.justificativa(),
                        item.observacao(),
                        item.respostaColab(),
                        Optional.empty(), // anexoInicial - não disponível no Analyst1
                        item.anexoAdicional(),
                        Optional.empty() // anexo genérico
                ));
            }
        }

        return new OccurrenceFFDTO(
                null, // matriculaReclamante (String)
                Optional.empty(), // grupoResponsavel (Optional<String>)
                dto.observacaoAnalista1().orElse(null), // observacao (String)
                null, // origem (Origem)
                Optional.empty(), // extratoColaborador (Optional<MultipartFile>)
                Optional.empty(), // comprovanteDePagamento (Optional<MultipartFile>)
                dto.motivoAnalista().orElse(null), // causa (String)
                Optional.empty(), // bancoDadosErrado (Optional<Boolean>)
                null, // dadosCorretos (BankDetailDTO)
                null, // data_ocorrencia (LocalDate)
                null, // numeroDoProtocolo (String)
                dto.telefone().orElse(null), // telefone (String)
                null, // telefoneDeContato (String)
                dto.email().orElse(null), // email (String)
                null, // tipoDeBeneficio (String)
                null, // motivo (String)
                null, // valorContestado (String)
                null, // valorPagarDescontar (String)
                null, // tipoFluxo (String)
                null, // aprovacaoGestor (Boolean)
                dto.observacaoAnalista1().orElse(null), // observacaoAnalista1 (String)
                dto.motivoAnalista().orElse(null), // motivoAnalista (String)
                dto.aprovacaoAnalista1().orElse(null), // aprovacaoAnalista1 (Boolean)
                null, // prioridade (String)
                null, // tipoAtendimento (String)
                itemsReclamados, // itemsreclamados (List<ItemReclamadoDTO>)
                Optional.empty(), // dataAtendimento (Optional<LocalDateTime>)
                Optional.empty(), // dataFim (Optional<LocalDateTime>)
                Optional.empty(), // atendenteRHlocal (Optional<String>)
                Optional.empty(), // atendenteRHmatriz (Optional<String>)
                Optional.empty(), // competencia (Optional<String>)
                Optional.empty(), // conversaChat (Optional<String>)
                Optional.empty(), // descricaoOcorrencia (Optional<String>)
                dto.respostaEmpregado(), // respostaEmpregado (Optional<String>)
                Optional.empty(), // justificativaOcorrencia (Optional<String>)
                Optional.empty(), // anexoTicket (Optional<MultipartFile>)
                dto.canais(), // canais (Optional<List<String>>)
                meiosComunicacao, // meiosComunicacao (Optional<MeiosComunicacao>)
                Optional.empty(), // pertinente (Optional<Boolean>)
                Optional.empty() // temperatura (Optional<String>)
        );
    }

    /**
     * Extrai todas as respostasColab dos itens reclamados da ocorrência, separadas
     * por item
     */
    private String extractRespostaColabFromItems(com.indux.modules.ocf.domain.model.OcorrenciaFF occurrence) {

        if (occurrence.getItemsreclamados() == null) {
            return null;
        }

        if (occurrence.getItemsreclamados().isEmpty()) {
            return null;
        }

        StringBuilder respostas = new StringBuilder();
        int itemCount = 0;
        int totalItems = occurrence.getItemsreclamados().size();

        // Coletar todas as respostasColab dos itens reclamados
        for (int i = 0; i < totalItems; i++) {
            com.indux.modules.ocf.domain.model.ItemReclamado item = occurrence.getItemsreclamados().get(i);

            if (item.getRespostaColab() != null && !item.getRespostaColab().trim().isEmpty()) {
                itemCount++;

                // Adicionar separador se não for o primeiro item
                if (respostas.length() > 0) {
                    respostas.append("\n\n");
                }

                // Adicionar descrição do item e resposta
                respostas.append(item.getDescricao()).append(": ");
                respostas.append(item.getRespostaColab());
            } else {
            }
        }

        return respostas.length() > 0 ? respostas.toString() : null;
    }

    // ------------ Notification Schedule Endpoints ------------//

    /**
     * Cria um novo agendamento de notificação
     */
    @PostMapping("/notification-schedule")
    public ResponseEntity<NotificationScheduleDTO> createNotificationSchedule(
            @RequestBody CreateNotificationScheduleDTO dto,
            JwtAuthenticationToken jwt) {
        NotificationScheduleDTO schedule = notificationScheduleService.createSchedule(dto, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    /**
     * Atualiza um agendamento de notificação existente
     */
    @PutMapping("/notification-schedule/{id}")
    public ResponseEntity<NotificationScheduleDTO> updateNotificationSchedule(
            @PathVariable String id,
            @RequestBody UpdateNotificationScheduleDTO dto,
            JwtAuthenticationToken jwt) {
        NotificationScheduleDTO schedule = notificationScheduleService.updateSchedule(id, dto, jwt.getName());
        return ResponseEntity.ok(schedule);
    }

    /**
     * Busca um agendamento por ID
     */
    @GetMapping("/notification-schedule/{id}")
    public ResponseEntity<NotificationScheduleDTO> getNotificationSchedule(@PathVariable String id) {
        NotificationScheduleDTO schedule = notificationScheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Lista todos os agendamentos ativos
     */
    @GetMapping("/notification-schedule")
    public ResponseEntity<List<NotificationScheduleDTO>> getAllNotificationSchedules() {
        List<NotificationScheduleDTO> schedules = notificationScheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por canal
     */
    @GetMapping("/notification-schedule/canal/{canal}")
    public ResponseEntity<List<NotificationScheduleDTO>> getNotificationSchedulesByCanal(
            @PathVariable String canal) {
        List<NotificationScheduleDTO> schedules = notificationScheduleService.getSchedulesByCanal(canal);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por etapa
     */
    @GetMapping("/notification-schedule/etapa/{etapa}")
    public ResponseEntity<List<NotificationScheduleDTO>> getNotificationSchedulesByEtapa(
            @PathVariable Integer etapa) {
        List<NotificationScheduleDTO> schedules = notificationScheduleService.getSchedulesByEtapa(etapa);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por userId
     */
    @GetMapping("/notification-schedule/user/{userId}")
    public ResponseEntity<List<NotificationScheduleDTO>> getNotificationSchedulesByUserId(
            @PathVariable String userId) {
        List<NotificationScheduleDTO> schedules = notificationScheduleService.getSchedulesByUserId(userId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Busca agendamento específico por canal, etapa e userId
     */
    @GetMapping("/notification-schedule/canal/{canal}/etapa/{etapa}/user/{userId}")
    public ResponseEntity<NotificationScheduleDTO> getNotificationScheduleByCanalEtapaAndUserId(
            @PathVariable String canal,
            @PathVariable Integer etapa,
            @PathVariable String userId) {
        return notificationScheduleService.getScheduleByCanalEtapaAndUserId(canal, etapa, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Desativa um agendamento
     */
    @PutMapping("/notification-schedule/{id}/deactivate")
    public ResponseEntity<GenericMessage> deactivateNotificationSchedule(
            @PathVariable String id,
            JwtAuthenticationToken jwt) {
        GenericMessage message = notificationScheduleService.deactivateSchedule(id, jwt.getName());
        return ResponseEntity.ok(message);
    }

    /**
     * Ativa um agendamento
     */
    @PutMapping("/notification-schedule/{id}/activate")
    public ResponseEntity<GenericMessage> activateNotificationSchedule(
            @PathVariable String id,
            JwtAuthenticationToken jwt) {
        GenericMessage message = notificationScheduleService.activateSchedule(id, jwt.getName());
        return ResponseEntity.ok(message);
    }

    /**
     * Remove um agendamento permanentemente
     */
    @DeleteMapping("/notification-schedule/{id}")
    public ResponseEntity<GenericMessage> deleteNotificationSchedule(@PathVariable String id) {
        GenericMessage message = notificationScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(message);
    }

    // ------------ Notification Schedule Endpoints with User Details ------------//

    /**
     * Busca um agendamento por ID com informações completas dos usuários
     */
    @GetMapping("/notification-schedule/{id}/with-users")
    public ResponseEntity<NotificationScheduleWithUsersDTO> getNotificationScheduleWithUsers(@PathVariable String id) {
        NotificationScheduleWithUsersDTO schedule = notificationScheduleService.getScheduleByIdWithUsers(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Lista todos os agendamentos ativos com informações completas dos usuários
     */
    @GetMapping("/notification-schedule/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getAllNotificationSchedulesWithUsers() {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService.getAllActiveSchedulesWithUsers();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por canal com informações completas dos usuários
     */
    @GetMapping("/notification-schedule/canal/{canal}/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getNotificationSchedulesByCanalWithUsers(
            @PathVariable String canal) {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService
                .getSchedulesByCanalWithUsers(canal);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por etapa com informações completas dos usuários
     */
    @GetMapping("/notification-schedule/etapa/{etapa}/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getNotificationSchedulesByEtapaWithUsers(
            @PathVariable Integer etapa) {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService
                .getSchedulesByEtapaWithUsers(etapa);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos por userId com informações completas dos usuários
     */
    @GetMapping("/notification-schedule/user/{userId}/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getNotificationSchedulesByUserIdWithUsers(
            @PathVariable String userId) {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService
                .getSchedulesByUserIdWithUsers(userId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Busca agendamento específico por canal, etapa e userId com informações
     * completas dos usuários
     */
    @GetMapping("/notification-schedule/canal/{canal}/etapa/{etapa}/user/{userId}/with-users")
    public ResponseEntity<NotificationScheduleWithUsersDTO> getNotificationScheduleByCanalEtapaAndUserIdWithUsers(
            @PathVariable String canal,
            @PathVariable Integer etapa,
            @PathVariable String userId) {
        return notificationScheduleService.getScheduleByCanalEtapaAndUserIdWithUsers(canal, etapa, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ------------ Endpoints específicos para registro de canais de notificação
    // ------------//

    /**
     * Registra canais de notificação para ocorrências criadas (etapa 1)
     */
    @PostMapping("/notification-schedule/register-occurrence-channels")
    public ResponseEntity<NotificationScheduleDTO> registerOccurrenceNotificationChannels(
            @RequestBody RegisterOccurrenceNotificationChannelsDTO dto,
            JwtAuthenticationToken jwt) {
        NotificationScheduleDTO schedule = notificationScheduleService.registerOccurrenceNotificationChannels(dto,
                jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    /**
     * Registra canais de notificação para ocorrências que saem da primeira etapa
     */
    @PostMapping("/notification-schedule/register-next-step-channels")
    public ResponseEntity<NotificationScheduleDTO> registerNextStepNotificationChannels(
            @RequestBody RegisterNextStepNotificationChannelsDTO dto,
            JwtAuthenticationToken jwt) {
        NotificationScheduleDTO schedule = notificationScheduleService.registerNextStepNotificationChannels(dto,
                jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    /**
     * Lista agendamentos de notificação para ocorrências criadas (etapa 1)
     */
    @GetMapping("/notification-schedule/occurrence-creation")
    public ResponseEntity<List<NotificationScheduleDTO>> getOccurrenceCreationNotificationSchedules() {
        List<NotificationScheduleDTO> schedules = notificationScheduleService
                .getOccurrenceCreationNotificationSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos de notificação para uma etapa específica (após a primeira)
     */
    @GetMapping("/notification-schedule/next-step/{etapa}")
    public ResponseEntity<List<NotificationScheduleDTO>> getNextStepNotificationSchedules(
            @PathVariable Integer etapa) {
        List<NotificationScheduleDTO> schedules = notificationScheduleService.getNextStepNotificationSchedules(etapa);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos de notificação para ocorrências criadas com informações
     * completas dos usuários
     */
    @GetMapping("/notification-schedule/occurrence-creation/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getOccurrenceCreationNotificationSchedulesWithUsers() {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService
                .getOccurrenceCreationNotificationSchedulesWithUsers();
        return ResponseEntity.ok(schedules);
    }

    /**
     * Lista agendamentos de notificação para uma etapa específica com informações
     * completas dos usuários
     */
    @GetMapping("/notification-schedule/next-step/{etapa}/with-users")
    public ResponseEntity<List<NotificationScheduleWithUsersDTO>> getNextStepNotificationSchedulesWithUsers(
            @PathVariable Integer etapa) {
        List<NotificationScheduleWithUsersDTO> schedules = notificationScheduleService
                .getNextStepNotificationSchedulesWithUsers(etapa);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Altera o status de uma ocorrência para pendente
     * 
     * @param id  ID da ocorrência
     * @param jwt Token de autenticação
     * @return ResponseEntity com resultado da operação
     */
    @PutMapping("/{id}/status/pending")
    public ResponseEntity<GenericMessage> setOccurrenceStatusToPending(
            @PathVariable String id,
            JwtAuthenticationToken jwt) {
        GenericMessage message = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .setOccurrenceStatusToPending(id, jwt.getName());
        return ResponseEntity.ok(message);
    }

    /**
     * Altera o status de múltiplas ocorrências para pendente em lote
     * 
     * @param request DTO contendo lista de IDs das ocorrências
     * @param jwt     Token de autenticação
     * @return ResponseEntity com resultado da operação
     */
    @PutMapping("/batch/status/pending")
    public ResponseEntity<GenericMessage> setMultipleOccurrencesStatusToPending(
            @RequestBody BatchStatusDTO request,
            JwtAuthenticationToken jwt) {
        GenericMessage message = ((com.indux.modules.ocf.application.service.OccurrenceFFService) service)
                .setMultipleOccurrencesStatusToPending(request.itens(), jwt.getName());
        return ResponseEntity.ok(message);
    }

    /**
     * Endpoint para buscar informações do time e atendentes por contrato
     */
    @GetMapping("/team-by-contract/{contrato}")
    public ResponseEntity<com.indux.modules.ocf.application.dto.TeamInfoByContractDTO> getTeamInfoByContract(
            @PathVariable String contrato) {
        try {
            com.indux.modules.ocf.application.dto.TeamInfoByContractDTO teamInfo = atendenteService
                    .getTeamInfoByContract(contrato);
            return ResponseEntity.ok(teamInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Método privado para enviar notificações específicas para ocorrências
     * procedentes
     * com layouts diferentes para email e WhatsApp usando o
     * CollaboratorNotificationService
     */
    private void enviarNotificacaoProcedente(OcorrenciaFF occurrence, List<String> canais, String nomeColaborador,
            String numeroTicket, String dataAbertura, String respostaEquipe) {

        // Obter nome do módulo
        String moduleName = "OCF"; // Nome padrão do módulo
        if (occurrence.getModulo_id() != null) {
            try {
                // Buscar nome do módulo usando ModuleManagementService
                var modulo = moduleService.getModuleByID(occurrence.getModulo_id());
                if (modulo != null && modulo.getName() != null) {
                    moduleName = modulo.getName();
                }
            } catch (Exception e) {
                // Em caso de erro, manter o nome padrão
                moduleName = "OCF";
            }
        }

        for (String canal : canais) {
            if ("EMAIL".equalsIgnoreCase(canal)) {
                // Layout específico para EMAIL (HTML) - PROCEDENTE
                String textoEmail = String.format(
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "  <meta charset='UTF-8'>" +
                                "</head>" +
                                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;'>"
                                +
                                "  <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                                "    <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden;'>"
                                +
                                "      <div style='background: #28a745; color: #ffffff; padding: 20px; text-align: center;'>"
                                +
                                "        <h1 style='margin: 0; font-size: 22px;'>TICKET: %s </h1>" +
                                "      </div>" +
                                "      <div style='background: #f8f9fa; padding: 20px;'>" +
                                "        <div style='background: #ffffff; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #28a745;'>"
                                +
                                "          <div style='display: flex; margin: 8px 0;'><span style='font-weight: bold; min-width: 120px; color: #495057;'>Data de Abertura: </span><span style='color: #212529;'>%s</span></div>"
                                +
                                "          <div style='display: flex; margin: 8px 0;'><span style='font-weight: bold; min-width: 120px; color: #495057;'>Status: </span><span style='color: #212529;'>Concluído - Procedente</span></div>"
                                +
                                "        </div>" +
                                "        <div style='font-size: 16px; margin-bottom: 20px;'>" +
                                "          <p style='margin: 0;'>Olá, <strong>%s</strong>!</p>" +
                                "        </div>" +
                                "        <div style='margin: 15px 0;'>" +
                                "          <p style='margin: 0;'>Seu chamado <strong>(ticket: %s)</strong>, aberto no SAC Engeman em <strong>%s</strong>, foi analisado e concluído pelo nosso time de RH como procedente.</p>"
                                +
                                "        </div>" +
                                "        <div style='background: #e9ecef; padding: 15px; border-radius: 5px; margin: 15px 0; font-style: italic;'>"
                                +
                                "          <strong>Comentários da equipe:</strong><br/>%s" +
                                "        </div>" +
                                "        <div style='margin: 15px 0;'>" +
                                "          <p style='margin: 0 0 10px 0;'>Se precisar de ajuda novamente abra um novo chamado.</p>"
                                +
                                "          <p style='margin: 0;'>Estamos à disposição para o que precisar!</p>" +
                                "        </div>" +
                                "      </div>" +
                                "      <div style='text-align: center; padding: 16px; color: #6c757d; font-size: 12px; background: #ffffff; border-top: 1px solid #f1f3f5;'>"
                                +
                                "        <p style='margin: 0;'>Esta notificação foi gerada automaticamente pelo sistema KOGNI.</p>"
                                +
                                "      </div>" +
                                "    </div>" +
                                "  </div>" +
                                "</body>" +
                                "</html>",
                        numeroTicket, dataAbertura, nomeColaborador, numeroTicket, dataAbertura, respostaEquipe);

                // Usar o CollaboratorNotificationService para enviar email personalizado
                collaboratorNotificationService.sendResponseNotificationToCollaboratorWithMeiosComunicacao(occurrence,
                        List.of("EMAIL"), textoEmail);

            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                // Layout específico para WHATSAPP (texto simples) - PROCEDENTE
                String textoWhatsApp = String.format(
                        "🟢 *ENGEMAN INFORMA* 🟢\n\n\n" +
                                "Olá, %s!\n\n\n" +
                                "Seu chamado (ticket: *%s*), aberto no SAC Engeman em *%s*, foi analisado e concluído pelo nosso time de RH como *procedente*.\n\n\n"
                                +
                                "💬 *MENSAGEM PARA VOCÊ:*\n\n" +
                                "%s\n\n\n" +
                                "✅ Estamos à disposição para o que precisar!\n\n\n" +
                                "Se precisar de ajuda novamente ou abrir um novo chamado, é só me chamar por aqui.\n\n\n"
                                +
                                "---\n\n" +
                                "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n\n" +
                                "⚠️ *OBS.:* Não é necessário responder a esta mensagem! ##ENGEMAN##",
                        nomeColaborador, numeroTicket, dataAbertura, respostaEquipe);

                // Usar o CollaboratorNotificationService para enviar WhatsApp personalizado
                collaboratorNotificationService.sendResponseNotificationToCollaboratorWithMeiosComunicacao(occurrence,
                        List.of("WHATSAPP"), textoWhatsApp);
            }
        }

    }

    /**
     * Método privado para enviar notificações específicas para ocorrências não
     * procedentes
     * com layouts diferentes para email e WhatsApp usando o
     * CollaboratorNotificationService
     */
    private void enviarNotificacaoNaoProcedente(OcorrenciaFF occurrence, List<String> canais, String nomeColaborador,
            String numeroTicket, String dataAbertura, String respostaEquipe) {
        // Obter nome do módulo
        String moduleName = "OCF"; // Nome padrão do módulo

        if (occurrence.getModulo_id() != null) {
            try {
                // Buscar nome do módulo usando ModuleManagementService
                var modulo = moduleService.getModuleByID(occurrence.getModulo_id());
                if (modulo != null && modulo.getName() != null) {
                    moduleName = modulo.getName();
                }
            } catch (Exception e) {
                // Em caso de erro, manter o nome padrão
                moduleName = "OCF";
            }
        }

        for (String canal : canais) {
            if ("EMAIL".equalsIgnoreCase(canal)) {
                // Layout específico para EMAIL (HTML) - NÃO PROCEDENTE
                String textoEmail = String.format(
                        "<!DOCTYPE html>" +
                                "<html>" +
                                "<head>" +
                                "  <meta charset='UTF-8'>" +
                                "</head>" +
                                "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4;'>"
                                +
                                "  <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                                "    <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden;'>"
                                +
                                "      <div style='background: #dc3545; color: #ffffff; padding: 20px; text-align: center;'>"
                                +
                                "        <h1 style='margin: 0; font-size: 22px;'>🚫 TICKET: %s</h1>" +
                                "      </div>" +
                                "        <div style='background: #ffffff; padding: 15px; border-radius: 5px; margin: 10px 0; border-left: 4px solid #dc3545;'>"
                                +
                                "          <div style='display: flex; margin: 8px 0;'><span style='font-weight: bold; min-width: 120px; color: #495057;'>Data de Abertura:</span><span style='color: #212529;'>%s</span></div>"
                                +
                                "          <div style='display: flex; margin: 8px 0;'><span style='font-weight: bold; min-width: 120px; color: #495057;'>Status:</span><span style='color: #212529;'>Recusada</span></div>"
                                +
                                "        </div>" +
                                "      <div style='background: #f8f9fa; padding: 20px;'>" +
                                "        <div style='font-size: 16px; margin-bottom: 20px;'>" +
                                "          <p style='margin: 0;'>Olá, <strong>%s</strong>!</p>" +
                                "        </div>" +
                                "        <div style='margin: 15px 0;'>" +
                                "          <p style='margin: 0;'>Seu chamado <strong>(ticket: %s)</strong>, aberta no SAC Engeman em <strong>%s</strong>, foi analisada e não foi aprovada.</p>"
                                +
                                "        </div>" +

                                "        <div style='background: #e9ecef; padding: 15px; border-radius: 5px; margin: 15px 0; font-style: italic;'>"
                                +
                                "          <strong>Resposta da equipe:</strong><br/>%s" +
                                "        </div>" +
                                "        <div style='margin: 15px 0;'>" +
                                "          <p style='margin: 0 0 10px 0;'>Se você discorda desta decisão ou possui informações adicionais, entre em contato conosco para esclarecimentos.</p>"
                                +
                                "          <p style='margin: 0;'>Estamos à disposição para o que precisar!</p>" +
                                "        </div>" +
                                "      </div>" +
                                "      <div style='text-align: center; padding: 16px; color: #6c757d; font-size: 12px; background: #ffffff; border-top: 1px solid #f1f3f5;'>"
                                +
                                "        <p style='margin: 0;'>Esta notificação foi gerada automaticamente pelo sistema KOGNI.</p>"
                                +
                                "      </div>" +
                                "    </div>" +
                                "  </div>" +
                                "</body>" +
                                "</html>",
                        occurrence.getCodeID(),
                        dataAbertura,
                        nomeColaborador,
                        occurrence.getCodeID(),
                        dataAbertura,
                        respostaEquipe);

                // Usar o CollaboratorNotificationService para enviar email personalizado
                collaboratorNotificationService.sendResponseNotificationToCollaboratorWithMeiosComunicacao(occurrence,
                        List.of("EMAIL"), textoEmail);

            } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
                // Layout específico para WHATSAPP (texto simples)
                String textoWhatsApp = String.format(
                        "🔴 *ENGEMAN INFORMA* 🔴\n\n" +
                                "Olá, %s!\n\n" +
                                "Seu chamado (ticket: *%s*), aberto no SAC Engeman em *%s*, foi analisado e concluído pelo nosso time de RH como *não procedente*.\n\n"
                                +
                                "💬 *MENSAGEM PARA VOCÊ:*\n" +
                                "%s\n\n" +
                                "Se você discorda desta decisão ou possui informações adicionais, entre em contato conosco para esclarecimentos.\n\n"
                                +
                                "✅ Estamos à disposição para o que precisar!\n\n" +
                                "---\n" +
                                "SAC - Serviço de Atendimento ao Colaborador Engeman\n\n" +
                                "⚠️ *OBS.:* Não é necessário responder a esta mensagem! ##ENGEMAN##",
                        nomeColaborador, numeroTicket, dataAbertura, respostaEquipe);

                // Usar o CollaboratorNotificationService para enviar WhatsApp personalizado
                collaboratorNotificationService.sendResponseNotificationToCollaboratorWithMeiosComunicacao(occurrence,
                        List.of("WHATSAPP"), textoWhatsApp);
            }
        }

    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOccurrences(
            @RequestParam(value = "format", defaultValue = "excel") String format,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false) List<String> situacao,
            @RequestParam(required = false) List<Integer> currentStep,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name = "codeId") Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false) List<Integer> filiais,
            @RequestParam(required = false) List<Integer> contratos,
            @RequestParam(required = false) String nomeSolicitante,
            @RequestParam(required = false) String stepLogUserId,
            @RequestParam(required = false) String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            JwtAuthenticationToken jwt) {

        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, null, null, null, null, null);

            byte[] data;
            String filename;
            String contentType;

            switch (format.toLowerCase()) {
                case "pdf":
                    data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_ocf.pdf";
                    contentType = "application/pdf";
                    break;
                case "csv":
                    data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_ocf.csv";
                    contentType = "text/csv; charset=UTF-8";
                    break;
                case "excel":
                default:
                    data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_ocf.xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(data);

        } catch (Exception e) {
            logger.error("Erro ao exportar ocorrências no formato {}: {}", format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/debug/diretoria/{diretoriaId}")
    public ResponseEntity<?> debugDiretoriaFilter(@PathVariable Long diretoriaId) {
        try {
            diretoriaFilterService.debugDiretoriaFilter(diretoriaId);
            return ResponseEntity
                    .ok(Map.of("message", "Debug executado para diretoria " + diretoriaId + ". Verifique os logs."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro no debug: " + e.getMessage()));
        }
    }

    @GetMapping("/debug/compare-hcm-ids/{diretoriaId}")
    public ResponseEntity<?> debugCompareHcmIds(@PathVariable Long diretoriaId) {
        try {
            // HCM IDs que você está usando no banco (baseado na imagem)
            List<String> expectedHcmIds = List.of(
                    "2070", "2190", "2090", "1109001", "25524", "1910", "110",
                    "6925", "98", "99", "2080", "1108001", "8224", "2100", "1",
                    "24524", "2260", "1390", "2060", "2280", "980", "102", "3");

            diretoriaFilterService.debugHcmIdsComparison(diretoriaId, expectedHcmIds);
            return ResponseEntity.ok(Map.of("message",
                    "Comparação de HCM IDs executada para diretoria " + diretoriaId + ". Verifique os logs."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro na comparação: " + e.getMessage()));
        }
    }

    @GetMapping("/test/specific-hcm-ids")
    public ResponseEntity<Page<OcorrenciaFF>> testWithSpecificHcmIds(Pageable pageable) {
        try {
            // HCM IDs completos gerados pelo sistema para diretoria 2 (baseado no log)
            List<String> completeHcmIds = List.of(
                    "2070", "2190", "2090", "1109001", "25524", "1910", "110", "2200", "93", "13024",
                    "50", "94", "95", "96", "6925", "98", "99", "2080", "1108001", "8224", "2100",
                    "1990", "1890", "2120", "41523", "2240", "2140", "24524", "2260", "1390", "2060",
                    "2280", "980", "102", "33324", "103", "3325", "1980", "108", "109", "40");

            // Método testFilterWithSpecificHcmIds foi removido
            // Use o endpoint de filtro normal com filialHcmIds
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(List.of()));
        } catch (Exception e) {
            logger.error("Erro no teste com HCM IDs específicos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/test/old-hcm-ids")
    public ResponseEntity<Page<OcorrenciaFF>> testWithOldHcmIds(Pageable pageable) {
        try {
            // Método testFilterWithSpecificHcmIds foi removido
            // Use o endpoint de filtro normal com filialHcmIds
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(List.of()));
        } catch (Exception e) {
            logger.error("Erro no teste com HCM IDs antigos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/filial-hcm/{filialHcmId}")
    public ResponseEntity<?> getOccurrencesByFilialHcmId(
            @PathVariable Long filialHcmId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false, name = "situacao") List<String> situacao,
            @RequestParam(required = false, name = "currentStep") List<Integer> currentStep,
            @RequestParam(required = false, name = "createdFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false, name = "createdTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false, name = "filiais") List<Integer> filiais,
            @RequestParam(required = false, name = "contratos") List<Integer> contratos,
            @RequestParam(required = false, name = "nomeSolicitante") String nomeSolicitante,
            @RequestParam(required = false, name = "stepLogUserId") String stepLogUserId,
            @RequestParam(required = false, name = "reclamanteId") String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, null, null, null, null, List.of(filialHcmId));

            if (export != null && !export.isEmpty()) {
                try {
                    byte[] data;
                    String filename;
                    String contentType;

                    if ("pdf".equalsIgnoreCase(export)) {
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_filial_hcm_" + filialHcmId + ".pdf";
                        contentType = "application/pdf";
                    } else if ("csv".equalsIgnoreCase(export)) {
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_filial_hcm_" + filialHcmId + ".csv";
                        contentType = "text/csv";
                    } else if ("xlsx".equalsIgnoreCase(export)) {
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_filial_hcm_" + filialHcmId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        return ResponseEntity.badRequest().build();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(data);

                } catch (Exception e) {
                    logger.error("Erro ao exportar ocorrências da filial HCM {}: {}", filialHcmId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Erro ao buscar ocorrências da filial HCM {}: {}", filialHcmId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/projeto/{projetoId}")
    public ResponseEntity<?> getOccurrencesByProjetoId(
            @PathVariable Long projetoId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false, name = "situacao") List<String> situacao,
            @RequestParam(required = false, name = "currentStep") List<Integer> currentStep,
            @RequestParam(required = false, name = "createdFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false, name = "createdTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false, name = "filiais") List<Integer> filiais,
            @RequestParam(required = false, name = "contratos") List<Integer> contratos,
            @RequestParam(required = false, name = "nomeSolicitante") String nomeSolicitante,
            @RequestParam(required = false, name = "stepLogUserId") String stepLogUserId,
            @RequestParam(required = false, name = "reclamanteId") String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, null, null, null, List.of(projetoId), null);

            if (export != null && !export.isEmpty()) {
                try {
                    byte[] data;
                    String filename;
                    String contentType;

                    if ("pdf".equalsIgnoreCase(export)) {
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_projeto_" + projetoId + ".pdf";
                        contentType = "application/pdf";
                    } else if ("csv".equalsIgnoreCase(export)) {
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_projeto_" + projetoId + ".csv";
                        contentType = "text/csv";
                    } else if ("xlsx".equalsIgnoreCase(export)) {
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_projeto_" + projetoId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        return ResponseEntity.badRequest().build();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(data);

                } catch (Exception e) {
                    logger.error("Erro ao exportar ocorrências do projeto {}: {}", projetoId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Erro ao buscar ocorrências do projeto {}: {}", projetoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/contrato/{contratoId}")
    public ResponseEntity<?> getOccurrencesByContratoId(
            @PathVariable Long contratoId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false, name = "situacao") List<String> situacao,
            @RequestParam(required = false, name = "currentStep") List<Integer> currentStep,
            @RequestParam(required = false, name = "createdFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false, name = "createdTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false, name = "filiais") List<Integer> filiais,
            @RequestParam(required = false, name = "contratos") List<Integer> contratos,
            @RequestParam(required = false, name = "nomeSolicitante") String nomeSolicitante,
            @RequestParam(required = false, name = "stepLogUserId") String stepLogUserId,
            @RequestParam(required = false, name = "reclamanteId") String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, null, null, List.of(contratoId), null, null);

            if (export != null && !export.isEmpty()) {
                try {
                    byte[] data;
                    String filename;
                    String contentType;

                    if ("pdf".equalsIgnoreCase(export)) {
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_contrato_" + contratoId + ".pdf";
                        contentType = "application/pdf";
                    } else if ("csv".equalsIgnoreCase(export)) {
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_contrato_" + contratoId + ".csv";
                        contentType = "text/csv";
                    } else if ("xlsx".equalsIgnoreCase(export)) {
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_contrato_" + contratoId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        return ResponseEntity.badRequest().build();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(data);

                } catch (Exception e) {
                    logger.error("Erro ao exportar ocorrências do contrato {}: {}", contratoId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Erro ao buscar ocorrências do contrato {}: {}", contratoId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/setor/{setorId}")
    public ResponseEntity<?> getOccurrencesBySetorId(
            @PathVariable Long setorId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false, name = "situacao") List<String> situacao,
            @RequestParam(required = false, name = "currentStep") List<Integer> currentStep,
            @RequestParam(required = false, name = "createdFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false, name = "createdTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false, name = "filiais") List<Integer> filiais,
            @RequestParam(required = false, name = "contratos") List<Integer> contratos,
            @RequestParam(required = false, name = "nomeSolicitante") String nomeSolicitante,
            @RequestParam(required = false, name = "stepLogUserId") String stepLogUserId,
            @RequestParam(required = false, name = "reclamanteId") String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, null, List.of(setorId), null, null, null);

            if (export != null && !export.isEmpty()) {
                try {
                    byte[] data;
                    String filename;
                    String contentType;

                    if ("pdf".equalsIgnoreCase(export)) {
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_setor_" + setorId + ".pdf";
                        contentType = "application/pdf";
                    } else if ("csv".equalsIgnoreCase(export)) {
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_setor_" + setorId + ".csv";
                        contentType = "text/csv";
                    } else if ("xlsx".equalsIgnoreCase(export)) {
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_setor_" + setorId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        return ResponseEntity.badRequest().build();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(data);

                } catch (Exception e) {
                    logger.error("Erro ao exportar ocorrências do setor {}: {}", setorId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Erro ao buscar ocorrências do setor {}: {}", setorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/regional/{regionalId}")
    public ResponseEntity<?> getOccurrencesByRegionalId(
            @PathVariable Long regionalId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false, name = "situacao") List<String> situacao,
            @RequestParam(required = false, name = "currentStep") List<Integer> currentStep,
            @RequestParam(required = false, name = "createdFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false, name = "createdTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false, name = "filiais") List<Integer> filiais,
            @RequestParam(required = false, name = "contratos") List<Integer> contratos,
            @RequestParam(required = false, name = "nomeSolicitante") String nomeSolicitante,
            @RequestParam(required = false, name = "stepLogUserId") String stepLogUserId,
            @RequestParam(required = false, name = "reclamanteId") String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        try {
            boolean hasPermission = jwt.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                            "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

            OccurrenceFilter filter = new OccurrenceFilter(
                    statuses, situacao, currentStep,
                    createdFrom, createdTo, id,
                    codeId,
                    competencias,
                    filiais, contratos,
                    nomeSolicitante, stepLogUserId,
                    (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                    null, List.of(regionalId), null, null, null, null);

            if (export != null && !export.isEmpty()) {
                try {
                    byte[] data;
                    String filename;
                    String contentType;

                    if ("pdf".equalsIgnoreCase(export)) {
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_regional_" + regionalId + ".pdf";
                        contentType = "application/pdf";
                    } else if ("csv".equalsIgnoreCase(export)) {
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_regional_" + regionalId + ".csv";
                        contentType = "text/csv";
                    } else if ("xlsx".equalsIgnoreCase(export)) {
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_regional_" + regionalId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    } else {
                        return ResponseEntity.badRequest().build();
                    }

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType(contentType));
                    headers.setContentDispositionFormData("attachment", filename);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(data);

                } catch (Exception e) {
                    logger.error("Erro ao exportar ocorrências da regional {}: {}", regionalId, e.getMessage(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Erro ao buscar ocorrências da regional {}: {}", regionalId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter/superintendencia/{superintendenciaId}")
    public ResponseEntity<?> getOccurrencesBySuperintendenciaId(
            @PathVariable Long superintendenciaId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false) List<String> situacao,
            @RequestParam(required = false) List<Integer> currentStep,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name = "codeId") Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false) List<Integer> filiais,
            @RequestParam(required = false) List<Integer> contratos,
            @RequestParam(required = false) String nomeSolicitante,
            @RequestParam(required = false) String stepLogUserId,
            @RequestParam(required = false) String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            JwtAuthenticationToken jwt,
            Pageable pageable) {
        boolean hasPermission = jwt.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                        "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

        OccurrenceFilter filter = new OccurrenceFilter(
                statuses, situacao, currentStep,
                createdFrom, createdTo, id,
                codeId,
                competencias,
                filiais, contratos,
                nomeSolicitante, stepLogUserId,
                (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds, null,
                List.of(superintendenciaId), null, null, null, null, null);

        if (export != null && !export.isEmpty()) {
            try {
                byte[] data;
                String filename;

                if ("pdf".equalsIgnoreCase(export)) {
                    data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_superintendencia_" + superintendenciaId + ".pdf";
                } else if ("csv".equalsIgnoreCase(export)) {
                    data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_superintendencia_" + superintendenciaId + ".csv";
                } else if ("excel".equalsIgnoreCase(export)) {
                    data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                    filename = "ocorrencias_superintendencia_" + superintendenciaId + ".xlsx";
                } else {
                    return ResponseEntity.badRequest().build();
                }

                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=" + filename)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(data);

            } catch (Exception e) {
                logger.error("Erro ao exportar ocorrências no formato {}: {}", export, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        Page<OcorrenciaFF> result = service.searchFilter(filter, hasPermission, jwt.getName(), pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter/diretoria/{diretoriaId}")
    public ResponseEntity<?> getOccurrencesByDiretoriaId(
            @PathVariable Long diretoriaId,
            @RequestParam(required = false, name = "statuses") List<String> statuses,
            @RequestParam(required = false) List<String> situacao,
            @RequestParam(required = false) List<Integer> currentStep,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String id,
            @RequestParam(required = false, name = "codeId") Integer codeId,
            @RequestParam(required = false, name = "competencias") List<String> competencias,
            @RequestParam(required = false) List<Integer> filiais,
            @RequestParam(required = false) List<Integer> contratos,
            @RequestParam(required = false) String nomeSolicitante,
            @RequestParam(required = false) String stepLogUserId,
            @RequestParam(required = false) String reclamanteId,
            @RequestParam(required = false, name = "reclamanteNome") String reclamanteNome,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String tipoFluxo,
            @RequestParam(required = false, name = "teamIds") List<Long> teamIds,
            @RequestParam(required = false, name = "export") String export,
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable) {
        boolean hasPermission = jwt.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_ADMINISTRADOR".equals(role) ||
                        "ROLE_DESENVOLVEDOR".equals(role) || "ROLE_USUARIO".equals(role));

        OccurrenceFilter filter = new OccurrenceFilter(
                statuses, situacao, currentStep,
                createdFrom, createdTo, id,
                codeId,
                competencias,
                filiais, contratos,
                nomeSolicitante, stepLogUserId,
                (reclamanteNome != null ? reclamanteNome : reclamanteId), origem, tipo, tipoFluxo, teamIds,
                List.of(diretoriaId), null, null, null, null, null, null);

        if (export != null && !export.isEmpty()) {
            try {
                byte[] data;
                String filename;
                String contentType;

                switch (export.toLowerCase()) {
                    case "pdf":
                        data = exportService.exportToPdf(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf_diretoria_" + diretoriaId + ".pdf";
                        contentType = "application/pdf";
                        break;
                    case "csv":
                        data = exportService.exportToCsv(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf_diretoria_" + diretoriaId + ".csv";
                        contentType = "text/csv; charset=UTF-8";
                        break;
                    case "excel":
                    default:
                        data = exportService.exportToExcel(filter, hasPermission, jwt.getName());
                        filename = "ocorrencias_ocf_diretoria_" + diretoriaId + ".xlsx";
                        contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                        break;
                }

                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .body(data);

            } catch (Exception e) {
                logger.error("Erro ao exportar ocorrências da diretoria {} no formato {}: {}", diretoriaId, export,
                        e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok(
                service.searchFilter(filter, hasPermission, jwt.getName(), pageable));
    }

}

package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.ppu.application.dtos.requests.ClosedCompetenceRequest;
import com.indux.modules.ppu.application.dtos.response.competence.CompetenceResponse;
import com.indux.modules.ppu.application.projection.PendingRDOProjection;
import com.indux.modules.ppu.application.services.rdo.GetMissingRDOsUseCase;
import com.indux.modules.ppu.domain.entities.mongo.ClosedCompetenceRDO;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class CloseCompetenceService {
    private final MongoTemplate mongoTemplate;
    private final RDORepository repository;
    private final PPURepository ppuRepository;
    private final ClosedCompetenceRDORepository closedRepository;
    private final UserService userService;
    private final RDOLoggerUserMapper userMapper;
    private final GetMissingRDOsUseCase missingUseCase;
    private static final String RH_SECTOR = "RH";
    private static final Integer FIRST_DAY_OF_SHEET = 16;
    private static final Integer LAST_DAY_OF_SHEET = 15;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    Locale ptBrLocale = Locale.of("pt", "BR");

    public CloseCompetenceService(MongoTemplate mongoTemplate, RDORepository repository, PPURepository ppuRepository, ClosedCompetenceRDORepository closedRepository, UserService userService, RDOLoggerUserMapper userMapper, GetMissingRDOsUseCase missingUseCase) {
        this.mongoTemplate = mongoTemplate;
        this.repository = repository;
        this.ppuRepository = ppuRepository;
        this.closedRepository = closedRepository;
        this.userService = userService;
        this.userMapper = userMapper;
        this.missingUseCase = missingUseCase;
    }

    /**
     * Realiza o fechamento da competência para o período e o projeto informado.
     *
     * @param request DTO contendo os detalhes da competência a ser fechada, incluindo observações e confirmação
     * @param userID  ID do usuário responsável pela aprovação
     * @return DTO contendo informações da competência.
     * @throws ModuleFailure    se:
     *                          <ul>
     *                          <li>Já existir uma competência fechada para o período</li>
     *                          <li>Houver datas sem RDOs registrados</li>
     *                          <li>Existirem RDOs pendentes de aprovação</li>
     *                          </ul>
     * @throws NotFoundEmployee se o usuário informado não for encontrado
     */
    public CompetenceResponse closeCompetence(ClosedCompetenceRequest request, String userID) {

        LocalDate start = request.competencia().minusMonths(1).atDay(FIRST_DAY_OF_SHEET);
        LocalDate end = request.competencia().atDay(LAST_DAY_OF_SHEET);

        var ppu = ppuRepository.findByProjectId(request.projeto()).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para o projeto " + request.projeto()));
        validate(start, end, request);

        var rdos = repository.findAllByDateAndPpuID(start, end, ppu.getId());
        var user = userService.getUserById(userID).orElseThrow(() -> new NotFoundEmployee("Colaborador aprovador não encontrado."));
        var toClose = markAsClosed(rdos, request.competencia(), userMapper.toLogger(user));
        batchUpdateItems(toClose);
        createCompetence(rdos, user, request);

        return new CompetenceResponse(request.competencia().toString(), start, end, toClose.size());

    }

    /**
     * Valida se é possível fechar a competência.
     * A validação considera:
     * - Existência de competência fechada para o período e projeto
     * - Ausência de dias sem RDO (Considera-se que todos os dias do período devem ter RDOs registrados)
     * - Ausência de RDOs pendentes
     *
     * @param start   Data inicial (gerada com base na competência)
     * @param end     Data final (gerada com base na competência)
     * @param request DTO da competência
     * @throws ModuleFailure em caso de inconsistências
     */
    private void validate(LocalDate start, LocalDate end, ClosedCompetenceRequest request) {
        validateCompetenceIsNotClosed(request.competencia(), request.projeto());
        validateNoPendingRdos(start, end, request.projeto());
        validateNoMissingRdos(start, end, request.projeto());
    }

    /**
     * Cria a entidade de competência fechada contendo:
     * - IDs dos RDOs incluídos
     * - Usuário responsável
     * - Data de criação
     * - Competência
     *
     * @param rdos     Lista de RDOs consolidados
     * @param approver Usuário que aprovou o fechamento
     * @param request  DTO da competência fechada contendo a competência
     */
    private void createCompetence(List<RDOEntity> rdos, SimpleUser approver, ClosedCompetenceRequest request) {
        var ids = rdos.stream().map(RDOEntity::getId).toList();
        var today = LocalDate.now();
        LocalDate start = request.competencia().atDay(FIRST_DAY_OF_SHEET);
        LocalDate end = request.competencia().plusMonths(1).atDay(LAST_DAY_OF_SHEET);
        var entity = ClosedCompetenceRDO.builder()
                .year(request.competencia().getYear())
                .month(request.competencia().getMonth())
                .rdosClosed(ids)
                .approver(approver)
                .createdAt(today)
                .competence(request.competencia().toString())
                .checked(request.checado())
                .observations(request.observacoes())
                .project(request.projeto())
                .initialDate(start)
                .finalDate(end)
                .build();

        closedRepository.save(entity);
    }

    private void validateCompetenceIsNotClosed(YearMonth competence, Long project) {
        if (closedRepository.existsByCompetenceAndProject(competence.toString(), project)) {
            throw new ModuleFailure("Não foi possível realizar a operação. A competência de " +
                    competence.getMonth().getDisplayName(TextStyle.FULL, ptBrLocale) +
                    " de " + competence.getYear() +
                    " para o projeto de número " + project + " já foi fechada.");
        }
    }

    private void validateNoMissingRdos(LocalDate start, LocalDate end, Long project) {
        MissingRDORequest request = new MissingRDORequest(null, project, null, start, end);
        var missingRdos = missingUseCase.execute(request);
        if (!missingRdos.missing().isEmpty()) {
            String details = missingRdos.missing().stream()
                    .map(rdo -> String.format("Data: %s, RDO: %s, Contrato: %s",
                            rdo.getData().format(dateFormatter),
                            rdo.getRdo(),
                            rdo.getNomeContrato()))
                    .collect(Collectors.joining("; "));
            throw new ModuleFailure("Existem RDOs faltando no período. Por favor, preencha-os antes de continuar. Detalhes: " + details);
        }
    }

    private void validateNoPendingRdos(LocalDate start, LocalDate end, Long project) {
        List<PendingRDOProjection> pendings = repository.findPendingsInPeriodAndProjectId(start, end, project);
        if (!pendings.isEmpty()) {
            String details = pendings.stream()
                    .map(p -> String.format("RDO %s (data: %s)",
                            p.getSequentialId(),
                            p.getDate().format(dateFormatter)))
                    .collect(Collectors.joining("; "));
            throw new ModuleFailure("Existem RDOs pendentes de preenchimento ou aprovação: " + details +
                    ". Por favor, revise-os.");
        }
    }

    /**
     * Marca todos os RDOs como consolidados na competência.
     * Define o status como {@code CLOSED_COMPETENCE}, atualiza a competência e insere o log da ação.
     *
     * @param rdos       Lista de RDOs a serem consolidados
     * @param competence Competência
     * @param user       Usuário responsável
     * @return Lista de RDOs alterados
     */
    private List<RDOEntity> markAsClosed(List<RDOEntity> rdos, YearMonth competence, RDOLoggerUser user) {
        return rdos.stream()
                .peek(rdo -> {
                    rdo.setStatusDP(RDOStatusDP.CLOSED_COMPETENCE);
                    rdo.setCompetence(competence.toString());
                    if (rdo.getLoggers() == null || rdo.getLoggers().isEmpty()) {
                        rdo.setLoggers(List.of(competenceLogger(user)));

                    } else {
                        rdo.getLoggers().add(competenceLogger(user));
                    }
                })
                .collect(Collectors.toList());
    }

    private RDOLogger competenceLogger(RDOLoggerUser user) {
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.COMPETENCE)
                .justification("Competência fechada")
                .date(LocalDateTime.now())
                .sector(RH_SECTOR)
                .user(user)
                .build();
    }

    /**
     * Executa a atualização em lote (bulk) no MongoDB para todos os RDOs alterados.
     * Campos atualizados: statusDP, competence, loggers
     *
     * @param items RDOs modificados
     */
    private void batchUpdateItems(List<RDOEntity> items) {
        BulkOperations ops = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                RDOEntity.class
        );

        items.forEach(dto -> {
            Query q = Query.query(Criteria.where("_id").is(dto.getId()));
            Update u = new Update()
                    .set("statusDP", dto.getStatusDP())
                    .set("competence", dto.getCompetence())
                    .set("loggers", dto.getLoggers());

            ops.updateOne(q, u);
        });

        ops.execute();
    }

}

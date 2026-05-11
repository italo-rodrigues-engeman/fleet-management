package com.indux.modules.ppu.application.services.bm;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.request.CreateBMRequest;
import com.indux.modules.ppu.application.dtos.request.MioDivergenceJustificationItem;
import com.indux.modules.ppu.domain.entities.bm.MioDivergenceJustification;
import com.indux.modules.ppu.application.dtos.response.bm.BMGrid;
import com.indux.modules.ppu.application.dtos.response.bm.BMModel;
import com.indux.modules.ppu.application.dtos.response.bm.BMPlatformReport;
import com.indux.modules.ppu.application.projection.PendingRDOProjection;
import com.indux.modules.ppu.application.services.ppu.PPUGridCacheService;
import com.indux.modules.ppu.application.services.rdo.GetMissingRDOsUseCase;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.application.services.rdo.operation.bm.BMPlatformHandler;
import com.indux.modules.ppu.domain.entities.bm.RMLog;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.PPUEnhancedGridProjection;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.bm.BMMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BMService {
    private final BMRepository repository;
    private final PPURepository ppuRepository;
    private final BMMapper mapper;
    private final RDORepository rdoRepository;
    private final PPUGridCacheService ppuGrid;
    private final MongoTemplate mongoTemplate;
    private final GetMissingRDOsUseCase missingUseCase;
    private final BMPlatformHandler bmHandler;
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BMService(BMRepository repository, BMPlatformHandler bmHandler, PPURepository ppuRepository, BMMapper mapper,
            RDORepository rdoRepository, PPUGridCacheService ppuGrid, MongoTemplate mongoTemplate,
            GetMissingRDOsUseCase missingUseCase) {
        this.repository = repository;
        this.bmHandler = bmHandler;
        this.ppuRepository = ppuRepository;
        this.mapper = mapper;
        this.rdoRepository = rdoRepository;
        this.ppuGrid = ppuGrid;
        this.mongoTemplate = mongoTemplate;
        this.missingUseCase = missingUseCase;
    }

    public void create(@Validated CreateBMRequest request) {
        var ppu = ppuRepository.findById(request.ppuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada pelo ID informado."));

        var entity = BMEntity.builder()
                .ppu(ppu)
                .projectId(request.projectId())
                .period(request.period())
                .contract(ppu.getContract())
                .status(DocumentStatus.ABERTO)
                .build();

        repository.save(entity);
    }

    public BMModel getByID(String id) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada pelo ID informado."));
        return mapper.toModel(entity);
    }

    public Page<BMGrid> getAllGrid(Pageable pageable) {
        var bms = repository.findAllBy(pageable);
        var ppus = ppuGrid.findAllEnhancedGrid(Pageable.unpaged()).getContent();
        Map<String, PPUEnhancedGridProjection> ppuMap = ppus.stream()
                .collect(Collectors.toMap(PPUEnhancedGridProjection::getId, p -> p, (p1, p2) -> p1));

        return bms.map(bm -> {
            var ppu = ppuMap.get(bm.getPpu().getId());
            BigDecimal totalRM = sumRM(bm.getAuditRMLog());
            BigDecimal percentage = calculatePercentage(totalRM, bm.getValueClosed());

            var result = mapper.toGrid(bm, ppu, percentage);
            return result;
        });
    }

    public void approve(String id, String userID) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada pelo ID informado."));
        validate(entity);
        checkAllItems(entity);
        validateAllRdos(entity.getPeriod(), entity.getProjectId());
        var rdos = rdoRepository.findAllByDateAndPpuID(entity.getPeriod().getStart(), entity.getPeriod().getEnd(),
                entity.getPpuId());
        var sums = bmHandler.fetchAll(entity, rdos);
        var total = sums.stream().filter((e) -> e.getType().equals("total")).findFirst().orElse(new BMPlatformReport());
        entity.setValueClosed(total.getTotalValue());
        entity.setRdosClosed(rdos.stream().map(RDOEntity::getId).toList());
        entity.setApprovedBy(userID);
        entity.setStatus(DocumentStatus.APROVADO);
        entity.setApprovedAt(Instant.now());
        repository.save(entity);
    }

    private void validateAllRdos(DateRange range, Long project) {
        validateNoPendingRdos(range.getStart(), range.getEnd(), project);
        validateNoMissingRdos(range.getStart(), range.getEnd(), project);
    }

    private BigDecimal calculatePercentage(BigDecimal totalRM, BigDecimal totalBM) {
        if (totalBM == null || totalBM.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        if (totalRM == null) {
            return BigDecimal.ZERO;
        }

        return totalRM
                .divide(totalBM, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumRM(List<RMLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return logs.stream()
                .map(RMLog::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateNoPendingRdos(LocalDate start, LocalDate end, Long project) {
        List<PendingRDOProjection> pendings = rdoRepository.findPendingsInPeriodAndProjectId(start, end, project);
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
            throw new ModuleFailure(
                    "Existem RDOs faltando no período. Por favor, preencha-os antes de continuar. Detalhes: "
                            + details);
        }
    }

    private void validate(BMEntity entity) {
        var allAuditCompleted = entity.getAuditMIOChecked() & entity.getAuditSAMCChecked();
        if (!allAuditCompleted)
            throw new ModuleFailure("Auditorias não validadas no sistema.");
    }

    private void checkAllItems(BMEntity entity) {
        var hasEmptyList = entity.getTimelines().isEmpty() || entity.getResumeMonthly().isEmpty()
                || entity.getResumePlatforms().isEmpty() || entity.getDetails().isEmpty();
        if (hasEmptyList)
            throw new ModuleFailure("BM com itens vazio.");
    }

    public void consolidate(String id, BMModel request, JwtAuthenticationToken token) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada pelo ID informado."));
        var currentUser = LoggerUserHelper.createUser(token);
        var logger = bmLogger(currentUser);
        batchUpdate(entity.getRdosClosed(), logger);
        if (request.justification() != null) {
            entity.setJustification(request.justification());
        }
        entity.setUpdatedAt(Instant.now());
        entity.setStatus(DocumentStatus.FINALIZADO);

        repository.save(entity);
    }

    private void batchUpdate(List<String> rdos, RDOLogger logger) {
        BulkOperations ops = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                RDOEntity.class);
        rdos.forEach(id -> {
            Query q = Query.query(Criteria.where("_id").is(id));
            Update u = new Update()
                    .set("statusOP", RDOStatusOP.BM)
                    .addToSet("logger", logger);
            ops.updateOne(q, u);
        });
        ops.execute();
    }

    private RDOLogger bmLogger(RDOLoggerUser user) {
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.BM)
                .justification("Consolidação de BM")
                .date(LocalDateTime.now())
                .sector("OP")
                .user(user)
                .build();
    }

    /**
     * Persiste justificativas em lote para divergências MIO.
     * As justificativas ficam em {@code BMEntity.mioJustifications}, separadas do
     * log de auditoria, para não serem perdidas em re-execuções de auditoria.
     *
     * <p>
     * Comportamento de upsert: se já existe uma justificativa para a mesma
     * combinação {@code registration + date}, o texto é atualizado; caso contrário,
     * uma nova entrada é inserida.
     * </p>
     *
     * @param bmId  identificador da BM
     * @param items lista de justificativas a aplicar
     */
    public void justifyMioDivergences(String bmId, List<MioDivergenceJustificationItem> items) {
        var entity = repository.findById(bmId)
                .orElseThrow(() -> new ModuleNotFoundFailure("BM não encontrada pelo ID informado."));

        var justifications = entity.getMioJustifications();

        // Índice dos existentes para upsert eficiente
        var existingIndex = justifications.stream()
                .collect(Collectors.toMap(
                        j -> j.getRegistration() + "|" + j.getDate(),
                        j -> j,
                        (a, b) -> a));

        items.forEach(item -> {
            var key = item.registration() + "|" + item.date();
            if (existingIndex.containsKey(key)) {
                // Atualiza a justificativa existente
                var existing = existingIndex.get(key);
                existing.setJustification(item.justification());
                existing.setJustifiedAt(Instant.now());
            } else {
                // Insere nova justificativa
                justifications.add(
                        MioDivergenceJustification.builder()
                                .registration(item.registration())
                                .date(item.date())
                                .justification(item.justification())
                                .justifiedAt(Instant.now())
                                .build());
            }
        });

        entity.setUpdatedAt(Instant.now());
        repository.save(entity);
    }
}

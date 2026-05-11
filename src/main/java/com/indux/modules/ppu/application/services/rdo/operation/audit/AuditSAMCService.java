package com.indux.modules.ppu.application.services.rdo.operation.audit;

import com.indux.core.domain.service.importers.ExcelReader;
import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.services.rdo.GetMissingRDOsUseCase;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.domain.services.bm.audit.AuditEngine;
import com.indux.modules.ppu.domain.services.bm.audit.PlatformAliasResolver;
import com.indux.modules.ppu.domain.services.bm.audit.samc.AuditSAMC;
import com.indux.modules.ppu.domain.entities.bm.BMSamcLog;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditBatchContext;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditContext;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.presentation.dtos.MissingRDOResponse;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.infra.exceptions.MissingHeaderException;
import com.indux.modules.ppu.infra.importers.ExcelReaderAuditSamc;
import com.indux.modules.ppu.infra.mapper.bm.AuditMapper;
import com.indux.modules.ppu.presentation.dtos.audit.AuditResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditSAMCService implements AuditSAMC<AuditResponse> {

    private static final Set<String> ALLOWED_STATUSES = Set.of("APROVADO", "FINALIZADO");

    private final RDORepository repository;
    private final PPURepository ppuRepository;
    private final BMRepository bmRepository;
    private final AuditMapper mapper;
    private final GetMissingRDOsUseCase missingRdos;
    private final AuditEngine engine;
    private final ExcelReader<SAMCRow> excelReader;
    private final PlatformAliasResolver platformResolver;

    public AuditSAMCService(RDORepository repository,
                            PPURepository ppuRepository,
                            BMRepository bmRepository, AuditMapper mapper,
                            GetMissingRDOsUseCase missingRdos,
                            AuditEngine engine,
                            ExcelReader<SAMCRow> excelReader,
                            PlatformAliasResolver platformResolver) {
        this.repository = repository;
        this.ppuRepository = ppuRepository;
        this.bmRepository = bmRepository;
        this.mapper = mapper;
        this.missingRdos = missingRdos;
        this.engine = engine;
        this.excelReader = excelReader;
        this.platformResolver = platformResolver;
    }

    @Override
    public List<AuditResponse> audit(String rdoID, MultipartFile file, JwtAuthenticationToken token) throws ParseException {
        RDOEntity rdo = repository.findById(rdoID).orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrado."));
        PPUEntity ppu = ppuRepository.findById(rdo.getPpuId()).orElseThrow(() -> new ModuleNotFoundFailure("RDO sem PPU encontrada."));
        RDOLoggerUser user = LoggerUserHelper.createUser(token);
        List<SAMCRow> samcRows = parseSamcFile(file, ppu);
        String rdoPlatformSigla = platformResolver.toSigla(rdo.getPlatform());
        samcRows = samcRows.stream()
                .filter(e -> platformResolver.toSigla(e.local()).equals(rdoPlatformSigla))
                .toList();

        AuditContext ctx = new AuditContext(rdo.getDate(), rdo.getPlatform(), ppu, rdo, samcRows);
        var divergences = engine.audit(ctx);

        rdo.getLoggers().add(createRdoLog(user, "Auditoria 1: Encontrado " + divergences.size() + " divergências."));
        repository.save(rdo);
        return mapper.toResponses(divergences);
    }


    protected List<SAMCRow> parseSamcFile(MultipartFile file, PPUEntity ppu) {
        Map<String, String> headerMapping = new HashMap<>(Map.of(
                "contrato", "Contrato",
                "local", "Local",
                "data", "Período Fim",
                "numeroDetalhamento", "Número detalhamento EAC",
                "descricaoServico", "Descrição do Serviço",
                "quantidadeExecutada", "Qntd Executada",
                "status", "Status RO"
        ));
        Optional.ofNullable(ppu)
                .map(PPUEntity::getAuditableConfig)
                .map(AuditableConfig::getColumnName)
                .filter(Objects::nonNull)
                .ifPresent(name -> headerMapping.put("auditableValue", name));
        try {
            return (excelReader instanceof ExcelReaderAuditSamc samcReader
                    ? samcReader.readerSpecificHeaders(file, headerMapping, Optional.ofNullable(ppu).map(PPUEntity::getAuditableConfig).orElse(null))
                    : excelReader.readerSpecificHeaders(file, headerMapping))
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(row -> {
                        String status = Optional.ofNullable(row.status()).orElse("").trim();
                        return !status.equalsIgnoreCase("Cancelado");
                    })
                    .toList();
        } catch (MissingHeaderException e) {
            throw new ModuleBadRequest(e.getMessage());
        }
    }
    @Override
    public List<AuditResponse> auditBatch(String bmID, MultipartFile file, JwtAuthenticationToken user) throws ParseException {
        var bm = bmRepository.findById(bmID).orElseThrow(() -> new ModuleNotFoundFailure("BM naõ encontrada"));
        var ppu = ppuRepository.findById(bm.getPpuId()).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada."));
        RDOLoggerUser loggerUser = LoggerUserHelper.createUser(user);

        validMissingRDOS(bm.getPeriod().getStart(), bm.getPeriod().getEnd(), ppu);

        List<SAMCRow> allSamc = parseSamcFile(file, ppu);
        List<RDOEntity> rdos = getRDOsForGeneralAudit(ppu, bm.getPeriod().getStart(), bm.getPeriod().getEnd());

        AuditBatchContext ctx = new AuditBatchContext(
                bm.getPeriod().getStart(),
                bm.getPeriod().getEnd(),
                ppu,
                rdos,
                allSamc,
                Map.of(),
                Map.of()
        );

        List<AuditDivergence> divergences = engine.audit(ctx, ALLOWED_STATUSES);

        logAuditInRDOs(rdos, loggerUser, divergences.size());

        if (!bm.isFinished()) {
            BMSamcLog log = createBmLog(loggerUser, divergences);
            bm.getAuditSAMCLog().add(log);
            bm.setUpdatedAt(Instant.now());
            if (divergences.isEmpty()) bm.setAuditSAMCChecked(true);
            bmRepository.save(bm);
        }

        return mapper.toResponses(divergences);
    }

    @Override
    public List<AuditResponse> auditMultipleRDOs(List<String> rdoIDs, MultipartFile file, JwtAuthenticationToken token) throws ParseException {
        if (rdoIDs == null || rdoIDs.isEmpty()) {
            throw new ModuleBadRequest("Informe ao menos um RDO para auditoria.");
        }

        List<RDOEntity> rdos = repository.findByIdIn(rdoIDs);
        validateAllRDOsWereFound(rdoIDs, rdos);

        Map<String, PPUEntity> ppuById = findPpuById(rdos);
        validateSameDateAndContract(rdos, ppuById);

        PPUEntity referencePpu = ppuById.get(rdos.get(0).getPpuId());
        List<SAMCRow> allSamcRows = parseSamcFile(file, referencePpu);
        RDOLoggerUser loggerUser = LoggerUserHelper.createUser(token);

        List<AuditDivergence> allDivergences = new ArrayList<>();
        for (RDOEntity rdo : rdos) {
            List<SAMCRow> samcRowsByPlatform = filterSamcByPlatform(allSamcRows, rdo.getPlatform());
            AuditContext context = new AuditContext(rdo.getDate(), rdo.getPlatform(), ppuById.get(rdo.getPpuId()), rdo, samcRowsByPlatform);
            List<AuditDivergence> divergences = engine.audit(context);
            allDivergences.addAll(divergences);

            if (rdo.getLoggers() == null) {
                rdo.setLoggers(new ArrayList<>());
            }
            rdo.getLoggers().add(createRdoLog(
                    loggerUser,
                    String.format("Auditoria múltipla SAMC: Encontrado %d divergências para este RDO.", divergences.size())));
        }

        repository.saveAll(rdos);
        return mapper.toResponses(allDivergences);
    }

    void validMissingRDOS(LocalDate start, LocalDate end, PPUEntity ppu) {
        var missingDTO = new MissingRDORequest(ppu.getContractId(), ppu.getProjectId(), null, start, end);
        var missing = missingRdos.execute(missingDTO);

        if (!missing.missing().isEmpty()) {
            Map<LocalDate, List<String>> missingByDate = missing.missing().stream()
                    .collect(Collectors.groupingBy(
                            MissingRDOResponse::getData,
                            TreeMap::new,
                            Collectors.mapping(MissingRDOResponse::getPlataforma, Collectors.toList())
                    ));

            StringBuilder message = new StringBuilder("Há RDOs em falta no período de medição:\n\n");
            missingByDate.forEach((date, platforms) -> {
                String plataformaInfo = String.join(", ", platforms);
                message.append("- ").append(date).append(": ").append(plataformaInfo).append("\n");
            });

            message.append("\nAprove ou preencha os RDOs antes de continuar.");
            throw new ModuleFailure(message.toString());
        }
    }

    private List<RDOEntity> getRDOsForGeneralAudit(PPUEntity ppu, LocalDate start, LocalDate end) {
        if (ppu.getPlatforms() == null || ppu.getPlatforms().isEmpty()) return List.of();
        List<RDOEntity> out = new ArrayList<>();
        for (String platform : ppu.getPlatforms()) {
            out.addAll(repository.findAllByPlatformAndDateAndPpuID(platform, start, end, ppu.getId()));
        }
        return out;
    }

    private Map<String, PPUEntity> findPpuById(List<RDOEntity> rdos) {
        return rdos.stream()
                .map(RDOEntity::getPpuId)
                .distinct()
                .collect(Collectors.toMap(
                        ppuId -> ppuId,
                        ppuId -> ppuRepository.findById(ppuId)
                                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para o RDO informado."))
                ));
    }

    private void validateAllRDOsWereFound(List<String> requestedIds, List<RDOEntity> foundRdos) {
        Set<String> foundIds = foundRdos.stream()
                .map(RDOEntity::getId)
                .collect(Collectors.toSet());
        List<String> missingIds = requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new ModuleNotFoundFailure("RDO(s) não encontrado(s): " + String.join(", ", missingIds));
        }
    }

    private void validateSameDateAndContract(List<RDOEntity> rdos, Map<String, PPUEntity> ppuById) {
        RDOEntity referenceRdo = rdos.get(0);
        LocalDate referenceDate = referenceRdo.getDate();
        Long referenceContractId = ppuById.get(referenceRdo.getPpuId()).getContractId();

        for (RDOEntity rdo : rdos) {
            if (!Objects.equals(referenceDate, rdo.getDate())) {
                throw new ModuleBadRequest("Todos os RDOs devem possuir a mesma data para auditoria em lote.");
            }

            Long contractId = ppuById.get(rdo.getPpuId()).getContractId();
            if (!Objects.equals(referenceContractId, contractId)) {
                throw new ModuleBadRequest("Todos os RDOs devem pertencer ao mesmo contrato para auditoria em lote.");
            }
        }
    }

    private List<SAMCRow> filterSamcByPlatform(List<SAMCRow> samcRows, String platform) {
        String rdoPlatformSigla = platformResolver.toSigla(platform);
        return samcRows.stream()
                .filter(row -> platformResolver.toSigla(row.local()).equals(rdoPlatformSigla))
                .toList();
    }

    private RDOLogger createRdoLog(RDOLoggerUser user, String justification) {
        final String sector = "OP";
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.EDIT)
                .justification(justification)
                .coordinatorJustification(null)
                .date(LocalDateTime.now())
                .sector(sector)
                .user(user)
                .build();
    }

    private BMSamcLog createBmLog(RDOLoggerUser user, List<AuditDivergence> result) {
        return BMSamcLog.builder()
                .auditAt(Instant.now())
                .success(result.isEmpty())
                .divergences(result)
                .user(user)
                .build();
    }

    void logAuditInRDOs(List<RDOEntity> rdos, RDOLoggerUser loggerUser, int divergenceCount) {
        String logMessage = String.format(
                "Auditoria 2: Auditoria geral realizada em %d RDOs. Encontrado %d divergências.",
                rdos.size(), divergenceCount);

        for (RDOEntity rdo : rdos) {
            RDOLogger log = RDOLogger.builder()
                    .wasAnalyzed(true)
                    .isInfoCorrect(divergenceCount == 0)
                    .action(RDOLoggerType.EDIT)
                    .justification(logMessage)
                    .coordinatorJustification(null)
                    .date(LocalDateTime.now())
                    .sector("OP")
                    .user(loggerUser)
                    .build();

            rdo.getLoggers().add(log);
        }
        repository.saveAll(rdos);
    }
}

package com.indux.modules.ppu.application.services.bm.audit.rm;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.application.dtos.response.bm.BMPlatformReport;
import com.indux.modules.ppu.application.dtos.response.RMLogResponse;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.application.services.rdo.operation.bm.BMPlatformHandler;
import com.indux.modules.ppu.domain.entities.bm.RMLog;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AuditRM {
    private final BMRepository repository;
    private final AttachmentService attachmentService;

    public AuditRM(BMRepository repository, AttachmentService attachmentService) {
        this.repository = repository;
        this.attachmentService = attachmentService;
    }


    /**
     * Executa a auditoria entre o valor total de uma BM e o valor bruto informado em um
     * arquivo de RM (PDF).
     *
     * <p>Fluxo resumido:</p>
     * <ul>
     *     <li>Carrega a BM pelo {@code bmID};</li>
     *     <li>Recupera o registro de total da BM via {@code BMPlatformHandler.fetchAll};</li>
     *     <li>Lê os arquivos PDF de RM e extrai os valores brutos via {@code RMPDFReader};</li>
     *     <li>Compara os valores BM x RM;</li>
     *     <li>Quando a BM não estiver finalizada:
     *         <ul>
     *             <li>registra um {@code BMRMLog} com os valores comparados e o resultado;</li>
     *             <li>marca {@code auditRMChecked = true} em caso de sucesso;</li>
     *             <li>persiste a BM atualizada.</li>
     *         </ul>
     *     </li>
     *     <li>Em caso de divergência, lança {@code ModuleFailure} com mensagem contendo o
     *     valor esperado (BM) e o valor entregue no arquivo de RM.</li>
     * </ul>
     *
     * @param bmID  identificador da BM a ser auditada
     * @param files arquivos de RM (PDF) contendo o valor bruto a ser comparado
     * @param token usuário autenticado, utilizado para registro de log
     * @throws Exception caso ocorra qualquer falha na leitura do PDF, recuperação da BM
     *                   ou processamento da auditoria
     */
    public RMLogResponse execute(String bmID, List<MultipartFile> files, JwtAuthenticationToken token) throws Exception {
        var bm = findBMOrThrow(bmID);
        var total = bm.getValueClosed();
        var loggerUser = LoggerUserHelper.createUser(token);
        
        var totalValueExistingRM = calculateExistingRMTotal(bm);
        var parsedResults = validateAndParseFiles(files, bm);
        var totalValueNewRM = sumParsedResults(parsedResults);
        
        var entities = attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID);
        var rmLogs = createRMLogs(parsedResults, entities, loggerUser);
        
        persistAuditResults(bm, rmLogs);
        
        var totalValueRM = totalValueExistingRM.add(totalValueNewRM);
        var porcentagem = calculatePercentage(totalValueRM, total);
        
        return buildResponse(bm, total, totalValueRM, porcentagem);
    }

    private BMEntity findBMOrThrow(String bmID) {
        return repository.findById(bmID)
                .orElseThrow(() -> new ModuleFailure("BM não encontrada pelo ID informado."));
    }


    private BigDecimal calculateExistingRMTotal(BMEntity bm) {
        return bm.getAuditRMLog().stream()
                .map(RMLog::getValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<RMPDFReader.Resumo> validateAndParseFiles(List<MultipartFile> files, BMEntity bm) throws Exception {
        String expectedContractCode = (String) bm.getPpu().getContract().get("codeSap");
        var parsedResults = new ArrayList<RMPDFReader.Resumo>();
        
        for (MultipartFile file : files) {
            var pdfResult = RMPDFReader.parse(file);
            validateContractCode(pdfResult, expectedContractCode, file.getOriginalFilename());
            parsedResults.add(pdfResult);
        }
        
        return parsedResults;
    }

    private void validateContractCode(RMPDFReader.Resumo pdfResult, String expectedCode, String fileName) {
        if (!pdfResult.contract().equals(expectedCode)) {
            throw new ModuleFailure(
                    "Código SAP do contrato no arquivo '" + fileName + 
                    "' diverge. Esperado: " + expectedCode + 
                    ", Encontrado: " + pdfResult.contract()
            );
        }
    }

    private BigDecimal sumParsedResults(List<RMPDFReader.Resumo> parsedResults) {
        return parsedResults.stream()
                .map(RMPDFReader.Resumo::valorBruto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<RMLog> createRMLogs(
            List<RMPDFReader.Resumo> parsedResults,
            List<AttachmentEntity> entities,
            RDOLoggerUser loggerUser
    ) {
        var rmLogs = new ArrayList<RMLog>();
        for (int i = 0; i < parsedResults.size(); i++) {
            var pdfResult = parsedResults.get(i);
            var entity = entities.get(i);
            var rmLog = createRMLog(pdfResult.valorBruto(), entity, loggerUser);
            rmLogs.add(rmLog);
        }
        return rmLogs;
    }

    private void persistAuditResults(BMEntity bm, List<RMLog> rmLogs) {
        bm.getAuditRMLog().addAll(rmLogs);
        bm.setAuditRMChecked(true);
        repository.save(bm);
    }

    private RMLogResponse buildResponse(
            BMEntity bm,
           BigDecimal total,
            BigDecimal totalValueRM,
            Double porcentagem
    ) {
        return RMLogResponse.builder()
                .totalBM(total)
                .rms(bm.getAuditRMLog())
                .totalRM(totalValueRM)
                .porcentagem(porcentagem)
                .build();
    }

    RMLog createRMLog(BigDecimal value, AttachmentEntity file, RDOLoggerUser user) {
        return RMLog
                .builder()
                .id(java.util.UUID.randomUUID().toString())
                .attachment(file)
                .user(user)
                .upload_At(Instant.now())
                .value(value)
                .build();
    }

    private Double calculatePercentage(BigDecimal totalRM, BigDecimal totalBM) {
        if (totalBM == null || totalBM.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        if (totalRM == null) {
            return 0.0;
        }
        
        return totalRM
                .divide(totalBM, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    public void removeRMLog(String bmID, String rmLogId) {
        var bm = repository.findById(bmID)
            .orElseThrow(() -> new ModuleFailure("BM não encontrada pelo ID informado."));
        
        if (bm.isFinished()) {
            throw new ModuleFailure("Não é possível remover RM de uma BM já aprovada.");
        }
        
        boolean removed = bm.getAuditRMLog().removeIf(log -> log.getId().equals(rmLogId));
        
        if (!removed) {
            throw new ModuleFailure("RMLog não encontrado com ID: " + rmLogId);
        }
        
        bm.setAuditRMChecked(false);
        repository.save(bm);
    }


}

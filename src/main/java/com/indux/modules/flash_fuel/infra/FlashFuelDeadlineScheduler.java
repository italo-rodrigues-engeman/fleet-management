package com.indux.modules.flash_fuel.infra;

import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FlashFuelDeadlineScheduler {
    private final ModuleManagementService moduleService;
    private final MongoTemplate mongoTemplate;
    /**
     * UUID do módulo “Ocorrências Financeiras”
     */
    @Value("${module.flashFuel.id}")
    private String FLASH_COMBUSTIVEL;

    public FlashFuelDeadlineScheduler(ModuleManagementService moduleService,
                                      MongoTemplate mongoTemplate) {
        this.moduleService = moduleService;
        this.mongoTemplate = mongoTemplate;
    }

    ;
    private static final Map<DocumentStatus, Integer> SITUACAO_ORDINAL = Map.of(
            DocumentStatus.ATRASADO, 1,
            DocumentStatus.ANDAMENTO, 2,
            DocumentStatus.FINALIZADO, 3
    );

    /**
     * A cada 5 minutos:
     * Marca como ATRASADO toda a ocorrência que:
     * - pertença ao módulo financeiro,
     * - esteja na etapa X (etapa_atual > 0),
     * - não esteja CANCELADA, REJEITADA ou APROVADA,
     * - e cujo data_log (data do último 'log') seja anterior a now - prazoHoras.
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void markOverdueOccurrences() {
        Instant now = Instant.now();

        Set<DocumentStatus> ignore = Set.of(
                DocumentStatus.REJEITADO,
                DocumentStatus.APROVADO
        );

        Modulo finance = moduleService.getModuleByID(UUID.fromString(FLASH_COMBUSTIVEL));

        for (StepModule cfg : finance.getConfigEtapas()) {
            int step = cfg.getEtapa();
            if (step == 0) continue;

            long prazoHoras = cfg.getTempo();
            Date cutoff = Date.from(now.minus(prazoHoras, ChronoUnit.HOURS));

            Query q = Query.query(
                    Criteria.where("etapa_atual").is(step)
                            .and("situacao").nin(ignore)
                            .and("data_log").lt(cutoff)
            );

            Update u = new Update()
                    .set("situacao", DocumentStatus.ATRASADO)
                    .set("situacaoOrder",
                            SITUACAO_ORDINAL.getOrDefault(DocumentStatus.ATRASADO, Integer.MAX_VALUE));

            mongoTemplate.updateMulti(q, u, FlashFuel.class);
        }
    }

}

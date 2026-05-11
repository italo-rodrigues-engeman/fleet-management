package com.indux.modules.advance_suppliers.infra;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.service.notification.NotificationService;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.notification.Notification;
import com.indux.core.domain.model.notification.NotificationType;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.advance_suppliers.domain.entities.AdvanceSuppliers;
import com.indux.modules.advance_suppliers.domain.entities.PendingNotification;
import com.indux.modules.advance_suppliers.domain.repository.PendingNotificationRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class AdvanceSuppliersScheduler {
    private static final Logger log = LoggerFactory.getLogger(AdvanceSuppliersScheduler.class);
    private final PendingNotificationRepository pendingNotificationRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ModuleManagementService moduleService;
    private final MongoTemplate mongoTemplate;

    /**
     * UUID do módulo "Adiantamento de Fornecedores"
     */
    @Value("${module.advance_suppliers.id}")
    private String ADVANCE_MODULE_ID;

    private static final Map<DocumentStatus, Integer> SITUACAO_ORDINAL = Map.of(
            DocumentStatus.ATRASADO, 1,
            DocumentStatus.ANDAMENTO, 2,
            DocumentStatus.FINALIZADO, 3
    );

    private static final Set<DocumentStatus> IGNORE_STATUS = Set.of(
            DocumentStatus.REJEITADO,
            DocumentStatus.APROVADO,
            DocumentStatus.FINALIZADO
    );

    public AdvanceSuppliersScheduler(
            PendingNotificationRepository pendingNotificationRepository,
            NotificationService notificationService,
            UserService userService,
            ModuleManagementService moduleService,
            MongoTemplate mongoTemplate) {
        this.pendingNotificationRepository = pendingNotificationRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.moduleService = moduleService;
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(cron = "0 9 * * * *") // Executa todo dia às 9h
    public void processPendingInvoiceNotifications() throws Exception {
        try {
            // Use the corrected method with explicit parameter
            List<PendingNotification> tasks = pendingNotificationRepository
                    .findPendingNotifications(Instant.now());

            for (PendingNotification task : tasks) {
                for (String userId : task.getResponsibleUserIds()) {
                    try {
                        var user = userService.getUserById(userId).orElseThrow(() -> new ModuleFailure("Erro ao buscar usuário para envio da notificação"));
                        var notification = getNotification(task, user);
                        notificationService.send(notification);

                    } catch (Exception e) {
                        log.error("Erro ao enviar notificação para o usuário {}: {}", userId, e.getMessage(), e);
                        // Continue processing other users instead of throwing
                    }
                }

                task.setNotified(true);
                pendingNotificationRepository.save(task);
            }
        } catch (Exception e) {
            log.error("Erro no processamento de notificações pendentes: {}", e.getMessage(), e);
            // Don't re-throw to avoid breaking the scheduler
        }
    }

    @NotNull
    private static Notification getNotification(PendingNotification task, SimpleUser user) {
        var notificationValue = new Notification();
        notificationValue.setTo(user);
        notificationValue.setTemplateName(null);
        notificationValue.setVariables(null);
        notificationValue.setSubject("Pendência de Nota Fiscal");
        notificationValue.setMessage(String.format(
                "Você precisa enviar a nota fiscal da ocorrência %s. Acesse o sistema e conclua essa etapa.",
                task.getCode()
        ));
        notificationValue.setType(NotificationType.EMAIL);
        return notificationValue;
    }

    /**
     * A cada 6 minutos:
     * Marca como ATRASADO toda a ocorrência que:
     * - pertença ao módulo de adiantamento de fornecedores,
     * - esteja na etapa X (etapa_atual > 0),
     * - não esteja REJEITADA, APROVADA ou FINALIZADA,
     * - e cujo data_log (data do último 'log') seja anterior a now - prazoHoras da etapa atual.
     * 
     * Para a etapa 3, considera como atrasado baseado no campo paymentDate.
     * Para a etapa 4, considera como atrasado baseado no campo invoiceDate.
     */
    @Scheduled(cron = "0 0/6 * * * *")
    public void markOverdueOccurrences() {
        Instant now = Instant.now();

        try {
            Modulo advance = moduleService.getModuleByID(UUID.fromString(ADVANCE_MODULE_ID));

            // Para cada etapa do módulo
            for (StepModule cfg : advance.getConfigEtapas()) {
                int step = cfg.getEtapa();
                if (step == 0) continue; // Ignora etapa 0 (finalizada)

                // Pega o prazo em horas configurado para esta etapa
                long prazoHoras = cfg.getTempo();
                
                try {
                    Query q;
                    
                    if (step == 3) {
                        q = Query.query(
                                Criteria.where("etapa_atual").is(step)
                                        .and("status").nin(IGNORE_STATUS)
                                        .and("paymentDate").lt(LocalDate.now())
                        );
                    } else if (step == 4) {
                        q = Query.query(
                                Criteria.where("etapa_atual").is(step)
                                        .and("status").nin(IGNORE_STATUS)
                                        .and("invoiceDate").lt(LocalDate.now())
                        );
                    } else {
                        // Para outras etapas, usa data_log como referência (Date)
                        Date cutoff = Date.from(now.minus(prazoHoras, ChronoUnit.HOURS));
                        q = Query.query(
                                Criteria.where("etapa_atual").is(step)
                                        .and("status").nin(IGNORE_STATUS)
                                        .and("data_log").lt(cutoff)
                        );
                    }

                    // Conta quantas ocorrências foram encontradas
                    long count = mongoTemplate.count(q, AdvanceSuppliers.class);

                    if (count > 0) {

                        
                        // Atualiza para ATRASADO e define ordem de exibição
                        Update u = new Update()
                                .set("situacao", DocumentStatus.ATRASADO)
                                .set("situacaoOrder", SITUACAO_ORDINAL.get(DocumentStatus.ATRASADO));

                        mongoTemplate.updateMulti(q, u, AdvanceSuppliers.class);
                    }
                } catch (Exception e) {
                    log.error("Erro ao processar etapa {}: {}", step, e.getMessage(), e);
                    // Continue processing other steps
                }
            }
        } catch (Exception e) {
            // Log do erro mas não interrompe o scheduler
            log.error("Erro no scheduler de adiantamento de fornecedores: {}", e.getMessage(), e);
        }
    }
}

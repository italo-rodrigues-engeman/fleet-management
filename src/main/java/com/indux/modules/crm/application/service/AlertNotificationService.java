package com.indux.modules.crm.application.service;

import com.indux.core.infra.notifcation.mailsender.CustomMailSender;
import com.indux.modules.crm.application.gateway.AlertGateway;
import com.indux.modules.crm.domain.entity.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertNotificationService {

    private final AlertGateway alertGateway;
    private final CustomMailSender mailSender;

    public void processDailyAlerts() {
        log.info("Starting processing of daily CRM alerts...");
        List<Alert> pendingAlerts = alertGateway.findPendingAlertsForDate(LocalDate.now());
        
        if (pendingAlerts.isEmpty()) {
            log.info("No pending alerts found for today.");
            return;
        }

        log.info("Found {} pending alerts for today.", pendingAlerts.size());

        for (Alert alert : pendingAlerts) {
            try {
                if (alert.getEngemanAgent() != null && alert.getEngemanAgent().getMainEmail() != null) {
                    String to = alert.getEngemanAgent().getMainEmail();
                    String subject = "CRM: Lembrete de Ação Futura";
                    String body = String.format("Olá %s,\n\nVocê tem um alerta agendado para hoje.\n\nDescrição da Ação: %s",
                            alert.getEngemanAgent().getName(),
                            alert.getFutureActionDescription());

                    mailSender.sendGenericEmail(to, subject, body, Collections.emptyMap());
                    alertGateway.markAsSent(alert.getId());
                    log.info("Alert {} sent successfully to {}", alert.getId(), to);
                } else {
                    log.warn("Alert {} does not have a valid engemanAgent or email. Skipping...", alert.getId());
                }
            } catch (Exception e) {
                log.error("Failed to send alert {}: {}", alert.getId(), e.getMessage());
            }
        }
        log.info("Finished processing daily CRM alerts.");
    }
}

package com.indux.modules.crm.infra.scheduler;

import com.indux.modules.crm.application.service.AlertNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final AlertNotificationService alertNotificationService;

    /**
     * Executes every minute for testing purposes.
     * Use "0 0 8 * * *" to execute every day at 08:00 AM.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void scheduleDailyAlerts() {
        alertNotificationService.processDailyAlerts();
    }
}

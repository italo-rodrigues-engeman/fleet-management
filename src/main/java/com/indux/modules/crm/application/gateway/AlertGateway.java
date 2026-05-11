package com.indux.modules.crm.application.gateway;

import com.indux.modules.crm.domain.entity.Alert;
import java.time.LocalDate;
import java.util.List;

public interface AlertGateway {
    
    List<Alert> findPendingAlertsForDate(LocalDate date);
    
    void markAsSent(String alertId);
}

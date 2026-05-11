package com.indux.modules.crm.presentation;

import com.indux.modules.crm.application.service.AlertNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crm/alerts")
@RequiredArgsConstructor
public class AlertTestController {

    private final AlertNotificationService alertNotificationService;

    @GetMapping("/force-send")
    public ResponseEntity<String> forceSendAlerts() {
        alertNotificationService.processDailyAlerts();
        return ResponseEntity.ok("O processamento de alertas diários foi acionado com sucesso. Verifique os logs e a caixa de entrada para confirmar os envios.");
    }
}

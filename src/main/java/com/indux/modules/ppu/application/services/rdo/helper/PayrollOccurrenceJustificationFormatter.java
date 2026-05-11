package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.services.UpdateCheckDiff;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class PayrollOccurrenceJustificationFormatter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public String format(RDOEntity rdo, UpdateCheckDiff.Change change) {
        StringBuilder justification = new StringBuilder("Correção de valores na folha de pagamento.\n");
        justification.append("RDO: ").append(rdo.getSequentialId() != null ? rdo.getSequentialId() : "N/A").append("\n");
        justification.append("Data: ").append(rdo.getDate() != null ? rdo.getDate().format(DATE_FORMATTER) : "N/A").append("\n");
        justification.append("Plataforma: ").append(rdo.getPlatform() != null ? rdo.getPlatform() : "N/A").append("\n");
        justification.append("\n");
        
        List<String> details = new ArrayList<>();
        
        if (change.delta().overtimeMinutes() != 0) {
            String before = formatDuration(change.before().overtimeDuration());
            String after = formatDuration(change.after().overtimeDuration());
            details.add(String.format("Hora Extra - Anterior: %s, Atual: %s", before, after));
        }
        
        if (change.delta().nightMinutes() != 0) {
            String before = formatDuration(change.before().nightDuration());
            String after = formatDuration(change.after().nightDuration());
            details.add(String.format("Adicional Noturno - Anterior: %s, Atual: %s", before, after));
        }
        
        if (!details.isEmpty()) {
            justification.append(String.join("; ", details));
        }
        
        return justification.toString();
    }
    
    private String formatDuration(Duration duration) {
        if (duration == null || duration.isZero()) {
            return "0h";
        }
        
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "min";
    }
}

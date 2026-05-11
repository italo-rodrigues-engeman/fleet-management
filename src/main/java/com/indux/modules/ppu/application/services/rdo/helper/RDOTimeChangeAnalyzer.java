package com.indux.modules.ppu.application.services.rdo.helper;

import com.indux.modules.ppu.domain.services.UpdateCheckDiff;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RDOTimeChangeAnalyzer {
    
    public TimeChangesSeparated separateChanges(List<UpdateCheckDiff.Change> changes) {
        List<UpdateCheckDiff.Change> increases = changes.stream()
                .filter(change -> hasTimeIncrease(change.delta()))
                .toList();
        
        List<UpdateCheckDiff.Change> decreases = changes.stream()
                .filter(change -> hasTimeDecrease(change.delta()))
                .toList();
        
        return new TimeChangesSeparated(increases, decreases);
    }
    
    private boolean hasTimeIncrease(UpdateCheckDiff.TimeTotals delta) {
        return delta.overtimeMinutes() > 0 || delta.nightMinutes() > 0;
    }
    
    private boolean hasTimeDecrease(UpdateCheckDiff.TimeTotals delta) {
        return delta.overtimeMinutes() < 0 || delta.nightMinutes() < 0;
    }
    
    public record TimeChangesSeparated(
            List<UpdateCheckDiff.Change> increases,
            List<UpdateCheckDiff.Change> decreases
    ) {}
}

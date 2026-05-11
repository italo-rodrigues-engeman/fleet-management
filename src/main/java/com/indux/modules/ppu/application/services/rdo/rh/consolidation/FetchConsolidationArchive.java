package com.indux.modules.ppu.application.services.rdo.rh.consolidation;

import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationOvertimeEvent;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationPremiumNightEvent;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.events.ConsolidationTeamLeaderEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class FetchConsolidationArchive {
    private final ConsolidationOvertimeEvent overtimeEvent;
    private final ConsolidationTeamLeaderEvent teamLeaderEvent;
    private final ConsolidationPremiumNightEvent premiumNightEvent;

    public FetchConsolidationArchive(ConsolidationOvertimeEvent overtimeEvent, ConsolidationTeamLeaderEvent teamLeaderEvent, ConsolidationPremiumNightEvent premiumNightEvent) {
        this.overtimeEvent = overtimeEvent;
        this.teamLeaderEvent = teamLeaderEvent;
        this.premiumNightEvent = premiumNightEvent;
    }

    public String consolidateOvertime(YearMonth competence, Long project, LocalDate startDate, LocalDate endDate) {
        return overtimeEvent.consolidate(competence, project, startDate, endDate);
    }

    public String consolidatePremiumNight(YearMonth competence, Long project, LocalDate startDate, LocalDate endDate) {
        return premiumNightEvent.consolidate(competence, project, startDate, endDate);
    }

    public String consolidateTeamLeader(YearMonth competence, Long project, LocalDate startDate, LocalDate endDate) {
        return teamLeaderEvent.consolidate(competence, project, startDate, endDate);
    }
}

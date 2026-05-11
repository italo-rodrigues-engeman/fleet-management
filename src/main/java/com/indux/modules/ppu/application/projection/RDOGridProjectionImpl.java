package com.indux.modules.ppu.application.projection;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;

import java.time.LocalDate;
import java.util.Map;

public class RDOGridProjectionImpl implements RDOGridProjection {
    private final String id;
    private final LocalDate date;
    private final Long sequentialId;
    private final String regionalName;
    private final String platform;
    private final RDOStatusDP statusDP;
    private final RDOStatusOP statusOP;
    private final String clientName;
    private final String competence;
    private final Map<String, Object> contract;
    private final String supervisorName;

    public RDOGridProjectionImpl(String id, LocalDate date, Long sequentialId, String regionalName, String platform, RDOStatusDP statusDP, RDOStatusOP statusOP, String clientName, String competence, Map<String, Object> contract, String supervisorName) {
        this.id = id;
        this.date = date;
        this.sequentialId = sequentialId;
        this.regionalName = regionalName;
        this.platform = platform;
        this.statusDP = statusDP;
        this.statusOP = statusOP;
        this.clientName = clientName;
        this.competence = competence;
        this.contract = contract;
        this.supervisorName = supervisorName;
    }

    public static RDOGridProjectionImpl fromEntity(RDOEntity e) {
        return new RDOGridProjectionImpl(
                e.getId(),
                e.getDate(),
                e.getSequentialId(),
                e.getRegionalNome(),
                e.getPlatform(),
                e.getStatusDP(),
                e.getStatusOP(),
                e.getClientName(),
                e.getCompetence(),
                e.getContract(),
                e.getCreatorName()
        );
    }

    @Override public String getId() { return id; }
    @Override public LocalDate getDate() { return date; }
    @Override public Long getSequentialId() { return sequentialId; }
    @Override public String getRegionalNome() { return regionalName; }
    @Override public String getPlatform() { return platform; }
    @Override public RDOStatusDP getStatusDP() { return statusDP; }
    @Override public RDOStatusOP getStatusOP() { return statusOP; }
    @Override public String getClientName() { return clientName; }
    @Override public String getCompetence() { return competence; }
    @Override public Map<String, Object> getContract() { return contract; }
    @Override public String getCreatorName() { return supervisorName; }
} 
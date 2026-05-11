package com.indux.modules.ppu.domain.entities.mongo;

import java.time.Instant;

/**
 * Grid projection for ChangeTicket entity.
 * Contains essential fields for displaying in grid/list views.
 */
public interface ChangeTicketGridProjection {
    String getId();
    String getPpuId();
    Instant getCreatedAt();
    String getReason();
    Long getFromVersion();
    Long getToVersion();
    Actor getActor();
    
    // PPU Information for grid display
    String getApelido();
    String getRegionalName();
    
    // Approval status for grid display
    String getStatus(); // Will be enum name as string
    Instant getApprovedAt();
    String getApproverName();
    
    interface Actor {
        String getUserId();
        String getName();
    }
}
package com.indux.modules.crm.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.crm.domain.enums.ContactType;
import com.indux.modules.crm.persistence.model.EngemanAgentModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommercialInteractions {

    private String id;

    private ContactType contactType;

    private LocalDate date;

    private String company;

    private String unit;

    private String lead;

    private List<EngemanAgent> engemanAgent;

    private String description;

    private List<AttachmentEntity> descriptionAttachments;

    private String attention;

    private List<AttachmentEntity> attentionAttachments;

    private String windowOfOpportunity;

    private List<AttachmentEntity> windowOfOpportunityAttachments;

    private String status;

    private List<Alert> alerts;
}

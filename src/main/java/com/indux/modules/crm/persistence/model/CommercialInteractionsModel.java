package com.indux.modules.crm.persistence.model;

import com.indux.modules.crm.domain.entity.EngemanAgent;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.modules.crm.domain.enums.ContactType;
import com.mongodb.annotations.Immutable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Immutable
@Document(collection = "crm_commercial_interactions")
public class CommercialInteractionsModel {

    @Id
    private String id;

    private ContactType contactType;

    private LocalDate date;

    private String description;

    private List<AttachmentEntity> descriptionAttachments;

    private String attention;

    private List<AttachmentEntity> attentionAttachments;

    private String windowOfOpportunity;

    private List<AttachmentEntity> windowOfOpportunityAttachments;

    private Boolean status;

    private String company;

    private String unit;

    private String lead;

    @DBRef
    private List<EngemanAgentModel> engemanAgent;

    @DBRef
    private List<AlertModel> alertModel;

    public void toggleStatus() {
        setStatus(!getStatus());
    }
}

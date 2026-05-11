package com.indux.modules.crm.domain.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.domain.model.modules.AttachmentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngemanAgent {

    private String id;

    private String name;

    private String cpf;

    private String cep;

    private String address;

    private String state;

    private String city;

    private String mainEmail;

    private String alternativeEmail;

    private String mainNumber;

    private String alternativeNumber;

    private Boolean status;

    private List<AttachmentEntity> attachments;

    private List<Commission> commissions;
}

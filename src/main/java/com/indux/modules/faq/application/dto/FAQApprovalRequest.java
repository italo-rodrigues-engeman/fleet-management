package com.indux.modules.faq.application.dto;

import lombok.Builder;

@Builder
public record FAQApprovalRequest(
        String observacaoAprove
) {}

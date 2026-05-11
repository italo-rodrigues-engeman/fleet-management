package com.indux.modules.ocf.application.dto;

import java.util.List;

public record UpdateTimeMembersDTO(
        List<Long> projetos,
        List<Long> atendentes
) {
}




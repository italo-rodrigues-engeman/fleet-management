package com.indux.core.application.dto.user;

import lombok.Builder;

import java.util.List;

@Builder
public record SystemUserInfoDTO(Long quantidadeTotal,
                                Long quantidadeDesativados,
                                List<String> ultimosCadastrados) {
}

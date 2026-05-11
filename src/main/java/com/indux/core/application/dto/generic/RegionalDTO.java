package com.indux.core.application.dto.generic;

import com.indux.core.domain.model.employee.Filial;
import com.indux.core.domain.model.employee.Regional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalDTO {
    private Long id;
    private String regional;
    private String matriculaResponsavel;
    private List<FilialDTO> filiais;

    public static RegionalDTO fromEntity(Regional regional) {
        return RegionalDTO.builder()
                .id(regional.getId())
                .regional(regional.getRegional())
                .matriculaResponsavel(regional.getMatriculaResponsavel())
                .filiais(regional.getFiliais().stream()
                        .map(FilialDTO::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilialDTO {
        private Long branchId;
        private String branchName;
        private String corporateName;
        private String cnpj;
        private String branchCity;
        private String endereco;

        public static FilialDTO fromEntity(Filial filial) {
            return FilialDTO.builder()
                    .branchId(filial.getBranchId())
                    .branchName(filial.getBranchName())
                    .corporateName(filial.getCorporateName())
                    .cnpj(filial.getCnpj())
                    .branchCity(filial.getBranchCity())
                    .endereco(filial.getEndereco())
                    .build();
        }
    }
} 
package com.indux.modules.modulo_mega.domain.entity.abc;

import com.indux.modules.modulo_mega.domain.enums.AbcClassificationCriteria;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AbcFilter {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> idItem;
    private List<Integer> projectCode;
    private List<Integer> contractCode;
    private List<String> contractName;
    private List<Integer> sectorId;
    private List<String> sectorName;
    private String acronymSector;
    private List<Integer> regionalCode;
    private List<String> regionalName;
    private String acronymRegional;
    private List<Integer> superId;
    private List<String> superName;
    private String acronymSuper;
    private List<Integer> directoryId;
    private List<String> directoryName;
    private String acronymDirectory;
    private AbcClassificationCriteria criteria;
    private List<String> situation;
    private List<String> status;
    private List<String> category;
    private List<String> orderType;
}

package com.indux.modules.modulo_mega.application.dto;

import com.indux.modules.modulo_mega.domain.enums.AbcClassificationCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import java.time.LocalDate;

@Data
@NoArgsConstructor  
@AllArgsConstructor 
public class AbcFilterDTO {
    
    
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
    private List<String>  situation;
    private List<String> status;
    private List<String> category;
    private List<String>  orderType;

}
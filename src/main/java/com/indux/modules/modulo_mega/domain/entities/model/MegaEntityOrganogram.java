package com.indux.modules.modulo_mega.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MegaEntityOrganogram {
    Integer projectCode;
    Integer contractCode;
    String contractName;
    
    // Agrupando por níveis para facilitar o uso no negócio
    Integer sectorId;
    String sectorName;
    
    Integer regionalCode;
    String regionalName;
    
    Integer superId;
    String superName;
    
    Integer directoryId;
    String directoryName;
}
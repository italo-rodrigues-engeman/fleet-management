package com.indux.modules.union_registration.application.dto.labor_rights;

import lombok.Data;

@Data
public class WorkShiftTypesDTO {
    
    // 7 - Tipos de escala
    private String escala5x2; // 5x2
    private String escala6x1; // 6x1
    private String escala12x36; // 12x36
    private String escala4x4; // 4x4
    private String escala7x7; // 7x7
    private String escalaL5811; // lei 5811/72
    private String outro; // outros tipos de escala
}

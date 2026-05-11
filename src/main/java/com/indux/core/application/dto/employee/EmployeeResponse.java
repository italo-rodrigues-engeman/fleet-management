package com.indux.core.application.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String name;
    private String cpf;
    private String email;
    private String phone;
    private String phone2;
    private String position;
    private String department;
    private LocalDate birthDate;
    private LocalDate admissionDate;
    private String status;
    private String contractId;
    private String gestorInternoContrato;
    private String cliente;
    private List<Map<String, String>> coordenadoresContrato;
} 
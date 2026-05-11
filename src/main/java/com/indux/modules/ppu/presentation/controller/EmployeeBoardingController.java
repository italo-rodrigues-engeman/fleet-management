package com.indux.modules.ppu.presentation.controller;

import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employee-boarding")
public class EmployeeBoardingController {
    private final EmployeeBoardingETL employeeBoardingETL;

    public EmployeeBoardingController(EmployeeBoardingETL employeeBoardingETL) {
        this.employeeBoardingETL = employeeBoardingETL;
    }

    @GetMapping
    public ResponseEntity<List<BoardedEmployee>> getBoardedEmployees(
            @RequestParam(required = false) String registration,
            @RequestParam String platform,
            @RequestParam String initialDate,
            @RequestParam String finalDate) {
        try {
            List<BoardedEmployee> employees = employeeBoardingETL.fetchBoardedEmployees(
                    registration,
                    platform,
                    initialDate,
                    finalDate);



            return ResponseEntity.ok(employees);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
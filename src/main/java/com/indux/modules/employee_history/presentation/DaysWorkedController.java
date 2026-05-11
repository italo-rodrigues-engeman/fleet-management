package com.indux.modules.employee_history.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.employee_history.application.dto.CompetenceDTO;
import com.indux.modules.employee_history.application.dto.CreateAlert;
import com.indux.modules.employee_history.application.dto.DaysWordedDTO;
import com.indux.modules.employee_history.application.dto.FilterDaysWorked;
import com.indux.modules.employee_history.application.service.DaysWorkedService;
import com.indux.modules.employee_history.domain.repository.DaysWorkedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/worked")
@Validated
public class DaysWorkedController {

    private final DaysWorkedService daysWorkedService;

    public DaysWorkedController(DaysWorkedService daysWorkedService) {
        this.daysWorkedService = daysWorkedService;
    }

    @GetMapping
    public Page<DaysWordedDTO> getDaysWorked(
            Pageable pageable,
            FilterDaysWorked filter) {
        return daysWorkedService.getDaysWorked(filter, pageable);
    }

    @GetMapping("/competence")
    public Page<LocalDate> getCompetence(Pageable pageable) {
        return daysWorkedService.getCompetence(pageable);
    }

    @GetMapping("/gerenciar")
    public Page<CompetenceDTO> getGerenciar(FilterDaysWorked filter, Pageable pageable) { return  daysWorkedService.getCompetencesManage(filter, pageable); }

    @PostMapping("/alert")
    public ResponseEntity<GenericMessage> createAlert(
            @RequestBody CreateAlert alert,
            JwtAuthenticationToken jwt
    ){
        var name = jwt.getName();
        daysWorkedService.createAlert(alert,name);
        return ResponseEntity.ok(new GenericMessage("Alerta criado com sucesso", HttpStatus.OK.value()));
    }
}

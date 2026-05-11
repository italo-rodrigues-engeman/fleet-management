package com.indux.modules.ocf.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ocf.application.dto.*;
import com.indux.modules.ocf.application.service.TimeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @PutMapping("/{timeId}/membros")
    public ResponseEntity<GenericMessage> atualizarMembros(
            @PathVariable Long timeId,
            @RequestBody UpdateTimeMembersDTO body
    ) {
        timeService.atualizarMembrosDoTime(timeId, body.projetos(), body.atendentes());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("Membros do time atualizados com sucesso", 200));
    }

    @GetMapping
    public ResponseEntity<java.util.List<TimeSummaryDTO>> listarTimes() {
        return ResponseEntity.ok(timeService.listarTimes());
    }

    @PutMapping("/{timeId}/contratos")
    public ResponseEntity<GenericMessage> atualizarContratos(
            @PathVariable Long timeId,
            @RequestBody UpdateTimeContractsDTO body
    ) {
        timeService.atualizarProjetos(timeId, body.projetos());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("Contratos do time atualizados com sucesso", 200));
    }

    @PutMapping("/{timeId}/atendentes")
    public ResponseEntity<GenericMessage> atualizarAtendentes(
            @PathVariable Long timeId,
            @RequestBody UpdateTimeAgentsDTO body
    ) {
        timeService.atualizarAtendentes(timeId, body.atendentes());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("Atendentes do time atualizados com sucesso", 200));
    }

    @GetMapping("/full")
    public ResponseEntity<java.util.List<TimeWithMembersDTO>> listarTimesComMembros() {
        return ResponseEntity.ok(timeService.listarTimesComMembros());
    }
}



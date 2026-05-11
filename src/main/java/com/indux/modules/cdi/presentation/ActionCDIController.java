package com.indux.modules.cdi.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.cdi.aplication.dtos.ActionCDIDTO;
import com.indux.modules.cdi.aplication.dtos.DevCDIDTO;
import com.indux.modules.cdi.aplication.dtos.DevelopmentDTO;
import com.indux.modules.cdi.aplication.service.ActionCDIService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cdi")
public class ActionCDIController {
    private final ActionCDIService actionCDIService;

    public ActionCDIController(ActionCDIService actionCDIService) {
        this.actionCDIService = actionCDIService;
    }

    @GetMapping("/action/{cdiId}")
    public ResponseEntity<ActionCDIDTO> getAction(
            @PathVariable String cdiId
    ){
        return ResponseEntity.ok(actionCDIService.findByCdiId(cdiId));
    }

    @PutMapping("/action/{id}")
    public ResponseEntity<GenericMessage> updateAction(
            @PathVariable String id,
            @RequestBody List<DevelopmentDTO> developmentDTO
    ){
        actionCDIService.updateActionCdi(id, developmentDTO);
        return ResponseEntity.ok(new GenericMessage("Ação atualizada com sucesso", HttpStatus.OK.value()));
    }

    @PutMapping("/dev/{id}")
    public ResponseEntity<GenericMessage> updateAction(
            @PathVariable String id,
            @ModelAttribute DevCDIDTO  devDTO
            ){
        actionCDIService.updateDev(id, devDTO);
        return ResponseEntity.ok(new GenericMessage("Ação atualizada com sucesso", HttpStatus.OK.value()));
    }

    @GetMapping("/getAllDev")
    public ResponseEntity<List<ActionCDIDTO>> getAllDev(){
        return ResponseEntity.ok(actionCDIService.getAllDev());
    }
}

package com.indux.modules.cdi.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.cdi.aplication.dtos.PointsDTO;
import com.indux.modules.cdi.aplication.dtos.UpdatePointsDTO;
import com.indux.modules.cdi.aplication.service.PointsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cdi/points")
public class PointsController {
    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<PointsDTO>> getAllPoints(){
        return ResponseEntity.ok(pointsService.getAllPoints());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage>  updatePoints(
            @PathVariable Long id,
            @RequestBody UpdatePointsDTO pointsDTO
    ){
        pointsService.updatePoints(id, pointsDTO);
        return ResponseEntity.ok(new GenericMessage("Pontuação editados com sucesso", HttpStatus.OK.value()));
    }
}

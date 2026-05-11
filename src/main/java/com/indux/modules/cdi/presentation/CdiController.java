package com.indux.modules.cdi.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.cdi.aplication.dtos.*;
import com.indux.modules.cdi.aplication.service.CdiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cdi")
public class CdiController {
    private final CdiService cdiService;

    public CdiController(CdiService cdiService) {
        this.cdiService = cdiService;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createCdi(
            @RequestBody @Validated CreateCdiDTO dto){
        cdiService.createCdi(dto);
        return ResponseEntity.ok(new GenericMessage("Cadastro de ideia realizado", HttpStatus.CREATED.value()));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<GetAllCdiDTO>> getAllCdi(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String abrangencia,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String etapa,
            @RequestParam(required = false) String[] filiais,
            Pageable pageable
    ){
        // Create the filter with the array of branches
        FilterCdiDTO filter = new FilterCdiDTO(
                tipo != null ? com.indux.modules.cdi.domain.entities.models.Type.valueOf(tipo) : null,
                abrangencia != null ? com.indux.modules.cdi.domain.entities.models.Scope.valueOf(abrangencia) : null,
                status != null ? com.indux.modules.cdi.domain.entities.models.Status.valueOf(status) : null,
                etapa != null ? com.indux.modules.cdi.domain.entities.models.Stage.valueOf(etapa) : null,
                filiais // Pass the array of branches
        );
        
        return ResponseEntity.ok(cdiService.findAll(filter,pageable));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<GetIdCdiDTO> getCdiById(
            @PathVariable String id
    ){
        return ResponseEntity.ok(cdiService.findById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage> updateCdi(
            @PathVariable String id,
            @RequestBody UpdateCdiDTO avaliation
            ){
            cdiService.updateCdi(id, avaliation);
            return ResponseEntity.ok(new GenericMessage("Avaliação de ideia realizado", HttpStatus.OK.value()));
    }
}
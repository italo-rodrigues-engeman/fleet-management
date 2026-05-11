package com.indux.modules.training.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.training.application.dto.InstituinRequestDTO;
import com.indux.modules.training.application.dto.InstituinResponseDTO;
import com.indux.modules.training.application.service.InstituinService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instituin")
@Validated
public class InstituinController {
    private final InstituinService instituinService;

    public InstituinController(InstituinService instituinService) {
        this.instituinService = instituinService;
    }

    @PostMapping(consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> createInstituin(
        @Valid @ModelAttribute InstituinRequestDTO dto
    ){
      instituinService.createInstituin(dto);
      return ResponseEntity.ok(new GenericMessage("Instituição Criado com Sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public Page<InstituinResponseDTO> getInstituin(
            @RequestParam(required = false) String search,
            @RequestParam(name = "trainingIds", required = false) List<String> trainingIds,
            Pageable pageable
    ){
        return instituinService.findAllInstituin(search, trainingIds, pageable);
    }

    @GetMapping("/{id}")
    public InstituinResponseDTO getInstituin(
            @PathVariable String id
    ){
        return instituinService.findInstituinById(id);
    }

    @PutMapping(value="/{id}", consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateInstituin(
            @PathVariable String id,
            @Valid @ModelAttribute InstituinRequestDTO dto
    ){
        instituinService.updateInstituin(id, dto);
        return ResponseEntity.ok(new GenericMessage("Instituição Editada com Sucesso", HttpStatus.OK.value()));

    }
}

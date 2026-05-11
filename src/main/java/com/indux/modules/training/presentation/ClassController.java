package com.indux.modules.training.presentation;

import com.indux.core.application.dto.generic.EmployeeFilter;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.training.application.dto.*;
import com.indux.modules.training.application.service.ClassService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class")
@Validated
public class ClassController {
    private final ClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping(consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> create(
        @ModelAttribute @Validated ClassRequest classRequest
    ){
        classService.createClass(classRequest);
        return ResponseEntity.ok(new GenericMessage("Treinamento Criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public Page<ClassResponse> getClasses(Pageable pageable){
        return classService.findAllClasses(pageable);
    }

    @GetMapping("/{id}")
    public ClassResponse getClass(
            @PathVariable String id
    ){
        return classService.findClassById(id);
    }

    @GetMapping("/dossier")
    public List<Dossier> getDossiers(
            @RequestParam(required = false) List<String> treinamento,
            @RequestParam(required = false) String matricula
    ){
        return classService.getDossiers(treinamento, matricula);
    }

    @GetMapping("/due")
    public Page<DueDate> getDueDates(
            EmployeeFilter filterDTO,
            Pageable pageable
    ){
        return classService.getDueDates(filterDTO, pageable);
    }

    @GetMapping("/dueDetail")
    public Page<DueDetail> getDueDetail(
            EmployeeFilter filterDTO,
            Pageable pageable
    ){
        return classService.getDueDetail(filterDTO, pageable);
    }

    @PutMapping(value="/{id}", consumes ={"multipart/form-data"})
    public ResponseEntity<GenericMessage> updateClass(
            @ModelAttribute @Validated ClassRequest classRequest,
            @PathVariable String id
    ){
        classService.updateClass(id, classRequest);
        return ResponseEntity.ok(new GenericMessage("Treinamento Atualizado com sucesso", HttpStatus.OK.value()));
    }
}

package com.indux.modules.organization_chart.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.organization_chart.application.dtos.CreateProjectDTO;
import com.indux.modules.organization_chart.application.dtos.ProjectFilter;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/organization/project")
public class OrganizationProjectController {
    private final ProjectService projectService;

    public OrganizationProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createProject(
            @RequestBody CreateProjectDTO dto) {
        projectService.createProject(dto);
        return ResponseEntity.ok(new GenericMessage("Projeto criado com sucesso", HttpStatus.CREATED.value()));
    }

    @GetMapping("/getAll")
    public ResponseEntity<Page<ProjectEntity>> getAllProjects(
            ProjectFilter projectFilter,
            Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjects(projectFilter, pageable));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ProjectEntity> getProjectById(
            @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getbyId(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GenericMessage> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectDTO dto) {
        projectService.updateProject(id, dto);
        return ResponseEntity.ok(new GenericMessage("Projeto atualizado", HttpStatus.OK.value()));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<GenericMessage> deleteProject(
            @PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(new GenericMessage("Projeto apagado", HttpStatus.OK.value()));
    }

}

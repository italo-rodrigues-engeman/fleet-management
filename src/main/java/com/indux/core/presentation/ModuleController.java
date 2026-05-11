package com.indux.core.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.application.dto.module.*;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.module.ModuleStepService;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.domain.service.user.FavoriteModuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/module")
public class ModuleController {
    private final ModuleManagementService moduleService;
    private final ModuleUserService userService;
    private final ModuleStepService stepService;
    private final FavoriteModuleService favoriteService;
    private final ModuleResponseMapper moduleResponseMapper;

    public ModuleController(ModuleManagementService moduleService,
                            ModuleUserService userService,
                            ModuleStepService stepService,
                            FavoriteModuleService favoriteService,
                            ModuleResponseMapper moduleResponseMapper) {
        this.moduleService = moduleService;
        this.userService = userService;
        this.stepService = stepService;
        this.favoriteService = favoriteService;
        this.moduleResponseMapper = moduleResponseMapper;
    }

    // ---------------- ONLY DEV Permissions ----------------
    @PreAuthorize("hasAuthority('ROLE_DESENVOLVEDOR')")
    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createModule(@RequestBody CreateModuleDTO dto,
                                                       JwtAuthenticationToken jwt) {
        UUID userId = UUID.fromString(jwt.getName());
        moduleService.createModule(dto, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericMessage("Módulo criado com sucesso", 201));
    }


   @DeleteMapping("/{moduleId}")
    public ResponseEntity<GenericMessage> deleteModule(@PathVariable String moduleId, JwtAuthenticationToken jwt) {
        moduleService.deleteModule(moduleId);
        return ResponseEntity
                .ok(new GenericMessage("Módulo removido com sucesso", 200));
    }

    // ---------------- Module Management ----------------
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}/name")
    public ResponseEntity<GenericMessage> updateModuleName(
            @PathVariable String moduleId,
            @RequestBody Map<String, String> nome,
            JwtAuthenticationToken jwt) {
        moduleService.updateModuleName(moduleId, nome.get("newName"));
        return ResponseEntity
                .ok(new GenericMessage("Nome do módulo atualizado com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}")
    public ResponseEntity<GenericMessage> updateModule(
            @PathVariable String moduleId,
            @RequestBody Map<String, Object> nome,
            JwtAuthenticationToken jwt) {
        moduleService.updateModule(moduleId, (String) nome.get("nome"), (String) nome.get("descricao"), (List) nome.get("setores"));
        return ResponseEntity
                .ok(new GenericMessage("Módulo atualizado com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/{moduleId}/user")
    public ResponseEntity<GenericMessage> addUserToModule(
            @PathVariable String moduleId,
            @RequestBody ModulePermissionInput permission,
            JwtAuthenticationToken jwt) {
        userService.addUserToModule(moduleId, permission);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericMessage("Usuário adicionado ao módulo", 201));
    }

    @PreAuthorize("hasAuthority('ROLE_DESENVOLVEDOR')")
    @PostMapping("/user/all")
    public ResponseEntity<GenericMessage> addUserInAllModules(
            @RequestBody ModulePermissionInput permission
    ) {
        userService.addUserInAllModules(permission);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("Usuário adicionado em todos os modulos com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @DeleteMapping("/{moduleId}/user/{userId}")
    public ResponseEntity<GenericMessage> removeUserFromModule(
            @PathVariable String moduleId,
            @PathVariable UUID userId,
            JwtAuthenticationToken jwt) {
        userService.removeUsersFromModule(moduleId, userId);
        return ResponseEntity
                .ok(new GenericMessage("Usuário removido do módulo", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}/user/permissions")
    public ResponseEntity<GenericMessage> updateUserPermissions(
            @PathVariable String moduleId,
            @RequestBody BatchModulePermissionsDTO dto,
            JwtAuthenticationToken jwt) {
        userService.updateUserPermissions(dto);
        return ResponseEntity
                .ok(new GenericMessage("Permissões de usuário atualizadas com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/batch")
    public ResponseEntity<GenericMessage> addPermissionsBatch(
            @RequestBody BatchModulePermissionsDTO dto,
            JwtAuthenticationToken jwt) {
        userService.addPermissionsBatch(dto);
        return ResponseEntity
                .ok(new GenericMessage("Permissões em lote aplicadas com sucesso", 200));
    }

    // ---------------- Module User & Permissions ----------------
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @GetMapping("/all")
    public ResponseEntity<List<ModuleResponseDTO>> getAllModules(JwtAuthenticationToken jwt) {
        UUID userId = UUID.fromString(jwt.getName());
        boolean isAdmin = jwt.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));
        Set<UUID> favoriteIds = favoriteService.listFavorites(userId).stream()
                .map(ModuleResponseDTO::id)
                .collect(Collectors.toSet());
        List<ModuleResponseDTO> response = moduleService.listAllModules().stream()
                .map(mod -> moduleResponseMapper.toDTO(
                        mod,
                        userId,
                        isAdmin,
                        favoriteIds.contains(mod.getId())    // passa true/false
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<ModuleResponseDTO>> getFavoriteModules(JwtAuthenticationToken jwt) {
        return ResponseEntity
                .ok(favoriteService.listFavorites(UUID.fromString(jwt.getName())));
    }

    @GetMapping("/setor/{id}")
    public ResponseEntity<List<ModuleResponseDTO>> getBySetor(@PathVariable String setor, JwtAuthenticationToken jwt) {
        return ResponseEntity
                .ok(moduleService.listBySetor(UUID.fromString(jwt.getName()), Integer.parseInt(setor), false));
    }


    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @GetMapping("/{moduleId}")
    public ResponseEntity<Modulo> getModule(
            @PathVariable String moduleId,
            JwtAuthenticationToken jwt) {

            Modulo modulo = moduleService.getModuleByID(UUID.fromString(moduleId));
            return ResponseEntity.ok(modulo);

        }


    // -------------------------------

    @GetMapping("/")
    public ResponseEntity<List<ModuleResponseDTO>> getMyModules(JwtAuthenticationToken jwt) {
        UUID userId = UUID.fromString(jwt.getName());
        boolean isAdmin = jwt.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        List<ModuleResponseDTO> modules = moduleService.getAllByResponsavel(userId, isAdmin);

        return ResponseEntity.ok(modules);
    }
    // ---------------- Module Steps & SLA ----------------

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/{moduleId}/step")
    public ResponseEntity<GenericMessage> createModuleStep(
            @PathVariable String moduleId,
            @RequestBody StepModuleDTO dto,
            JwtAuthenticationToken jwt) {
        stepService.createModuleStep(moduleId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericMessage("Etapa criada com sucesso", 201));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}/step/{numEtapa}/sla")
    public ResponseEntity<GenericMessage> updateStepSLA(
            @PathVariable String moduleId,
            @PathVariable int numEtapa,
            @RequestBody Map<String, Integer> value,
            JwtAuthenticationToken jwt) {
        stepService.updateStepSLA(moduleId, numEtapa, value.get("novoTempo"));
        return ResponseEntity
                .ok(new GenericMessage("SLA da etapa atualizado com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}/steps/sla")
    public ResponseEntity<GenericMessage> updateAllStepsSLA(
            @PathVariable String moduleId,
            @RequestBody Map<String, Integer> value,
            JwtAuthenticationToken jwt) {
        stepService.updateAllStepsSLA(moduleId, value.get("novoTempo"));
        return ResponseEntity
                .ok(new GenericMessage("SLA de todas as etapas atualizado com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PutMapping("/{moduleId}/steps/")
    public ResponseEntity<GenericMessage> updateOneStep(
            @PathVariable String moduleId,
            @RequestBody Map<String, Object> value,
            JwtAuthenticationToken jwt) {
        stepService.update(moduleId, (Integer) value.get("tempo"), (Integer) value.get("etapa"),
                (String) value.get("descricao"));
        return ResponseEntity
                .ok(new GenericMessage("A etapa foi atualizada com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/{moduleId}/desativar")
    public ResponseEntity<GenericMessage> disableModule(
            @PathVariable String moduleId,
            JwtAuthenticationToken jwt) {
        moduleService.disableModule(UUID.fromString(moduleId));
        return ResponseEntity
                .ok(new GenericMessage("Modulo desativado com sucesso", 201));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/{moduleId}/gerente/{userId}")
    public ResponseEntity<GenericMessage> addGerente(
            @PathVariable String moduleId,
            @PathVariable UUID userId,
            JwtAuthenticationToken jwt) {
        moduleService.addGerente(UUID.fromString(moduleId), userId);
        return ResponseEntity
                .ok(new GenericMessage("Gerente adicionado com sucesso", 200));
    }

    @GetMapping("/{moduleID}/users")
    public ResponseEntity<List<String>> getUsersFromModule(
            @PathVariable String moduleID,
            ModuleFilter filter) {

        List<String> result = userService.filterPeopleInModule(UUID.fromString(moduleID), filter);

        List<String> formatted = result.stream()
                .sorted()
                .toList();
        return ResponseEntity.ok(formatted);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @DeleteMapping("/{moduleId}/gerente/{userId}")
    public ResponseEntity<GenericMessage> removeGerente(
            @PathVariable String moduleId,
            @PathVariable UUID userId,
            JwtAuthenticationToken jwt) {
        moduleService.removeGerente(UUID.fromString(moduleId), userId);
        return ResponseEntity
                .ok(new GenericMessage("Gerente removido com sucesso", 200));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or @moduleService.isGerente(#moduleId, #jwt.name)")
    @PostMapping("/{moduleId}/users/batch")
    public ResponseEntity<GenericMessage> addMultipleUsersToModule(
            @PathVariable String moduleId,
            @RequestBody MultipleUsersModulePermissionsDTO dto,
            JwtAuthenticationToken jwt) {
        userService.addMultipleUsersToModule(moduleId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new GenericMessage("Usuários adicionados ao módulo com sucesso", 201));
    }
}
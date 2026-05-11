package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.module.ModuleResponseDTO.PermissoesUsuarioDTO;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.application.service.module.ModulePermissionChecker.RegionaisAndProjects;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.infra.exception.module.ForbiddenModuleAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModulePermissionCheckerTest {

    @Mock
    private ModuleManagementService moduleManagementService;

    @Mock
    private ModuleResponseMapper moduleResponseMapper;

    @InjectMocks
    private ModulePermissionChecker checker;

    private final UUID moduleId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private void mockPermissions(Set<Integer> etapas,
            Set<Integer> regionais,
            Set<Integer> projetos) {
        Modulo modulo = Modulo.builder().id(moduleId).name("Test").build();
        PermissoesUsuarioDTO perms = new PermissoesUsuarioDTO(
                etapas, Set.of(), regionais, projetos);
        ModuleResponseDTO dto = new ModuleResponseDTO(
                moduleId, "Test", null, 0, Set.of(), perms, false, false, false);

        when(moduleManagementService.getModuleByID(moduleId)).thenReturn(modulo);
        when(moduleResponseMapper.toDTO(modulo, userId, false, false)).thenReturn(dto);
    }

    @Test
    @DisplayName("Should return permissions for user with access")
    void getPermissions_WhenUserHasAccess_ShouldReturnPermissions() {
        mockPermissions(Set.of(1, 2), Set.of(10), Set.of(5));

        PermissoesUsuarioDTO perms = checker.getPermissions(moduleId, userId);

        assertNotNull(perms);
        assertEquals(Set.of(1, 2), perms.etapasPermitidas());
        assertEquals(Set.of(10), perms.regionaisPermitidas());
        assertEquals(Set.of(5), perms.projetosPermitidos());
    }

    @Test
    @DisplayName("Should return true when user has regionais")
    void hasAccess_WhenUserHasRegionais_ShouldReturnTrue() {
        mockPermissions(Set.of(), Set.of(1), Set.of());
        assertTrue(checker.hasAccess(moduleId, userId));
    }

    @Test
    @DisplayName("Should return true when user has steps")
    void hasAccess_WhenUserHasSteps_ShouldReturnTrue() {
        mockPermissions(Set.of(1), Set.of(), Set.of());
        assertTrue(checker.hasAccess(moduleId, userId));
    }

    @Test
    @DisplayName("Should return false when user has no permissions")
    void hasAccess_WhenUserHasNoPermissions_ShouldReturnFalse() {
        mockPermissions(Set.of(), Set.of(), Set.of());
        assertFalse(checker.hasAccess(moduleId, userId));
    }

    @Test
    @DisplayName("Should throw ForbiddenModuleAccessException when user has no access")
    void requireAccess_WhenUserHasNoAccess_ShouldThrow() {
        mockPermissions(Set.of(), Set.of(), Set.of());
        assertThrows(ForbiddenModuleAccessException.class,
                () -> checker.requireAccess(moduleId, userId));
    }

    @Test
    @DisplayName("Should not throw when user has access")
    void requireAccess_WhenUserHasAccess_ShouldNotThrow() {
        mockPermissions(Set.of(1), Set.of(10), Set.of());
        assertDoesNotThrow(() -> checker.requireAccess(moduleId, userId));
    }

    @Test
    @DisplayName("Should return allowed steps")
    void getAllowedSteps_ShouldReturnCorrectSteps() {
        mockPermissions(Set.of(1, 3), Set.of(10), Set.of());
        assertEquals(Set.of(1, 3), checker.getAllowedSteps(moduleId, userId));
    }

    @Test
    @DisplayName("Should return allowed regionais")
    void getAllowedRegionais_ShouldReturnCorrectRegionais() {
        mockPermissions(Set.of(), Set.of(10, 20), Set.of());
        assertEquals(Set.of(10, 20), checker.getAllowedRegionais(moduleId, userId));
    }

    @Test
    @DisplayName("Should return allowed projects")
    void getAllowedProjects_ShouldReturnCorrectProjects() {
        mockPermissions(Set.of(), Set.of(), Set.of(5, 6));
        assertEquals(Set.of(5, 6), checker.getAllowedProjects(moduleId, userId));
    }

    @Test
    @DisplayName("Should return regionais and projects combined")
    void getAllowedRegionaisAndProjects_ShouldReturnBoth() {
        mockPermissions(Set.of(1), Set.of(10, 20), Set.of(5, 6));

        RegionaisAndProjects result = checker.getAllowedRegionaisAndProjects(moduleId, userId);

        assertEquals(Set.of(10, 20), result.regionais());
        assertEquals(Set.of(5, 6), result.projetos());
    }

    @Test
    @DisplayName("Should return empty sets when user has no regionais or projects")
    void getAllowedRegionaisAndProjects_WhenEmpty_ShouldReturnEmptySets() {
        mockPermissions(Set.of(), Set.of(), Set.of());

        RegionaisAndProjects result = checker.getAllowedRegionaisAndProjects(moduleId, userId);

        assertTrue(result.regionais().isEmpty());
        assertTrue(result.projetos().isEmpty());
    }
}

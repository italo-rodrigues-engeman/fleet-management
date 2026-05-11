package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.CreateModuleDTO;
import com.indux.core.application.dto.module.ModulePermissionInput;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.module.StepModuleDTO;
import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.service.user.FavoriteModuleService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleManagementServiceImplTest {
    @Mock
    private ModuleRepository repository;
    @Mock
    private ModuleResponseMapper moduleResponseMapper;
    @Mock
    private FavoriteModuleService favoriteService;
    @Mock
    private UserService userService;
    @InjectMocks
    private ModuleManagementServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private ModulePermissionInput makePerm(String moduleId,
                                           UUID user,
                                           boolean isGroup,
                                           Set<Integer> regionais,
                                           Set<Integer> etapas,
                                           Set<Integer> acoes,
                                           Set<Integer> projetos) {
        return new ModulePermissionInput(
                UUID.fromString(moduleId),
                user,
                isGroup,
                regionais,
                etapas,
                acoes,
                projetos
        );
    }

    private StepModuleDTO makeStepDto(int numEtapa, int tempo, String nome) {
        return new StepModuleDTO(nome, tempo, numEtapa);
    }

    @Test
    @DisplayName("Should build and save module")
    void createModule_ShouldBuildAndSaveModulo() {
        // --- fixtures
        var permDto = makePerm(
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                true,
                Set.of(1, 2),
                Set.of(10, 20),
                Set.of(1, 2),
                Set.of(0)
        );
        var stepDto = makeStepDto(1, 30, "Início");
        var request = new CreateModuleDTO(
                "Módulo X",
                1,
                Set.of(stepDto),
                Set.of(permDto),
                Set.of()
        );
        service.createModule(request, UUID.randomUUID());
        ArgumentCaptor<Modulo> captor = ArgumentCaptor.forClass(Modulo.class);
        verify(repository).save(captor.capture());
        Modulo saved = captor.getValue();
        assertEquals("Módulo X", saved.getName());
        assertEquals(1, saved.getStepsQuantity());
        // verifica permissão mapeada
        assertEquals(1, saved.getPermissoes().size());
        ModulePermission mp = saved.getPermissoes().iterator().next();
        assertEquals(permDto.user(), mp.getResponsable());
        assertEquals(permDto.isGroup(), mp.isGroup());
        assertEquals(permDto.regionais(), mp.getRegionais());
        assertTrue(mp.getStepsAllowed().containsAll(permDto.etapasResponsaveis()));
        assertEquals(permDto.acoes(), mp.getActions());
        // verifica etapas mapeadas
        assertEquals(1, saved.getConfigEtapas().size());
        StepModule sm = saved.getConfigEtapas().iterator().next();
        assertEquals(stepDto.numEtapa(), sm.getEtapa());
        assertEquals(stepDto.tempoAceitavel(), sm.getTempo());
        assertEquals(stepDto.nome(), sm.getNome());
    }

    @Test
    @DisplayName("Should call delete module by id")
    void deleteModule_ShouldCallDeleteById() {
        String id = UUID.randomUUID().toString();
        service.deleteModule(id);
        verify(repository).deleteById(UUID.fromString(id));
    }

    @Test
    @DisplayName("Should throw error when try delete")
    void deleteModule_WhenIdNotFound_ShouldPropagate() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(repository).deleteById(any());
        assertThrows(EmptyResultDataAccessException.class, () -> {
            service.deleteModule(UUID.randomUUID().toString());
        });
    }

    @Test
    @DisplayName("Should update module")
    void updateModuleName_ShouldLoadModifyAndSave() {
        UUID id = UUID.randomUUID();
        Modulo existing = Modulo.builder()
                .id(id)
                .name("Old")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.updateModuleName(id.toString(), "New");
        assertEquals("New", existing.getName());
        verify(repository).save(existing);
    }

//    @Test
//    @DisplayName("Should throw error when try update module")
//    void updateModuleName_WhenNotFound_ShouldThrow() {
//        UUID id = UUID.randomUUID();
//        when(repository.findById(id)).thenReturn(Optional.empty());
//        assertThrows(NoSuchElementException.class, () -> {
//            service.updateModuleName(id.toString(), "Novo");
//        });
//    }

    @Test
    @DisplayName("Should return a list of modules when present")
    void listAllModules_ShouldReturnRepositoryFindAll() {
        var m1 = Modulo.builder().name("A").desativado(false).build();
        var m2 = Modulo.builder().name("B").desativado(false).build();
        when(repository.findAllByDesativadoFalse()).thenReturn(List.of(m1, m2));
        List<Modulo> result = service.listAllModules();
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(m1, m2)));
    }

    @Test
    @DisplayName("Should throw exception when creating module with wrong step quantity")
    void createModule_WhenStepQuantityMismatch_ShouldThrowException() {
        var stepDto = makeStepDto(1, 30, "Início");
        var request = new CreateModuleDTO(
                "Módulo X",
                2, // expects 2 steps but only 1 provided
                Set.of(stepDto),
                Set.of(),
                Set.of()
        );

        assertThrows(ModuleFailure.class, () -> {
            service.createModule(request, UUID.randomUUID());
        });
    }

    @Test
    @DisplayName("Should call favorite service before deleting module")
    void deleteModule_ShouldRemoveFavoritesFirst() {
        String moduleId = UUID.randomUUID().toString();
        UUID moduleUuid = UUID.fromString(moduleId);

        service.deleteModule(moduleId);

        verify(favoriteService).removeAllUsersFromModule(moduleUuid);
        verify(repository).deleteById(moduleUuid);
        
        // Verify order: favorites removed before module deletion
        var inOrder = inOrder(favoriteService, repository);
        inOrder.verify(favoriteService).removeAllUsersFromModule(moduleUuid);
        inOrder.verify(repository).deleteById(moduleUuid);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent module name")
    void updateModuleName_WhenModuleNotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.updateModuleName(id.toString(), "New Name");
        });
    }

    @Test
    @DisplayName("Should return modules by responsible user")
    void getAllByResponsavel_ShouldReturnUserModules() {
        UUID userId = UUID.randomUUID();
        UUID moduleId1 = UUID.randomUUID();
        UUID moduleId2 = UUID.randomUUID();
        
        Modulo module1 = Modulo.builder().id(moduleId1).name("Module 1").build();
        Modulo module2 = Modulo.builder().id(moduleId2).name("Module 2").build();
        
        ModuleResponseDTO dto1 = new ModuleResponseDTO(moduleId1, "Module 1", null, 0, null, null, false, false, false);
        ModuleResponseDTO dto2 = new ModuleResponseDTO(moduleId2, "Module 2", null, 0, null, null, false, false, false);
        
        when(repository.findAllByResponsavel(userId)).thenReturn(List.of(module1, module2));
        when(favoriteService.listFavorites(userId)).thenReturn(List.of());
        when(moduleResponseMapper.toDTO(module1, userId, true, false)).thenReturn(dto1);
        when(moduleResponseMapper.toDTO(module2, userId, true, false)).thenReturn(dto2);

        List<ModuleResponseDTO> result = service.getAllByResponsavel(userId, true);

        assertEquals(2, result.size());
        verify(favoriteService).listFavorites(userId);
    }

    @Test
    @DisplayName("Should return module by ID with user names populated")
    void getModuleByID_ShouldReturnModuleWithUserNames() {
        UUID moduleId = UUID.randomUUID();
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();
        
        ModulePermission perm1 = ModulePermission.builder()
                .responsable(user1Id)
                .build();
        ModulePermission perm2 = ModulePermission.builder()
                .responsable(user2Id)
                .build();
        
        Modulo module = Modulo.builder()
                .id(moduleId)
                .name("Test Module")
                .permissoes(List.of(perm1, perm2))
                .build();
        
        UserNameProjection userProj1 = new UserNameProjection() {
            @Override
            public UUID getId() { return user1Id; }
            @Override
            public String getName() { return "User 1"; }
        };
        
        UserNameProjection userProj2 = new UserNameProjection() {
            @Override
            public UUID getId() { return user2Id; }
            @Override
            public String getName() { return "User 2"; }
        };

        when(repository.findById(moduleId)).thenReturn(Optional.of(module));
        when(userService.getUserNamesIn(List.of(user1Id, user2Id))).thenReturn(List.of(userProj1, userProj2));

        Modulo result = service.getModuleByID(moduleId);

        assertNotNull(result);
        assertEquals("Test Module", result.getName());
        assertEquals("User 1", result.getPermissoes().get(0).getResponsableName());
        assertEquals("User 2", result.getPermissoes().get(1).getResponsableName());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent module by ID")
    void getModuleByID_WhenModuleNotFound_ShouldThrowException() {
        UUID moduleId = UUID.randomUUID();
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.getModuleByID(moduleId);
        });
    }

    @Test
    @DisplayName("Should return module without permissions processing when no permissions exist")
    void getModuleByID_WhenNoPermissions_ShouldReturnModuleDirectly() {
        UUID moduleId = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .id(moduleId)
                .name("Test Module")
                .permissoes(new ArrayList<>())
                .build();

        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        Modulo result = service.getModuleByID(moduleId);

        assertNotNull(result);
        assertEquals("Test Module", result.getName());
        verify(userService, never()).getUserNamesIn(any());
    }

    @Test
    @DisplayName("Should return module by name")
    void getByName_ShouldReturnModule() {
        String moduleName = "Test Module";
        Modulo module = Modulo.builder().name(moduleName).build();
        
        when(repository.findByName(moduleName)).thenReturn(Optional.of(module));

        Modulo result = service.getByName(moduleName);

        assertNotNull(result);
        assertEquals(moduleName, result.getName());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent module by name")
    void getByName_WhenModuleNotFound_ShouldThrowException() {
        String moduleName = "Non-existent Module";
        when(repository.findByName(moduleName)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.getByName(moduleName);
        });
    }

    @Test
    @DisplayName("Should disable module")
    void disableModule_ShouldSetModuleAsDisabled() {
        UUID moduleId = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .id(moduleId)
                .name("Test Module")
                .desativado(false)
                .build();
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.disableModule(moduleId);

        assertTrue(module.isDesativado());
    }

    @Test
    @DisplayName("Should throw exception when disabling non-existent module")
    void disableModule_WhenModuleNotFound_ShouldThrowException() {
        UUID moduleId = UUID.randomUUID();
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.disableModule(moduleId);
        });
    }

    @Test
    @DisplayName("Should return all modules")
    void getAll_ShouldReturnAllModules() {
        Modulo module1 = Modulo.builder().name("Module 1").build();
        Modulo module2 = Modulo.builder().name("Module 2").build();
        
        when(repository.findAll()).thenReturn(List.of(module1, module2));

        List<Modulo> result = service.getAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(module1, module2)));
    }

    @Test
    @DisplayName("Should update module name and description")
    void updateModule_ShouldUpdateNameAndDescription() {
        UUID moduleId = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .id(moduleId)
                .name("Old Name")
                .description("Old Description")
                .build();
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.updateModule(moduleId.toString(), "New Name", "New Description", List.of());

        assertEquals("New Name", module.getName());
        assertEquals("New Description", module.getDescription());
        verify(repository).save(module);
    }

    @Test
    @DisplayName("Should keep existing values when updating with blank strings")
    void updateModule_WithBlankValues_ShouldKeepExistingValues() {
        UUID moduleId = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .id(moduleId)
                .name("Existing Name")
                .description("Existing Description")
                .build();
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.updateModule(moduleId.toString(), "", " ", List.of());

        assertEquals("Existing Name", module.getName());
        assertEquals("Existing Description", module.getDescription());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent module")
    void updateModule_WhenModuleNotFound_ShouldThrowException() {
        UUID moduleId = UUID.randomUUID();
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.updateModule(moduleId.toString(), "New Name", "New Description", List.of());
        });
    }

    @Test
    @DisplayName("Should add gerente to module")
    void addGerente_ShouldAddUserAsGerente() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Modulo module = spy(Modulo.builder().id(moduleId).build());
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.addGerente(moduleId, userId);

        verify(module).addGerente(userId);
        verify(repository).save(module);
    }

    @Test
    @DisplayName("Should throw exception when adding gerente to non-existent module")
    void addGerente_WhenModuleNotFound_ShouldThrowException() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.addGerente(moduleId, userId);
        });
    }

    @Test
    @DisplayName("Should remove gerente from module")
    void removeGerente_ShouldRemoveUserAsGerente() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Modulo module = spy(Modulo.builder().id(moduleId).build());
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.removeGerente(moduleId, userId);

        verify(module).removeGerente(userId);
        verify(repository).save(module);
    }

    @Test
    @DisplayName("Should throw exception when removing gerente from non-existent module")
    void removeGerente_WhenModuleNotFound_ShouldThrowException() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(ModuleNotFoundFailure.class, () -> {
            service.removeGerente(moduleId, userId);
        });
    }

    @Test
    @DisplayName("Should return true when user is gerente")
    void isGerente_WhenUserIsGerente_ShouldReturnTrue() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Modulo module = spy(Modulo.builder().id(moduleId).build());
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));
        when(module.isGerente(userId)).thenReturn(true);

        boolean result = service.isGerente(moduleId.toString(), userId.toString());

        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not gerente")
    void isGerente_WhenUserIsNotGerente_ShouldReturnFalse() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Modulo module = spy(Modulo.builder().id(moduleId).build());
        
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));
        when(module.isGerente(userId)).thenReturn(false);

        boolean result = service.isGerente(moduleId.toString(), userId.toString());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when module not found")
    void isGerente_WhenModuleNotFound_ShouldReturnFalse() {
        UUID moduleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        when(repository.findById(moduleId)).thenReturn(Optional.empty());

        boolean result = service.isGerente(moduleId.toString(), userId.toString());

        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when invalid UUID format")
    void isGerente_WhenInvalidUUID_ShouldReturnFalse() {
        boolean result = service.isGerente("invalid-uuid", "another-invalid-uuid");

        assertFalse(result);
    }
}
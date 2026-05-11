package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.BatchModulePermissionsDTO;
import com.indux.core.application.dto.module.ModulePermissionInput;
import com.indux.core.application.dto.module.MultipleUsersModulePermissionsDTO;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ModuleUserServiceImplTest {
    @Mock
    private ModuleRepository repository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ModuleUserServiceImpl service;

    @BeforeEach
    void setUp() {
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
                etapas,
                acoes,
                regionais,
                projetos
        );
    }

    @Test
    @DisplayName("Should add permission to an existing module")
    void addUserToModule_ShouldAddPermission() {
        // given
        UUID moduleId = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .permissoes(new ArrayList<>())
                .build();
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        UUID userId = UUID.randomUUID();
        ModulePermissionInput dto = makePerm(
                moduleId.toString(),
                userId,
                false,
                Set.of(1, 2),
                Set.of(10, 20),
                Set.of(1),
                Set.of(0)
        );

        service.addUserToModule(moduleId.toString(), dto);

        verify(repository).findById(moduleId);
        assertEquals(1, module.getPermissoes().size());
        ModulePermission perm = module.getPermissoes().iterator().next();
        assertEquals(userId, perm.getResponsable());
        assertFalse(perm.isGroup());
        assertEquals(Set.of(1, 2), perm.getRegionais());
        assertTrue(perm.getStepsAllowed().containsAll(dto.etapasResponsaveis()));
        assertEquals(dto.acoes(), perm.getActions());
    }

    @Test
    @DisplayName("Should remove permissions for the specified user")
    void removeUsersFromModule_ShouldRemoveMatchingPermissions() {
        UUID moduleId = UUID.randomUUID();
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        ModulePermission pA = ModulePermission.builder().responsable(userA).build();
        ModulePermission pB = ModulePermission.builder().responsable(userB).build();
        Modulo module = Modulo.builder()
                .permissoes(new ArrayList<>(Set.of(pA, pB)))
                .build();
        when(repository.findById(moduleId)).thenReturn(Optional.of(module));

        service.removeUsersFromModule(moduleId.toString(), userA);

        verify(repository).findById(moduleId);
        verify(repository).save(module);
        assertEquals(1, module.getPermissoes().size());
        assertTrue(module.getPermissoes().stream().noneMatch(p -> p.getResponsable().equals(userA)));
    }

    @Test
    @DisplayName("Should throw an error when removing from non-existent module")
    void removeUsersFromModule_ModuleNotFound_ShouldThrow() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(
                ModuleNotFoundFailure.class,
                () -> service.removeUsersFromModule(UUID.randomUUID().toString(), UUID.randomUUID())
        );
    }

    @Test
    @DisplayName("Should update or create permission for each module in batch")
    void updateUserPermissions_ShouldInvokeUpdateOrCreateOnEachModule() {
        // given
        UUID userId = UUID.randomUUID();
        UUID moduleId1 = UUID.randomUUID();
        UUID moduleId2 = UUID.randomUUID();

        ModulePermissionInput dto1 = makePerm(moduleId1.toString(), userId, true, Set.of(3), Set.of(30), Set.of(1), Set.of(0));
        ModulePermissionInput dto2 = makePerm(moduleId2.toString(), userId, false, Set.of(4), Set.of(40), Set.of(2), Set.of(0));

        Modulo mod1 = spy(Modulo.builder().configEtapas(new HashSet<>()).permissoes(new ArrayList<>()).build());
        Modulo mod2 = spy(Modulo.builder().configEtapas(new HashSet<>()).permissoes(new ArrayList<>()).build());

        when(repository.findById(moduleId1)).thenReturn(Optional.of(mod1));
        when(repository.findById(moduleId2)).thenReturn(Optional.of(mod2));

        BatchModulePermissionsDTO batch = new BatchModulePermissionsDTO(
                userId,
                Set.of(dto1, dto2)
        );

        service.updateUserPermissions(batch);

        verify(repository).findById(moduleId1);
        verify(repository).findById(moduleId2);
        verify(mod1).updateOrCreatePermission(userId, dto1);
        verify(mod2).updateOrCreatePermission(userId, dto2);
    }

    @Test
    @DisplayName("Should behave like updateUserPermissions when adding batch")
    void addPermissionsBatch_BehavesLikeUpdateUserPermissions() {
        UUID userId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        ModulePermissionInput dto = makePerm(moduleId.toString(), userId, false, Set.of(), Set.of(), Set.of(), Set.of());

        Modulo mod = spy(Modulo.builder().configEtapas(new HashSet<>()).permissoes(new ArrayList<>()).build());
        when(repository.findById(moduleId)).thenReturn(Optional.of(mod));

        BatchModulePermissionsDTO batch = new BatchModulePermissionsDTO(userId, Set.of(dto));

        service.addPermissionsBatch(batch);

        verify(repository).findById(moduleId);
        verify(mod).updateOrCreatePermission(userId, dto);
    }

    @Test
    @DisplayName("Should return modules for which the user has permission")
    void listModulesWithPermission_ShouldDelegateToRepository() {
        UUID userId = UUID.randomUUID();
        Modulo m1 = Modulo.builder().name("M1").build();
        Modulo m2 = Modulo.builder().name("M2").build();
        when(repository.findAllByResponsavel(userId)).thenReturn(List.of(m1, m2));

        List<Modulo> result = service.listModulesWithPermission(userId.toString());

        verify(repository).findAllByResponsavel(userId);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(m1, m2)));
    }

    @Test
    @DisplayName("Should add multiple users to module successfully")
    void addMultipleUsersToModule_ShouldAddMultipleUsers() {
        // given
        String moduleId = UUID.randomUUID().toString();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm1 = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId1.toString(),
                List.of(101, 102),
                List.of(0),
                List.of(1, 2),
                List.of(0, 1),
                false
            );
            
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm2 = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId2.toString(),
                List.of(103),
                List.of(0),
                List.of(1),
                List.of(0),
                true
            );

        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(
            List.of(userPerm1, userPerm2)
        );

        Modulo modulo = spy(Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build());

        User user1 = User.builder().id(userId1).build();
        User user2 = User.builder().id(userId2).build();

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));
        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        // when
        service.addMultipleUsersToModule(moduleId, dto);

        // then
        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository).findById(userId1);
        verify(userRepository).findById(userId2);
        verify(modulo, times(2)).addPermissao(any(ModulePermission.class));
        verify(repository).save(modulo);
    }

    @Test
    @DisplayName("Should add single user to module successfully")
    void addMultipleUsersToModule_ShouldAddSingleUser() {
        // given
        String moduleId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId.toString(),
                List.of(101),
                List.of(0),
                List.of(1),
                List.of(0),
                false
            );

        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(
            List.of(userPerm)
        );

        Modulo modulo = spy(Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build());

        User user = User.builder().id(userId).build();

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        service.addMultipleUsersToModule(moduleId, dto);

        // then
        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository).findById(userId);
        verify(modulo, times(1)).addPermissao(any(ModulePermission.class));
        verify(repository).save(modulo);
    }

    @Test
    @DisplayName("Should add user with empty lists successfully")
    void addMultipleUsersToModule_ShouldAddUserWithEmptyLists() {
        // given
        String moduleId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId.toString(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                false
            );

        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(
            List.of(userPerm)
        );

        Modulo modulo = spy(Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build());

        User user = User.builder().id(userId).build();

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        service.addMultipleUsersToModule(moduleId, dto);

        // then
        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository).findById(userId);
        verify(modulo, times(1)).addPermissao(any(ModulePermission.class));
        verify(repository).save(modulo);
    }

    @Test
    @DisplayName("Should throw ModuleNotFoundFailure when module does not exist")
    void addMultipleUsersToModule_ModuleNotFound_ShouldThrow() {
        // given
        String moduleId = UUID.randomUUID().toString();
        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(List.of());

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.empty());

        // when & then
        assertThrows(
            ModuleNotFoundFailure.class,
            () -> service.addMultipleUsersToModule(moduleId, dto)
        );

        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository, never()).findById(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when user does not exist")
    void addMultipleUsersToModule_UserNotFound_ShouldThrow() {
        // given
        String moduleId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId.toString(),
                List.of(101),
                List.of(0),
                List.of(1),
                List.of(0),
                false
            );

        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(
            List.of(userPerm)
        );

        Modulo modulo = Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build();

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(
            ModuleFailure.class,
            () -> service.addMultipleUsersToModule(moduleId, dto)
        );

        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository).findById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when second user does not exist")
    void addMultipleUsersToModule_SecondUserNotFound_ShouldThrow() {
        // given
        String moduleId = UUID.randomUUID().toString();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm1 = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId1.toString(),
                List.of(101),
                List.of(0),
                List.of(1),
                List.of(0),
                false
            );
            
        MultipleUsersModulePermissionsDTO.UserModulePermission userPerm2 = 
            new MultipleUsersModulePermissionsDTO.UserModulePermission(
                userId2.toString(),
                List.of(102),
                List.of(0),
                List.of(1),
                List.of(0),
                false
            );

        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(
            List.of(userPerm1, userPerm2)
        );

        Modulo modulo = Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build();

        User user1 = User.builder().id(userId1).build();

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));
        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(userId2)).thenReturn(Optional.empty());

        // when & then
        assertThrows(
            ModuleFailure.class,
            () -> service.addMultipleUsersToModule(moduleId, dto)
        );

        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository).findById(userId1);
        verify(userRepository).findById(userId2);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle empty permissions list")
    void addMultipleUsersToModule_EmptyPermissionsList_ShouldNotAddAnyUsers() {
        // given
        String moduleId = UUID.randomUUID().toString();
        MultipleUsersModulePermissionsDTO dto = new MultipleUsersModulePermissionsDTO(List.of());

        Modulo modulo = spy(Modulo.builder()
            .id(UUID.fromString(moduleId))
            .configEtapas(new HashSet<>())
            .permissoes(new ArrayList<>())
            .build());

        when(repository.findById(UUID.fromString(moduleId))).thenReturn(Optional.of(modulo));

        // when
        service.addMultipleUsersToModule(moduleId, dto);

        // then
        verify(repository).findById(UUID.fromString(moduleId));
        verify(userRepository, never()).findById(any());
        verify(modulo, never()).addPermissao(any(ModulePermission.class));
        verify(repository).save(modulo);
    }
}
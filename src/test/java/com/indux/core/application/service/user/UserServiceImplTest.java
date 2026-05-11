package com.indux.core.application.service.user;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.dto.user.UserFilter;
import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.application.service.fixtures.Fixture;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.auth.UserRole;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.repository.user.UserRoleRepository;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private UserRepository repository;
    private UserServiceImpl service;
    private UserRoleRepository userRoleRepository;
    private StorageService storage;
    private ModuleUserService moduleUserService;
    private GetEmployeeUseCase getEmployeeUseCase;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        userRoleRepository = mock(UserRoleRepository.class);
        storage = mock(StorageService.class);
        moduleUserService = mock(ModuleUserService.class);
        getEmployeeUseCase = mock(GetEmployeeUseCase.class);

        service = new UserServiceImpl(repository, userRoleRepository, storage, moduleUserService, getEmployeeUseCase);
    }

    @Test
    @DisplayName("Should update user role successfully")
    void shouldUpdateRoleSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = new User();
        UserRole role = new UserRole();

        when(userRoleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.updateRoleUser(id, 2L);

        verify(repository, times(1)).save(user);
        assertEquals(role, user.getRole());
    }

    @Test
    @DisplayName("Should return all users successfully")
    void shouldReturnAllUsers() {
        when(repository.findAll()).thenReturn(Fixture.userList());

        List<SimpleUser> users = service.getAllUsers();
        verify(repository, times(1)).findAll();
        assertEquals(2, users.size());
    }

    @Test
    @DisplayName("Should return an empty list without error when there are no users")
    void shouldReturnEmptyUsers() {
        when(repository.findAll()).thenReturn(List.of());

        List<SimpleUser> users = service.getAllUsers();
        verify(repository, times(1)).findAll();
        assertEquals(0, users.size());
    }

    @Test
    @DisplayName("Should throw when role not found")
    void shouldThrowWhenRoleNotFound() {
        UUID id = UUID.randomUUID();
        when(userRoleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.updateRoleUser(id, 2L));
    }

    @Test
    @DisplayName("Should throw when user not found on role update")
    void shouldThrowWhenUserNotFoundOnRoleUpdate() {
        UUID id = UUID.randomUUID();
        UserRole role = new UserRole();

        when(userRoleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.updateRoleUser(id, 2L));
    }

    @Test
    @DisplayName("Should disable user successfully")
    void shouldDisableUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = new User();

        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.disableUser(id);

        verify(repository, times(1)).save(user);
        assertTrue(user.isDisable());
    }

    @Test
    @DisplayName("Should enable user successfully")
    void shouldEnableUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setDisable(true);

        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.enableUser(id);

        verify(repository, times(1)).save(user);
        assertFalse(user.isDisable());
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = new User();

        when(repository.findById(id)).thenReturn(Optional.of(user));

        service.deleteUser(id);

        verify(repository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Should throw when user not found on disable")
    void shouldThrowWhenUserNotFoundOnDisable() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.disableUser(id));
    }

    @Test
    @DisplayName("Should throw when user not found on enable")
    void shouldThrowWhenUserNotFoundOnEnable() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.enableUser(id));
    }

    @Test
    @DisplayName("Should throw when user not found on delete")
    void shouldThrowWhenUserNotFoundOnDelete() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundEmployee.class, () -> service.deleteUser(id));
    }

    @Test
    @DisplayName("Should return empty page when search filter is null or blank")
    void shouldReturnEmptyPageWhenFilterBlank() {
        Pageable pageable = mock(Pageable.class);

        Page<SimpleUser> resultNull = service.searchUsers(pageable, null);
        assertTrue(resultNull.isEmpty());

        Page<SimpleUser> resultBlank = service.searchUsers(pageable, "   ");
        assertTrue(resultBlank.isEmpty());

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should search by CPF when filter contains exactly 11 digits")
    void shouldSearchByCpfWhenFilterIs11Digits() {
        Pageable pageable = mock(Pageable.class);
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setName("User CPF");
        mockUser.setCreatedAt(Instant.now());

        when(repository.findByCpf("12345678910")).thenReturn(mockUser);

        Page<SimpleUser> result = service.searchUsers(pageable, "123.456.789-10");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals("User CPF", result.getContent().get(0).getNome());

        verify(repository, times(1)).findByCpf("12345678910");
        verify(repository, never()).findAllByEmailContainingIgnoreCase(any(), anyString());
        verify(repository, never()).findAllByNameContainingIgnoreCase(any(), anyString());
    }

    @Test
    @DisplayName("Should search by email when filter contains @")
    void shouldSearchByEmailWhenFilterContainsAt() {
        Pageable pageable = mock(Pageable.class);
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setCreatedAt(Instant.now());

        when(repository.findAllByEmailContainingIgnoreCase(pageable, "test@email.com"))
                .thenReturn(new PageImpl<>(List.of(mockUser)));

        Page<SimpleUser> result = service.searchUsers(pageable, " test@email.com ");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());

        verify(repository, never()).findByCpf(anyString());
        verify(repository, times(1)).findAllByEmailContainingIgnoreCase(pageable, "test@email.com");
        verify(repository, never()).findAllByNameContainingIgnoreCase(any(), anyString());
    }

    @Test
    @DisplayName("Should search by name as fallback")
    void shouldSearchByNameAsFallback() {
        Pageable pageable = mock(Pageable.class);
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setCreatedAt(Instant.now());

        when(repository.findAllByNameContainingIgnoreCase(pageable, "Joao Vitor"))
                .thenReturn(new PageImpl<>(List.of(mockUser)));

        Page<SimpleUser> result = service.searchUsers(pageable, " Joao Vitor ");

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());

        verify(repository, never()).findByCpf(anyString());
        verify(repository, never()).findAllByEmailContainingIgnoreCase(any(), anyString());
        verify(repository, times(1)).findAllByNameContainingIgnoreCase(pageable, "Joao Vitor");
    }

    @Test
    @DisplayName("Should return filtered users by role and status")
    void shouldReturnFilteredUsers() {
        UserRole mockRole = new com.indux.core.domain.model.auth.UserRole();
        UserFilter filter = new com.indux.core.application.dto.user.UserFilter(mockRole, true);
        Pageable pageable = mock(Pageable.class);
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setCreatedAt(Instant.now());

        when(repository.findByRoleAndIsDisable(mockRole, true, pageable))
                .thenReturn(new PageImpl<>(List.of(mockUser)));

        Page<SimpleUser> result = service.getFilteredUsers(filter, pageable);

        assertFalse(result.isEmpty());
        verify(repository).findByRoleAndIsDisable(mockRole, true, pageable);
    }

    @Test
    @DisplayName("Should update profile image successfully")
    void shouldUpdateProfileImage() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        
        Path rootPath = Path.of("/root");
        Path storedPath = Path.of("/root/user/profile/file.jpg");
        Path thumbPath = Path.of("/root/user/profile/thumb.jpg");

        when(repository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(storage.store(file, "user/profile", userId.toString())).thenReturn(storedPath);
        when(storage.getRootLocation()).thenReturn(rootPath);
        when(storage.createThumbnail(anyString(), eq(150), eq(150))).thenReturn(thumbPath);

        String uri = service.updateProfileImageUser(userId, file);

        assertEquals("user/profile/file.jpg", uri);
        assertEquals("user/profile/thumb.jpg", mockUser.getThumbPhotoUrl());
        verify(repository).save(mockUser);
    }
    
    @Test
    @DisplayName("Should get employee from user successfully")
    void shouldGetEmployeeFromUser() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setCpf("12345678910");
        
        EmployeeDTO dto = new com.indux.core.application.dto.generic.EmployeeDTO();
        
        when(repository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(getEmployeeUseCase.getActiveEmployeeByCPF("12345678910")).thenReturn(dto);
        
        EmployeeDTO result = service.getEmployeeFromUser(userId);
        
        assertNotNull(result);
        assertEquals(dto, result);
    }
    
    @Test
    @DisplayName("Should throw NotFoundEmployee if employee missing for user")
    void shouldThrowIfEmployeeMissingForUser() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setCpf("12345678910");
        
        when(repository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(getEmployeeUseCase.getActiveEmployeeByCPF("12345678910")).thenReturn(null);
        
        assertThrows(NotFoundEmployee.class, () -> service.getEmployeeFromUser(userId));
    }
    
    @Test
    @DisplayName("Should get basic users with or without filter")
    void shouldGetBasicUsers() {
        Pageable pageable = mock(Pageable.class);
        UserNameProjection mockProjection = mock(com.indux.core.application.dto.user.UserNameProjection.class);
        
        when(repository.findByNameContainingIgnoreCase("maria", pageable))
                .thenReturn(new PageImpl<>(List.of(mockProjection)));
                
        when(repository.findAllProjectedBy(pageable))
                .thenReturn(new PageImpl<>(List.of(mockProjection, mockProjection)));
                
        Page<UserNameProjection> withFilter = service.getBasicUsers("maria", pageable);
        assertEquals(1, withFilter.getTotalElements());
        
        Page<UserNameProjection> withoutFilter = service.getBasicUsers(null, pageable);
        assertEquals(2, withoutFilter.getTotalElements());
    }
}

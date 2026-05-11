package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.ppu.application.services.fixtures.RDOFixture;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RDOApprovalUseCaseTest {
    @Mock
    private RDORepository repository;
    @Mock
    private StorageService service;
    @Mock
    private UserService userService;
    @Mock
    RDOLoggerUserMapper userMapper;
    @InjectMocks
    private RDOApprovalUseCase useCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);


    }

    @Test
    @DisplayName("Should approve RDO successfully")
    void ShouldAproveRDOSuccessfully() {
        var userFake = RDOFixture.fakeSimpleUserToRDO;
        var entity = RDOFixture.createFakeEntity();
        var userID = "ID_TESTE";

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(userService.getUserById(userID)).thenReturn(Optional.of(userFake));

        var response = useCase.approve(RDOFixture.fakeFlowRequest, false, userID);
        assertEquals(RDOStatusOP.APPROVED, response.getStatusOP());
        assertEquals(1, response.getLoggers().size());
    }

    @Test
    @DisplayName("Should create a logger when approving RDO")
    void ShouldCreateALoggerWhenApprovingRDO() {
        var userFake = RDOFixture.fakeSimpleUserToRDO;
        var entity = RDOFixture.createFakeEntity();
        var userID = "ID_TESTE";

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(userService.getUserById(userID)).thenReturn(Optional.of(userFake));

        var response = useCase.approve(RDOFixture.fakeFlowRequest, false, userID);
        assertNotNull(response.getLoggers().get(0));
        assertEquals("APPROVAL", response.getLoggers().get(0).getAction().name());
        assertFalse(response.getLoggers().isEmpty());
    }


    @Test
    @DisplayName("Should throw ModuleNotFoundFailure when RDO is not found")
    void shouldThrowModuleNotFoundFailureWhenRDONotFound() {

        when(repository.findById("TESTE")).thenReturn(Optional.empty());
        var request = RDOFixture.fakeFlowRequest;


        assertThrows(ModuleNotFoundFailure.class, () ->
                        useCase.approve(request, false, "USER_ID"),
                "RDO não encontrada no sistema."
        );
        verify(userService, never()).getUserById(anyString());
        verify(repository, never()).save(any(RDOEntity.class));
    }

    @Test
    @DisplayName("Should throw NotFoundEmployee when user is not found")
    void shouldThrowNotFoundEmployeeWhenUserNotFound() {
        var entity = RDOFixture.createFakeEntity();

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(userService.getUserById("USER_ID")).thenReturn(Optional.empty());
        var request = RDOFixture.fakeFlowRequest;

        assertThrows(NotFoundEmployee.class, () ->
                        useCase.approve(request, false, "USER_ID"),
                "Usuário não encontrado."
        );
        verify(repository, times(1)).findById(entity.getId());
        verify(repository, never()).save(any(RDOEntity.class));
    }

    @Test
    @DisplayName("Should throw ModuleNotFoundFailure when RDO is already approved")
    void shouldThrowWhenRDOAlreadyApproved() {
        var userFake = RDOFixture.fakeSimpleUserToRDO;
        var entity = RDOFixture.createFakeEntity();
        entity.setStatusOP(RDOStatusOP.APPROVED);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(userService.getUserById("USER_ID")).thenReturn(Optional.of(userFake));

        var request = RDOFixture.fakeFlowRequest;

        var ex = assertThrows(ModuleNotFoundFailure.class, () ->
                useCase.approve(request, false, "USER_ID"));
        assertEquals("RDO já aprovada.", ex.getMessage());
    }

    @Test
    @DisplayName("Should not set attachments when request has none")
    void shouldIgnoreEmptyAttachments() {
        var userFake = RDOFixture.fakeSimpleUserToRDO;
        var entity = RDOFixture.createFakeEntity();
        var request = RDOFixture.fakeFlowRequest;
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(userService.getUserById("USER_ID")).thenReturn(Optional.of(userFake));

        var response = useCase.approve(request, false, "USER_ID");
        assertTrue(response.getAttachments() == null || response.getAttachments().isEmpty());
    }

}
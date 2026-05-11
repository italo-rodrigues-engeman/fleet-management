package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.StepModuleDTO;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ModuleStepServiceImplTest {
    @Mock
    private ModuleRepository repository;
    @InjectMocks
    private ModuleStepServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should add a step in the module")
    void createModuleStep_ShouldAddStepToModule() {
        UUID id = UUID.randomUUID();
        Modulo module = Modulo.builder()
                .configEtapas(new HashSet<>())
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(module));
        StepModuleDTO dto = new StepModuleDTO("Validação", 45, 1);

        service.createModuleStep(id.toString(), dto);
        verify(repository).findById(id);
        assertEquals(1, module.getConfigEtapas().size());
        StepModule added = module.getConfigEtapas().iterator().next();
        assertEquals(1, added.getEtapa());
        assertEquals(45, added.getTempo());
        assertEquals("Validação", added.getNome());
    }

    @Test
    @DisplayName("Should throw an error when try create step and module not exist")
    void createModuleStep_ModuleNotFound() {
        UUID randomID = UUID.randomUUID();
        when(repository.findById(randomID)).thenReturn(Optional.empty());

        ModuleNotFoundFailure ex = assertThrows(
                ModuleNotFoundFailure.class,
                () -> service.createModuleStep(randomID.toString(), new StepModuleDTO("Any", 30, 1))
        );
        assertTrue(ex.getMessage().contains(randomID.toString()));

    }

    @Test
    @DisplayName("Should change time")
    void updateStepSLA() {
        UUID fixtureID = UUID.randomUUID();
        StepModule builder = StepModule.builder()
                .etapa(5)
                .tempo(20)
                .nome("Execução")
                .build();
        Modulo module = Modulo.builder()
                .configEtapas(new HashSet<>(List.of(builder))).build();
        when(repository.findById(fixtureID)).thenReturn(Optional.of(module));

        service.updateStepSLA(fixtureID.toString(), 5, 30);

        assertEquals(30, builder.getTempo());

    }

    @Test
    @DisplayName("Should throw an error when trying to change a step that does not exist")
    void updateAllStepsSLA() {
        UUID fixtureID = UUID.randomUUID();
        StepModule builder = StepModule.builder()
                .etapa(5)
                .tempo(20)
                .nome("Execução")
                .build();
        Modulo module = Modulo.builder()
                .configEtapas(new HashSet<>(List.of(builder))).build();
        when(repository.findById(fixtureID)).thenReturn(Optional.of(module));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateStepSLA(fixtureID.toString(), 2, 30)
        );
        assertTrue(ex.getMessage().contains("Etapa 2 não encontrada"));
    }

    @Test
    @DisplayName("Should throw an error when try change all times")
    void updateAllStepsSLA_ShouldChangeAllTimes() {

        UUID fixtureID = UUID.randomUUID();
        StepModule s1 = StepModule.builder().etapa(1).tempo(10).nome("A").build();
        StepModule s2 = StepModule.builder().etapa(2).tempo(20).nome("B").build();
        Modulo module = Modulo.builder()
                .configEtapas(new HashSet<>(List.of(s1, s2)))
                .build();
        when(repository.findById(fixtureID)).thenReturn(Optional.of(module));

        service.updateAllStepsSLA(fixtureID.toString(), 77);

        assertEquals(77, s1.getTempo());
        assertEquals(77, s2.getTempo());
        verify(repository).findById(fixtureID);
    }

    @Test
    @DisplayName("Should throw an error when try change a step and module not found")
    void updateAllStepsSLA_ModuleNotFound_ShouldThrow() {
        UUID fixtureID = UUID.randomUUID();
        when(repository.findById(fixtureID)).thenReturn(Optional.empty());

        assertThrows(
                ModuleNotFoundFailure.class,
                () -> service.updateAllStepsSLA(fixtureID.toString(), 100)
        );
    }
}
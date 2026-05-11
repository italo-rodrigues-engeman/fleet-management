package com.indux.modules.ppu.application.services.ppu.available;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.response.AvailableServiceDTO;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.AvailableServiceResponseMapper;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailableHandler Tests")
class AvailableHandlerTest {

    @Mock
    private PPURepository ppuRepository;

    @Mock
    private AvailableServiceResponseMapper mapper;

    @Mock
    private GetEmployeeUseCase getEmployeeUseCase;

    @Mock
    private BoardedEmployeeMapper boardedMapper;

    @InjectMocks
    private AvailableHandler availableHandler;

    private static final String FIXED_PPU_ID = "6849cf01c7b0250cfab80edc";
    private PPUEntity mockPPU;

    @BeforeEach
    void setUp() {
        var service1 = ServiceLine.builder().id("service-1").disposicao(true).build();
        var service2 = ServiceLine.builder().id("service-2").disposicao(false).build();
        var service3 = ServiceLine.builder().id("service-3").disposicao(true).build();

        var typeA = new AvailableType();
        typeA.setName("A");
        typeA.setQuantityDays(10);

        var typeB = new AvailableType();
        typeB.setName("B");
        typeB.setQuantityDays(3);

        mockPPU = PPUEntity.builder()
                .id(FIXED_PPU_ID)
                .services(List.of(service1, service2, service3))
                .platforms(List.of("PRA-1", "P-25"))
                .availableType(List.of(typeA, typeB))
                .build();
    }

    @Nested
    @DisplayName("getAvailableServices")
    class GetAvailableServicesTests {

        @Test
        @DisplayName("returns only 'disposicao' services + platforms")
        void returnsAvailableServicesAndPlatforms() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));
            when(mapper.toResponse(any(ServiceLine.class)))
                    .thenAnswer(invocation -> {
                        ServiceLine s = invocation.getArgument(0);
                        return new AvailableServiceDTO(s.getId(), s.getName(), s.getPpuNumber(), s.getGenericNumber(), List.of());
                    });

            var response = availableHandler.getAvailableServices(FIXED_PPU_ID);

            assertThat(response).isNotNull();
            assertThat(response.servicos()).extracting(AvailableServiceDTO::id).containsExactly("service-1", "service-3");
            assertThat(response.plataformas()).containsExactly("PRA-1", "P-25");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verify(mapper, times(2)).toResponse(any(ServiceLine.class));
        }

        @Test
        @DisplayName("throws ModuleNotFoundFailure when PPU not found")
        void throwsWhenPPUNotFound() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.empty());

            var ex = assertThrows(ModuleNotFoundFailure.class, () -> availableHandler.getAvailableServices(FIXED_PPU_ID));
            assertThat(ex.getMessage()).isEqualTo("PPU não encontrada no sistema.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    @DisplayName("addEmployeeAvailable")
    class AddEmployeeAvailableTests {

        @Test
        @DisplayName("throws ModuleFailure when PPU not found")
        void throwsWhenPPUNotFound() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.empty());

            var ex = assertThrows(ModuleFailure.class,
                    () -> availableHandler.addEmployeeAvailable(List.of(), "service-1", FIXED_PPU_ID));

            assertThat(ex.getMessage()).isEqualTo("PPU não encontrada no sistema.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verifyNoInteractions(getEmployeeUseCase, boardedMapper);
            verify(ppuRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ModuleFailure when service not found")
        void throwsWhenServiceNotFound() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var today = LocalDate.now();
            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-57"), null, null)
            );

            when(getEmployeeUseCase.findByRegistrations(List.of("123")))
                    .thenReturn(List.of(new SimpleEmployeeDTO("123", "John Doe", null, null, null, null)));

            when(boardedMapper.fromSimpleEmployeeDTO(any()))
                    .thenAnswer(inv -> BoardedEmployee.builder().registration("123").availableStartDate(today).build());

            var ex = assertThrows(ModuleFailure.class,
                    () -> availableHandler.addEmployeeAvailable(availableEmployees, "missing-service", FIXED_PPU_ID));

            assertThat(ex.getMessage()).isEqualTo("Serviço com ID missing-service não encontrado na PPU.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verify(ppuRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ModuleFailure when service is not disposicao")
        void throwsWhenServiceNotDisposicao() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-57"), null, null)
            );

            when(getEmployeeUseCase.findByRegistrations(List.of("123")))
                    .thenReturn(List.of(new SimpleEmployeeDTO("123", "John Doe", null, null, null, null)));

            when(boardedMapper.fromSimpleEmployeeDTO(any()))
                    .thenAnswer(inv -> BoardedEmployee.builder().registration("123").build());

            var ex = assertThrows(ModuleFailure.class,
                    () -> availableHandler.addEmployeeAvailable(availableEmployees, "service-2", FIXED_PPU_ID));

            assertThat(ex.getMessage()).isEqualTo("Serviço não está marcado como de disposição.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verify(ppuRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ModuleFailure when any employee is missing from base")
        void throwsWhenEmployeeNotFoundInBase() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-74"), null, null),
                    new AvailableHandler.AvailableEmployee("456", List.of("P-75"), null, null)
            );

            when(getEmployeeUseCase.findByRegistrations(List.of("123", "456")))
                    .thenReturn(List.of(new SimpleEmployeeDTO("123", "John Doe", null, null, null, null)));

            var ex = assertThrows(ModuleFailure.class,
                    () -> availableHandler.addEmployeeAvailable(availableEmployees, "service-1", FIXED_PPU_ID));

            assertThat(ex.getMessage()).isEqualTo("Um ou mais colaboradores não foram encontrados na base de dados.");
            verify(ppuRepository).findById(FIXED_PPU_ID);
            verify(ppuRepository, never()).save(any());
        }

        @Test
        @DisplayName("maps multiple platforms and applies AvailableType end date + type")
        void mapsMultiplePlatformsAndAppliesType() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var today = LocalDate.now();

            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-57", "P-25"), null, "A")
            );

            var employeeDTO = new SimpleEmployeeDTO("123", "John Doe", null, null, null, null);
            when(getEmployeeUseCase.findByRegistrations(List.of("123"))).thenReturn(List.of(employeeDTO));

            when(boardedMapper.fromSimpleEmployeeDTO(employeeDTO))
                    .thenAnswer(inv -> BoardedEmployee.builder().registration("123").build());

            availableHandler.addEmployeeAvailable(availableEmployees, "service-1", FIXED_PPU_ID);

            var ppuCaptor = ArgumentCaptor.forClass(PPUEntity.class);
            verify(ppuRepository).save(ppuCaptor.capture());

            var saved = ppuCaptor.getValue();
            var updatedService = saved.getServices().stream().filter(s -> s.getId().equals("service-1")).findFirst().orElseThrow();

            assertThat(updatedService.getEmployees()).hasSize(2);
            assertThat(updatedService.getEmployees()).extracting(BoardedEmployee::getPlatform).containsExactly("P-57", "P-25");
            assertThat(updatedService.getEmployees()).allSatisfy(e -> {
                assertThat(e.getRegistration()).isEqualTo("123");
                assertThat(e.getAvailableStartDate()).isEqualTo(today);
                assertThat(e.getAvailableEndDate()).isEqualTo(today.plusDays(10));
                assertThat(e.getAvailableType()).isEqualTo("A");
            });
        }

        @Test
        @DisplayName("fimDisposicao overrides AvailableType end date")
        void fimDisposicaoOverridesTypeEndDate() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var today = LocalDate.now();
            var overrideEnd = today.plusDays(2);

            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-57"), overrideEnd, "A")
            );

            var employeeDTO = new SimpleEmployeeDTO("123", "John Doe", null, null, null, null);
            when(getEmployeeUseCase.findByRegistrations(List.of("123"))).thenReturn(List.of(employeeDTO));

            when(boardedMapper.fromSimpleEmployeeDTO(employeeDTO))
                    .thenAnswer(inv -> BoardedEmployee.builder().registration("123").build());

            availableHandler.addEmployeeAvailable(availableEmployees, "service-1", FIXED_PPU_ID);

            var ppuCaptor = ArgumentCaptor.forClass(PPUEntity.class);
            verify(ppuRepository).save(ppuCaptor.capture());

            var saved = ppuCaptor.getValue();
            var updatedService = saved.getServices().stream().filter(s -> s.getId().equals("service-1")).findFirst().orElseThrow();

            assertThat(updatedService.getEmployees()).hasSize(1);
            var e = updatedService.getEmployees().getFirst();
            assertThat(e.getAvailableStartDate()).isEqualTo(today);
            assertThat(e.getAvailableEndDate()).isEqualTo(overrideEnd);
            assertThat(e.getAvailableType()).isEqualTo("A");
        }

        @Test
        @DisplayName("unknown type keeps end date null (unless fimDisposicao), still sets start date and platform")
        void unknownTypeDoesNotSetEndDate() {
            when(ppuRepository.findById(FIXED_PPU_ID)).thenReturn(Optional.of(mockPPU));

            var today = LocalDate.now();

            var availableEmployees = List.of(
                    new AvailableHandler.AvailableEmployee("123", List.of("P-57"), null, "UNKNOWN")
            );

            var employeeDTO = new SimpleEmployeeDTO("123", "John Doe", null, null, null, null);
            when(getEmployeeUseCase.findByRegistrations(List.of("123"))).thenReturn(List.of(employeeDTO));

            when(boardedMapper.fromSimpleEmployeeDTO(employeeDTO))
                    .thenAnswer(inv -> BoardedEmployee.builder().registration("123").build());

            availableHandler.addEmployeeAvailable(availableEmployees, "service-1", FIXED_PPU_ID);

            var ppuCaptor = ArgumentCaptor.forClass(PPUEntity.class);
            verify(ppuRepository).save(ppuCaptor.capture());

            var saved = ppuCaptor.getValue();
            var updatedService = saved.getServices().stream().filter(s -> s.getId().equals("service-1")).findFirst().orElseThrow();

            assertThat(updatedService.getEmployees()).hasSize(1);
            var e = updatedService.getEmployees().getFirst();
            assertThat(e.getPlatform()).isEqualTo("P-57");
            assertThat(e.getAvailableStartDate()).isEqualTo(today);
            assertThat(e.getAvailableEndDate()).isNull();
            assertThat(e.getAvailableType()).isNull();
        }
    }
}

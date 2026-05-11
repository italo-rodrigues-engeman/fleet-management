package com.indux.modules.ppu.application.services.rdo.operation;


import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.modules.ppu.application.services.fixtures.RDOFixture;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

//todo: corrigir o teste para nova regra de negocio
@DisplayName("FetchRDOUseCase Test")
class FetchRDOUseCaseTest {
    @Mock
    private PPURepository ppuRepository;
    @Mock
    private RDORepository repository;
    @Mock
    private GetEmployeeUseCase employeeUseCase;

    @InjectMocks
    private FetchRDOUseCase useCase;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
//
//    @Test
//    @DisplayName("Should add total planned in service when fetching RDO")
//    void shouldAddTotalPlannedInService() {
//        var fakeRDO = RDOFixture.createFakeEntity();
//        var fakePPU = PPUFixture.fakePPUEntityFunction();
//        var employeeFake = RDOFixture.fakeEmployeeToRDO;
//
//        when(repository.findById(fakeRDO.getId())).thenReturn(Optional.of(fakeRDO));
//        when(ppuRepository.findById(fakeRDO.getPpuId())).thenReturn(Optional.of(fakePPU));
//        when(employeeUseCase.getEmployeeByMatricula(fakeRDO.getCreatorRegistration())).thenReturn(employeeFake);
//
//        var response = useCase.execute(fakeRDO.getId());
//
//        var serviceWithPlanned = response.getServices().getFirst();
//        var forecast = PPUFixture.fakeForecast.getTotal();
//        assertNotNull(serviceWithPlanned.getTotalPlanned());
//        assertEquals(forecast, serviceWithPlanned.getTotalPlanned());
//    }

//    @Test
//    @DisplayName("Should add total planned in equipment when fetching RDO")
//    void shouldAddTotalPlannedInEquipment() {
//        var fakeRDO = RDOFixture.createFakeEntity();
//        var fakePPU = PPUFixture.fakePPUEntityFunction();
//        var employeeFake = RDOFixture.fakeEmployeeToRDO;
//
//        when(repository.findById(fakeRDO.getId())).thenReturn(Optional.of(fakeRDO));
//        when(ppuRepository.findById(fakeRDO.getPpuId())).thenReturn(Optional.of(fakePPU));
//        when(employeeUseCase.getEmployeeByMatricula(fakeRDO.getCreatorRegistration())).thenReturn(employeeFake);
//
//        RDOResponse response = useCase.execute(fakeRDO.getId());
//
//        var forecast = PPUFixture.fakeEquipmentService.getMeasurementForecasts().getFirst().getTotal();
//        var equipmentWithPlanned = response.getTotalPlannedEquipments();
//        assertNotNull(response.getEquipments());
//        assertEquals(forecast, equipmentWithPlanned);
//    }

//    @Test
//    @DisplayName("Should add total planned in accessories when fetching RDO")
//    void shouldAddTotalPlannedInAccessories() {
//        var fakeRDO = RDOFixture.createFakeEntity();
//        var fakePPU = PPUFixture.fakePPUEntityFunction();
//        var employeeFake = RDOFixture.fakeEmployeeToRDO;
//
//        when(repository.findById(fakeRDO.getId())).thenReturn(Optional.of(fakeRDO));
//        when(ppuRepository.findById(fakeRDO.getPpuId())).thenReturn(Optional.of(fakePPU));
//        when(employeeUseCase.getEmployeeByMatricula(fakeRDO.getCreatorRegistration())).thenReturn(employeeFake);
//
//        RDOResponse response = useCase.execute(fakeRDO.getId());
//        var responsePlanned = response.getAccessoryKits().getFirst().totalPrevistoRDO();
//        var ppuPlanned = fakePPU.getAccessoryKits().getFirst().getMeasurementForecasts().getFirst().getTotal();
//
//        assertNotNull(ppuPlanned);
//        assertNotNull(responsePlanned);
//        assertEquals(ppuPlanned, responsePlanned);
//    }
//
//    @Test
//    @DisplayName("Should add total planned in steel cables when fetching RDO")
//    void shouldAddTotalPlannedInSteelCables() {
//        var fakeRDO = RDOFixture.createFakeEntity();
//        var fakePPU = PPUFixture.fakePPUEntityFunction();
//        var employeeFake = RDOFixture.fakeEmployeeToRDO;
//
//        when(repository.findById(fakeRDO.getId())).thenReturn(Optional.of(fakeRDO));
//        when(ppuRepository.findById(fakeRDO.getPpuId())).thenReturn(Optional.of(fakePPU));
//        when(employeeUseCase.getEmployeeByMatricula(fakeRDO.getCreatorRegistration())).thenReturn(employeeFake);
//
//        RDOResponse response = useCase.execute(fakeRDO.getId());
//        var responsePlanned = response.getSteelCable().getFirst().totalPrevisto();
//        var ppuPlanned = fakePPU.getSteelCables().getFirst().getTotalPlanned();
//
//        assertNotNull(ppuPlanned);
//        assertEquals(ppuPlanned, responsePlanned);
//    }


    @Test
    @DisplayName("Should throw exception when RDO not found")
    void shouldThrowIfRDONotFound() {
        when(repository.findById("not-found")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> useCase.execute("not-found"));
    }

    @Test
    @DisplayName("Should throw exception when PPU not found")
    void shouldThrowIfPPUNotFound() {
        RDOEntity rdo = RDOFixture.createFakeEntity();

        when(repository.findById(rdo.getId())).thenReturn(Optional.of(rdo));
        when(ppuRepository.findById("ppu-1")).thenReturn(Optional.empty());
        when(employeeUseCase.getEmployeeByMatricula(rdo.getCreatorRegistration())).thenReturn(RDOFixture.fakeEmployeeToRDO);

        assertThrows(RuntimeException.class, () -> useCase.execute(rdo.getId()));
    }
}

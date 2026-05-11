package com.indux.modules.ppu.application.services.fixtures;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.application.dtos.rdo.itens.EmployeeOvertime;
import com.indux.modules.ppu.domain.entities.rdo.SteelCableChecker;
import com.indux.modules.ppu.application.dtos.requests.DivergenceRecord;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.item.RDORejectionType;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RDOFixture {

    public static final ClientEmployee fakeClientEmployee=
         new ClientEmployee(
                "123456789",
                "João da Silva",
                "213"
        );

    public static final RDOFlowRequest fakeFlowRequest = new RDOFlowRequest(
          "ID123",
                "Justificativa de teste",
                null,
            List.of(RDORejectionType.EMPLOYEE_NOT_BOARDED),
                List.of(
                        new DivergenceRecord("kit-123", LocalDate.now(), "Informação de teste", 3, 4)
                )
    );

    public static final SimpleUser fakeSimpleUserToRDO = SimpleUser.builder().build();

    public static final RDOLoggerUser fakeLoggerUser =
new            RDOLoggerUser(
        "Usuário de Teste",
        UUID.randomUUID());
    public static final EmployeeDTO fakeEmployeeToRDO = EmployeeDTO
            .builder().
            cargo("Cargo de Teste")
            .name("Nome do Funcionário")
            .matricula("Matricula123")
            .build();


    public static final RDOEquipment fakeEquipment =
         RDOEquipment.builder()
                .id("equipamento-123")
                .number("EQUIP-001")
                .name("Guindaste Teste")
                .totalPlanned(5)
                 .number("1.0.0.1")
                 .checkers(new ArrayList<>())
                .build();


    public static final RDOEquipment fakeEquipmentWithoutPlanned =
         RDOEquipment.builder()
                .id("equipamento-123")
                .number("EQUIP-001")
                .name("Guindaste Teste")
                 .number("1.0.0.1")
                 .checkers(new ArrayList<>())
                 .build();


    public static final SteelCableChecker fakeSteelCableCheckerWithoutPlanned =
         new SteelCableChecker(
                "ID",
                "123456789",
                "EQUIP-001",
                "",
                "DIARIA",
                250.0,
                1.0,
                100.0,
                null, null
        );

        public static final SteelCableChecker fakeSteelCableChecker =
             new SteelCableChecker(
                    "ID",
                    "123456789",
                    "EQUIP-001",
                    "",
                    "DIARIA",
                    250.0,
                    1.0,
                    100.0,
                    10, null
            );

    public static final AccessoryKitDTO fakeAccessoryKitWithoutPlanned =
         new AccessoryKitDTO(
                "kit-123",
                "1.0.0.01",
                "1.0",
                "Kit de Acessórios Teste",
                "Kit de Acessórios Teste111",
                "UN",
                150.0,
                1.0,
                true,
                true,
                "",
                null,
                null,
                 1.0, null
        );


    public static final AccessoryKitDTO fakeAccessoryKit =
         new AccessoryKitDTO(
                "kit-123",
                "1.0.1001",
                "1.0.1",
                 "Kit de Acessórios Teste",
                "EQUIP-001",
                "UN",
                150.0,
                1.0,
                true,
                true,
                "",
                null,
                150,
                 1.0, null
        );


    public static final CraneControl fakeCraneControl =
         new CraneControl(
          "123,",
                "NORTH_FACE",
                LocalDateTime.now(),
                true,
                "1000 horas",
                true,
                 new ArrayList<>(),
                "Responsável pela inoperância",
                "Justificativa de teste",
                 "PRA-1",
                 List.of()
        );


    public static final SteelCableControl fakeSteelCableControl=
         new SteelCableControl(
                 "NORTH_FACE",
                "Principal",
                true,
                "Supervisor de Teste",
                true,
                "Não houve necessidade de lubrificação",
                 "PRA-1"
         );


    public static final RDOEmployeeDeparture fakeRDOEmployeeDeparture=
         new RDOEmployeeDeparture(
                "123456789",
                "João da Silva",
                "124541",
                "Operador de Teste",
                LocalTime.now()
        );


    public static final RDOServiceEntity fullFakeRDOServiceEntity() {
        return RDOServiceEntity.builder()
                .id("TESTE")
                .serviceID("TESTE")
                .serviceName("Nome do Serviço")
                .serviceNumber("1.1.101")
                .registration("123123")
                .name("Nome Fake")
                .cargoID("CARGO123")
                .cargoNome("Cargo Nome")
                .present(true)
                .schedule(RDOFixture.fakeShiftSchedule)
                .dayType("Rotina")
                .horaChegadaVoo(LocalTime.of(6, 0))
                .flagman(true)
                .overtimes(RDOFixture.fakeEmployeeOvertimes)
                .sispat("SISPAT123")
                .teamLeader(false)
                .nightShiftPremium(Duration.ofHours(1))
                .normalHours(Duration.ofHours(8))
                .hourTotais(Duration.ofHours(11))
                .overtimeHourTotais(Duration.ofHours(3))
                .statusEmployee("Embarcado")
                .totalPlanned(5)
                .build();
    }


    public static RDOServiceEntity fullfakeRDOServiceComum(){
      return        RDOServiceEntity.builder()
              .id("ID")
              .serviceID("TESTE1")
              .serviceName("Serviço comum")
              .serviceNumber("1.1.102")
              .registration("123123")
              .name("Nome Fake")
              .cargoID("CARGO123")
              .cargoNome("Cargo Nome")
              .present(true)
              .schedule(RDOFixture.fakeShiftSchedule)
              .dayType("Rotina")
              .horaChegadaVoo(LocalTime.of(6, 0))
              .flagman(true)
              .overtimes(RDOFixture.fakeEmployeeOvertimes)
              .sispat("SISPAT123")
              .teamLeader(false)
              .nightShiftPremium(Duration.ofHours(1))
              .normalHours(Duration.ofHours(8))
              .hourTotais(Duration.ofHours(11))
              .overtimeHourTotais(Duration.ofHours(3))
              .statusEmployee("Embarcado")
              .totalPlanned(5)
              .build();

    }


    public static final ShiftSchedule fakeShiftSchedule=
             new ShiftSchedule(
                    "Diurno",
                    LocalTime.of(8, 0),
                    LocalTime.of(12, 0),
                    LocalTime.of(13, 0),
                    LocalTime.of(17, 0),
                     false
            );


    public static final List<EmployeeOvertime> fakeEmployeeOvertimes=
        List.of(
                new EmployeeOvertime(LocalTime.of(19, 0), LocalTime.of(21, 0)),
                new EmployeeOvertime(LocalTime.of(5, 0), LocalTime.of(6, 0))
        );

    public static final List<DivergenceRecord> fakeDivergences = List.of(
            new DivergenceRecord("TESTE", LocalDate.now(), "Informação de teste", 3, 2),
            new DivergenceRecord("kit-123", LocalDate.now(), "Informação de teste", 3, 4)
    );
    public static RDOEntity createFakeEntity() {
        return new RDOEntity(
                "ID123",
                "123987dsafsd9874923",
                "PRA-1",
                1L,
                LocalDate.now().minusDays(1),
                "33152",
                "Supervisor de Teste",
                LocalDateTime.now(),
                "WEB",
                "Cargo de Teste",
                Map.of(),
                "Cliente Teste",
                "Regional Teste",
                "2025-06",
                RDOStatusDP.PENDING,
                RDOStatusOP.PENDING,
                RDOFixture.fakeClientEmployee,
                "Sem observações",
                List.of(RDOFixture.fakeEquipmentWithoutPlanned),
                null,
                List.of(RDOFixture.fakeSteelCableCheckerWithoutPlanned),
                List.of(RDOFixture.fakeAccessoryKitWithoutPlanned),
                List.of(RDOFixture.fakeCraneControl),
                List.of(RDOFixture.fakeSteelCableControl),
                List.of(RDOFixture.fakeRDOEmployeeDeparture),
                List.of(RDOFixture.fullFakeRDOServiceEntity(), RDOFixture.fullfakeRDOServiceComum()),
                "Serviços contratados",
                "Justificado",
                new ArrayList<>(), // <- mutable loggers
                new ArrayList<>(), // <- mutable loggers
                new ArrayList<>(), // <- outros logs
                fakeDivergences,
                0L,
                null,
                null,
                false
        );
    }


}

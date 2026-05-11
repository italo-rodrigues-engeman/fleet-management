//package com.indux.modules.ppu.application.services;
//
//import com.indux.core.application.dto.generic.EmployeeDTO;
//import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
//import com.indux.core.domain.model.employee.ContractProject;
//import com.indux.core.domain.model.employee.EmployeePosition;
//import com.indux.modules.ppu.application.dtos.requests.PPURequest;
//import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
//import com.indux.modules.ppu.domain.entities.jpa.DatabaseSequencePPU;
//import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
//import com.indux.modules.ppu.domain.entities.item.PPUServiceItem;
//import com.indux.modules.ppu.domain.entities.item.TeamLeader;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.time.LocalTime;
//
//public class PPUServiceTestFixtures {
//
//    public static PPURequest createPPURecord() {
//        return new PPURequest(
//                1L, // filialID
//                "Filial Teste", // filialName
//                1, // clienteID
//                1, // contratoID
//                Arrays.asList("Plataforma1"), // plataformas
//                Collections.emptyList(), // servicos
//                Collections.emptyList(), // equipamentos
//                Collections.emptyList(), // cabosAcos
//                Collections.emptyList(), // kitAcessorios
//                Collections.emptyList(), // controleGuindaste
//                Collections.emptyList(), // controleCaboAco
//                Arrays.asList("reg1", "reg2"), // disposicao
//                "Descrição teste", // descricao
//                LocalTime.of(7,0),
//                LocalTime.of(19,0),
//                LocalTime.of(19,0),
//                LocalTime.of(7,0),
//                new TeamLeader(),
//                1
//        );
//    }
//
//    public static ContractProject createContractProject() {
//        return new ContractProject();
//    }
//
//    public static List<SimpleEmployeeDTO> createDispositionEmployees() {
//        return Arrays.asList(
//                new SimpleEmployeeDTO("reg1", "Empregado 1", "cargo1", "Cargo 1", "sispat1"),
//                new SimpleEmployeeDTO("reg2", "Empregado 2", "cargo2", "Cargo 2", "sispat2"));
//    }
//
//    public static DatabaseSequencePPU createPPUSequence() {
//        DatabaseSequencePPU sequence = new DatabaseSequencePPU();
//        sequence.setId(1L);
//        return sequence;
//    }
//
//    public static PPUEntity createPPUEntity() {
//        PPUEntity ppu = new PPUEntity();
//        ppu.setCodeID(1L);
//        return ppu;
//    }
//
//    public static EmployeeDTO createEmployeeDTO() {
//        EmployeeDTO employee = new EmployeeDTO();
//        employee.setMatricula("123");
//        return employee;
//    }
//
//    public static BoardedEmployee createBoardedEmployee() {
//        BoardedEmployee boarded = new BoardedEmployee();
//        boarded.setRegistration("123");
//        boarded.setPlatform("PlatformA");
//        return boarded;
//    }
//
//    public static PPUEntity createActivePPU() {
//        PPUEntity ppu = new PPUEntity();
//        ppu.setPlatforms(Arrays.asList("PlatformA"));
//        return ppu;
//    }
//
//    public static List<SimpleEmployeeDTO> createPlatformEmployees() {
//        return Arrays.asList(
//                new SimpleEmployeeDTO("123", "Empregado Plataforma", "cargo1", "Cargo 1", "sispat1"));
//    }
//
//    public static PPUEntity createPPUWithServices() {
//        PPUEntity ppu = new PPUEntity();
//
//        PPUServiceItem service1 = new PPUServiceItem();
//        EmployeePosition position1 = new EmployeePosition();
//        position1.setIdHCM("pos1");
//        position1.setName("Position1");
//        service1.setPositions(Arrays.asList(position1));
//
//        PPUServiceItem service2 = new PPUServiceItem();
//        EmployeePosition position2 = new EmployeePosition();
//        position2.setIdHCM("pos2");
//        position2.setName("Position2");
//        service2.setPositions(Arrays.asList(position2));
//
//        ppu.setServices(Arrays.asList(service1, service2));
//        return ppu;
//    }
//
//    public static List<SimpleEmployeeDTO> createAvailableEmployees() {
//        return Arrays.asList(
//                new SimpleEmployeeDTO("reg1", "Empregado 1", "cargo1", "Cargo 1", "sispat1"),
//                new SimpleEmployeeDTO("reg2", "Empregado 2", "cargo2", "Cargo 2", "sispat2"));
//    }
//}

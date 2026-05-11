package com.indux.modules.ppu.application.services.rdo.operation.audit.mio;

import com.indux.core.domain.model.generic.DateRange;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.services.ProjectService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.ppu.domain.entities.bm.MioDivergenceJustification;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import java.util.concurrent.ExecutionException;

import static com.indux.modules.ppu.application.services.rdo.operation.audit.mio.AuditMIOFixtures.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditMIOTest {

        @Mock
        private EmployeeBoardingETL etl;

        @Mock
        private RDORepository repository;

        @Mock
        private PPURepository ppuRepository;

        @Mock
        private BMRepository bmRepository;

        @Mock
        private ProjectService projectService;

        @Spy
        private BoardedEmployeeMapper mapper = Mappers.getMapper(BoardedEmployeeMapper.class);

        @InjectMocks
        private AuditMioImpl auditMioImpl;

        private JwtAuthenticationToken token;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                String userId = "11111111-1111-1111-1111-111111111111"; // qualquer UUID válido

                Jwt jwt = new Jwt(
                                "teste",
                                Instant.now(),
                                Instant.now().plusSeconds(3600),
                                Map.of("alg", "none"),
                                Map.of(
                                                "sub", userId,
                                                "preferred_username", "tester"));

                token = new JwtAuthenticationToken(jwt, Collections.emptyList(), userId);
        }

        @Test
        @DisplayName("Should run call() end-to-end and return a non-null result")
        void shouldReturnAnythingDivergence() throws IOException, ExecutionException, InterruptedException {
                BMEntity bm = bmEntity(3);
                PPUEntity ppu = ppuEntity();
                List<RDOEntity> rdos = fakeRdos(
                                3,
                                bm.getPeriod().getStart(),
                                List.of(fakeService(), fakeService2()));

                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.of(ppu));

                ProjectEntity project = mock(ProjectEntity.class);
                when(project.getFilial()).thenReturn(Collections.emptyList());
                when(projectService.getbyId(ppu.getProjectId())).thenReturn(project);

                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(
                                eq(ppu.getPlatforms()),
                                eq(RDOStatusOP.APPROVED),
                                eq(bm.getPeriod().getStart()),
                                eq(bm.getPeriod().getEnd().minusDays(1)))).thenReturn(rdos);

                when(etl.fetchBoardedEmployees(
                                isNull(),
                                isNull(),
                                eq(bm.getPeriod().getStart().toString()),
                                eq(bm.getPeriod().getEnd().toString()))).thenReturn(Collections.emptyList());

                var result = auditMioImpl.call("bmFake", token);

                assertThat(result).isNotNull();
                verify(bmRepository).findById("bmFake");
                verify(ppuRepository).findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO);
                verify(projectService).getbyId(ppu.getProjectId());
                verify(repository).findAllByPlatformInAndStatusOPAndDateBetween(
                                ppu.getPlatforms(),
                                RDOStatusOP.APPROVED,
                                bm.getPeriod().getStart(),
                                bm.getPeriod().getEnd().minusDays(1));
                verify(etl).fetchBoardedEmployees(
                                null,
                                null,
                                bm.getPeriod().getStart().toString(),
                                bm.getPeriod().getEnd().toString());
        }

        @Test
        @DisplayName("Should return divergence in STATUS RDO")
        void shouldReturnDivergence() throws IOException, ExecutionException, InterruptedException {
                BMEntity bm = bmEntity(3);
                var boardedEmployees = List.of(
                                fakeBoardedA(bm.getPeriod().getStart(), bm.getPeriod().getEnd()),
                                fakeBoardedB(bm.getPeriod().getStart(), bm.getPeriod().getEnd()));
                PPUEntity ppu = ppuEntity();
                List<RDOEntity> rdos = fakeRdos(
                                2,
                                bm.getPeriod().getStart(),
                                List.of(fakeService(), fakeService2()));

                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.of(ppu));

                ProjectEntity project = mock(ProjectEntity.class);
                List<FilialHcmEntity> filiais = List.of(FilialHcmEntity.builder().filialId(99).build());
                when(project.getFilial()).thenReturn(filiais);
                when(projectService.getbyId(ppu.getProjectId())).thenReturn(project);

                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(
                                eq(ppu.getPlatforms()),
                                eq(RDOStatusOP.APPROVED),
                                eq(bm.getPeriod().getStart()),
                                eq(bm.getPeriod().getEnd()))).thenReturn(rdos);

                when(etl.fetchBoardedEmployees(
                                isNull(),
                                isNull(),
                                eq(bm.getPeriod().getStart().toString()),
                                eq(bm.getPeriod().getEnd().toString()))).thenReturn(boardedEmployees);

                var result = auditMioImpl.call("bmFake", token);

                assertThat(result).isNotNull();
                assertTrue(result.stream().anyMatch(e -> e.getStatusRDO().contains("DESEMBARQUE")));

                rdos = fakeRdos(
                                1,
                                bm.getPeriod().getStart(),
                                List.of(fakeService(), fakeService2()));
                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(
                                eq(ppu.getPlatforms()),
                                eq(RDOStatusOP.APPROVED),
                                eq(bm.getPeriod().getStart()),
                                eq(bm.getPeriod().getEnd()))).thenReturn(rdos);

                result = auditMioImpl.call("bmFake", token);
                assertTrue(result.stream().anyMatch(e -> e.getStatusRDO().contains("AUSENTE")));
        }

        @Test
        @DisplayName("Should return divergence in MIO RDO")
        void shouldReturnDivergenceByMIO() throws IOException, ExecutionException, InterruptedException {
                BMEntity bm = bmEntity(3);
                var boardedEmployees = List.of(
                                fakeBoardedA(bm.getPeriod().getStart(), bm.getPeriod().getEnd()),
                                fakeBoardedB(bm.getPeriod().getStart(), bm.getPeriod().getEnd()));
                PPUEntity ppu = ppuEntity();
                List<RDOEntity> rdos = fakeRdos(
                                5,
                                bm.getPeriod().getStart(),
                                List.of(fakeService(), fakeService2()));

                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.of(ppu));

                ProjectEntity project = mock(ProjectEntity.class);
                List<FilialHcmEntity> filiais = List.of(FilialHcmEntity.builder().filialId(99).build());
                when(project.getFilial()).thenReturn(filiais);
                when(projectService.getbyId(ppu.getProjectId())).thenReturn(project);

                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(
                                eq(ppu.getPlatforms()),
                                eq(RDOStatusOP.APPROVED),
                                eq(bm.getPeriod().getStart()),
                                eq(bm.getPeriod().getEnd().minusDays(1)))).thenReturn(rdos);

                when(etl.fetchBoardedEmployees(
                                isNull(),
                                isNull(),
                                eq(bm.getPeriod().getStart().toString()),
                                eq(bm.getPeriod().getEnd().toString()))).thenReturn(boardedEmployees);

                var result = auditMioImpl.call("bmFake", token);

                assertThat(result).isNotNull();
                assertTrue(result.stream().anyMatch(e -> e.getStatusRDO().contains("PRESENTE")));
                assertTrue(result.stream().anyMatch(e -> e.getStatusMIO().contains("AUSENTE")));
        }

        @Test
        @DisplayName("Should return a divergence because the employee is not valid")
        void shouldReturnDivergenceBecauseEmployeeNotValid()
                        throws IOException, ExecutionException, InterruptedException {
                BMEntity bm = bmEntity(1);
                var boardedEmployees = List.of(
                                fakeBoardedA(bm.getPeriod().getStart(), bm.getPeriod().getEnd()),
                                fakeBoardedB(bm.getPeriod().getStart(), bm.getPeriod().getEnd()));
                PPUEntity ppu = ppuEntity();
                List<RDOEntity> rdos = fakeRdos(
                                1,
                                bm.getPeriod().getStart(),
                                List.of(fakeService(), fakeService2()));

                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.of(ppu));

                ProjectEntity project = mock(ProjectEntity.class);
                List<FilialHcmEntity> filiais = List.of(FilialHcmEntity.builder().filialId(100).build());
                when(project.getFilial()).thenReturn(filiais);
                when(projectService.getbyId(ppu.getProjectId())).thenReturn(project);

                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(
                                eq(ppu.getPlatforms()),
                                eq(RDOStatusOP.APPROVED),
                                eq(bm.getPeriod().getStart()),
                                eq(bm.getPeriod().getEnd().minusDays(1)))).thenReturn(rdos);

                when(etl.fetchBoardedEmployees(
                                isNull(),
                                isNull(),
                                eq(bm.getPeriod().getStart().toString()),
                                eq(bm.getPeriod().getEnd().toString()))).thenReturn(boardedEmployees);

                var result = auditMioImpl.call("bmFake", token);

                assertThat(result).isNotNull();
                assertEquals(4, result.size());
                assertTrue(result.stream().anyMatch(e -> e.getStatusMIO().contains("AUSENTE")));
                assertTrue(result.stream().anyMatch(e -> e.getStatusRDO().contains("PRESENTE")));
        }

        @Test
        @DisplayName("Should return justification text and include already justified (but fixed) items")
        void shouldReturnJustificationTextAndIncludeFixedItems()
                        throws IOException, ExecutionException, InterruptedException {
                BMEntity bm = bmEntity(1);
                LocalDate date = bm.getPeriod().getStart();

                bm.getMioJustifications().add(MioDivergenceJustification.builder()
                                .registration("8888")
                                .date(date)
                                .justification("Justificativa de teste")
                                .build());

                PPUEntity ppu = ppuEntity();
                List<RDOEntity> rdos = fakeRdos(0, date, List.of(
                                RDOServiceEntity.builder().registration("8888").serviceName("Func Fixed").build(),
                                RDOServiceEntity.builder().registration("1010").serviceName("Func Div").build()));

                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.of(ppu));

                ProjectEntity project = mock(ProjectEntity.class);
                when(project.getFilial()).thenReturn(List.of(FilialHcmEntity.builder().filialId(99).build()));
                when(projectService.getbyId(ppu.getProjectId())).thenReturn(project);

                when(repository.findAllByPlatformInAndStatusOPAndDateBetween(any(), any(), any(), any()))
                                .thenReturn(rdos);

                when(etl.fetchBoardedEmployees(any(), any(), any(), any())).thenReturn(List.of(
                                BoardedEmployee.builder().registration("8888").name("Func Fixed").platform("Plataforma")
                                                .boarding(date).filial_HCM("99 - F").build()));

                var result = auditMioImpl.call("bmFake", token);

                assertThat(result).isNotNull();


                assertEquals(2, result.size());


                var fixedItem = result.stream().filter(d -> d.getRegistration().equals("8888")).findFirst()
                                .orElseThrow();
                assertTrue(fixedItem.isJustified());
                assertEquals("Justificativa de teste", fixedItem.getJustification());
                assertEquals("PRESENTE", fixedItem.getStatusMIO());
                assertEquals("PRESENTE", fixedItem.getStatusRDO());


                var divItem = result.stream().filter(d -> d.getRegistration().equals("1010")).findFirst().orElseThrow();
                assertFalse(divItem.isJustified());
                assertNull(divItem.getJustification());
        }

        @Test
        @DisplayName("Should throw when BM not found")
        void shouldThrowWhenBmNotFound() {
                when(bmRepository.findById("bmFake")).thenReturn(Optional.empty());
                assertThrows(ModuleNotFoundFailure.class, () -> auditMioImpl.call("bmFake", token));
        }

        @Test
        @DisplayName("Should throw when PPU not found")
        void shouldThrowWhenPpuNotFound() {
                BMEntity bm = bmEntity(1);
                when(bmRepository.findById("bmFake")).thenReturn(Optional.of(bm));
                when(ppuRepository.findByProjectIdAndStatus(bm.getProjectId(), DocumentStatus.ABERTO))
                                .thenReturn(Optional.empty());

                assertThrows(ModuleNotFoundFailure.class, () -> auditMioImpl.call("bmFake", token));
        }
}

class AuditMIOFixtures {
        private static DateRange dateRange(long quantity) {
                var start = LocalDate.of(2025, 9, 10);
                return new DateRange(
                                start,
                                start.plusDays(quantity));
        }

        static BMEntity bmEntity(long quantity) {
                new PPUEntity();
                return BMEntity
                                .builder()
                                .id("bmFake")
                                .period(dateRange(quantity))
                                .ppu(PPUEntity.builder().id("ppuFake").build())
                                .projectId(1L)
                                .build();
        }

        static PPUEntity ppuEntity() {
                return PPUEntity.builder()
                                .id("ppuFake")
                                .platforms(List.of("Plataforma"))
                                .projectId(1L)
                                .build();
        }

        static RDOServiceEntity fakeService() {
                return RDOServiceEntity.builder()
                                .registration("1010")
                                .serviceName("Funcionário A")
                                .statusEmployee("Embarque")
                                .build();
        }

        static RDOServiceEntity fakeService2() {
                return RDOServiceEntity.builder()
                                .registration("1011")
                                .serviceName("Funcionário B")
                                .statusEmployee("Embarque")
                                .build();
        }

        static List<RDOEntity> fakeRdos(Integer quantity, LocalDate initialDay,
                        List<RDOServiceEntity> serviceEntities) {
                List<RDOEntity> rdos = new ArrayList<>();
                for (int i = 0; i <= quantity; i++) {
                        RDOEntity rdo = RDOEntity.builder()
                                        .id(UUID.randomUUID().toString())
                                        .date(initialDay.plusDays(i))
                                        .statusOP(RDOStatusOP.APPROVED)
                                        .platform("Plataforma")
                                        .services(serviceEntities)
                                        .build();
                        rdos.add(rdo);
                }
                return rdos;
        }

        static BoardedEmployee fakeBoardedA(LocalDate initial, LocalDate end) {
                return BoardedEmployee.builder()
                                .registration("1010")
                                .name("Funcionário A")
                                .platform("Plataforma")
                                .boarding(initial)
                                .landingForecast(end)
                                .filial_HCM("99 - FILIAL")
                                .build();
        }

        static BoardedEmployee fakeBoardedB(LocalDate initial, LocalDate end) {
                return BoardedEmployee.builder()
                                .registration("1011")
                                .name("Funcionário B")
                                .platform("Plataforma")
                                .boarding(initial)
                                .landingForecast(end)
                                .filial_HCM("99 - FILIAL")
                                .build();
        }
}

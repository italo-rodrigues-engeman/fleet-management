package com.indux.modules.cdi.aplication.service;

import com.indux.modules.cdi.aplication.dtos.ApplicantDTO;
import com.indux.modules.cdi.aplication.dtos.AvaliationDTO;
import com.indux.modules.cdi.aplication.dtos.CreateCdiDTO;
import com.indux.modules.cdi.aplication.dtos.UpdateCdiDTO;
import com.indux.modules.cdi.domain.entities.models.*;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import com.indux.modules.cdi.domain.repositories.mongo.CdiRepository;
import com.indux.modules.cdi.infra.mappers.CdiMapper;
import com.indux.modules.cdi.infra.mappers.FilterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.*;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CdiServiceTest {
    @Mock
    private CdiRepository cdiRepository;
    @Mock
    private FilterMapper filterMapper;
    @Mock
    private PointsService pointsService;

    @Mock
    private ActionCDIService actionCDIService;
    @Mock
    private CounterService  counterService;

    @InjectMocks
    private CdiService cdiService;

    @Spy
    private CdiMapper cdiMapper = Mappers.getMapper(CdiMapper.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return a DTO when I put the entity")
    public void shouldReturnDTOWhenIPutTheEntity() {
        var avaliation = new UpdateCdiDTO(10, 10, 10, "Testes", null, null, 100, null, null,null,null);

        var response = CdiService.newAvaliationDTO(avaliation);

        assertNotNull(response);

        assertEquals(avaliation.gravidade(), response.severity());
        assertEquals(avaliation.urgencia(), response.urgency());
        assertEquals(avaliation.observacao(), response.observation());
    }

    @Test
    @DisplayName("Should create news cdi")
    public void shouldCreateNewCdi() {
        var application =new ApplicantDTO("teste","teste","teste","teste","teste","teste","teste","teste","tetse","teste");
        var type = Type.DESENVOLVIMENTO;
        var scope = Scope.CONTRATO;
        var complexty = Complexity.ALTO;
        var previst = PrevistTime.CURTO;
        var points = 70;
        var dto = new CreateCdiDTO(application, type, scope, complexty, previst,"teste",false,null,"teste","teste",null,"titulo",null,null);

        when(pointsService.getAllPoints()).thenReturn(CDIFixtures.fakeGetAllPoints());
        when(cdiRepository.save(ArgumentMatchers.any())).thenReturn(CDIFixtures.fakeCreateCdi(Stage.LOCAL));

        var response = cdiService.createCdi(dto);


        assertTrue(response instanceof CdiEntity);

        assertEquals(points, response.getPoints());
    }

    @Test
    @DisplayName("Should update local CDi")
    public void shouldUpdateLocalCdi() {

        var points = 110;

        var update = new UpdateCdiDTO(10,10,10,"teste",null,null,10,null, null, null, null);

        when(cdiRepository.save(ArgumentMatchers.any())).thenReturn(CDIFixtures.fakeUpdateLocalCDI(Stage.DIRETORIA1));
        when(cdiRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(CDIFixtures.fakeCreateCdi(Stage.LOCAL)));
        var response = cdiService.updateCdi("s",update);

        assertEquals(points, response.getPoints());
    }

    @Test
    @DisplayName("Should update dir1 CDi")
    public void shouldUpdateDir1Cdi() {
        var stage = Stage.DIRETORIA2;
        var points = 110;

        var update = new UpdateCdiDTO(10,10,10,"teste",null,null,10,null, null, null, null);

        when(cdiRepository.save(ArgumentMatchers.any())).thenReturn(CDIFixtures.fakeUpdateLocalCDI(Stage.DIRETORIA2));
        when(cdiRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(CDIFixtures.fakeCreateCdi(Stage.DIRETORIA1)));
        var response = cdiService.updateCdi("s",update);

        assertEquals(stage, response.getStage());
    }

    @Test
    @DisplayName("Should update dir2/comite CDi")
    public void shouldUpdateDir2ComiteCdi() {
        var status = Status.APROVADO;
        var points = 110;

        var application =new ApplicantDTO("teste","teste","teste","teste","teste","teste","teste","teste","tetse","teste");
        var update = new UpdateCdiDTO(10,10,10,"teste",Complexity.ALTO,application,10,Action.APROVADO, null,null,null);

        when(pointsService.getAllPoints()).thenReturn(CDIFixtures.fakeGetAllPoints());
        when(cdiRepository.save(ArgumentMatchers.any())).thenReturn(CDIFixtures.fakeUpdateDir2ComiteCDI(Status.APROVADO));
        when(cdiRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(CDIFixtures.fakeCreateCdi(Stage.DIRETORIA2)));
        var response = cdiService.updateCdi("s",update);

        assertEquals(status, response.getStatus());
    }


}
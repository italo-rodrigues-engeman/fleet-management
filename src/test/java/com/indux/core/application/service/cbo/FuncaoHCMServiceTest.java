package com.indux.core.application.service.cbo;

import com.indux.core.application.dto.cbo.FuncaoDTO;
import com.indux.core.application.dto.cbo.DataLog;
import com.indux.core.application.dto.cbo.HistoryFuncao;
import com.indux.core.application.dto.cbo.UpdateTraining;
import com.indux.core.application.mapper.FuncaoHCMMapper;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.repository.cbo.CourseSuperiorRepository;
import com.indux.core.domain.repository.cbo.CourseTechnicalRepository;
import com.indux.core.domain.repository.cbo.FuncaoHCMRepository;
import com.indux.core.domain.service.AttachmentService;
import com.indux.modules.cdi.aplication.service.CounterService;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import com.indux.modules.faq.domain.repository.mongo.PerguntaMongoRepository;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.training.domain.entity.TrainingEntity;
import com.indux.modules.training.domain.repository.TrainingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncaoHCMServiceTest {

    @Mock
    private FuncaoHCMRepository funcaoHCMRepository;
    @Mock
    private FilialService filialService;
    @Mock
    private FuncaoHCMMapper funcaoHCMMapper;
    @Mock
    private CounterService counterService;
    @Mock
    private AttachmentService attachmentService;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private CourseSuperiorRepository courseSuperiorRepository;
    @Mock
    private CourseTechnicalRepository courseTechnicalRepository;
    @Mock
    private PerguntaMongoRepository perguntaMongoRepository;

    @InjectMocks
    private FuncaoHCMService funcaoHCMService;

    private FuncaoHCM mockFuncao;

    @BeforeEach
    void setUp() {
        mockFuncao = new FuncaoHCM();
        mockFuncao.setId("F123");
        mockFuncao.setHcmId("HCM-999");
    }

    @Test
    @DisplayName("Should generate next sequence block and correctly call repository bounds on create")
    void ShouldGenerateNextSequenceBlockAndCorrectlyCallRepositoryBoundsOnCreate() {
        FuncaoDTO dto = new FuncaoDTO();
        dto.setTrainingsId(List.of("T1"));
        dto.setCourseSuperiorId(List.of("C1"));
        dto.setCourseTechnicalId(List.of("CT1"));
        dto.setObrigatoryId(List.of("O1"));
        dto.setProactiveId(List.of("P1"));

        when(counterService.getNextSequence("funcaoHCM_sequence")).thenReturn(101L);
        when(funcaoHCMMapper.toEntity(eq(dto), anyList())).thenReturn(mockFuncao);
        
        when(trainingRepository.findAllById(any())).thenReturn(List.of(new TrainingEntity()));
        when(courseSuperiorRepository.findAllById(any())).thenReturn(List.of(new CourseSuperior()));
        when(courseTechnicalRepository.findAllById(any())).thenReturn(List.of(new CourseTechnical()));
        when(perguntaMongoRepository.findAllById(List.of("O1"))).thenReturn(List.of(new PerguntaMongo()));
        when(perguntaMongoRepository.findAllById(List.of("P1"))).thenReturn(List.of(new PerguntaMongo()));

        funcaoHCMService.createFuncaoHCM(dto);

        verify(counterService, times(1)).getNextSequence(anyString());
        verify(funcaoHCMRepository, times(1)).save(mockFuncao);
        assertEquals(101L, mockFuncao.getAutoIncrementId());
        assertNotNull(mockFuncao.getTrainings());
        assertNotNull(mockFuncao.getCourseSuperior());
        assertNotNull(mockFuncao.getObrigatory());
        assertNotNull(mockFuncao.getProactive());
    }

    @Test
    @DisplayName("Should maintain relationship lists untouched if passed IDs miss database matches entity")
    void ShouldMaintainRelationshipListsUntouchedIfPassedIdsMissDatabaseMatchesEntity() {
        FuncaoDTO dto = new FuncaoDTO();
        dto.setTrainingsId(List.of("INVALID"));

        when(counterService.getNextSequence("funcaoHCM_sequence")).thenReturn(102L);
        when(funcaoHCMMapper.toEntity(eq(dto), anyList())).thenReturn(mockFuncao);
        
        when(trainingRepository.findAllById(any())).thenReturn(Collections.emptyList());

        funcaoHCMService.createFuncaoHCM(dto);

        assertNull(mockFuncao.getTrainings()); 
        verify(funcaoHCMRepository, times(1)).save(mockFuncao);
    }

    @Test
    @DisplayName("Should process new multipart attachments cleanly when updating entity")
    void ShouldProcessNewMultipartAttachmentsCleanlyWhenUpdatingEntity() {
        FuncaoDTO updateDto = new FuncaoDTO();
        updateDto.setAnexo(List.of(new MockMultipartFile("file", new byte[0])));

        when(funcaoHCMRepository.findById("F123")).thenReturn(Optional.of(mockFuncao));
        
        List<AttachmentEntity> mocksAnexos = List.of(new AttachmentEntity());
        when(attachmentService.createAttachmentsFromMultipartFiles(anyList(), anyString())).thenReturn(mocksAnexos);
        
        FuncaoHCM mappedFuncao = new FuncaoHCM();
        when(funcaoHCMMapper.toEntity(updateDto, mocksAnexos)).thenReturn(mappedFuncao);

        funcaoHCMService.updateFuncaoHCM("F123", updateDto);

        verify(attachmentService, times(1)).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(funcaoHCMRepository, times(1)).save(mappedFuncao);
        assertEquals("F123", mappedFuncao.getId());
    }

    @Test
    @DisplayName("Should skip parsing new uploads keeping legacy attachments in absence of multfiles updates")
    void ShouldSkipParsingNewUploadsKeepingLegacyAttachmentsInAbsenceOfMultfilesUpdates() {
        FuncaoDTO updateDto = new FuncaoDTO(); // no anexo
        mockFuncao.setAnexo(List.of(new AttachmentEntity()));

        when(funcaoHCMRepository.findById("F123")).thenReturn(Optional.of(mockFuncao));
        
        FuncaoHCM mappedFuncao = new FuncaoHCM();
        when(funcaoHCMMapper.toEntity(updateDto, mockFuncao.getAnexo())).thenReturn(mappedFuncao);

        funcaoHCMService.updateFuncaoHCM("F123", updateDto);

        verify(attachmentService, never()).createAttachmentsFromMultipartFiles(anyList(), anyString());
        verify(funcaoHCMRepository, times(1)).save(mappedFuncao);
    }

    @Test
    @DisplayName("Should append snapshot to history chain correctly if last entity log equals aprovado")
    void ShouldAppendSnapshotToHistoryChainCorrectlyIfLastEntityLogEqualsAprovado() {
        FuncaoDTO updateDto = new FuncaoDTO(); 

        DataLog logAprovado = new DataLog();
        logAprovado.setJustification("Aprovado");
        logAprovado.setDate(LocalDateTime.now());
        
        FuncaoHCM mappedFuncao = new FuncaoHCM();
        mappedFuncao.setDataLog(List.of(logAprovado));

        when(funcaoHCMRepository.findById("F123")).thenReturn(Optional.of(mockFuncao));
        when(funcaoHCMMapper.toEntity(updateDto, null)).thenReturn(mappedFuncao);

        HistoryFuncao snapshot = new HistoryFuncao();
        when(funcaoHCMMapper.toHistorySnapshot(mappedFuncao)).thenReturn(snapshot);

        funcaoHCMService.updateFuncaoHCM("F123", updateDto);

        assertNotNull(mappedFuncao.getHistory());
        assertEquals(1, mappedFuncao.getHistory().size());
        verify(funcaoHCMRepository, times(1)).save(mappedFuncao);
    }

    @Test
    @DisplayName("Should ignore snapshot creation history completely avoiding overlaps if action deviates from aprovado")
    void ShouldIgnoreSnapshotCreationHistoryCompletelyAvoidingOverlapsIfActionDeviatesFromAprovado() {
        FuncaoDTO updateDto = new FuncaoDTO();

        DataLog logNegado = new DataLog();
        logNegado.setJustification("Reprovado");

        FuncaoHCM mappedFuncao = new FuncaoHCM();
        mappedFuncao.setDataLog(List.of(logNegado));
        
        mockFuncao.setHistory(new ArrayList<>()); 

        when(funcaoHCMRepository.findById("F123")).thenReturn(Optional.of(mockFuncao));
        when(funcaoHCMMapper.toEntity(updateDto, null)).thenReturn(mappedFuncao);

        funcaoHCMService.updateFuncaoHCM("F123", updateDto);

        assertTrue(mappedFuncao.getHistory().isEmpty());
    }

    @Test
    @DisplayName("Should instantiate new zero footprint function if updateTraining faces missing prior hcmId records")
    void ShouldInstantiateNewZeroFootprintFunctionIfUpdateTrainingFacesMissingPriorHcmIdRecords() {
        UpdateTraining updateTraining = new UpdateTraining(List.of("T1"), null, null, "NEW-HCM", null, null, null, null);

        when(funcaoHCMRepository.findByHcmId("NEW-HCM")).thenReturn(Optional.empty());
        when(trainingRepository.findAllById(anyList())).thenReturn(List.of(new TrainingEntity()));
        when(counterService.getNextSequence("funcaoHCM_sequence")).thenReturn(10L);

        FuncaoHCM novoMock = new FuncaoHCM();
        when(funcaoHCMMapper.updateEntity(updateTraining)).thenReturn(novoMock);

        funcaoHCMService.updateTraining(updateTraining, "UsuarioX");

        verify(funcaoHCMRepository, times(1)).save(novoMock);
        assertEquals(10L, novoMock.getAutoIncrementId());
        assertNotNull(novoMock.getDataLog());
        assertEquals("CRIADO", novoMock.getDataLog().get(0).getAction());
    }

    @Test
    @DisplayName("Should selectively swap fields replacing old records bypassing base creation if hcm matches existing item")
    void ShouldSelectivelySwapFieldsReplacingOldRecordsBypassingBaseCreationIfHcmMatchesExistingItem() {
        UpdateTraining updateTraining = new UpdateTraining(List.of("T1"), new ArrayList<>(), null, "HCM-999", null, null, null, null);

        when(funcaoHCMRepository.findByHcmId("HCM-999")).thenReturn(Optional.of(mockFuncao));
        when(trainingRepository.findAllById(anyList())).thenReturn(List.of(new TrainingEntity()));

        funcaoHCMService.updateTraining(updateTraining, "UsuarioX");

        verify(counterService, never()).getNextSequence(anyString());
        assertNotNull(mockFuncao.getTrainings());
        verify(funcaoHCMRepository, times(1)).save(mockFuncao);
    }
}

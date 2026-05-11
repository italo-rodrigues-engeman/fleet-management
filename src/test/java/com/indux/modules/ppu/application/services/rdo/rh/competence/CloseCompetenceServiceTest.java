package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.ppu.application.dtos.requests.ClosedCompetenceRequest;
import com.indux.modules.ppu.application.services.rdo.GetMissingRDOsUseCase;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CloseCompetenceServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private RDORepository repository;
    @Mock
    private PPURepository ppuRepository;
    @Mock
    private ClosedCompetenceRDORepository closedRepository;
    @Mock
    private UserService userService;
    @Spy
    private RDOLoggerUserMapper userMapper;
    @Mock
    private GetMissingRDOsUseCase missingUseCase;

    @InjectMocks
    private CloseCompetenceService service;

    @Test
    @DisplayName("Should use correct date range (STARTING previous month)")
    void testCloseCompetenceRangeCorrect() {
        YearMonth competence = YearMonth.of(2023, 9);
        Long projectId = 123L;
        String userId = "user1";
        ClosedCompetenceRequest request = new ClosedCompetenceRequest(competence, projectId, "Obs", true);

        PPUEntity ppu = new PPUEntity();
        ppu.setId("ppu1");

        when(ppuRepository.findByProjectId(projectId)).thenReturn(Optional.of(ppu));
        when(closedRepository.existsByCompetenceAndProject(any(), any())).thenReturn(false);
        when(missingUseCase.execute(any())).thenReturn(new GetMissingRDOsUseCase.ResponseMissingDTO(null, List.of()));

        org.springframework.data.mongodb.core.BulkOperations bulkOps = mock(
                org.springframework.data.mongodb.core.BulkOperations.class);
        when(mongoTemplate.bulkOps(any(), any(Class.class))).thenReturn(bulkOps);

        when(repository.findAllByDateAndPpuID(any(), any(), any())).thenReturn(Collections.emptyList());

        when(userService.getUserById(userId)).thenReturn(Optional.of(mock(SimpleUser.class)));
        service.closeCompetence(request, userId);


        LocalDate expectedStart = LocalDate.of(2023, 8, 16);
        LocalDate expectedEnd = LocalDate.of(2023, 9, 15);

        verify(repository).findAllByDateAndPpuID(eq(expectedStart), eq(expectedEnd), eq("ppu1"));
    }
}

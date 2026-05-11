package com.indux.core.application.service.cbo;

import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import com.indux.core.domain.repository.cbo.CourseSuperiorRepository;
import com.indux.core.domain.repository.cbo.CourseTechnicalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseSuperiorRepository courseSuperiorRepository;

    @Mock
    private CourseTechnicalRepository courseTechnicalRepository;

    @InjectMocks
    private CourseService courseService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Should bypass conditional matching querying entirely all bounds if search string resolves to null for superior course")
    void ShouldBypassConditionalMatchingQueryingEntirelyAllBoundsIfSearchStringResolvesToNullForSuperiorCourse() {
        CourseSuperior mockCourse = new CourseSuperior();
        when(courseSuperiorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(mockCourse)));

        Page<CourseSuperior> result = courseService.findAllSuperiorSearch(null, pageable);

        assertFalse(result.isEmpty());
        verify(courseSuperiorRepository, times(1)).findAll(pageable);
        verify(courseSuperiorRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should limit and pattern match bounds invoking containing bounds query if string exists for superior course")
    void ShouldLimitAndPatternMatchBoundsInvokingContainingBoundsQueryIfStringExistsForSuperiorCourse() {
        CourseSuperior mockCourse = new CourseSuperior();
        when(courseSuperiorRepository.findByNameContainingIgnoreCase("Admin", pageable))
                .thenReturn(new PageImpl<>(List.of(mockCourse)));

        Page<CourseSuperior> result = courseService.findAllSuperiorSearch("Admin", pageable);

        assertFalse(result.isEmpty());
        verify(courseSuperiorRepository, never()).findAll(pageable);
        verify(courseSuperiorRepository, times(1)).findByNameContainingIgnoreCase("Admin", pageable);
    }

    @Test
    @DisplayName("Should bypass conditional matching querying entirely all bounds if search string resolves to null for technical course")
    void ShouldBypassConditionalMatchingQueryingEntirelyAllBoundsIfSearchStringResolvesToNullForTechnicalCourse() {
        CourseTechnical mockTechnical = new CourseTechnical();
        when(courseTechnicalRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(mockTechnical)));

        Page<CourseTechnical> result = courseService.findAllTechnicalSearch(null, pageable);

        assertFalse(result.isEmpty());
        verify(courseTechnicalRepository, times(1)).findAll(pageable);
        verify(courseTechnicalRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should limit and pattern match bounds invoking containing bounds query if string exists for technical course")
    void ShouldLimitAndPatternMatchBoundsInvokingContainingBoundsQueryIfStringExistsForTechnicalCourse() {
        CourseTechnical mockTechnical = new CourseTechnical();
        when(courseTechnicalRepository.findByNameContainingIgnoreCase("Tech", pageable))
                .thenReturn(new PageImpl<>(List.of(mockTechnical)));

        Page<CourseTechnical> result = courseService.findAllTechnicalSearch("Tech", pageable);

        assertFalse(result.isEmpty());
        verify(courseTechnicalRepository, never()).findAll(pageable);
        verify(courseTechnicalRepository, times(1)).findByNameContainingIgnoreCase("Tech", pageable);
    }
}

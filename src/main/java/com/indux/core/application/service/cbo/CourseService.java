package com.indux.core.application.service.cbo;


import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import com.indux.core.domain.repository.cbo.CourseSuperiorRepository;
import com.indux.core.domain.repository.cbo.CourseTechnicalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    private final CourseSuperiorRepository courseSuperiorRepository;
    private final CourseTechnicalRepository courseTechnicalRepository;

    public CourseService(CourseSuperiorRepository courseSuperiorRepository, CourseTechnicalRepository courseTechnicalRepository) {
        this.courseSuperiorRepository = courseSuperiorRepository;
        this.courseTechnicalRepository = courseTechnicalRepository;
    }

    public Page<CourseSuperior> findAllSuperiorSearch(String search, Pageable pageable) {
        if (search == null) {
            return courseSuperiorRepository.findAll(pageable);
        }else  {
            return courseSuperiorRepository.findByNameContainingIgnoreCase(search, pageable);
        }
    }

    public Page<CourseTechnical> findAllTechnicalSearch(String search, Pageable pageable) {
        if (search == null) {
            return courseTechnicalRepository.findAll(pageable);
        }else  {
            return courseTechnicalRepository.findByNameContainingIgnoreCase(search, pageable);
        }
    }
}

package com.indux.core.presentation;

import com.indux.core.application.service.cbo.CourseService;
import com.indux.core.domain.model.cbo.CourseSuperior;
import com.indux.core.domain.model.cbo.CourseTechnical;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/superior")
    public Page<CourseSuperior> getAllSuperior(
            String search,
            Pageable pageable
    ){
        return courseService.findAllSuperiorSearch(search, pageable);
    }

    @GetMapping("/technical")
    public Page<CourseTechnical> getAllTechnical(
            String search,
            Pageable pageable
    ){
        return courseService.findAllTechnicalSearch(search, pageable);
    }
}

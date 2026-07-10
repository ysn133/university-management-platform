package com.platform.universitygovernance.university.presentation;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platform.universitygovernance.university.application.UniversityService;
import com.platform.universitygovernance.university.presentation.dto.UniversityResponse;

@RestController
@RequestMapping("/api/v1/university")
public class UniversityController {

    private final UniversityService universityService;

    public UniversityController(UniversityService universityService) {
        this.universityService = universityService;
    }

    @GetMapping
    public UniversityResponse getUniversity() {
        return universityService.getUniversity();
    }

    @GetMapping("/{id}")
    public UniversityResponse getUniversityById(@PathVariable("id") UUID universityId) {
        return universityService.getUniversityById(universityId);
    }
}

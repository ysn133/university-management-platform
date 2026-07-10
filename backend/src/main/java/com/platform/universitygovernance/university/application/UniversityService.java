package com.platform.universitygovernance.university.application;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import com.platform.universitygovernance.university.presentation.dto.UniversityResponse;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;

    public UniversityService(
        UniversityRepository universityRepository
    ){

        this.universityRepository = universityRepository;

    }
    
    
    @Transactional(readOnly = true)
    public UniversityResponse getUniversity() {
        University university = universityRepository.findFirstByOrderByCreatedAtAsc()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found"));

        return new UniversityResponse(
            university.getId(),
            university.getName(),
            university.getCreatedAt(),
            university.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public UniversityResponse getUniversityById(UUID universityId) {
        University university = universityRepository.findById(universityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found"));

        return new UniversityResponse(
            university.getId(),
            university.getName(),
            university.getCreatedAt(),
            university.getUpdatedAt()
        );
    }
}

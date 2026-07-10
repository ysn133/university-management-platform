package com.platform.universitygovernance.university.application;

import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class BootstrapUniversityRunner implements ApplicationRunner {

    private final UniversityRepository universityRepository;

    @Value("${BOOTSTRAP_UNIVERSITY_NAME:Universite Ibn Zohr}")
    private String bootstrapUniversityName;

    public BootstrapUniversityRunner(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (universityRepository.count() > 0) {
            return;
        }

        if (bootstrapUniversityName == null || bootstrapUniversityName.isBlank()) {
            return;
        }

        University university = new University();
        university.setName(bootstrapUniversityName.trim());
        universityRepository.save(university);
    }
}

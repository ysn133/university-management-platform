package com.platform.universitygovernance.university.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record UniversityResponse(

   UUID universityId,
    String universityName,
    Instant createdAt,
    Instant updatedAt

){}

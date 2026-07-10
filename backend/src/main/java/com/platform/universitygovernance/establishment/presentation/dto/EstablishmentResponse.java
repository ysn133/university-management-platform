package com.platform.universitygovernance.establishment.presentation.dto;

import java.time.Instant;
import java.util.UUID;

import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;

public record EstablishmentResponse(
    UUID id ,
    UUID universityId,
    String name,
    EstablishmentType type,
    EstablishmentStatus status,
    Instant createdAt,
    Instant updatedAt


){}


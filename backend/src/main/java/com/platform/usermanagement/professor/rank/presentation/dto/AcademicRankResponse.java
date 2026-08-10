package com.platform.usermanagement.professor.rank.presentation.dto;

import com.platform.usermanagement.professor.rank.domain.AcademicRankStatus;
import java.util.UUID;

public record AcademicRankResponse(
    UUID id,
    UUID establishmentId,
    String code,
    String name,
    Integer seniorityOrder,
    boolean canHoldModuleResponsibility,
    AcademicRankStatus status
) {}

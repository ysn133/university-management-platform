package com.platform.scheduling.teachinggroup.presentation.dto;

import java.util.UUID;

public record TeachingGroupMemberResponse(
    UUID semesterRegistrationId,
    UUID studentId,
    String apogeeCode,
    String firstName,
    String lastName
) {
}

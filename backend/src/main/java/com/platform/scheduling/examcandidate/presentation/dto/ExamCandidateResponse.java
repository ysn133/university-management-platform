package com.platform.scheduling.examcandidate.presentation.dto;

import com.platform.scheduling.examschedule.domain.ExamSessionType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ExamCandidateResponse(
    UUID id,
    UUID moduleExamId,
    UUID moduleRegistrationId,
    UUID studentId,
    String apogeeCode,
    String nationalStudentCode,
    String cin,
    String lastName,
    String firstName,
    UUID subjectModuleId,
    UUID examGroupId,
    String examGroupLabel,
    ExamSessionType sessionType,
    LocalDate examDate,
    LocalTime startTime,
    String roomCode,
    Instant createdAt
) {
}

package com.platform.universitygovernance.semester.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.time.LocalDate;

public record CreateSemesterRequest(
    @NotBlank @Size(max = 100) String name,
    @Positive @Max(32767) int semesterOrder,
    @NotNull SemesterTermType termType,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {
    public CreateSemesterRequest(String name, int semesterOrder, SemesterTermType termType) {
        this(name, semesterOrder, termType, LocalDate.of(2000, 1, 1), LocalDate.of(2000, 6, 30));
    }
}

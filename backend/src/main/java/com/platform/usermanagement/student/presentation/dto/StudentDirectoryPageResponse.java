package com.platform.usermanagement.student.presentation.dto;

import java.util.List;

public record StudentDirectoryPageResponse(
    List<StudentProfileResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {
}

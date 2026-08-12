package com.platform.scheduling.moduleexam.application;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.examgroup.infrastructure.ExamRoomAllocationRepository;
import com.platform.scheduling.moduleexam.domain.ModuleExam;
import com.platform.scheduling.moduleexam.infrastructure.ModuleExamRepository;
import com.platform.scheduling.moduleexam.presentation.dto.ProfessorExamResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfessorExamScheduleService {

    private final ModuleExamRepository moduleExamRepository;
    private final ExamRoomAllocationRepository roomAllocationRepository;

    public ProfessorExamScheduleService(
        ModuleExamRepository moduleExamRepository,
        ExamRoomAllocationRepository roomAllocationRepository
    ) {
        this.moduleExamRepository = moduleExamRepository;
        this.roomAllocationRepository = roomAllocationRepository;
    }

    @Transactional(readOnly = true)
    public List<ProfessorExamResponse> getMyExams(AuthenticatedUserPrincipal principal) {
        if (principal == null || principal.role() != AccountRoleType.PROFESSOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Professor access required");
        }

        return moduleExamRepository.findPublishedResponsibleExams(principal.roleEntityId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ProfessorExamResponse toResponse(ModuleExam exam) {
        var schedule = exam.getExamSchedule();
        var module = exam.getSubjectModule();
        var level = schedule.getSemester().getAcademicLevel();
        var program = level.getProgramFiliere();
        List<String> rooms = roomAllocationRepository
            .findByModuleExamIdOrderByExamGroupGroupOrderAsc(exam.getId())
            .stream()
            .map(allocation -> allocation.getRoom().getCode())
            .toList();
        if (rooms.isEmpty() && exam.getRoom() != null) {
            rooms = List.of(exam.getRoom().getCode());
        }

        return new ProfessorExamResponse(
            exam.getId(),
            module.getId(),
            module.getCode(),
            module.getTitle(),
            exam.getClassGroup().getId(),
            exam.getClassGroup().getName(),
            schedule.getAcademicYear().getId(),
            schedule.getAcademicYear().getLabel(),
            schedule.getAcademicYear().getStatus(),
            schedule.getSemester().getId(),
            schedule.getSemester().getName(),
            schedule.getSemester().getStartDate(),
            schedule.getSemester().getEndDate(),
            level.getId(),
            level.getName(),
            program.getCode(),
            program.getName(),
            schedule.getSessionType(),
            exam.getExamDate(),
            exam.getStartTime(),
            exam.getEndTime(),
            rooms
        );
    }
}

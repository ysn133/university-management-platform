package com.platform.scheduling.semesterschedule.infrastructure;

import com.platform.scheduling.semesterschedule.domain.ScheduleEntry;
import com.platform.scheduling.semesterschedule.domain.SchedulePublicationStatus;
import com.platform.universitygovernance.semester.domain.SemesterTermType;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {

    List<ScheduleEntry> findBySemesterScheduleId(UUID semesterScheduleId);

    List<ScheduleEntry> findByRoomIdAndDayOfWeek(UUID roomId, DayOfWeek dayOfWeek);

    long countByTeachingAssignmentId(UUID teachingAssignmentId);

    long countByTeachingAssignmentIdAndIdNot(
        UUID teachingAssignmentId,
        UUID scheduleEntryId
    );

    @Query("""
        select entry
        from ScheduleEntry entry
        where entry.teachingAssignment.professor.id = :professorId
          and entry.teachingAssignment.status = com.platform.teachingassignment.domain.TeachingAssignmentStatus.ACTIVE
          and entry.semesterSchedule.publicationStatus = :publicationStatus
        order by entry.dayOfWeek, entry.startTime
        """)
    List<ScheduleEntry> findProfessorSchedule(
        @Param("professorId") UUID professorId,
        @Param("publicationStatus") SchedulePublicationStatus publicationStatus
    );

    @Query("""
        select distinct entry
        from ScheduleEntry entry, ModuleRegistration registration
        where registration.semesterRegistration.academicRegistration.student.id = :studentId
          and registration.status = com.platform.academicregistration.moduleregistration.domain.ModuleRegistrationStatus.ACTIVE
          and registration.subjectModule = entry.teachingAssignment.teachingRequirement.moduleTeachingComponent.subjectModule
          and registration.semesterRegistration.semester = entry.semesterSchedule.semester
          and entry.teachingAssignment.status = com.platform.teachingassignment.domain.TeachingAssignmentStatus.ACTIVE
          and entry.semesterSchedule.publicationStatus = :publicationStatus
          and (
            entry.teachingAssignment.teachingRequirement.teachingGroup.audienceType = com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode.WHOLE_COHORT
            or (
              entry.teachingAssignment.teachingRequirement.teachingGroup.audienceType = com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode.CLASS_GROUP
              and exists (
                select assignment.id from StudentClassAssignment assignment
                where assignment.semesterRegistration = registration.semesterRegistration
                  and assignment.classGroup = entry.teachingAssignment.teachingRequirement.teachingGroup.sourceClassGroup
              )
            )
            or (
              entry.teachingAssignment.teachingRequirement.teachingGroup.audienceType = com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingAudienceMode.SUBGROUP
              and exists (
                select membership.id from TeachingGroupMembership membership
                where membership.semesterRegistration = registration.semesterRegistration
                  and membership.teachingGroup = entry.teachingAssignment.teachingRequirement.teachingGroup
              )
            )
          )
        order by entry.dayOfWeek, entry.startTime
        """)
    List<ScheduleEntry> findStudentSchedule(
        @Param("studentId") UUID studentId,
        @Param("publicationStatus") SchedulePublicationStatus publicationStatus
    );

    @Query("""
        select entry
        from ScheduleEntry entry
        where entry.semesterSchedule.establishment.id = :establishmentId
          and entry.semesterSchedule.academicYear.id = :academicYearId
          and entry.semesterSchedule.semester.termType = :termType
          and entry.dayOfWeek = :dayOfWeek
        """)
    List<ScheduleEntry> findPotentialConflicts(
        @Param("establishmentId") UUID establishmentId,
        @Param("academicYearId") UUID academicYearId,
        @Param("termType") SemesterTermType termType,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}

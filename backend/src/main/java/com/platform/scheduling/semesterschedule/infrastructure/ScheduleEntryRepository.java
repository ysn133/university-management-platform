package com.platform.scheduling.semesterschedule.infrastructure;

import com.platform.scheduling.semesterschedule.domain.ScheduleEntry;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {

    List<ScheduleEntry> findBySemesterScheduleId(UUID semesterScheduleId);

    @Query("""
        select entry
        from ScheduleEntry entry
        where entry.semesterSchedule.establishment.id = :establishmentId
          and entry.semesterSchedule.academicYear.id = :academicYearId
          and entry.semesterSchedule.semester.semesterOrder = :semesterOrder
          and entry.dayOfWeek = :dayOfWeek
        """)
    List<ScheduleEntry> findPotentialConflicts(
        @Param("establishmentId") UUID establishmentId,
        @Param("academicYearId") UUID academicYearId,
        @Param("semesterOrder") int semesterOrder,
        @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}

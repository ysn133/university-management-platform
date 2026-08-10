package com.platform.teachingassignment.rankpreference.infrastructure;

import com.platform.teachingassignment.rankpreference.domain.TeachingAssignmentRankPreference;
import com.platform.universitygovernance.moduleteachingcomponent.domain.TeachingComponentType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingAssignmentRankPreferenceRepository extends JpaRepository<TeachingAssignmentRankPreference, UUID> {
    List<TeachingAssignmentRankPreference> findByEstablishmentIdOrderByComponentTypeAscPriorityAsc(UUID establishmentId);
    List<TeachingAssignmentRankPreference> findByEstablishmentIdAndComponentTypeOrderByPriorityAsc(UUID establishmentId, TeachingComponentType componentType);
    void deleteByEstablishmentIdAndComponentType(UUID establishmentId, TeachingComponentType componentType);
    boolean existsByAcademicRankId(UUID academicRankId);
}

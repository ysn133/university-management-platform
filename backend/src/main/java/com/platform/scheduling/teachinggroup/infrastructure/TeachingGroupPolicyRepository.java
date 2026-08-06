package com.platform.scheduling.teachinggroup.infrastructure;

import com.platform.scheduling.teachinggroup.domain.TeachingGroupPolicy;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeachingGroupPolicyRepository
    extends JpaRepository<TeachingGroupPolicy, UUID> {

    List<TeachingGroupPolicy> findByAcademicLevelIdAndAcademicYearIdOrderByGroupTypeAsc(
        UUID academicLevelId,
        UUID academicYearId
    );
}

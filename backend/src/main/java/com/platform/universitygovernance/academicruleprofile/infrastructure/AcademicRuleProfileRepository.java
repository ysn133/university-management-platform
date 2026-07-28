package com.platform.universitygovernance.academicruleprofile.infrastructure;

import com.platform.universitygovernance.academicruleprofile.domain.AcademicRuleProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicRuleProfileRepository extends JpaRepository<AcademicRuleProfile, UUID> {

    @Query("""
        select profile
        from AcademicRuleProfile profile
        where profile.establishment.id = :establishmentId
        order by lower(profile.name), profile.version desc
        """)
    List<AcademicRuleProfile> findByEstablishmentOrderByNameAndVersion(
        @Param("establishmentId") UUID establishmentId
    );

    Optional<AcademicRuleProfile> findTopByEstablishmentIdAndNameIgnoreCaseOrderByVersionDesc(
        UUID establishmentId,
        String name
    );

    boolean existsByEstablishmentIdAndNameIgnoreCaseAndVersionAndIdNot(
        UUID establishmentId,
        String name,
        int version,
        UUID academicRuleProfileId
    );
}

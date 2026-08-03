package com.platform.universitygovernance.establishment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;

import java.util.List;
import java.util.UUID;


public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {
    @Query("""
        select establishment
        from Establishment establishment
        where establishment.university.id = :universityId
          and (:query = '' or lower(establishment.name) like lower(concat('%', :query, '%')))
          and (:type is null or establishment.establishmentType = :type)
          and (:status is null or establishment.establishmentStatus = :status)
        order by establishment.name asc
        """)
    List<Establishment> searchByUniversity(
        @Param("universityId") UUID universityId,
        @Param("query") String query,
        @Param("type") EstablishmentType type,
        @Param("status") EstablishmentStatus status
    );
}

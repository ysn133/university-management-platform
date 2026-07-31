package com.platform.universitygovernance.subjectmodules.infrastructure;

import com.platform.universitygovernance.subjectmodules.domain.SubjectModuleDomain;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectModuleDomainRepository
    extends JpaRepository<SubjectModuleDomain, UUID> {

    List<SubjectModuleDomain> findBySubjectModuleId(UUID subjectModuleId);

    List<SubjectModuleDomain> findBySubjectModuleIdIn(Collection<UUID> subjectModuleIds);

    boolean existsByAcademicDomainId(UUID academicDomainId);
}

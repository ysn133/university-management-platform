package com.platform.universitygovernance.classgroup.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.universitygovernance.classgroup.application.ClassGroupService;
import com.platform.universitygovernance.classgroup.application.ClassGroupGenerationService;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupGenerationResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupRebalanceResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.ClassGroupResponse;
import com.platform.universitygovernance.classgroup.presentation.dto.CreateClassGroupRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.GenerateClassGroupsRequest;
import com.platform.universitygovernance.classgroup.presentation.dto.UpdateClassGroupRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class ClassGroupController {

    private final ClassGroupService classGroupService;
    private final ClassGroupGenerationService classGroupGenerationService;

    public ClassGroupController(
        ClassGroupService classGroupService,
        ClassGroupGenerationService classGroupGenerationService
    ) {
        this.classGroupService = classGroupService;
        this.classGroupGenerationService = classGroupGenerationService;
    }

    @PostMapping("/academic-levels/{academicLevelId}/class-groups")
    public ClassGroupResponse createClassGroup(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody CreateClassGroupRequest request
    ) {
        return classGroupService.createClassGroup(principal, academicLevelId, academicYearId, request);
    }

    @GetMapping("/academic-levels/{academicLevelId}/class-groups")
    public List<ClassGroupResponse> getClassGroups(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId
    ) {
        return classGroupService.getClassGroups(principal, academicLevelId, academicYearId);
    }

    @PostMapping("/academic-levels/{academicLevelId}/class-groups/generate")
    public ClassGroupGenerationResponse generateClassGroups(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody GenerateClassGroupsRequest request
    ) {
        return classGroupGenerationService.generateClassGroups(
            principal,
            academicLevelId,
            academicYearId,
            request
        );
    }

    @PutMapping("/academic-levels/{academicLevelId}/class-groups/rebalance")
    public ClassGroupRebalanceResponse rebalanceClassGroups(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID academicLevelId,
        @RequestParam UUID academicYearId,
        @Valid @RequestBody GenerateClassGroupsRequest request
    ) {
        return classGroupGenerationService.rebalanceClassGroups(
            principal,
            academicLevelId,
            academicYearId,
            request
        );
    }

    @GetMapping("/class-groups/{classGroupId}")
    public ClassGroupResponse getClassGroup(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID classGroupId
    ) {
        return classGroupService.getClassGroup(principal, classGroupId);
    }

    @PutMapping("/class-groups/{classGroupId}")
    public ClassGroupResponse updateClassGroup(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID classGroupId,
        @Valid @RequestBody UpdateClassGroupRequest request
    ) {
        return classGroupService.updateClassGroup(principal, classGroupId, request);
    }
}

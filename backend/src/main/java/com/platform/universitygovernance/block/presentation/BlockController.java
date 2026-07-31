package com.platform.universitygovernance.block.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.block.application.BlockService;
import com.platform.universitygovernance.block.presentation.dto.BlockResponse;
import com.platform.universitygovernance.block.presentation.dto.CreateBlockRequest;
import com.platform.universitygovernance.block.presentation.dto.UpdateBlockRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ROOT_SUPER_ADMIN', 'SUPER_ADMIN', 'ADMIN')")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/establishments/{establishmentId}/blocks")
    public BlockResponse createBlock(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateBlockRequest request
    ) {
        return blockService.createBlock(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/blocks")
    public List<BlockResponse> getBlocks(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return blockService.getBlocks(principal, establishmentId);
    }

    @GetMapping("/blocks/{blockId}")
    public BlockResponse getBlock(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID blockId
    ) {
        return blockService.getBlock(principal, blockId);
    }

    @PutMapping("/blocks/{blockId}")
    public BlockResponse updateBlock(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID blockId,
        @Valid @RequestBody UpdateBlockRequest request
    ) {
        return blockService.updateBlock(principal, blockId, request);
    }

    @DeleteMapping("/blocks/{blockId}")
    public ActionResponse deactivateBlock(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID blockId
    ) {
        return blockService.deactivateBlock(principal, blockId);
    }
}

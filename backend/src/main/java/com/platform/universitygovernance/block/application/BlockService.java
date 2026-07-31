package com.platform.universitygovernance.block.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.block.domain.Block;
import com.platform.universitygovernance.block.domain.BlockStatus;
import com.platform.universitygovernance.block.infrastructure.BlockRepository;
import com.platform.universitygovernance.block.presentation.dto.BlockResponse;
import com.platform.universitygovernance.block.presentation.dto.CreateBlockRequest;
import com.platform.universitygovernance.block.presentation.dto.UpdateBlockRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public BlockService(
        BlockRepository blockRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.blockRepository = blockRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public BlockResponse createBlock(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateBlockRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.BLOCK_CREATE);
        Establishment establishment = findEstablishment(establishmentId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, null);

        Block block = new Block();
        block.setEstablishment(establishment);
        block.setCode(code);
        block.setName(request.name().trim());
        block.setStatus(BlockStatus.ACTIVE);
        return toResponse(blockRepository.save(block));
    }

    @Transactional(readOnly = true)
    public List<BlockResponse> getBlocks(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.BLOCK_VIEW);
        findEstablishment(establishmentId);
        return blockRepository.findByEstablishmentIdOrderByCodeAsc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BlockResponse getBlock(
        AuthenticatedUserPrincipal principal,
        UUID blockId
    ) {
        Block block = findBlock(blockId);
        requirePermission(
            principal,
            block.getEstablishment().getId(),
            PermissionCode.BLOCK_VIEW
        );
        return toResponse(block);
    }

    @Transactional
    public BlockResponse updateBlock(
        AuthenticatedUserPrincipal principal,
        UUID blockId,
        UpdateBlockRequest request
    ) {
        Block block = findBlock(blockId);
        UUID establishmentId = block.getEstablishment().getId();
        requirePermission(principal, establishmentId, PermissionCode.BLOCK_UPDATE);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, blockId);
        block.setCode(code);
        block.setName(request.name().trim());
        block.setStatus(request.status());
        return toResponse(blockRepository.save(block));
    }

    @Transactional
    public ActionResponse deactivateBlock(
        AuthenticatedUserPrincipal principal,
        UUID blockId
    ) {
        Block block = findBlock(blockId);
        requirePermission(
            principal,
            block.getEstablishment().getId(),
            PermissionCode.BLOCK_DELETE
        );
        block.setStatus(BlockStatus.INACTIVE);
        blockRepository.save(block);
        return new ActionResponse(true, "Block deactivated");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private Block findBlock(UUID blockId) {
        return blockRepository.findById(blockId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Block not found"
            ));
    }

    private void ensureCodeAvailable(UUID establishmentId, String code, UUID blockId) {
        boolean exists = blockId == null
            ? blockRepository.existsByEstablishmentIdAndCodeIgnoreCase(establishmentId, code)
            : blockRepository.existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
                establishmentId,
                code,
                blockId
            );
        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A block with this code already exists in the establishment"
            );
        }
    }

    private void requirePermission(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        PermissionCode permissionCode
    ) {
        permissionAuthorizationService.requirePermission(
            principal,
            establishmentId,
            permissionCode
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private BlockResponse toResponse(Block block) {
        return new BlockResponse(
            block.getId(),
            block.getEstablishment().getId(),
            block.getCode(),
            block.getName(),
            block.getStatus(),
            block.getCreatedAt(),
            block.getUpdatedAt()
        );
    }
}

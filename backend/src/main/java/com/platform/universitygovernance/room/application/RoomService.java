package com.platform.universitygovernance.room.application;

import com.platform.identityaccess.application.AdminPermissionAuthorizationService;
import com.platform.identityaccess.domain.PermissionCode;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.block.domain.Block;
import com.platform.universitygovernance.block.infrastructure.BlockRepository;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.room.domain.Room;
import com.platform.universitygovernance.room.domain.RoomStatus;
import com.platform.universitygovernance.room.infrastructure.RoomRepository;
import com.platform.universitygovernance.room.presentation.dto.CreateRoomRequest;
import com.platform.universitygovernance.room.presentation.dto.RoomResponse;
import com.platform.universitygovernance.room.presentation.dto.UpdateRoomRequest;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BlockRepository blockRepository;
    private final EstablishmentRepository establishmentRepository;
    private final AdminPermissionAuthorizationService permissionAuthorizationService;

    public RoomService(
        RoomRepository roomRepository,
        BlockRepository blockRepository,
        EstablishmentRepository establishmentRepository,
        AdminPermissionAuthorizationService permissionAuthorizationService
    ) {
        this.roomRepository = roomRepository;
        this.blockRepository = blockRepository;
        this.establishmentRepository = establishmentRepository;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    @Transactional
    public RoomResponse createRoom(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId,
        CreateRoomRequest request
    ) {
        requirePermission(principal, establishmentId, PermissionCode.ROOM_CREATE);
        Establishment establishment = findEstablishment(establishmentId);
        Block block = findOptionalBlock(request.blockId(), establishmentId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, null);

        Room room = new Room();
        room.setEstablishment(establishment);
        room.setBlock(block);
        room.setCode(code);
        room.setName(request.name().trim());
        room.setRoomType(request.roomType());
        room.setCapacity(request.capacity());
        room.setStatus(RoomStatus.ACTIVE);
        return toResponse(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRooms(
        AuthenticatedUserPrincipal principal,
        UUID establishmentId
    ) {
        requirePermission(principal, establishmentId, PermissionCode.ROOM_VIEW);
        findEstablishment(establishmentId);
        return roomRepository.findByEstablishmentIdOrderByCodeAsc(establishmentId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(
        AuthenticatedUserPrincipal principal,
        UUID roomId
    ) {
        Room room = findRoom(roomId);
        requirePermission(
            principal,
            room.getEstablishment().getId(),
            PermissionCode.ROOM_VIEW
        );
        return toResponse(room);
    }

    @Transactional
    public RoomResponse updateRoom(
        AuthenticatedUserPrincipal principal,
        UUID roomId,
        UpdateRoomRequest request
    ) {
        Room room = findRoom(roomId);
        UUID establishmentId = room.getEstablishment().getId();
        requirePermission(principal, establishmentId, PermissionCode.ROOM_UPDATE);
        Block block = findOptionalBlock(request.blockId(), establishmentId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(establishmentId, code, roomId);
        room.setBlock(block);
        room.setCode(code);
        room.setName(request.name().trim());
        room.setRoomType(request.roomType());
        room.setCapacity(request.capacity());
        room.setStatus(request.status());
        return toResponse(roomRepository.save(room));
    }

    @Transactional
    public ActionResponse deactivateRoom(
        AuthenticatedUserPrincipal principal,
        UUID roomId
    ) {
        Room room = findRoom(roomId);
        requirePermission(
            principal,
            room.getEstablishment().getId(),
            PermissionCode.ROOM_DELETE
        );
        room.setStatus(RoomStatus.INACTIVE);
        roomRepository.save(room);
        return new ActionResponse(true, "Room deactivated");
    }

    private Establishment findEstablishment(UUID establishmentId) {
        return establishmentRepository.findById(establishmentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Establishment not found"
            ));
    }

    private Room findRoom(UUID roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Room not found"
            ));
    }

    private Block findOptionalBlock(UUID blockId, UUID establishmentId) {
        if (blockId == null) {
            return null;
        }
        Block block = blockRepository.findById(blockId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Block not found"
            ));
        if (!establishmentId.equals(block.getEstablishment().getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Block and room must belong to the same establishment"
            );
        }
        return block;
    }

    private void ensureCodeAvailable(UUID establishmentId, String code, UUID roomId) {
        boolean exists = roomId == null
            ? roomRepository.existsByEstablishmentIdAndCodeIgnoreCase(establishmentId, code)
            : roomRepository.existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
                establishmentId,
                code,
                roomId
            );
        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A room with this code already exists in the establishment"
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

    private RoomResponse toResponse(Room room) {
        Block block = room.getBlock();
        return new RoomResponse(
            room.getId(),
            room.getEstablishment().getId(),
            block == null ? null : block.getId(),
            block == null ? null : block.getCode(),
            room.getCode(),
            room.getName(),
            room.getRoomType(),
            room.getCapacity(),
            room.getStatus(),
            room.getCreatedAt(),
            room.getUpdatedAt()
        );
    }
}

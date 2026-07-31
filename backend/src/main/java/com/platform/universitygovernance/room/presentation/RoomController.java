package com.platform.universitygovernance.room.presentation;

import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.shared.presentation.ActionResponse;
import com.platform.universitygovernance.room.application.RoomService;
import com.platform.universitygovernance.room.presentation.dto.CreateRoomRequest;
import com.platform.universitygovernance.room.presentation.dto.RoomResponse;
import com.platform.universitygovernance.room.presentation.dto.UpdateRoomRequest;
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
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/establishments/{establishmentId}/rooms")
    public RoomResponse createRoom(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId,
        @Valid @RequestBody CreateRoomRequest request
    ) {
        return roomService.createRoom(principal, establishmentId, request);
    }

    @GetMapping("/establishments/{establishmentId}/rooms")
    public List<RoomResponse> getRooms(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID establishmentId
    ) {
        return roomService.getRooms(principal, establishmentId);
    }

    @GetMapping("/rooms/{roomId}")
    public RoomResponse getRoom(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID roomId
    ) {
        return roomService.getRoom(principal, roomId);
    }

    @PutMapping("/rooms/{roomId}")
    public RoomResponse updateRoom(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID roomId,
        @Valid @RequestBody UpdateRoomRequest request
    ) {
        return roomService.updateRoom(principal, roomId, request);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ActionResponse deactivateRoom(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable UUID roomId
    ) {
        return roomService.deactivateRoom(principal, roomId);
    }
}

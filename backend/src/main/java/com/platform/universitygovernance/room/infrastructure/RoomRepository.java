package com.platform.universitygovernance.room.infrastructure;

import com.platform.universitygovernance.room.domain.Room;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByEstablishmentIdOrderByCodeAsc(UUID establishmentId);

    boolean existsByEstablishmentIdAndCodeIgnoreCase(UUID establishmentId, String code);

    boolean existsByEstablishmentIdAndCodeIgnoreCaseAndIdNot(
        UUID establishmentId,
        String code,
        UUID roomId
    );
}

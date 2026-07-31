package com.platform.universitygovernance.room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platform.identityaccess.domain.AccountRoleType;
import com.platform.platform.PlatformApplication;
import com.platform.platform.infrastructure.security.AuthenticatedUserPrincipal;
import com.platform.scheduling.domain.RoomType;
import com.platform.universitygovernance.block.application.BlockService;
import com.platform.universitygovernance.block.domain.BlockStatus;
import com.platform.universitygovernance.block.infrastructure.BlockRepository;
import com.platform.universitygovernance.block.presentation.dto.CreateBlockRequest;
import com.platform.universitygovernance.establishment.domain.Establishment;
import com.platform.universitygovernance.establishment.domain.EstablishmentStatus;
import com.platform.universitygovernance.establishment.domain.EstablishmentType;
import com.platform.universitygovernance.establishment.infrastructure.EstablishmentRepository;
import com.platform.universitygovernance.room.application.RoomService;
import com.platform.universitygovernance.room.domain.RoomStatus;
import com.platform.universitygovernance.room.infrastructure.RoomRepository;
import com.platform.universitygovernance.room.presentation.dto.CreateRoomRequest;
import com.platform.universitygovernance.room.presentation.dto.UpdateRoomRequest;
import com.platform.universitygovernance.university.domain.University;
import com.platform.universitygovernance.university.infrastructure.UniversityRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(classes = PlatformApplication.class)
@ActiveProfiles("test")
class FacilityManagementIntegrationTest {

    @Autowired
    private BlockService blockService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    private AuthenticatedUserPrincipal root;
    private Establishment firstEstablishment;
    private Establishment secondEstablishment;

    @BeforeEach
    void setUp() {
        clearBusinessData();
        University university = new University();
        university.setName("Universite Ibn Zohr");
        university = universityRepository.save(university);
        firstEstablishment = saveEstablishment(university, "ENSA Agadir");
        secondEstablishment = saveEstablishment(university, "Faculty of Sciences");
        root = new AuthenticatedUserPrincipal(
            UUID.randomUUID(),
            AccountRoleType.ROOT_SUPER_ADMIN,
            UUID.randomUUID(),
            null,
            "root@uiz.ac.ma"
        );
    }

    @AfterEach
    void tearDown() {
        clearBusinessData();
    }

    @Test
    void roomsMayBelongToABlockOrDirectlyToTheEstablishment() {
        var firstBlock = blockService.createBlock(
            root,
            firstEstablishment.getId(),
            new CreateBlockRequest(" a ", "Block A")
        );
        var otherBlock = blockService.createBlock(
            root,
            secondEstablishment.getId(),
            new CreateBlockRequest("A", "Other Block A")
        );

        var classroom = roomService.createRoom(
            root,
            firstEstablishment.getId(),
            new CreateRoomRequest(
                firstBlock.id(),
                " A12 ",
                "Room A12",
                RoomType.CLASSROOM,
                60
            )
        );
        var amphitheatre = roomService.createRoom(
            root,
            firstEstablishment.getId(),
            new CreateRoomRequest(
                null,
                "AMPHI-1",
                "Amphitheatre 1",
                RoomType.LECTURE_HALL,
                300
            )
        );

        assertThat(classroom.blockId()).isEqualTo(firstBlock.id());
        assertThat(classroom.code()).isEqualTo("A12");
        assertThat(amphitheatre.blockId()).isNull();
        assertThat(roomService.getRooms(root, firstEstablishment.getId()))
            .extracting(response -> response.code())
            .containsExactly("A12", "AMPHI-1");

        assertThatThrownBy(() -> roomService.createRoom(
            root,
            firstEstablishment.getId(),
            new CreateRoomRequest(
                otherBlock.id(),
                "INVALID",
                "Invalid Room",
                RoomType.CLASSROOM,
                30
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("400 BAD_REQUEST");

        assertThatThrownBy(() -> roomService.createRoom(
            root,
            firstEstablishment.getId(),
            new CreateRoomRequest(
                null,
                "a12",
                "Duplicate Room",
                RoomType.CLASSROOM,
                30
            )
        ))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");

        var updated = roomService.updateRoom(
            root,
            classroom.id(),
            new UpdateRoomRequest(
                null,
                "A13",
                "Room A13",
                RoomType.COMPUTER_LAB,
                40,
                RoomStatus.ACTIVE
            )
        );
        assertThat(updated.blockId()).isNull();
        assertThat(updated.roomType()).isEqualTo(RoomType.COMPUTER_LAB);

        assertThat(blockService.deactivateBlock(root, firstBlock.id()).success()).isTrue();
        assertThat(blockRepository.findById(firstBlock.id()).orElseThrow().getStatus())
            .isEqualTo(BlockStatus.INACTIVE);
        assertThat(roomService.deactivateRoom(root, updated.id()).success()).isTrue();
        assertThat(roomRepository.findById(updated.id()).orElseThrow().getStatus())
            .isEqualTo(RoomStatus.INACTIVE);
    }

    private Establishment saveEstablishment(University university, String name) {
        Establishment establishment = new Establishment();
        establishment.setUniversity(university);
        establishment.setName(name);
        establishment.setEstablishmentType(EstablishmentType.SCHOOL);
        establishment.setEstablishmentStatus(EstablishmentStatus.ACTIVE);
        return establishmentRepository.save(establishment);
    }

    private void clearBusinessData() {
        roomRepository.deleteAll();
        blockRepository.deleteAll();
        establishmentRepository.deleteAll();
        universityRepository.deleteAll();
    }
}

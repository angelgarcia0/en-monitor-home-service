package com.energymonitor.home.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.energymonitor.home.api.MemberLeft;
import com.energymonitor.home.application.command.LeaveHomeCommand;
import com.energymonitor.home.application.exception.LastOwnerCannotLeaveException;
import com.energymonitor.home.application.exception.UserHomeNotFoundException;
import com.energymonitor.home.application.usecase.LeaveHomeService;
import com.energymonitor.home.domain.model.Home;
import com.energymonitor.home.domain.model.Role;
import com.energymonitor.home.domain.model.UserHome;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LeaveHomeServiceTest {

    private HomeUseCaseFixtures.FakeUserHomePersistencePort userHomePort;
    private HomeUseCaseFixtures.FakeHomePersistencePort homePort;
    private final HomeUseCaseFixtures.FakeHomeEventPort events = new HomeUseCaseFixtures.FakeHomeEventPort();
    private LeaveHomeService service;

    @BeforeEach
    void setUp() {
        userHomePort = new HomeUseCaseFixtures.FakeUserHomePersistencePort();
        homePort = new HomeUseCaseFixtures.FakeHomePersistencePort();
        homePort.seed(Home.create("hom0000001", "Casa", "hous000001", "Calle 1", "AAA11111", null,
                HomeUseCaseFixtures.NOW));
        service = new LeaveHomeService(userHomePort, homePort, events, HomeUseCaseFixtures.clock());
    }

    @Test
    void announcesTheLeaveWithTheMembersWhoStay() {
        userHomePort.save(UserHome.owner("use0000009", "hom0000001"));
        userHomePort.save(UserHome.member("use0000001", "hom0000001"));

        service.leave(new LeaveHomeCommand("use0000001", "hom0000001"));

        assertEquals(List.of(new MemberLeft("hom0000001", "Casa", "use0000001", List.of("use0000009"),
                HomeUseCaseFixtures.NOW)), events.left);
    }

    @Test
    void leaveHomeAsMember() {
        userHomePort.save(UserHome.member("use0000001", "hom0000001"));

        var command = new LeaveHomeCommand("use0000001", "hom0000001");
        service.leave(command);

        assert userHomePort.findActive("use0000001", "hom0000001").isEmpty();
    }

    @Test
    void leaveHomeAsOwnerWithOtherOwners() {
        userHomePort.save(UserHome.owner("use0000001", "hom0000001"));
        userHomePort.save(UserHome.owner("use0000002", "hom0000001"));

        var command = new LeaveHomeCommand("use0000001", "hom0000001");
        service.leave(command);

        assert userHomePort.findActive("use0000001", "hom0000001").isEmpty();
    }

    @Test
    void leaveHomeAsOnlyOwnerThrows() {
        userHomePort.save(UserHome.owner("use0000001", "hom0000001"));

        var command = new LeaveHomeCommand("use0000001", "hom0000001");
        assertThrows(LastOwnerCannotLeaveException.class, () -> service.leave(command));
    }

    @Test
    void leaveHomeWithoutMembershipThrows() {
        var command = new LeaveHomeCommand("use0000001", "hom0000001");
        assertThrows(UserHomeNotFoundException.class, () -> service.leave(command));
    }
}

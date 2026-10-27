package com.energymonitor.home.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.energymonitor.home.application.command.JoinHomeCommand;
import com.energymonitor.home.application.exception.AccessCodeNotFoundException;
import com.energymonitor.home.application.exception.HomeConflictException;
import com.energymonitor.home.application.usecase.JoinHomeService;
import com.energymonitor.home.domain.model.Home;
import com.energymonitor.home.domain.model.Role;
import com.energymonitor.home.domain.model.UserHome;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JoinHomeServiceTest {

    private HomeUseCaseFixtures.FakeHomePersistencePort homePort;
    private HomeUseCaseFixtures.FakeUserHomePersistencePort userHomePort;
    private final HomeUseCaseFixtures.FakeHomeEventPort events = new HomeUseCaseFixtures.FakeHomeEventPort();
    private JoinHomeService service;

    @BeforeEach
    void setUp() {
        homePort = new HomeUseCaseFixtures.FakeHomePersistencePort();
        userHomePort = new HomeUseCaseFixtures.FakeUserHomePersistencePort();

        homePort.seed(Home.create("hom0000001", "Casa", "hous000001", "Calle 123", "ABC12345", null,
                HomeUseCaseFixtures.NOW));

        service = new JoinHomeService(homePort, userHomePort, events, HomeUseCaseFixtures.clock());
    }

    @Test
    void announcesTheJoinWithEveryMember() {
        userHomePort.save(UserHome.owner("use0000001", "hom0000001"));

        service.join(new JoinHomeCommand("use0000002", "ABC12345"));

        assertEquals(1, events.joined.size());
        var joined = events.joined.getFirst();
        assertEquals("hom0000001", joined.homeId());
        assertEquals("Casa", joined.homeName());
        assertEquals("use0000002", joined.userId());
        assertEquals(Set.of("use0000001", "use0000002"), Set.copyOf(joined.memberIds()));
    }

    @Test
    void joinHomeWithValidAccessCode() {
        var command = new JoinHomeCommand("use0000002", "ABC12345");
        var result = service.join(command);

        assertEquals("use0000002", result.userId());
        assertEquals("hom0000001", result.homeId());
        assertEquals(Role.MEMBER, result.role());
        assertFalse(result.favorite());
    }

    @Test
    void joinHomeWithInvalidAccessCodeThrows() {
        var command = new JoinHomeCommand("use0000002", "WRONG123");
        assertThrows(AccessCodeNotFoundException.class, () -> service.join(command));
    }

    @Test
    void joinHomeWhenAlreadyMemberThrows() {
        // First join
        service.join(new JoinHomeCommand("use0000002", "ABC12345"));

        // Second join should fail
        assertThrows(HomeConflictException.class,
                () -> service.join(new JoinHomeCommand("use0000002", "ABC12345")));
    }

    @Test
    void joinHomeAfterLeavingReactivatesAsMember() {
        // Join
        service.join(new JoinHomeCommand("use0000002", "ABC12345"));

        // Leave (soft delete)
        userHomePort.remove("use0000002", "hom0000001");

        // Re-join
        var result = service.join(new JoinHomeCommand("use0000002", "ABC12345"));

        assertEquals(Role.MEMBER, result.role());
        assertFalse(result.favorite());
    }
}

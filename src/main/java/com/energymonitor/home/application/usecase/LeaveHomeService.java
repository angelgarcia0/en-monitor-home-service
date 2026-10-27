package com.energymonitor.home.application.usecase;

import com.energymonitor.home.api.MemberLeft;
import com.energymonitor.home.application.command.LeaveHomeCommand;
import com.energymonitor.home.application.exception.HomeNotFoundException;
import com.energymonitor.home.application.exception.LastOwnerCannotLeaveException;
import com.energymonitor.home.application.exception.UserHomeNotFoundException;
import com.energymonitor.home.application.port.in.LeaveHome;
import com.energymonitor.home.application.port.out.HomeEventPort;
import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.domain.model.UserHome;
import java.time.Clock;

/**
 * The user leaves a home.
 *
 * <p>HOME-INV-002: the only OWNER cannot leave. The service checks the owner count
 * before allowing the leave operation.
 *
 * <p>Announces {@link MemberLeft} with the members who stay; {@code TransactionalLeaveHome} runs
 * it in one transaction with the removal.
 */
public class LeaveHomeService implements LeaveHome {

    private final UserHomePersistencePort userHomePort;
    private final HomePersistencePort homePort;
    private final HomeEventPort events;
    private final Clock clock;

    public LeaveHomeService(UserHomePersistencePort userHomePort, HomePersistencePort homePort,
                            HomeEventPort events, Clock clock) {
        this.userHomePort = userHomePort;
        this.homePort = homePort;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public void leave(LeaveHomeCommand command) {
        // Verify membership
        UserHome membership = userHomePort.findActive(command.userId(), command.homeId())
                .orElseThrow(() -> new UserHomeNotFoundException(
                        "no active membership for user " + command.userId() + " in home " + command.homeId()));

        // Check if user is the only OWNER
        boolean otherOwnersExist = userHomePort.countActiveOwnersByHomeId(command.homeId()) > 1;

        // Domain validates HOME-INV-002
        try {
            membership.leaveHome(otherOwnersExist);
        } catch (IllegalStateException e) {
            throw new LastOwnerCannotLeaveException(e.getMessage());
        }

        // Soft delete
        userHomePort.remove(command.userId(), command.homeId());
        events.publish(new MemberLeft(command.homeId(), HomeMembers.name(homePort, command.homeId()),
                command.userId(), HomeMembers.ids(userHomePort, command.homeId()), clock.instant()));
    }
}

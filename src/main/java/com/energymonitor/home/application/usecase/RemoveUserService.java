package com.energymonitor.home.application.usecase;

import com.energymonitor.home.api.MemberRemoved;
import com.energymonitor.home.application.command.RemoveUserCommand;
import com.energymonitor.home.application.exception.HomeAccessDeniedException;
import com.energymonitor.home.application.exception.HomeConflictException;
import com.energymonitor.home.application.exception.HomeNotFoundException;
import com.energymonitor.home.application.exception.UserHomeNotFoundException;
import com.energymonitor.home.application.port.in.RemoveUser;
import com.energymonitor.home.application.port.out.HomeEventPort;
import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.domain.model.UserHome;
import java.time.Clock;

/**
 * Removes a user from a home.
 *
 * <p>The actor must be an active member with {@code canManageHome()} permission.
 * The target must be an active member. An OWNER cannot be removed (HOME-INV-004).
 *
 * <p>Announces {@link MemberRemoved} with the members who stay; {@code TransactionalRemoveUser}
 * runs it in one transaction with the removal.
 */
public class RemoveUserService implements RemoveUser {

    private final UserHomePersistencePort userHomePort;
    private final HomePersistencePort homePort;
    private final HomeEventPort events;
    private final Clock clock;

    public RemoveUserService(UserHomePersistencePort userHomePort, HomePersistencePort homePort,
                             HomeEventPort events, Clock clock) {
        this.userHomePort = userHomePort;
        this.homePort = homePort;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public void remove(RemoveUserCommand command) {
        // Verify actor membership (404 if not a member)
        UserHome actor = userHomePort.findActive(command.userId(), command.homeId())
                .orElseThrow(() -> new HomeNotFoundException(
                        "no active membership for user " + command.userId() + " in home " + command.homeId()));

        // Verify actor permission (403 if member without canManageHome)
        if (!actor.getPermissions().canManageHome()) {
            throw new HomeAccessDeniedException(
                    "user " + command.userId() + " cannot manage home " + command.homeId());
        }

        // Verify target membership
        UserHome target = userHomePort.findActive(command.targetUserId(), command.homeId())
                .orElseThrow(() -> new UserHomeNotFoundException(
                        "no active membership for target user " + command.targetUserId()));

        // Check removal rules (HOME-INV-004: OWNER cannot be removed)
        if (!target.canBeRemovedBy(actor)) {
            throw new HomeConflictException("cannot remove an OWNER from the home");
        }

        // Soft delete
        userHomePort.remove(command.targetUserId(), command.homeId());
        events.publish(new MemberRemoved(command.homeId(), HomeMembers.name(homePort, command.homeId()),
                command.targetUserId(), command.userId(),
                HomeMembers.ids(userHomePort, command.homeId()), clock.instant()));
    }
}

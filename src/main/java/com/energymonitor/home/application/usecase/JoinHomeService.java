package com.energymonitor.home.application.usecase;

import com.energymonitor.home.api.MemberJoined;
import com.energymonitor.home.application.command.JoinHomeCommand;
import com.energymonitor.home.application.exception.AccessCodeNotFoundException;
import com.energymonitor.home.application.exception.HomeConflictException;
import com.energymonitor.home.application.port.in.JoinHome;
import com.energymonitor.home.application.port.out.HomeEventPort;
import com.energymonitor.home.application.port.out.HomePersistencePort;
import com.energymonitor.home.application.port.out.UserHomePersistencePort;
import com.energymonitor.home.application.result.UserHomeResult;
import com.energymonitor.home.domain.model.UserHome;
import java.time.Clock;

/**
 * Joins a home with an access code. The user becomes a MEMBER.
 *
 * <p>If the user was previously a member and left (soft delete), the membership is
 * reactivated as MEMBER with {@code favorite = false}.
 *
 * <p>Announces {@link MemberJoined} with every member after the join; {@code TransactionalJoinHome}
 * runs it in one transaction with the membership.
 */
public class JoinHomeService implements JoinHome {

    private final HomePersistencePort homePort;
    private final UserHomePersistencePort userHomePort;
    private final HomeEventPort events;
    private final Clock clock;

    public JoinHomeService(HomePersistencePort homePort, UserHomePersistencePort userHomePort,
                           HomeEventPort events, Clock clock) {
        this.homePort = homePort;
        this.userHomePort = userHomePort;
        this.events = events;
        this.clock = clock;
    }

    @Override
    public UserHomeResult join(JoinHomeCommand command) {
        // Find home by access code
        var home = homePort.findActiveByAccessCode(command.accessCode())
                .orElseThrow(() -> new AccessCodeNotFoundException(
                        "no active home with access code"));

        // Check if user is already an active member
        if (userHomePort.findActive(command.userId(), home.idHome()).isPresent()) {
            throw new HomeConflictException("user is already a member of this home");
        }

        // Create MEMBER membership (reactivates if soft-deleted)
        UserHome membership = UserHome.member(command.userId(), home.idHome());
        userHomePort.save(membership);

        events.publish(new MemberJoined(home.idHome(), home.name(), command.userId(),
                HomeMembers.ids(userHomePort, home.idHome()), clock.instant()));

        return UserHomeResult.from(membership);
    }
}

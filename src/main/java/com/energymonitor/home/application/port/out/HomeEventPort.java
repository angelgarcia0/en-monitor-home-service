package com.energymonitor.home.application.port.out;

import com.energymonitor.home.api.HomeCreated;
import com.energymonitor.home.api.HomeDeleted;
import com.energymonitor.home.api.HomeOwnershipTransferred;
import com.energymonitor.home.api.MemberJoined;
import com.energymonitor.home.api.MemberLeft;
import com.energymonitor.home.api.MemberRemoved;
import com.energymonitor.home.api.ThresholdsUpdated;

/**
 * Domain events of the Home context ({@code api}): published inside the transaction of the use
 * case, and the ones other services need are relayed to the message broker.
 */
public interface HomeEventPort {

    void publish(HomeCreated event);

    void publish(MemberJoined event);

    void publish(MemberLeft event);

    void publish(MemberRemoved event);

    void publish(ThresholdsUpdated event);

    void publish(HomeDeleted event);

    void publish(HomeOwnershipTransferred event);
}

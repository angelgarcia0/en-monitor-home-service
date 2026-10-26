package com.energymonitor.home.application.port.out;

import com.energymonitor.home.api.HomeDeleted;
import com.energymonitor.home.api.HomeOwnershipTransferred;

/**
 * Domain events of the Home context ({@code api}): published inside the transaction of the use
 * case, and the ones other services need are relayed to the message broker.
 */
public interface HomeEventPort {

    void publish(HomeDeleted event);

    void publish(HomeOwnershipTransferred event);
}

package com.energymonitor.home.infrastructure;

import com.energymonitor.home.application.command.RemoveUserCommand;
import com.energymonitor.home.application.port.in.RemoveUser;
import com.energymonitor.home.application.usecase.RemoveUserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runs {@link RemoveUserService} in one transaction, so the removed membership and the
 * {@code home.member-removed} message commit together or not at all. The service stays
 * framework-free; this decorator is the one the web layer receives ({@link Primary}).
 */
@Component
@Primary
public class TransactionalRemoveUser implements RemoveUser {

    private final RemoveUserService target;
    private final TransactionTemplate transaction;

    public TransactionalRemoveUser(RemoveUserService target, PlatformTransactionManager transactions) {
        this.target = target;
        this.transaction = new TransactionTemplate(transactions);
    }

    @Override
    public void remove(RemoveUserCommand command) {
        transaction.executeWithoutResult(status -> target.remove(command));
    }
}

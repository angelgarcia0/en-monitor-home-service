package com.energymonitor.home.adapter.out.messaging;

/**
 * The messages this service publishes, named by their routing key on the
 * {@code energy-monitor.home} topic exchange.
 *
 * <p>These names are a contract with the services that consume them: renaming one silently
 * stops its delivery, because the consumer's binding no longer matches.
 */
public final class HomeMessageTypes {

    /** A user created a home: the notification service tells the owner. */
    public static final String HOME_CREATED = "home.created";

    /** A user joined a home: the notification service tells them and the other members. */
    public static final String MEMBER_JOINED = "home.member-joined";

    /** A member left a home: the notification service tells the members who stay. */
    public static final String MEMBER_LEFT = "home.member-left";

    /** An owner removed a member: the notification service tells them and the members who stay. */
    public static final String MEMBER_REMOVED = "home.member-removed";

    /** The thresholds of a home changed: the notification service tells the members. */
    public static final String THRESHOLDS_UPDATED = "home.thresholds-updated";

    /** The owner of a home changed: the notification service tells the members. */
    public static final String OWNERSHIP_TRANSFERRED = "home.ownership-transferred";

    /**
     * A home was deleted: the device service unlinks its modules, and the notification service
     * tells the people who were its members.
     */
    public static final String HOME_DELETED = "home.deleted";

    private HomeMessageTypes() {
    }
}

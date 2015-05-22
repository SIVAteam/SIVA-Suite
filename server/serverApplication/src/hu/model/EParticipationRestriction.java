package hu.model;

import hu.model.users.EUserType;
import hu.model.users.User;

/**
 * This enum describes how access to a {@link Video} is restricted.
 */
public enum EParticipationRestriction {
    /**
     * Token is an unguessable, unique {@link String} that is given to a user to
     * enter a {@link Video}.
     */
    Token,

    /**
     * Password is a {@link String} chosen by a owner of the
     * {@link Video} to allow multiple {@link User}s to enter the
     * {@link Video}.
     */
    Password,

    /**
     * The owner of a {@link Video} may give access to the
     * {@link Video}s to {@link User}s that are signed up for the
     * {@link Group} the {@link Video} belongs to.
     */
    GroupAttendants,

    /**
     * All {@link User}s with {@link EUserType#Participant} are able to access
     * the {@link Video}.
     */
    Registered,

    /**
     * The {@link Video} is public so every user may enter it.
     */
    Public,

    /**
     * The {@link Video} is only accessible for the owner of the
     * {@link Video}.
     */
    Private
}

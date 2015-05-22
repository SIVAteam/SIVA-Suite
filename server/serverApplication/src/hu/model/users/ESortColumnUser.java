package hu.model.users;


/**
 * This enum defines columns a list of {@link User}s can be sorted.
 */
public enum ESortColumnUser {
    /**
     * The list of {User}s is sorted by the e-mail address.
     */
    Email,

    /**
     * The list of {User}s is sorted by the last name, the first name and the academic title with this order.
     */
    Name
}
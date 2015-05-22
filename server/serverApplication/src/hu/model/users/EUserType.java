package hu.model.users;

import hu.model.Group;
import hu.model.Video;

/**
 * This enum defines the different types of {@link User}s. Depending on its
 * type, a {@link User} may be allowed or denied to use certain system
 * functionality. The types additionally contain numerical representations,
 * where a higher number stands for a role with more access privileges.
 */
public enum EUserType {
    /**
     * An Administrator is a {@link User} who has all rights for reading and
     * editing {@link Group}s, {@link Video}s and {@link User}s.
     */
    Administrator(4),

    /**
     * A Tutor is the owner of a {@link Group} and has all rights to edit the
     * {@link Group}(s) he is a owner of. A Tutor is also allowed to edit
     * {@link Video}(s) that belong to that {@link Group}(s)
     */
    Tutor(3),

    /**
     * A Participant is a {@link User} that is registered to the system. A
     * Participant is able to login to the system and has advanced rights for
     * {@link Group}s, {@link Video}s and evaluations.
     */
    Participant(2),

    /**
     * An Anonymous {@link User} has no advanced rights in {@link Group}s,
     * {@link Video}s or evaluations. An {@link User} with
     * {@link EUserType#Anonymous} may register within the system.
     */
    Anonymous(1);

    private int level;

    /**
     * 
     * @param level
     */
    private EUserType(int level) {
        this.level = level;
    }

    /**
     * 
     * @return the {@link User}s type as numerical representation. A higher
     *         number stands for a role with more access privileges.
     */
    public int getLevel() {
        return this.level;
    }
}
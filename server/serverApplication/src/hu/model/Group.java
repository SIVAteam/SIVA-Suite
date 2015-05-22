package hu.model;

import hu.model.users.EUserType;
import hu.model.users.User;

/**
 * This class describes an group. An {@link Group} may be owned and attended by
 * multiple {@link User}s, furthermore an {@link Group} may contain a number of
 * {@link Video}s.
 */
public class Group {
    private Integer id = null;
    private String title = null;
    private boolean visible = false;

    /**
     * Create an {@link Group} with an id.
     * 
     * @param id
     *            to set.
     */
    public Group(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return the id of the {@link Group}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * 
     * @return the title of the {@link Group}.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of the {@link Group}.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return true if the {@link Group} is visible for all {@link User} groups,
     *         false otherwise.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Set the flag whether the {@link Group} is visible for all {@link User}
     * groups or not.
     * 
     * @param visible
     *            is true if the {@link Group} is visible, if false the
     *            {@link Group} is only visible to {@link User}s with
     *            {@link EUserType#Administrator} or owner of the {@link Group}.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return String.format("Group (%d) \"%s\"", this.id, this.title);
    }

    @Override
    public int hashCode() {
        return (this.id == null) ? 0 : this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Group other = (Group) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
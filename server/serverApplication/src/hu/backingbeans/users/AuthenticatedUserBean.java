package hu.backingbeans.users;

import hu.model.users.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for the currently authenticated {@link User}. It is
 * used to display the name of the {@link User} in the page header.
 */
@ManagedBean
@RequestScoped
public class AuthenticatedUserBean {
    private User user;

    /**
     * 
     * @return the {@link User} that is authenticated within the current
     *         session.
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Set the {@link User} that is authenticated within the current session.
     * 
     * @param user
     *            to set.
     */
    public void setUser(User user) {
        this.user = user;
    }
}
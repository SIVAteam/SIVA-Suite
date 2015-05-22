package hu.backingbeans.videos;

import hu.model.Video;
import hu.model.users.User;

import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * This is the backing bean to temporarily hold the {@link User}s which are
 * about to be invited to participate in a {@link Video}.
 */
@ManagedBean
@SessionScoped
public class UsersInvitedToVideoBean {
    private Map<Integer, Set<Integer>> invitedUsers;

    /**
     * 
     * @return the {@link Map} of all invited {@link User}s.
     */
    public Map<Integer, Set<Integer>> getInvitedUsers() {
        return this.invitedUsers;
    }

    /**
     * Set the {@link Map} of all invited {@link User}s. The integer in the
     * map is a Id that specifies the video and the integers in the
     * set specifies the invited users.
     * 
     * @param invitedUsers
     *            to set.
     */
    public void setInvitedUsers(Map<Integer, Set<Integer>> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }
}
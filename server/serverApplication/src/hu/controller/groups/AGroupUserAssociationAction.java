package hu.controller.groups;

import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.controller.AController;
import hu.model.Group;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.util.Configuration;
import hu.util.SessionData;

import javax.faces.bean.ManagedProperty;

/**
 * This abstract class contains common functionality for associating
 * {@link User}s with {@link Group}s, e.g. as attendants or owners.
 */
public abstract class AGroupUserAssociationAction extends AController {
    @ManagedProperty("#{groupBean}")
    protected GroupBean groupBean;

    @ManagedProperty("#{userListBean}")
    protected UserListBean userListBean;
    
    @ManagedProperty("#{PersistenceProvider}")
    protected IPersistenceProvider persistenceProvider;
    
    @ManagedProperty("#{configuration}")
    protected Configuration configuration;
    
    @ManagedProperty("#{sessionData}")
    protected SessionData sessionData;
    
    @ManagedProperty("#{userBean}")
    protected UserBean userBean;
    
    /**
     * Set {@link UserBean} using injection.
     * 
     * @param userBean
     *            to inject.
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    /**
     * Set {@link SessionData} using injection.
     * 
     * @param sessionData
     *            to inject.
     */
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Set {@link GroupBean} using injection.
     * 
     * @param groupBean
     *            to inject.
     */
    public void setGroupBean(GroupBean groupBean) {
        this.groupBean = groupBean;
    }
    
    /**
     * Set {@link UserListBean} using injection.
     * 
     * @param userListBean
     *            to inject.
     */
    public void setUserListBean(UserListBean userListBean) {
        this.userListBean = userListBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }
    
    /**
     * Set {@link Configuration} using injection.
     * 
     * @param configuration
     *            to inject.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * List all the {@link User}s that are associated with an {@link Group}. The
     * list may either contain the owners or the attendants of an {@link Group}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Group}, have
     * permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public abstract String listAssociatedUsers();

    /**
     * Add a {@link User} as attendant or owner to an {@link Group}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Group}, have
     * permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public abstract String addUser();

    /**
     * Remove a {@link User} who is an attendant or owner from an {@link Group}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Group}, have
     * permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public abstract String removeUser();
}
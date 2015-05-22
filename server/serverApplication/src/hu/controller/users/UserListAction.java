package hu.controller.users;

import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.AController;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.util.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * This class provides the functionality to display a list of {@link User}s.
 */
@ManagedBean
@RequestScoped
public class UserListAction extends AController {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    @ManagedProperty("#{userListBean}")
    private UserListBean userListBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

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
     * Retrieve a list of {@link User}s.
     * 
     * Only users of {@link EUserType#Administrator} have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String listUsers() {
        
        // Set to all users and page 0 if there is a new search request
        if (this.getCurrentFcInstance().isPostback()) {
            this.userListBean.setPage(0);
            this.userListBean.setUserType(null);
        }
        
        IUserStore us = this.persistenceProvider.getUserStore();

        List<UserListEntryBean> list = new ArrayList<UserListEntryBean>();

        List<User> users;
        double usersCount;
        
        if (this.userListBean.getSearchQuery().equals("")) {
            
            // Get users with the specified user type, ordered in the specified 
            // direction for the current page of the table
            users = us.getAll(
                    this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    this.userListBean.getPage()
                            * this.configuration.getInteger(CFG_MAX_ROWS),
                    this.configuration.getInteger(CFG_MAX_ROWS));

            // Get the amount of users with the specified user type
            usersCount = us.getCountOfAll(this.userListBean.getUserType());
        } else {
            
            // Get users with the specified user type whose properties match a
            // given search term, ordered in the specified direction for the 
            // current page of the table
            users = us.search(this.userListBean.getSearchQuery(),
                    this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    this.userListBean.getPage()
                            * this.configuration.getInteger(CFG_MAX_ROWS),
                    this.configuration.getInteger(CFG_MAX_ROWS));

            // Get users with the specified user type ordered in the specified
            // direction for the current page of the table
            usersCount = us.getCountOfSearch(this.userListBean.getSearchQuery(),
                    this.userListBean.getUserType());
        }

        // Get number of pages starting with 0
        int pages = (int) Math.ceil(usersCount
                / this.configuration.getInteger(CFG_MAX_ROWS));
        this.userListBean.setPages(pages);

        // Add every user from result set to bean list
        for (Iterator<User> it = users.iterator(); it.hasNext();) {
            User currentUser = it.next();
            
            UserListEntryBean entry = new UserListEntryBean();
            entry.setUser(currentUser);
            
            list.add(entry);
        }
        this.userListBean.setList(list);
        
        return null;
    }
}
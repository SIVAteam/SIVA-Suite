package hu.controller.groups;

import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.users.EUserListAction;
import hu.model.Group;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IUserStore;
import hu.persistence.InconsistencyException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This class provides the functionality to list, add and remove owners (
 * {@link User}s) from an {@link Group}.
 */
@ManagedBean
@RequestScoped
public class GroupOwnershipAction extends AGroupUserAssociationAction {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    private static final String MSG_NOT_REMOVED = "addTutors_not_removed";
    private static final String MSG_REMOVED = "addTutors_removed";
    private static final String MSG_NOT_ADDED = "addTutors_not_added";
    private static final String MSG_ADDED = "addTutors_added";
    private static final String MSG_ALREADY_ADDED = "addTutor_already_added";
   
    private static final String ADD_TUTORS_FORM_LIST_USERS_TABLE = "addTutorsForm:listUsersTable";
    
    private static final String ADD_TUTORS_FACELET = "/xhtml/groups/addTutors";
    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";

    /**
     * {@inheritDoc}
     */
    @Override
    public String listAssociatedUsers() {

        IUserStore us = this.persistenceProvider.getUserStore();
        IGroupStore es = this.persistenceProvider.getGroupStore();
        
        // Check if an a groupId was set and redirect if not
        if (this.groupBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Check if group exists and redirect if not
        Group group = es.findById(this.groupBean.getId());
        if (group == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Get users currently owning the group
        Set<User> groupOwners = new HashSet<User>(
                us.getByOwnership(this.groupBean.getId()));
        Set<User> groupAttendants = new HashSet<User>(
                us.getByAttendance(this.groupBean.getId()));

        // Check if current user is allowed to use this page
        User currentUser = us.getById(this.sessionData.getUserId());
        if (currentUser.getUserType() != EUserType.Administrator) {

            // Check if current User is group owner and redirect if not
            if (!groupOwners.contains(currentUser)) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        }

        // Set group title for displaying
        this.groupBean.setTitle(group.getTitle());
        
        // Check if no search is done
        if (!this.getCurrentFcInstance().isPostback()) {
            
            // Perform add user and remove user if get parameter is set and
            // update owner list
            if (this.userListBean.getAction() == EUserListAction.add) {
                this.addUser();
                groupOwners = new HashSet<User>(
                        us.getByOwnership(this.groupBean.getId()));
            } else if (this.userListBean.getAction() == EUserListAction.remove) {
                this.removeUser();
                groupOwners = new HashSet<User>(
                        us.getByOwnership(this.groupBean.getId()));
            }
        }

        // Prgroup from displaying normal users
        if (this.userListBean.getUserType() == null) {
            this.userListBean.setUserType(EUserType.Tutor);
        }

        // Set to all users and page 0 if there is a new search request
        if (this.getCurrentFcInstance().isPostback()) {
            this.userListBean.setPage(0);
            this.userListBean.setUserType(EUserType.Tutor);
        }
        
        List<UserListEntryBean> list = new ArrayList<UserListEntryBean>();

        List<User> users;
        double usersCount;

        if (this.userListBean.getSearchQuery().equals("")) {

            // Get users with the specified user type, ordered in the specified
            // direction for the current page of the table
            users = us.getAll(this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    this.userListBean.getPage(),
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
                    this.userListBean.getPage(),
                    this.configuration.getInteger(CFG_MAX_ROWS));

            // Get users with the specified user type ordered in the specified
            // direction for the current page of the table
            usersCount = us.getCountOfSearch(
                    this.userListBean.getSearchQuery(),
                    this.userListBean.getUserType());
        }

        // Get number of pages starting with 0
        int pages = (int) Math.ceil(usersCount
                / this.configuration.getInteger(CFG_MAX_ROWS));
        this.userListBean.setPages(pages);

        // Add every user from result set to bean list and set available buttons
        for (Iterator<User> it = users.iterator(); it.hasNext();) {
            User user = it.next();

            UserListEntryBean entry = new UserListEntryBean();
            entry.setUser(user);

            boolean isOwner = groupOwners.contains(user);
            boolean isAttendant = groupAttendants.contains(user);
            entry.setAdded(isOwner);
            entry.setRemovable(isOwner
                    && user.getId() != currentUser.getId());
            entry.setAddable(!isAttendant && !isOwner);

            list.add(entry);
        }
        this.userListBean.setList(list);

        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String addUser() {
        
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        
        User user = userStore.getById(this.userBean.getId());
        Group group = groupStore.getById(groupBean.getId());
        
        List<User> owners = userStore.getByOwnership(group);
        
        if (owners.contains(user)) {
        	this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_ALREADY_ADDED));
        	return null;
        }
        
        try {
            groupStore.addOwner(group, user);
            this.sendMessageTo(ADD_TUTORS_FORM_LIST_USERS_TABLE, this.getCommonMessage(MSG_ADDED));
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_NOT_ADDED));
        }
        return ADD_TUTORS_FACELET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        
        User user = userStore.findById(this.userBean.getId());
        Group group = groupStore.findById(groupBean.getId());
        
        if (user == null || group == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        try {
            groupStore.removeOwner(group, user);
            this.sendMessageTo(ADD_TUTORS_FORM_LIST_USERS_TABLE, this.getCommonMessage(MSG_REMOVED));
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_NOT_REMOVED));
        }
        
        return ADD_TUTORS_FACELET;
    }
}
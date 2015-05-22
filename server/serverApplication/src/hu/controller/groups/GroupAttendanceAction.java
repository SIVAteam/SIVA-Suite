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
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

/**
 * This class provides the functionality to list, add and remove attendants (
 * {@link User}s) from an {@link Group}.
 */
@ManagedBean
@RequestScoped
public class GroupAttendanceAction extends AGroupUserAssociationAction {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
    private static final String LIST_GROUPS_FACELET = "/xhtml/groups/listGroups";
    private static final String ADD_USERS_FACELET = "/xhtml/groups/addUsers";
    
    private static final String LIST_GROUPS_TABLE = "listGroupsTable";
    private static final String LIST_USERS_TABLE = "listUsersTable";
    
    private static final String MSG_USER_ADDED = "listGroups_user_added";
    private static final String MSG_ADDED = "addUsers_added";
    private static final String MSG_ADD_FAILED = "addUsers_add_failed";
    private static final String MSG_REMOVED = "addUsers_removed";
    private static final String MSG_REMOVE_FAILED = "addUser_remove_failed";
    private static final String MSG_USER_REMOVED = "listGroups_user_removed";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String listAssociatedUsers() {
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();

        // Verify that an group id is provided.
        if (this.groupBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Check if group exists, redirect if not.
        Group group = groupStore.findById(this.groupBean.getId());
        if (group == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Fetch the owners and attendants of the group.
        Set<User> owners = new HashSet<User>(userStore.getByOwnership(group));
        Set<User> attendants = new HashSet<User>(userStore.getByAttendance(group));

        // Check if current user is allowed to use this page
        User currentUser = null;
        if (this.sessionData.getUserId() != null) {
            currentUser = userStore.getById(this.sessionData.getUserId());
        }
        if(currentUser == null
                || (currentUser.getUserType() != EUserType.Administrator
                && !owners.contains(currentUser))){
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // Set group title for displaying
        this.groupBean.setTitle(group.getTitle());
        
        // Do not run if a search was submitted.
        if (!this.getCurrentFcInstance().isPostback()) {
            
            // Perform add/remove operation and update list.
            if (this.userListBean.getAction() == EUserListAction.add) {
                this.addUser();
                attendants = new HashSet<User>(userStore.getByAttendance(group));
            } else if (this.userListBean.getAction() == EUserListAction.remove) {
                this.removeUser();
                attendants = new HashSet<User>(userStore.getByAttendance(group));
            }
        }

        // Go to first page on new search request.
        if (this.getCurrentFcInstance().isPostback()) {
            this.userListBean.setPage(0);
        }

        // Fetch users if no search query was entered.
        final int maxEntries = this.configuration.getInteger(CFG_MAX_ROWS);
        List<User> users;
        int numUsers;
        int page = Math.max(this.userListBean.getPage(), 0);
        if (this.userListBean.getSearchQuery().equals("")) {
            users = userStore.getAll(
                    this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    page * maxEntries,
                    maxEntries
                    );

            numUsers = userStore.getCountOfAll(this.userListBean.getUserType());

        // Fetch users for an entered search query.
        } else {
            users = userStore.search(
                    this.userListBean.getSearchQuery(),
                    this.userListBean.getSortColumn(),
                    this.userListBean.getSortDirection(),
                    this.userListBean.getUserType(),
                    page * maxEntries,
                    maxEntries);

            numUsers = userStore.getCountOfSearch(
                    this.userListBean.getSearchQuery(),
                    this.userListBean.getUserType());
        }

        // Calculate number of pages.
        int pages = numUsers / maxEntries
                + ((numUsers % maxEntries != 0) ? 1 : 0);
        this.userListBean.setPages(pages);

        // Populate list of users to be displayed.
        List<UserListEntryBean> list = new ArrayList<UserListEntryBean>();
        for (User user : users) {
            UserListEntryBean entry = new UserListEntryBean();
            entry.setUser(user);

            // Determine which buttons to show.
            boolean isOwner = owners.contains(user);
            boolean isAttendant = attendants.contains(user);
            entry.setAdded(isAttendant);
            entry.setRemovable(isAttendant);
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

        User currentUser = userStore.getById(sessionData.getUserId());

        FacesContext fctx = this.getCurrentFcInstance();
        Group group = null;

        // is an group id provided
        if (groupBean.getId() != null) {
            group = groupStore.findById(groupBean.getId());
            if (group == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        } else {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // set group title for displaying
        this.groupBean.setTitle(group.getTitle());

        // self sign up
        if (userBean.getId() == null) {

            if (groupStore.getByAttendant(currentUser).contains(group)
                    || groupStore.getByOwner(currentUser).contains(group)) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }

            if (!fctx.isPostback()) {

                return null;
            }
            // store to database
            try {
                groupStore.addAttendant(group.getId(), currentUser.getId());
                this.sendMessageTo(LIST_GROUPS_TABLE,
                        this.getCommonMessage(MSG_USER_ADDED));
                return LIST_GROUPS_FACELET;
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_ADD_FAILED));
                return LIST_GROUPS_FACELET;
            }

            // add by admin or tutor
        } else if (currentUser.getUserType().equals(EUserType.Administrator)
                || groupStore.getByOwner(currentUser).contains(group)) {

            User userToAdd = userStore.findById(userBean.getId());

            // does user exist and is already tutor or attendant
            if (userToAdd == null
                    || (groupStore.getByOwner(userToAdd).contains(group) || groupStore
                            .getByAttendant(userToAdd).contains(group))) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }

            // store to database
            try {
                groupStore.addAttendant(group, userToAdd);
                this.sendMessageTo(LIST_USERS_TABLE,
                        this.getCommonMessage(MSG_ADDED));
                return ADD_USERS_FACELET;
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_ADD_FAILED));
                return ADD_USERS_FACELET;
            }

        }
        return RESTRICTION_ERROR_FACELET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String removeUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();

        User currentUser = userStore.getById(sessionData.getUserId());

        FacesContext fctx = this.getCurrentFcInstance();
        Group group = null;
        // is an group id provided
        if (groupBean.getId() != null) {
            group = groupStore.findById(groupBean.getId());
            if (group == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        } else {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        this.groupBean.setTitle(group.getTitle());

        // self sign off
        if (userBean.getId() == null) {

            if (!groupStore.getByAttendant(currentUser).contains(group)) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }

            if (!fctx.isPostback()) {

                return null;
            }
            // store to database
            try {
                groupStore.removeAttendant(group.getId(), currentUser.getId());
                this.sendMessageTo(LIST_GROUPS_TABLE,
                        this.getCommonMessage(MSG_USER_REMOVED));
                return LIST_GROUPS_FACELET;
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_REMOVE_FAILED));
                return LIST_GROUPS_FACELET;
            }

            // remove by admin or tutor
        } else if (currentUser.getUserType().equals(EUserType.Administrator)
                || groupStore.getByOwner(currentUser).contains(group)) {

            User userToAdd = userStore.findById(userBean.getId());

            // does user exist and is already tutor or attendant
            if (userToAdd == null
                    || !(groupStore.getByAttendant(userToAdd).contains(group))) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
            // store to db
            try {
                groupStore.removeAttendant(group, userToAdd);
                this.sendMessageTo(LIST_USERS_TABLE,
                        this.getCommonMessage(MSG_REMOVED));
                return ADD_USERS_FACELET;
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_REMOVE_FAILED));
                return LIST_GROUPS_FACELET;
            }
        }

        return RESTRICTION_ERROR_FACELET;
    }
}
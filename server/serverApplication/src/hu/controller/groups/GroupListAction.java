package hu.controller.groups;

import hu.backingbeans.groups.GroupListBean;
import hu.backingbeans.groups.GroupListBean.GroupListEntryBean;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.util.Configuration;
import hu.util.SessionData;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * This class provides the functionality to display a list of {@link Group}s.
 */
@ManagedBean
@RequestScoped
public class GroupListAction {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    private static final int FIRST_PAGE = 0;
    
    private static final String LIST_GROUPS_FACELET = "/xhtml/groups/listGroups";

    @ManagedProperty("#{groupListBean}")
    private GroupListBean groupListBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{sessionData}")
    private SessionData session;

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    /**
     * Set {@link GroupListBean} using injection.
     * 
     * @param groupListBean
     *            to inject.
     */
    public void setGroupListBean(GroupListBean groupListBean) {
        this.groupListBean = groupListBean;
    }

    /**
     * Set {@link SessionData} using injection.
     * 
     * @param session
     *            to inject.
     */
    public void setSession(SessionData session) {
        this.session = session;
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
     * Set {@link Configuration} using injection for configuration file access.
     * 
     * @param configuration
     *            to inject.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Retrieve a list of {@link Group}s.
     * 
     * All {@link EUserType}s of users have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String listGroups() {
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();

        // Fetch current user, to decide which buttons are visible and which
        // groups are attended.
        User currentUser = null;
        boolean isAdmin = false;
        boolean isTutor = false;
        if (this.session.getUserId() != null) {
            currentUser = userStore.getById(this.session.getUserId());
            isAdmin = currentUser.getUserType() == EUserType.Administrator;
            isTutor = currentUser.getUserType() == EUserType.Tutor;
        }

        // Decide which tabs to show.
         // Show attended groups for authenticated users.
        groupListBean.setAttendedAvailable(currentUser != null);
        // Show owned groups for tutors and admins.
        groupListBean.setOwnedAvailable(isTutor || isAdmin);
        // Show all groups for all users
        groupListBean.setAllAvailable(true);

        // Parse GET parameters.
        ESortDirection direction = (this.groupListBean.getSortDirection() != null)
                ? this.groupListBean.getSortDirection() : ESortDirection.ASC; 
        boolean showVisible = "visible".equals(this.groupListBean.getListShown());
        boolean showAttended = "attended".equals(this.groupListBean.getListShown());
        boolean showOwned = "owned".equals(this.groupListBean.getListShown());
        boolean showAll = "all".equals(this.groupListBean.getListShown());
        int currentPage = (this.groupListBean.getPage() != null)
                ? this.groupListBean.getPage() : FIRST_PAGE;

        // Check if current user is allowed to see the selected tab.
        if (currentUser == null) {
            showAttended = false;
            showOwned = false;
        } else {
            // Only tutor and admin can own groups.
            if (!isAdmin && !isTutor) {
                showOwned = false;
            }
        }

        // By default, show visible groups.
        if (!(showAttended || showOwned || showAll || showVisible)) {
            showAll = true;
            // Also reset parameter string to default value.
            // This is required because we use this parameter in the pagination tag
            // which does not filter data properly.
            this.groupListBean.setListShown("all");
        }

        // Calculate number of pages for the current tab.
        // Count groups.
        int numGroups = 0;
        if (showAll || showVisible) {
            numGroups = groupStore.getCountOfAll(true, isAdmin, currentUser);
        } else if (showAttended) {
            numGroups = groupStore.getCountByAttendant(currentUser);
        } else if (showOwned) {
            numGroups = groupStore.getCountByOwner(currentUser);
        }
        // Determine how many pages are needed.
        int maxEntries = Math.max(1, this.configuration.getInteger(CFG_MAX_ROWS));
        int numPages = numGroups / maxEntries
                + ((numGroups % maxEntries == 0) ? 0 : 1);
        this.groupListBean.setPages(numPages);
        // Bound the current page.
        currentPage = Math.max(FIRST_PAGE, Math.min(currentPage, numPages-(1-FIRST_PAGE)));
        this.groupListBean.setPage(currentPage);
        
        // Fetch selected groups and their owners.
        List<Group> groups = null;
        if (showVisible || showAll) {
            groups = groupStore.getAll(direction, true, isAdmin, maxEntries,
                    (currentPage - FIRST_PAGE) * maxEntries, currentUser);
        } else if (showOwned) {
            groups = groupStore.getByOwner(currentUser, direction, maxEntries,
                    (currentPage - FIRST_PAGE) * maxEntries);
        } else if (showAttended) {
            groups = groupStore.getByAttendant(currentUser, direction,
                    maxEntries, (currentPage - FIRST_PAGE) * maxEntries);
        }
        Map<Group, List<User>> owners = userStore.getUsersOwningGroups(groups);

        // Fetch the groups owned and attended by the current user.
        Set<Group> owned, attended;
        // Fetching groups is not necessary in the owned and attended groups tab.
        if (currentUser != null && !showAttended && !showOwned) {
            owned = new HashSet<Group>(groupStore.getByOwner(currentUser));
            attended = new HashSet<Group>(groupStore.getByAttendant(currentUser));
        } else {
            owned = new HashSet<Group>();
            attended = new HashSet<Group>();
        }

        // Assemble the list to be displayed.
        List<GroupListEntryBean> listEntries = new LinkedList<GroupListEntryBean>();
        for (Group group : groups) {
        	GroupListEntryBean entry = new GroupListEntryBean();

            // Show edit/delete buttons for group owners and admins.
            entry.setEditAvailable(isAdmin || showOwned || owned.contains(group));
            entry.setDeleteAvailable(isAdmin || showOwned || owned.contains(group));

            entry.setSignOffAvailable(false);
            entry.setSignUpAvailable(false);
            if (currentUser != null) {
                // Show signOff button for attendants.
                if (showAttended || attended.contains(group)) {
                    entry.setSignOffAvailable(true);

                // Show signUp button for non-attendants/non-owners.
                } else if (!showOwned && !owned.contains(group)) {
                    entry.setSignUpAvailable(true);
                }
            }

            // Show showVideos button for everybody.
            entry.setShowVideosAvailable(true);

            // Fill entry with data.
            entry.setGroup(group);
            entry.setOwners(owners.get(group));
            entry.setCurrentUserOwner(showOwned || owned.contains(group));
            entry.setCurrentUserSignedUp(showAttended || attended.contains(group));

            // Append entry to list.
            listEntries.add(entry);
        }

        this.groupListBean.setList(listEntries);
        return LIST_GROUPS_FACELET;
    }
}
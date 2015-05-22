package hu.controller.groups;

import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.AController;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.persistence.InconsistencyException;
import hu.util.SessionData;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;

/**
 * This class provides the functionality to create, edit and delete an
 * {@link Group}.
 */
@ManagedBean
@RequestScoped
public class GroupAction extends AController {
	private static final String VIDEO_DESTINATION =  System.getProperty("user.home")
            + "/.sivaServer/videos/";
	
    private static final String FACELET_RESTRICTION_ERROR = "/xhtml/errors/restrictionError";
    private static final String FACELET_LIST_GROUPS = "/xhtml/groups/listGroups";
    
    private static final String LIST_GROUPS_TABLE = "listGroupsTable";
    
    private static final String MSG_GROUP_DELETED = "deleteGroup_deleted";
    private static final String MSG_GROUP_CREATED = "createGroup_success";
    private static final String MSG_GROUP_EDITED = "editGroup_success";
    private static final String MSG_FAILED_NO_PERMISSION = "createGroup_failed_no_permission";
    
    private static final String SQLERR_NO_PERMISSION = "HuTriggerEx0004";
    
    @ManagedProperty("#{groupBean}")
    private GroupBean groupBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{sessionData}")
    private SessionData sessionData;

    @ManagedProperty("#{userListBean}")
    private UserListBean userListBean;

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
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
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
     * Set {@link UserListBean} using injection.
     * 
     * @param userListBean
     *            to inject.
     */
    public void setUserListBean(UserListBean userListBean) {
        this.userListBean = userListBean;
    }

    /**
     * Save the id of the {@link Group} from the viewparam to a hidden field.
     * 
     * @return always null.
     */
    public String recoverId() {
        Integer groupId = null;
        if (groupBean.getId() != null) {
            groupId = groupBean.getId();
        } else if (groupBean.getHiddenGroupIdValue() != null) {
            try {
                groupId = Integer.parseInt((String) groupBean.getHiddenGroupIdValue());
            } catch (NumberFormatException ignored) {}
        }
        groupBean.setId(groupId);
        groupBean.setHiddenGroupIdValue(groupId);
        return null;
    }

    /**
     * Create a new {@link Group} in the database with the information provided
     * in the {@link GroupBean}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor} have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String createGroup() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();

        Group group = new Group(null);
        group.setTitle(groupBean.getTitle());
        group.setVisible(groupBean.isVisible());

        // Check if user is at least tutor
        if (sessionData.getUserId() != null
                && userStore.getById(sessionData.getUserId()).getUserType()
                        .getLevel() >= EUserType.Tutor.getLevel()) {
            try {
                groupBean.setId(groupStore.create(group,
                        sessionData.getUserId()).getId());
            } catch (InconsistencyException e) {
                if (e.getMessage().startsWith(SQLERR_NO_PERMISSION)) {
                    this.sendGlobalMessageToFacelet(this
                            .getCommonMessage(MSG_FAILED_NO_PERMISSION));
                    return null;
                } else {
                    throw new RuntimeException(e);
                }
            }
        } else {
            this.redirectTo(FACELET_RESTRICTION_ERROR);
            return null;
        }
        
        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash()
                    .setKeepMessages(true);
        }
        
        this.sendMessageTo(LIST_GROUPS_TABLE,
                this.getCommonMessage(MSG_GROUP_CREATED));

        return FACELET_LIST_GROUPS;
    }

    /**
     * Edit an existing {@link Group} in the database.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Group}, have
     * permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String editGroup() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();

        Group group = new Group(groupBean.getId());
        group.setTitle(groupBean.getTitle());
        group.setVisible(groupBean.isVisible());

        List<Group> ownedGroups = null;
        if (userStore.findById(sessionData.getUserId()) != null) {
            ownedGroups = groupStore.getByOwner(sessionData.getUserId());
        }
        
        // Check if user is administrator or owner of the group
        if (groupBean.getId() != null
                && ((ownedGroups != null && ownedGroups.contains(group)) || userStore
                        .getById(sessionData.getUserId()).getUserType() == EUserType.Administrator)) {
            FacesContext cxt = this.getCurrentFcInstance();

            if (!cxt.isPostback() || cxt.isValidationFailed()) {
                group = groupStore.findById(groupBean.getId());
                
                // Redirect if group does not exist
                if (group == null) {
                    this.redirectTo(FACELET_RESTRICTION_ERROR);
                    return null;
                }

                groupBean.setTitle(group.getTitle());
                groupBean.setVisible(group.isVisible());

                // Prepopulate the tutors of the group
                List<User> users = persistenceProvider.getUserStore()
                        .getByOwnership(groupBean.getId());
                List<UserListEntryBean> tutors = new LinkedList<UserListEntryBean>();

                for (User u : users) {
                    UserListEntryBean entry = new UserListEntryBean();
                    entry.setUser(u);
                    tutors.add(entry);
                }

                userListBean.setList(tutors);
            } else {
                try {
                    groupStore.save(group);

                    if (FacesContext.getCurrentInstance() != null) {
                        FacesContext.getCurrentInstance().getExternalContext().getFlash()
                                .setKeepMessages(true);
                    }
                    
                    this.sendMessageTo(LIST_GROUPS_TABLE,
                            this.getCommonMessage(MSG_GROUP_EDITED));
                } catch (InconsistencyException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            this.redirectTo(FACELET_RESTRICTION_ERROR);
            return null;
        }

        return FACELET_LIST_GROUPS;
    }

    /**
     * Delete an {@link Group} from the database.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Group}, have
     * permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String deleteGroup() {

        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IVideoStore videoStore = this.persistenceProvider.getVideoStore();
        
        
        User user = userStore.getById(sessionData.getUserId());
        //check if the id is null
        if(groupBean.getId() == null) {
            groupBean.setId(-10);
        }
        Group group = groupStore.findById(groupBean.getId());
        FacesContext fctx = this.getCurrentFcInstance();
        
        //check if the id was correct
        if (!fctx.isPostback()) {
            if (group == null || !(user.getUserType().equals(EUserType.Administrator)
                || (groupStore.getByOwner(user.getId()).contains(group)))) {
                this.redirectTo(FACELET_RESTRICTION_ERROR);
                return null;
            }
            
            //set group title for displaying
            this.groupBean.setTitle(group.getTitle());
            
            return null;
        }

        if (user.getUserType().equals(EUserType.Administrator)
                || (groupStore.getByOwner(user.getId()).contains(group))) {
        	List<Video> videos = videoStore.getForGroup(groupBean.getId());
        	for(Iterator<Video> it = videos.iterator(); it.hasNext(); ){
        		File videoArchive = new File(VIDEO_DESTINATION + it.next().getId() + "/");
                try {
					FileUtils.deleteDirectory(videoArchive);
				} catch (IOException ignored) {
					// ignore
				}
        	}
        	try {
                groupStore.delete(groupBean.getId());
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(
                        this.getCommonMessage(MSG_GROUP_DELETED));
                return FACELET_LIST_GROUPS;
            }

            this.sendMessageTo(LIST_GROUPS_TABLE,
                    this.getCommonMessage(MSG_GROUP_DELETED));

            return FACELET_LIST_GROUPS;
        }
        
        return FACELET_RESTRICTION_ERROR;
    }
}
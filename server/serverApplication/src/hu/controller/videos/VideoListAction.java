package hu.controller.videos;

import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.videos.VideoListBean;
import hu.backingbeans.videos.VideoListBean.VideoListEntryBean;
import hu.controller.AController;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.util.Configuration;
import hu.util.SessionData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * This class provides the functionality to display a list of
 * {@link Video}s.
 */
@ManagedBean
@RequestScoped
public class VideoListAction extends AController {
    private static final String CFG_MAX_ROWS = "max_rows_per_table";
    
    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
    
    private static final String MSG_TIME_FORMAT = "time_format";
    private static final String MSG_YES = "yes";
    private static final String MSG_NOT_STARTED_YET = "videotable_not_started_yet";
    private static final String MSG_STARTS = "videotable_starts";
    private static final String MSG_ENDS = "videotable_ends";
    private static final String MSG_ENDED = "videotable_ended";

    @ManagedProperty("#{groupBean}")
    private GroupBean groupBean;

    @ManagedProperty("#{configuration}")
    private Configuration configuration;
    
    @ManagedProperty("#{sessionData}")
    private SessionData sessionData;
    
    @ManagedProperty("#{videoListBean}")
    private VideoListBean videoListBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

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
     * Set {@link Configuration} using injection.
     * 
     * @param configuration
     *            to inject.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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
     * Set {@link VideoListBean} using injection.
     * 
     * @param videoListBean
     *            to inject.
     */
    public void setVideoListBean(
            VideoListBean videoListBean) {
        this.videoListBean = videoListBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(
            IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Retrieve a list of {@link Video}s that are visible for a certain
     * user.
     * 
     * All {@link EUserType}s of users have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String listVideosForUser() {

        IVideoStore qs = this.persistenceProvider
                .getVideoStore();
        IGroupStore es = this.persistenceProvider.getGroupStore();
        IUserStore us = this.persistenceProvider.getUserStore();

        List<Video> videos;
        double videosCount;

        // Check if groupId is set and bigger than null because of number
        // conversion
        Group group = null;
        if (this.groupBean != null && this.groupBean.getId() != null
                && this.groupBean.getId() > 0) {
            group = es.findById(this.groupBean.getId());

            // Check if the specified group exists
            if (group == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        } else if (this.groupBean != null && this.groupBean.getId() != null
                && this.groupBean.getId() == 0) {

            // Set id in group bean to null if 0 is set because of empty get
            // parameter
            this.groupBean.setId(null);
        }

        // Check if user is logged in and get user data if he is
        User currentUser = null;
        if (this.sessionData != null && this.sessionData.getUserId() != null) {
            currentUser = us.getById(this.sessionData.getUserId());
        }

        // Get users with the specified user type, ordered in the specified
        // direction for the current page of the table
        videos = qs.getAll(
                currentUser,
                group,
                this.videoListBean.getSortColumn(),
                this.videoListBean.getSortDirection(),
                this.videoListBean.getType(),
                this.videoListBean.getPage()
                        * this.configuration.getInteger(CFG_MAX_ROWS),
                this.configuration.getInteger(CFG_MAX_ROWS));

        // Get the amount of users with the specified user type
        videosCount = qs.getCountOfAll(currentUser, group,
                this.videoListBean.getType());

        // Get number of pages starting with 0
        int pages = (int) Math.ceil(videosCount
                / this.configuration.getInteger(CFG_MAX_ROWS));
        this.videoListBean.setPages(pages);

        // Set entries of video list
        this.setVideoListEntries(videos, currentUser);

        // Set create button if user is owner of group or administrator
        if (currentUser != null
                && currentUser.getUserType() == EUserType.Administrator) {
            this.videoListBean.setCreateButtonAvailable(true);
        } else if (currentUser != null
                && currentUser.getUserType() == EUserType.Tutor) {
            if (group != null) {
                int[] groupIds = { group.getId() };
                if (us.getUsersOwningGroups(groupIds).get(group.getId())
                        .contains(currentUser)) {
                    this.videoListBean.setCreateButtonAvailable(true);
                }                
            } else {
                this.videoListBean.setCreateButtonAvailable(true);
            }
        }

        return null;
    }
    
    /**
     * Set the entries and buttons for list of {@link Quesetionnaire}s for the
     * specified {@link User}.
     * 
     * @param videos
     *            to set.
     * @param currentUser
     *            to set.
     */
    private void setVideoListEntries(
            List<Video> videos, User currentUser) {

        IGroupStore es = this.persistenceProvider.getGroupStore();
        IUserStore us = this.persistenceProvider.getUserStore();

        List<VideoListEntryBean> list = 
                new ArrayList<VideoListEntryBean>();

        // Get the group for each video in list
        Map<Video, Group> groups = es
                .getForVideos(videos);
        
        // Get the owners for each video in list
        Map<Group, List<User>> owners = us
                .getUsersOwningGroups(new ArrayList<Group>(new HashSet<Group>(groups.values())));

        // Add every video from result to bean list and set available
        // buttons
        for (Iterator<Video> it = videos.iterator(); it
                .hasNext();) {
            VideoListEntryBean entry = new VideoListEntryBean();
            Video video = it.next();
            Group group = groups.get(video);

            // Set video and group in list entry
            entry.setVideo(video);
            entry.setGroup(group);

            Date currentDate = new Date();

            String active;
            // Check if video is active and set current status
            if (video.getStart() != null
                    && currentDate.compareTo(video.getStart()) > 0
                    && (video.getStop() == null || currentDate
                            .compareTo(video.getStop()) < 0)) {

                if (video.getStop() != null) {
                    active = this.getCommonMessage(MSG_ENDS)
                            + " "
                            + new SimpleDateFormat(
                                    this.getCommonMessage(MSG_TIME_FORMAT))
                                    .format(video.getStop());
                } else {
                    active = this.getCommonMessage(MSG_YES);
                }
            } else {
                if (video.getStart() == null) {
                    active = this.getCommonMessage(MSG_NOT_STARTED_YET);
                } else if (currentDate.compareTo(video.getStart()) < 0) {
                    active = this.getCommonMessage(MSG_STARTS)
                            + " "
                            + new SimpleDateFormat(
                                    this.getCommonMessage(MSG_TIME_FORMAT))
                                    .format(video.getStart());
                } else {
                    active = this.getCommonMessage(MSG_ENDED);
                }
            }

            entry.setActive(active);

            // Set available Buttons for Owners and Administrators
            if (currentUser != null
                    && (currentUser.getUserType() == EUserType.Administrator 
                    || owners.get(groups.get(video)).contains(
                                    currentUser))) {
                entry.setStartButtonAvailable(video.getStart() == null
                        || currentDate.compareTo(video.getStart()) < 0
                        || (video.getStop() != null && currentDate
                        .compareTo(video.getStop()) > 0));
                entry.setStopButtonAvailable(video.getStart() != null
                        && currentDate.compareTo(video.getStart()) > 0
                        && (video.getStop() == null || currentDate
                                .compareTo(video.getStop()) < 0));
                entry.setEditButtonAvailable(true);
                entry.setDeleteButtonAvailable(true);
            }
            
            // Set participation and download buttons if user is allowed to participate
            // and download
            if ((hasCorrectRoleForParticipation(currentUser, group,
            		video.getParticipationRestriction())
                    && video.getStart() != null
                    && currentDate.compareTo(video.getStart()) > 0
                    && (video.getStop() == null || currentDate
                            .compareTo(video.getStop()) < 0))
                    || (currentUser != null && (owners.get(
                            groups.get(video)).contains(currentUser)))) {
                entry.setParticipationButtonAvailable(true);
                
                if(video.isZipDownloadEnabled()){
                    entry.setZipDownloadButtonAvailable(true);
                }
                
                if(video.getChromeAppURL() != null && !video.getChromeAppURL().equals("")){
                    entry.setChromeAppDownloadButtonAvailable(true);
                }
            }

            // Add entry to list
            list.add(entry);
        }

        this.videoListBean.setList(list);
    }

    /**
     * Check if the specified {@link User} is allowed to participate. The
     * {@link Group} is required for groupAttendance checks.
     * 
     * @param user
     *            to set.
     * @param group
     *            to set.
     * @param restriction
     *            to set.
     * @return true if the specified {@link User} is allowed to participate or
     *         false if not.
     */
    private boolean hasCorrectRoleForParticipation(User user, Group group,
            EParticipationRestriction restriction) {
        
    	IUserStore us = this.persistenceProvider.getUserStore();

        // Check if current user does not met participation restrictions
    	if(user != null){
    		if (restriction == EParticipationRestriction.Registered || user.getUserType().equals(EUserType.Administrator)) {
    		    return true;
    		} else if (restriction == EParticipationRestriction.GroupAttendants) {
    	            Set<User> attendants = new HashSet<User>(us.getByAttendance(group));
    	            for(Iterator<User> it = attendants.iterator(); it.hasNext(); ){
    	        	User attendant = it.next();
    	        	if((int)user.getId() == (int)attendant.getId()){
    	        	    return true;
    	        	}
    	            }
    	        }
    		
    		// Check if user is owner of the appropriate group
    		Set<User> owners = new HashSet<User>(us.getByOwnership(group));  
    		if(owners.contains(user)){
    			return true;
    		}
    	}
    	else if (restriction == EParticipationRestriction.Public
                || restriction == EParticipationRestriction.Password
                || restriction == EParticipationRestriction.Token) {
            return true;            
        }    
    	
        return false;
    }
}
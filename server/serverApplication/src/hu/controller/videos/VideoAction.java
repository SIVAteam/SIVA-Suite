package hu.controller.videos;

import hu.backingbeans.common.TokenBean;
import hu.backingbeans.videos.VideoBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.controller.AController;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Token;
import hu.model.Video;
import hu.model.api.SivaPlayerLogEntry;
import hu.model.api.SivaPlayerSession;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.ITokenStore;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.persistence.InconsistencyException;
import hu.util.CommonUtils;
import hu.util.Configuration;
import hu.util.SecurityUtils;
import hu.util.SessionData;
import hu.util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;

/**
 * This class provides the functionality to create, edit, duplicate and delete a
 * {@link Video} as well as the ability to start or stop it. Furthermore
 * it provides the functionality to participate in a {@link Video}.
 */
@ManagedBean
@RequestScoped
public class VideoAction extends AController {
	
	private static final String VIDEO_DESTINATION =  System.getProperty("user.home")
            + "/.sivaServer/videos/";

	private static final String LIST_VIDEO_FACELET = "/xhtml/videos/listVideos";
	private static final String EDIT_VIDEO_FACELET = "/xhtml/videos/editVideo";
	
	private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
	private static final String ACCESS_VIDEO_FACELET = "/xhtml/videos/accessVideo";
	private static final String WATCH_VIDEO_FACELET = "/xhtml/videos/watchVideo";

	private static final String MSG_VIDEO_STARTED_SUCCESS = "video_started";
	private static final String MSG_VIDEO_START_AT = "video_start_at";
	private static final String MSG_VIDEO_STOPPED_SUCCESS = "video_stopped";
	private static final String MSG_VIDEO_STOPPED_FAILURE = "video_stopped_negative";
	private static final String MSG_VIDEO_DELETED_SUCCESS = "listVideos_video_deleted";
	private static final String MSG_VIDEO_NOT_YET_STARTED = "video_not_yet_started";
	private static final String MSG_VIDEO_ALREADY_STOPPED = "video_has_stopped";
	private static final String MSG_VIDEO_PASSWORD_NOT_CORRECT = "video_password_not_correct";
	private static final String MSG_VIDEO_ERROR = "uploadValidator_message";
	private static final String MSG_SIVA_ERROR = "video_siva_missing";
	private static final String MSG_NO_VIDEO_ARCHIVE = "startVideo_no_video_archive";

	private static final String MSG_TOKEN_NOT_FOUND = "token_not_found";
	private static final String MSG_TOKEN_NOT_VALID_PARTICIPATION = "token_not_valid_paticipation";
	private static final String MSG_TOKEN_NOT_VALID_VIDEO = "token_not_valid_video";

	private static final String MSG_NO_PASSWORD = "editPublicationSettings_password_required";
	private static final String MSG_EDIT_SUCCESS = "editVideo_success";

	private static final String MSG_CREATE_SUCCESS = "createVideo_success";
	private static final String MSG_CREATE_FAILED = "createVideo_failed";
	private static final String MSG_NO_PERMISSION = "no_permission";

	private static final String LIST_VIDEO_TABLE = "listVideosTable";
	private static final String CREATE_VIDEO_FORM = "createVideoForm";
	private static final String PASSWORD_FIELD = "accessForm:password";
	private static final String TOKEN_FIELD = "accessForm:token";

	private static final String APPEND_GET_PARAMETER = "?faces-redirect=true";
	private static final String APPEND_VIDEO_ID = "&videoId=";

	private static final String SQLERR_NO_PASSWORD = "HuConstraint0005";
    
    @ManagedProperty("#{videoBean}")
    private VideoBean videoBean;

    @ManagedProperty("#{videoPublicationBean}")
    private VideoPublicationBean videoPublicationBean;

    @ManagedProperty("#{tokenBean}")
    private TokenBean tokenBean;

    @ManagedProperty("#{sessionData}")
    private SessionData sessionData;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;
    
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    private File mockedVideoFile = null;

    /**
     * Set {@link VideoBean} using injection.
     * 
     * @param videoBean
     *            to inject.
     */
    public void setVideoBean(VideoBean videoBean) {
        this.videoBean = videoBean;
    }

    /**
     * Set {@link VideoPublicationBean} using injection.
     * 
     * @param VideoPublicationBean
     *            to inject.
     */
    public void setVideoPublicationBean(
            VideoPublicationBean videoPublicationBean) {
        this.videoPublicationBean = videoPublicationBean;
    }

    /**
     * Set {@link TokenBean} using injection.
     * 
     * @param TokenBean
     *            to inject.
     */
    public void setTokenBean(TokenBean tokenBean) {
        this.tokenBean = tokenBean;
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
     * Create a new {@link Video} in the database with the information
     * provided in the {@link VideoBean}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor} have permission to use.
     * 
     * @return the next page to show as {@link String}.
     * 
     */
    public String createVideo() {
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();

        // Get the list of owned groups
        List<Group> ownedGroups = null;
        if (sessionData.getUserId() != null
                && userStore.findById(sessionData.getUserId()) != null) {
            ownedGroups = groupStore.getByOwner(sessionData.getUserId());
        }
        Group group = new Group(videoBean.getGroupId());

        // Redirect if user has no permission to create the video
        if (sessionData.getUserId() == null
                || (!(ownedGroups != null && ownedGroups.contains(group)) && userStore
                        .getById(sessionData.getUserId()).getUserType() != EUserType.Administrator)) {
        	this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Create {@link Video} from input
        Video video = new Video(null);
        video.setTitle(videoBean.getTitle());
        video.setDescription(videoBean.getDescription());
        video.setParticipationRestriction(videoBean
                .getParticipationRestriction());
        video.setAuthorId(sessionData.getUserId());
        
        // Create directory key for video and make sure that there is
        // not another video using the same direcotry
        String directory = null;
        Video tmpVideo = null;
        do{
        	directory = SecurityUtils.randomString(15);
        	tmpVideo = videoStore.findByDirectory(directory);
        }
        while(tmpVideo != null);
        video.setDirectory(directory);

        try {
            video = videoStore.create(video,
                    videoBean.getGroupId());
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_CREATE_FAILED));
            return "";
        }

        videoBean.setId(video.getId());
        
        this.sendMessageTo(CREATE_VIDEO_FORM, 
        		this.getCommonMessage(MSG_CREATE_SUCCESS));
        return EDIT_VIDEO_FACELET + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                + video.getId();
    }

    /**
     * Edit an existing {@link Video} in the database.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * @return the next page to show as {@link String}.
     * 
     */
    public String editVideo() {

        // Get the needed stores
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();

        // Recover lost id after validation error
        Integer qid = null;
        if (videoBean.getId() != null) {
            qid = videoBean.getId();
        } else if (videoBean.getHiddenValue() != null) {
            qid = Integer.parseInt((String) videoBean.getHiddenValue());
        }
        videoBean.setHiddenValue(qid);
        videoBean.setId(qid);
        
        // Redirect if parameter is missing
        if (videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        FacesContext cxt = this.getCurrentFcInstance();

        Video video = videoStore
                .findById(videoBean.getId());
        
    	// Get the list of owned groups
        List<Group> ownedGroups = null;
        if (sessionData.getUserId() != null
                && userStore.findById(sessionData.getUserId()) != null) {
            ownedGroups = groupStore.getByOwner(sessionData.getUserId());
        }
        Group group = video == null ? null : groupStore
                .getForVideo(video);

        /*
         * Redirect, if there is no video with the given id or the user
         * is not an owner or administrator
         */
        if (video == null
                || ((ownedGroups != null && !ownedGroups.contains(group)) && userStore
                        .getById(sessionData.getUserId()).getUserType() != EUserType.Administrator)) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // Decide which fields to show based on the video's type.
        this.videoBean.setPasswordAvailable(
                video.getParticipationRestriction() == EParticipationRestriction.Password);
        this.videoBean.setTokenAvailable(video.getParticipationRestriction() == EParticipationRestriction.Token);
        
        // Prepopulate the restrictions
        this.videoBean.setParticipationRestriction(video
                .getParticipationRestriction());
        
        this.videoBean.setVideoAvailable((new File(VIDEO_DESTINATION + "/" + video.getDirectory() + "/video.zip")).exists());

        if (!cxt.isPostback()) {
            this.videoBean.setTitle(video.getTitle());
            this.videoBean.setGroupId(group.getId());
            this.videoBean.setDescription(video.getDescription());
            this.videoBean.setPassword(video.getPassword());
            this.videoBean.setStart(video.getStart());
            this.videoBean.setStop(video.getStop());    
            this.videoBean.setZipDownloadEnabled(video.isZipDownloadEnabled());
            this.videoBean.setChromeAppURL(video.getChromeAppURL());
        } else if (cxt.isPostback() && !cxt.isValidationFailed()) {
            
            // Set values for saving
            video.setTitle(videoBean.getTitle());
            video.setDescription(videoBean.getDescription());
            
            if(this.videoBean.getParticipationRestriction().equals(EParticipationRestriction.Password)) {
                video.setPassword(this.videoBean.getPassword());
            }
            
            if(this.videoBean.getStart() != null && this.videoBean.getVideo() == null && !this.videoBean.isVideoAvailable()){
            	this.sendGlobalMessageToFacelet(this
						.getCommonMessage(MSG_NO_VIDEO_ARCHIVE));
				return null;
            }
            
            video.setStart(this.videoBean.getStart());
            video.setStop(this.videoBean.getStop());
            
            video.setZipDownloadEnabled(this.videoBean.isZipDownloadEnabled());
            video.setChromeAppURL(this.videoBean.getChromeAppURL());

            // Move uploaded video if there has been uploaded one or if it's a unit test
            if (this.videoBean.getVideo() != null || this.mockedVideoFile != null) {
        	String message = this.saveVideoArchive(video);
        	if (message != null) {
        	    this.sendGlobalMessageToFacelet(this.getCommonMessage(message));
        	    return null;
        	}
        		        
        	// Check if it's a unit test and get the size of the archive
        	if(this.mockedVideoFile == null){
        	    video.setSize(this.videoBean.getVideo().getSize());
        	}
        	else{
        	    video.setSize(this.mockedVideoFile.length());
        	}
            }
                    	
            try {
                video = videoStore.save(video);
                videoBean.setId(video.getId());
                videoStore.moveVideo(video.getId(), -1,
                        videoBean.getGroupId());

                if (FacesContext.getCurrentInstance() != null) {
                    FacesContext.getCurrentInstance().getExternalContext()
                            .getFlash().setKeepMessages(true);
                }

                this.sendMessageTo(LIST_VIDEO_TABLE,
                        this.getCommonMessage(MSG_EDIT_SUCCESS));
            } catch (InconsistencyException e) {
                throw new RuntimeException();
            }           
        }        
        return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
    }
    
    /**
     * Delete a {@link Video} from the database.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use. *
     * 
     * @return the next page to show as {@link String}.
     */
    public String deleteVideo() {

        IGroupStore es = this.persistenceProvider.getGroupStore();
        IUserStore us = this.persistenceProvider.getUserStore();
        IVideoStore qqs = this.persistenceProvider
                .getVideoStore();

        // Check if a videoId was set and redirect if not
        if (this.videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Check if video exists and redirect if not
        Video video = qqs.findById(this.videoBean
                .getId());
        if (video == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Get group of video
        Group group = es.getForVideo(video);

        // Get users currently owning the group of the video
        Set<User> groupOwners = new HashSet<User>(us.getByOwnership(group));

        // Check if current user is allowed to use this page
        User currentUser = us.getById(this.sessionData.getUserId());
        if (currentUser.getUserType() != EUserType.Administrator) {

            // Check if current User is group owner and redirect if not
            if (!groupOwners.contains(currentUser)) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        }

        // Set video title for displaying
        this.videoBean.setTitle(video.getTitle());
        
        // Check if post and delete video and all its associated data
        // recursively
        if (this.getCurrentFcInstance().isPostback()) {
            try {
                qqs.delete(video.getId());
                File videoArchive = new File(VIDEO_DESTINATION + video.getDirectory() + "/");
                FileUtils.deleteDirectory(videoArchive);
            } catch (InconsistencyException ignored) {
            	// ignore
            } catch (IOException ignored) {
				// ignore
			}

            this.sendMessageTo(LIST_VIDEO_TABLE,
                    this.getCommonMessage(MSG_VIDEO_DELETED_SUCCESS));
            return LIST_VIDEO_FACELET;
        }

        return null;
    }

    /**
     * Start a {@link Video} independently from a previous set starting
     * time.
     * 
     * @return the next page to show as {@link String}.
     * 
     * @throws IOException 
     */
    public String startVideo() throws IOException {

	IGroupStore es = this.persistenceProvider.getGroupStore();
        IUserStore us = this.persistenceProvider.getUserStore();
        IVideoStore qqs = this.persistenceProvider
                .getVideoStore();

        // Check if a videoId was set and redirect if not
        if (this.videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // Check if video exists and redirect if not
        Video video = qqs.findById(this.videoBean
                .getId());
        if (video == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Get group of video
        Group group = es.getForVideo(video);

        // Get users currently owning the group of the video
        Set<User> groupOwners = new HashSet<User>(us.getByOwnership(group));

        // Check if current user is allowed to use this page
        User currentUser = us.getById(this.sessionData.getUserId());
        if (currentUser.getUserType() != EUserType.Administrator) {

            // Check if current User is group owner and redirect if not
            if (!groupOwners.contains(currentUser)) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
        }
	
	
        FacesContext fcxt = this.getCurrentFcInstance();
        
        if(!(new File(VIDEO_DESTINATION + video.getDirectory())).exists()){
        	if (FacesContext.getCurrentInstance() != null) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash()
                        .setKeepMessages(true);
        	}
        	this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_NO_VIDEO_ARCHIVE));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        }
        

        // set video title for displaying
        this.videoBean.setTitle(video.getTitle());
        
        // is prepopulation needed? id is needed in the bean
        if (!fcxt.isPostback() && !fcxt.isValidationFailed()) {
            this.populateVideoBean(video);
        } else if (fcxt.isPostback() && fcxt.isValidationFailed()) {
            return null;
        } else {
            
        	// set end to null if there is already an end date set
        	if(video.getStop() != null){
        		video.setStop(null);
        	}
        	
            // set now as start
            video.setStart(new Date());
            
            //redirect to list videos after both success or failure
            this.redirectTo(LIST_VIDEO_FACELET);
            
            //store to database
            try {
                qqs.save(video);

                this.sendMessageTo(LIST_VIDEO_TABLE, this
                        .getCommonMessage(MSG_VIDEO_STARTED_SUCCESS));

            } catch (InconsistencyException e) {
                //redirect to list video facelet in all cases
                this.redirectTo(LIST_VIDEO_FACELET);

                //video has to contain a password if it has appropriate publish function
                if (e.getMessage().contains(SQLERR_NO_PASSWORD)) {
                    this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_NO_PASSWORD));
                    return null;
                }

                // Error could not be handled.
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    /**
     * Stop a {@link Video} independently from a previous set ending
     * time.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} and
     * {@link EUserType#Tutor}, who are owners of the {@link Video},
     * have permission to use.
     * 
     * 
     * @return the next page to show as {@link String}.
     */
    public String stopVideo() {

        FacesContext fcxt = this.getCurrentFcInstance();
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        Video video = null;
        
        if (videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        video = videoStore.findById(videoBean.getId());
        
        if (video == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        User currentUser = userStore.getById(this.sessionData.getUserId());

        // check if user is owner of video or administrator
        if (currentUser.getUserType() != EUserType.Administrator
                && !userStore.isUserOwnerOfVideo(currentUser.getId(),
                        this.videoBean.getId())) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_NO_PERMISSION));
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // set video title for displaying
        this.videoBean.setTitle(video.getTitle());
        
        // is prepopulation needed? id is needed in the bean
        if (!fcxt.isPostback() && !fcxt.isValidationFailed()) {
            this.populateVideoBean(video);
        } else if (fcxt.isPostback() && fcxt.isValidationFailed()) {
            return null;
        } else {

            // set now as stop
            video.setStop(new Date());
            try {
                persistenceProvider.getVideoStore().save(video);

                this.sendMessageTo(LIST_VIDEO_TABLE, this
                        .getCommonMessage(MSG_VIDEO_STOPPED_SUCCESS));
            } catch (InconsistencyException e) {

                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_VIDEO_STOPPED_FAILURE));
            }
        }
        return LIST_VIDEO_FACELET;
    }

    /**
     * Populate the videoBean with data from a video.
     * 
     * @param video
     *            with data
     * 
     */
    private void populateVideoBean(Video video) {
        this.videoBean.setId(video.getId());
        this.videoBean.setTitle(video.getTitle());
        this.videoBean.setDescription(video.getDescription());
        this.videoBean.setParticipationRestriction(video
                .getParticipationRestriction());
    }

    /**
     * Participate in a {@link Video}. Possible restrictions for a
     * {@link Video} are defined in {@link EParticipationRestriction}
     * 
     * @return the next page to show as {@link String}.
     * 
     */
    public String accessVideo() {

        // set variables that are needed in all restriction types
        FacesContext fcxt = this.getCurrentFcInstance();
        IVideoStore qStore = this.persistenceProvider
                .getVideoStore();
        ITokenStore tStore = this.persistenceProvider.getTokenStore();
        IUserStore uStore = this.persistenceProvider.getUserStore();
        
        // Recover lost id after validation error
        Integer qid = null;
        if (videoBean.getId() != null) {
            qid = videoBean.getId();
        } else if (videoBean.getHiddenValue() != null) {
            qid = Integer.parseInt((String) videoBean.getHiddenValue());
        }
        videoBean.setHiddenValue(qid);
        videoBean.setId(qid);
    	
        Video video = null;
        
        // Check if video id is set in URL
        if(this.videoBean.getId() != null){
        	
        	// get video data from database for specified video id
        	video = qStore.findById(this.videoBean.getId());
        }
        
        // Check if token is set in URL
        else if (this.tokenBean.getToken() != null){
        	
        	// get the video that belongs to this token
        	video = qStore.findByToken(this.tokenBean.getToken().getToken());        	
        }
        
        // Verify that a video id is provided.
        if (video == null || video.getId() == null) {
            
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        if(this.sessionData.getUserId() != null){
        	User user = uStore.findById(this.sessionData.getUserId());
        	if(user.getUserType().equals(EUserType.Administrator) || uStore.isUserOwnerOfVideo(user.getId(), video.getId())) {
        		this.redirectTo(WATCH_VIDEO_FACELET
                        + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                        + video.getId());
        		return null;
            }
        }
        
        // Set needed form fields
		if (video.getParticipationRestriction() == EParticipationRestriction.Password) {
			this.videoBean.setPasswordAvailable(true);
			this.videoBean.setTokenAvailable(false);
		} else if (video.getParticipationRestriction() == EParticipationRestriction.Token) {
			this.videoBean.setTokenAvailable(true);
			this.videoBean.setPasswordAvailable(false);
		}
		
		// if no post then just populate beans
		if (!fcxt.isPostback()
				&& tokenBean.getToken() == null
				&& (video.getParticipationRestriction() == EParticipationRestriction.Password
				|| video.getParticipationRestriction() == EParticipationRestriction.Token)) {
			return null;
		}

        // verify that video has started and has not stopped yet
        Date currentDate = new Date();
        if (video.getStart() == null
                || video.getStart().compareTo(currentDate) > 0) {

            // video has not yet started
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_NOT_YET_STARTED),
                    video.getTitle())
                    + " " + (video.getStart() == null ? "" : String.format(
                            this.getCommonMessage(MSG_VIDEO_START_AT),
                            video.getStart())));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        } else if (video.getStop() != null
                && video.getStop().compareTo(currentDate) < 0) {

            // video has stopped
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_ALREADY_STOPPED),
                    video.getTitle()));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        }
        
        if (video.getParticipationRestriction() == EParticipationRestriction.Password) {

            // verify that password is correct
            if (video.getPassword() == null || !video.getPassword().equals(
                    videoBean.getPassword())) {

                this.sendMessageTo(PASSWORD_FIELD, this
                        .getCommonMessage(MSG_VIDEO_PASSWORD_NOT_CORRECT));                
            }
            else{
            	
            	this.redirectTo(WATCH_VIDEO_FACELET
                        + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                        + video.getId() + "&password=" +  SecurityUtils.hash(video.getPassword()));
            }
        } else if (video.getParticipationRestriction() == EParticipationRestriction.Token) {
        	
        	// check if there was a token submitted 
        	Token token = null;   	
        	if (tokenBean.getToken() != null) {
                token = tStore.find(tokenBean.getToken().getToken());
                
                // check if token is available
                if (token == null) {
                	this.sendMessageTo(TOKEN_FIELD, this
                            .getCommonMessage(MSG_TOKEN_NOT_FOUND));
                }
                else if (token != null && !token.isForParticipation()) {
                	
                	// check if token is valid for participating in Video
                	this.sendMessageTo(TOKEN_FIELD, this
                            .getCommonMessage(MSG_TOKEN_NOT_VALID_PARTICIPATION));
                    this.redirectTo(ACCESS_VIDEO_FACELET);
                }
                else {
                	
                	// check if valid token belongs to specified video and redirect if so
                	Video tokenVideo = qStore.findByToken(token.getToken());
                	if(!tokenVideo.getId().equals(video.getId())){
                		this.sendMessageTo(TOKEN_FIELD, this
                                .getCommonMessage(MSG_TOKEN_NOT_VALID_VIDEO));
                	}
                	else{
                		
                		// redirect to video if everything is okay
                    	this.redirectTo(WATCH_VIDEO_FACELET
                                + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                                + video.getId() + "&token=" +  token.getToken());
                	}
                }
            }
        	else{
        		this.sendMessageTo(TOKEN_FIELD, this
                        .getCommonMessage(MSG_TOKEN_NOT_FOUND));
        	}
        } else if (video.getParticipationRestriction() == EParticipationRestriction.Registered) {

        	// check if user is logged in, redirect if not
            if (this.sessionData.getUserId() == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }
            
            // redirect to video
        	this.redirectTo(WATCH_VIDEO_FACELET
                    + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                    + video.getId());
        } else if (video.getParticipationRestriction() == EParticipationRestriction.GroupAttendants) {

        	// Check if user is logged in, redirect if not
            if (this.sessionData.getUserId() == null) {
                this.redirectTo(RESTRICTION_ERROR_FACELET);
                return null;
            }

            // check if user is not attendant of the group of the video
            // and if so redirect to error page
            if (!uStore.isUserAttendantOfGroup(this.sessionData.getUserId(), video.getId())) {
            	this.redirectTo(RESTRICTION_ERROR_FACELET);
            	return null;
            }
            
            // redirect to video
            this.redirectTo(WATCH_VIDEO_FACELET
                    + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                    + video.getId());
        } else if (video.getParticipationRestriction() == EParticipationRestriction.Public) {
        	// redirect to video
        	this.redirectTo(WATCH_VIDEO_FACELET
                    + APPEND_GET_PARAMETER + APPEND_VIDEO_ID
                    + video.getId());
        }
        return null;
    }
    
    /**
     * Watch an existing {@link Video}.
     * 
     * @return the next page to show as {@link String}.
     * 
     */
    public String watchVideo() {

        // Get the needed stores
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        
        // Redirect if parameter is missing
        if (videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        Video video = videoStore.findById(videoBean.getId());
        // Redirect if video does not exist
        if (video == null || video.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        User user = null;
        if(this.sessionData.getUserId() != null){
    		user = userStore.findById(this.sessionData.getUserId());
        }
        
        // verify that video has started and has not stopped yet
        if(user == null || (!user.getUserType().equals(EUserType.Administrator) && !userStore.isUserOwnerOfVideo(user.getId(), video.getId()))){
        Date currentDate = new Date();
        if (video.getStart() == null
                || video.getStart().compareTo(currentDate) > 0) {

            // video has not yet started
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_NOT_YET_STARTED),
                    video.getTitle())
                    + " " + (video.getStart() == null ? "" : String.format(
                            this.getCommonMessage(MSG_VIDEO_START_AT),
                            video.getStart())));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        } else if (video.getStop() != null
                && video.getStop().compareTo(currentDate) < 0) {

            // video has stopped
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_ALREADY_STOPPED),
                    video.getTitle()));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        }
        }
        
        // check if user is allowed to participate
        if(!isAllowedToParicipate(video)){
        	this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;      
        }
        
        // generate new SivaPlayerSession and provide session token for direct video access
        String token = SecurityUtils.randomString(70);
	SivaPlayerSession session = new SivaPlayerSession(null, token);
	if(sessionData.getUserId() != null){
		session.setUserId(sessionData.getUserId());
	}
	session.setVideoId(video.getId());
	session.setVideoVersion(video.getVersion());
	try {
		session = this.persistenceProvider.getApiStore().createSivaPlayerSession(session);
	} catch (InconsistencyException e) {
		this.redirectTo(RESTRICTION_ERROR_FACELET);
		return null;
	}
	
	// Log Client IP address
	ArrayList<SivaPlayerLogEntry> entries = new ArrayList<SivaPlayerLogEntry>();
	SivaPlayerLogEntry entry = new SivaPlayerLogEntry();
	entry.setSessionId(session.getId());
	entry.setType("getClientInformation");
	entry.setElement("webAccess");
	/*
	entry.setElement("ip");
	entry.setAdditionalInformation(((HttpServletRequest)this.getCurrentFcInstance().getExternalContext().getRequest()).getRemoteAddr());
	*/
	entry.setTime(new Date());
	entries.add(entry);
	try {
	    this.persistenceProvider.getApiStore().createSivaPlayerLogEntries(entries);
	} catch (InconsistencyException ignore) {
	    this.redirectTo(RESTRICTION_ERROR_FACELET);
	    return null;
	}

	if(user == null || (!user.getUserType().equals(EUserType.Administrator) && !userStore.isUserOwnerOfVideo(user.getId(), video.getId()))){
	    videoStore.increaseViews(video.getId());
	}
        
	this.videoBean.setTitle(video.getTitle());
	this.videoBean.setDescription(video.getDescription());
	this.videoBean.setDirectory(video.getDirectory());
	this.videoBean.setSivaPlayerSession(session);

	return null;
    }
    
    /**
     * Download an existing {@link Video} as zip file by redirecting to it.
     * 
     * @return always null.
     * 
     */
    public String downloadVideo() {

	System.out.println(2);
        // Get the needed stores
        IVideoStore videoStore = this.persistenceProvider
                .getVideoStore();
        IUserStore userStore = this.persistenceProvider.getUserStore();
        
        // Redirect if parameter is missing
        if (videoBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        Video video = videoStore.findById(videoBean.getId());
        // Redirect if video does not exist
        if (video == null || video.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        User user = null;
        if(this.sessionData.getUserId() != null){
    		user = userStore.findById(this.sessionData.getUserId());
        }
        
        // verify that video has started and has not stopped yet
        if(user == null || (!user.getUserType().equals(EUserType.Administrator) && !userStore.isUserOwnerOfVideo(user.getId(), video.getId()))){
        Date currentDate = new Date();
        if (video.getStart() == null
                || video.getStart().compareTo(currentDate) > 0) {

            // video has not yet started
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_NOT_YET_STARTED),
                    video.getTitle())
                    + " " + (video.getStart() == null ? "" : String.format(
                            this.getCommonMessage(MSG_VIDEO_START_AT),
                            video.getStart())));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        } else if (video.getStop() != null
                && video.getStop().compareTo(currentDate) < 0) {

            // video has stopped
            this.sendGlobalMessageToFacelet(String.format(
                    this.getCommonMessage(MSG_VIDEO_ALREADY_STOPPED),
                    video.getTitle()));
            return LIST_VIDEO_FACELET + APPEND_GET_PARAMETER;
        }
        }
        
        // check if user is allowed to participate
        if(!isAllowedToParicipate(video)){
        	this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;      
        }
        
        // generate new SivaPlayerSession and provide session token for direct video access
        String token = SecurityUtils.randomString(70);
	SivaPlayerSession session = new SivaPlayerSession(null, token);
	if(sessionData.getUserId() != null){
		session.setUserId(sessionData.getUserId());
	}
	session.setVideoId(video.getId());
	session.setVideoVersion(video.getVersion());
	try {
		session = this.persistenceProvider.getApiStore().createSivaPlayerSession(session);
	} catch (InconsistencyException e) {
		this.redirectTo(RESTRICTION_ERROR_FACELET);
		return null;
	}
	
	// Log download event
	ArrayList<SivaPlayerLogEntry> entries = new ArrayList<SivaPlayerLogEntry>();
	SivaPlayerLogEntry entry = new SivaPlayerLogEntry();
	entry.setSessionId(session.getId());
	entry.setType("downloadZip");
	entry.setElement("");
	entry.setAdditionalInformation("");
	entry.setTime(new Date());
	entries.add(entry);
	
	// Log Client IP address
	/*entry = new SivaPlayerLogEntry();
	entry.setSessionId(session.getId());
	entry.setType("getClientInformation");
	entry.setElement("ip");
	entry.setAdditionalInformation(((HttpServletRequest)this.getCurrentFcInstance().getExternalContext().getRequest()).getRemoteAddr());
	entry.setTime(new Date());
	entries.add(entry);*/
	try {
	    this.persistenceProvider.getApiStore().createSivaPlayerLogEntries(entries);
	} catch (InconsistencyException ignore) {
	    this.redirectTo(RESTRICTION_ERROR_FACELET);
	    return null;
	}

	if(user == null || (!user.getUserType().equals(EUserType.Administrator) && !userStore.isUserOwnerOfVideo(user.getId(), video.getId()))){
	    videoStore.increaseViews(video.getId());
	}
        
	this.videoBean.setDirectory(video.getDirectory());
	this.videoBean.setSivaPlayerSession(session);
	
	// Redirect to zip file or throw restriction error if an error occurs
	try {
	    FacesContext fc = FacesContext.getCurrentInstance();
	    fc.getExternalContext().redirect(CommonUtils.buildContextPath("/sivaPlayerVideos/" + video.getDirectory() + "/video.zip", "token=" + session.getSessionToken()));
	}  catch (MalformedURLException ignore) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        } catch (URISyntaxException ignore) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        } catch (IOException e) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
	}

	return null;
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
     * @throws  
     */
    private boolean isAllowedToParicipate(Video video) {
        
    	IUserStore us = this.persistenceProvider.getUserStore();
    	IGroupStore gs = this.persistenceProvider.getGroupStore();
    	
    	EParticipationRestriction restriction = video.getParticipationRestriction();
    	User user = null;
    	Group group = gs.getForVideo(video);
    	
    	if(this.sessionData.getUserId() != null){
    		user = us.findById(this.sessionData.getUserId());
    	}
    	
        // Check if current user does not met participation restrictions
    	if(user != null){
    		if (restriction == EParticipationRestriction.Registered || user.getUserType().equals(EUserType.Administrator)) {
                return true;
    		}
    		
    		// Check if user is owner of the appropriate group
    		Set<User> owners = new HashSet<User>(us.getByOwnership(group));  
    		if(owners.contains(user)){
    			return true;
    		}
    	}
    	if (restriction == EParticipationRestriction.Public) {
            return true;            
        } else if (restriction == EParticipationRestriction.Password) {
        	
        	// Check if encrypted video password is equal to the password in the URL
        	if(SecurityUtils.hash(video.getPassword()).equals(this.videoBean.getPassword())){
        		return true;
        	}
    	} else if (restriction == EParticipationRestriction.Token) {
    		IVideoStore vs = this.persistenceProvider.getVideoStore();
    		Video tokenVideo = vs.findByToken(this.tokenBean.getToken().getToken());
    		if(tokenVideo != null && video.getId().equals(tokenVideo.getId())){
    			return true;
    		}
        } else if (restriction == EParticipationRestriction.GroupAttendants) {
            Set<User> attendants = new HashSet<User>(us.getByAttendance(group));
            if (attendants.contains(user)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Save uploaded video archive to video directory. Extract archive in video directory and
     * keep copy of the archive. Create video directory if not exist.
     * 
     * @param video contains the video
     * @return null if the video was successfully saved, an error message otherwise.
     */
    private String saveVideoArchive(Video video) {
	File destination = new File(VIDEO_DESTINATION + "/" + video.getDirectory());
	try {
	    FileUtils.deleteDirectory(destination);
	} catch (IOException ignored) {
	    // ignore
	}
	destination.mkdir();
		
	File zipDestination = new File(destination.getAbsolutePath() + "/video" + ".zip");
	InputStream inputStream = null;
	OutputStream outputStream = null;
	try {
			
	    // Check if its a unit test and get the input stream
	    if(this.mockedVideoFile == null){
		inputStream = this.videoBean.getVideo().getInputStream();
	    }
	    else{
		inputStream = new FileInputStream(this.mockedVideoFile);
	    }
	    outputStream = new FileOutputStream(zipDestination);
	    int read = 0;
	    byte[] bytes = new byte[1024];
	    while ((read = inputStream.read(bytes)) != -1) {
		outputStream.write(bytes, 0, read);
	    }
	    ZipUtils.extract(zipDestination, destination);
	} catch (IOException e) {
	    return MSG_VIDEO_ERROR;
	} finally {
	    if (inputStream != null) {
		try {
		    inputStream.close();
		} catch (IOException ignore) {
		    // ignore
		}
	    }
	    if (outputStream != null) {
		try {
		    outputStream.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
		
	File sivaDirectory = new File(destination.getAbsolutePath() + "/SivaPlayer");
	File indexFile = new File(destination.getAbsolutePath() + "/index.html");
	
	// Check if SIVA directory and index.html exist if zip download is enabled
	if(this.videoBean.isZipDownloadEnabled() && (!sivaDirectory.exists() || !indexFile.exists())){
	    return MSG_SIVA_ERROR;
	}
		
	// Delete files not needed
	if(sivaDirectory.exists()){
	    try {
		FileUtils.deleteDirectory(sivaDirectory);
	    } catch (IOException ignore) {
		// ignore
	    }
	}
	if(indexFile.exists()){
	    indexFile.delete();
	}

	// Get created subdirectory containing all video files
	String[] directories = destination.list(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return new File(dir, name).isDirectory();
	    }
	});
		
	// Check if its only one directory and move all its files
	// and directories from to root video directory if so to put video structure
	// into root directory of the video without intermediate directories
	if(directories.length == 1){
	    for(String directory: directories){
		File subDirecotry = new File(destination.getAbsolutePath() + "/" + directory);
		String[] subFiles = subDirecotry.list();
		for(String subFile: subFiles){
		    (new File(destination.getAbsolutePath() + "/" + directory + "/"+ subFile)).renameTo(new File(destination.getAbsolutePath() + "/"+ subFile));
		}
		try {
		    FileUtils.deleteDirectory(subDirecotry);
		} catch (IOException ignored) {
		    // ignore
		}
	    }
	}
	this.persistenceProvider.getVideoStore().increaseVersion(video.getId());
	return null;
    }

    /**
     * Set video file for unit tests as it's not possible to read
     * a file and convert it to a {@link Part} object.
     * @param videoFile to upload.
     */
    public void setVideoUploadMock(File videoFile) {
	this.mockedVideoFile  = videoFile;
    }
}
package hu.backingbeans.videos;

import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Token;
import hu.model.Video;
import hu.model.api.SivaPlayerSession;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.util.SessionData;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.html.HtmlInputHidden;
import javax.servlet.http.Part;

/**
 * This is the backing bean for a {@link Video}.
 */
@ManagedBean
@RequestScoped
public class VideoBean {
    private Integer id;
    private Integer groupId;
    private List<Group> groups;
    private String title;
    private String description;
    private Date start;
    private Date stop;
    private boolean zipDownloadEnabled = false;
    private String chromeAppURL = "";
    private boolean passwordAvailable = true;
    private String password;
    private String directory = null;
    private boolean tokenAvailable = true;
    private EParticipationRestriction participationRestriction;
    private HtmlInputHidden hiddenId = new HtmlInputHidden();
    private Part video;
    private boolean videoAvailable;
    private SivaPlayerSession sivaPlayerSession;
    
    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{sessionData}")
    private SessionData sessionData;

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
     * 
     * @return id of the {@link Video}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id of the {@link Video}.
     * 
     * @param id
     *            to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return the id of the {@link Group} the {@link Video} belongs to.
     */
    public Integer getGroupId() {
        return this.groupId;
    }

    /**
     * Set the id of the {@link Group} the {@link Video} belongs to.
     * 
     * @param groupId
     *            to set.
     */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     * 
     * @return the {@link List} of available {@link Group}s assigned to the
     *         tutor.
     */
    public List<Group> getGroups() {
        return this.groups;
    }

    /**
     * Set the {@link List} of available {@link Group}s assigned to the tutor.
     * 
     * @param groups
     *            to set.
     */
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    /**
     * 
     * @return the title of the {@link Video}.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of the {@link Video}.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return the description of the {@link Video}.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description of the {@link Video}.
     * 
     * @param description
     *            to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 
     * @return the starting time of the {@link Video}.
     */
    public Date getStart() {
        return this.start;
    }

    /**
     * Set the starting time of the {@link Video}.
     * 
     * @param start
     *            to set.
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * 
     * @return the end time of the {@link Video}.
     */
    public Date getStop() {
        return this.stop;
    }

    /**
     * Set the end time of the {@link Video}.
     * 
     * @param stop
     *            to set.
     */
    public void setStop(Date stop) {
        this.stop = stop;
    }
    
    /**
     * @return true if the {@link Video} can be downloaded as zip file.
     */
    public boolean isZipDownloadEnabled() {
        return this.zipDownloadEnabled;
    }

    /**
     * Set if the {@link Video} can be downloaded as zip file.
     * 
     * @param zipDownloadEnabled
     *            to set.
     */
    public void setZipDownloadEnabled(boolean zipDownloadEnabled) {
        this.zipDownloadEnabled = zipDownloadEnabled;
    }
    
    /**
     * 
     * @return the ChromeApp URL for the {@link Video}.
     */
    public String getChromeAppURL() {
        return this.chromeAppURL;
    }

    /**
     * Set the ChromeApp URL for the {@link Video}.
     * 
     * @param chromeAppURL
     *            to set.
     */
    public void setChromeAppURL(String chromeAppURL) {
        this.chromeAppURL = chromeAppURL;
    }
    
    /**
     * 
     * @return the password of the {@link Video}.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password of the {@link Video}.
     * 
     * @param password
     *            to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 
     * @return the directory of the {@link Video}.
     */
    public String getDirectory() {
        return this.directory;
    }

    /**
     * Set the directory of the {@link Video}.
     * 
     * @param password
     *            to set.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    /**
     * @return true if the {@link Video} is protected with a password.
     */
    public boolean isPasswordAvailable() {
        return passwordAvailable;
    }

    /**
     * Set if the {@link Video} is protected with a password.
     * 
     * @param passwordAvailable
     *            to set.
     */
    public void setPasswordAvailable(boolean passwordAvailable) {
        this.passwordAvailable = passwordAvailable;
    }

    /**
     * @return true if the {@link Video} is accessed by using
     *         {@link Token}s.
     */
    public boolean isTokenAvailable() {
        return this.tokenAvailable;
    }

    /**
     * Set if the {@link Video} is accessed by using {@link Token}s.
     * 
     * @param tokenAvailable
     *            to set.
     */
    public void setTokenAvailable(boolean tokenAvailable) {
        this.tokenAvailable = tokenAvailable;
    }

    /**
     * 
     * @return the {@link EParticipationRestriction} that defines which
     *         {@link EUserType}s are allowed to participate in the
     *         {@link Video}.
     */
    public EParticipationRestriction getParticipationRestriction() {
        return this.participationRestriction;
    }

    /**
     * Set the {@link EParticipationRestriction} that defines which
     * {@link EUserType}s are allowed to participate in the
     * {@link Video}.
     * 
     * @param participationRestriction
     *            to set.
     */
    public void setParticipationRestriction(
            EParticipationRestriction participationRestriction) {
        this.participationRestriction = participationRestriction;
    }

    /**
     * 
     * @return the hiddenId field.
     */
    public HtmlInputHidden getHiddenId() {
        return hiddenId;
    }

    /**
     * Set the hiddenId field.
     * 
     * @param hiddenId
     *            field to set.
     */
    public void setHiddenId(HtmlInputHidden hiddenId) {
        this.hiddenId = hiddenId;
    }

    /**
     * Set the value of the hiddenId field.
     * 
     * @param hiddenValue
     *            of hiddenId field to set.
     */
    public void setHiddenValue(Object hiddenValue) {
        hiddenId.setValue(hiddenValue);
    }

    /**
     * 
     * @return the value of the hiddenId field.
     */
    public Object getHiddenValue() {
        return hiddenId.getValue();
    }
    
    /**
     * 
     * @return the uploaded video.
     */
    public Part getVideo() {  
        return this.video;  
    }  
  
    /**
     * Set the uploaded video.
     * 
     * @param video
     *            to set.
     */
    public void setVideo(Part video) {  
        this.video = video;  
    }  
    
    /**
     * 
     * @return if a video has already been uploaded.
     */
    public boolean isVideoAvailable() {
        return this.videoAvailable;
    }

    /**
     * Set wheter a video has already been uploaded.
     * 
     * @param videoAvailable
     *            is true if a video has been uploaded, false otherwise.
     */
    public void setVideoAvailable(boolean videoAvailable) {
        this.videoAvailable = videoAvailable;
    }
    
    /**
     * 
     * @return the {@link SivaPlayerSession} for watching the video.
     */
    public SivaPlayerSession getSivaPlayerSession() {
        return this.sivaPlayerSession;
    }

    /**
     * Set the {@link SivaPlayerSession} for watching the video.
     * 
     * @param sivaPlayerSession
     *            for watching the video.
     */
    public void setSivaPlayerSession(SivaPlayerSession sivaPlayerSession) {
        this.sivaPlayerSession = sivaPlayerSession;
    }

    /**
     * Set the list of available groups for the current tutor or administrator.
     */
    @PostConstruct
    private void setGroupList() {
        User user = sessionData.getUserId() == null ? null
                : this.persistenceProvider.getUserStore().findById(
                        sessionData.getUserId());
        if (user != null && user.getUserType() == EUserType.Administrator) {
            this.groups = this.persistenceProvider.getGroupStore().getAll(user);
        } else if (user != null) {
            this.groups = this.persistenceProvider.getGroupStore().getByOwner(
                    user.getId());
        }
    }
}
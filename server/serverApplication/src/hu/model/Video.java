package hu.model;

import hu.model.users.EUserType;
import hu.model.users.User;

import java.util.Date;

/**
 * This class represents a {@link Video}. A {@link Video} belongs to an
 * {@link Group} and may contain several {@link AQuestion}s.
 */
public class Video {
    private Integer id = null;
    private String title = null;
    private String description = null;
    private Date start = null;
    private Date stop = null;
    private boolean zipDownloadEnabled = false;
    private String chromeAppURL = "";
    private boolean published = false;
    private String password = null;
    private String directory = null;
    private Integer ratingPoints = 0;
    private Integer ratings = 0;
    private Integer views = 0;
    private Integer downloads = 0;
    private Long size = 0l;
    private Integer authorId = null;
    private Date created = null;
    private Date lastUpdated = null;
    private Integer version = null;
    private EParticipationRestriction participationRestriction = null;

    /**
     * Create a {@link Video} with a id.
     * 
     * @param id
     *            to set.
     */
    public Video(Integer id) {
	this.id = id;
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
     * @return the {@link Date} when the {@link Video} starts.
     */
    public Date getStart() {
	return this.start;
    }

    /**
     * Set the {@link Date} when the {@link Video} starts.
     * 
     * @param start
     *            to set.
     */
    public void setStart(Date start) {
	this.start = start;
    }

    /**
     * 
     * @return the {@link Date} when the {@link Video} stops.
     */
    public Date getStop() {
	return this.stop;
    }

    /**
     * Set the {@link Date} when the {@link Video} stops.
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
     * @return true if the {@link Video} is published for all user groups, false
     *         otherwise.
     */
    public boolean isPublished() {
	return this.published;
    }

    /**
     * Set the flag if the {@link Video} is published for all user groups.
     * 
     * @param published
     *            is true if the {@link Video} is published for all user groups,
     *            false if the {@link Video} is only visible for {@link User}s
     *            with {@link EUserType#Administrator} "Administrator" and owner
     *            of the {@link Video}.
     */
    public void setPublished(boolean published) {
	this.published = published;
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
     * 
     * @return rating points of the {@link Video}.
     */
    public Integer getRatingPoints() {
	return this.ratingPoints;
    }

    /**
     * Set rating points of the {@link Video}.
     * 
     * @param rating
     *            points to set.
     */
    public void setRatingPoints(Integer ratingPoints) {
	this.ratingPoints = ratingPoints;
    }

    /**
     * 
     * @return ratings of the {@link Video}.
     */
    public Integer getRatings() {
	return this.ratings;
    }

    /**
     * Set ratings of the {@link Video}.
     * 
     * @param ratings
     *            to set.
     */
    public void setRatings(Integer ratings) {
	this.ratings = ratings;
    }

    /**
     * 
     * @return views of the {@link Video}.
     */
    public Integer getViews() {
	return this.views;
    }

    /**
     * Set views of the {@link Video}.
     * 
     * @param views
     *            to set.
     */
    public void setViews(Integer views) {
	this.views = views;
    }

    /**
     * 
     * @return downloads of the {@link Video}.
     */
    public Integer getDownloads() {
	return this.downloads;
    }

    /**
     * Set downloads of the {@link Video}.
     * 
     * @param downloads
     *            to set.
     */
    public void setDownloads(Integer downloads) {
	this.downloads = downloads;
    }

    /**
     * 
     * @return size of the {@link Video} in bytes.
     */
    public Long getSize() {
	return this.size;
    }

    /**
     * Set size of the {@link Video}.
     * 
     * @param size
     *            in bytes to set.
     */
    public void setSize(Long size) {
	this.size = size;
    }

    /**
     * 
     * @return the author's id of the {@link Video}.
     */
    public Integer getAuthorId() {
	return this.authorId;
    }

    /**
     * Set authorId of the {@link Video}.
     * 
     * @param authorId
     *            to set.
     */
    public void setAuthorId(Integer authorId) {
	this.authorId = authorId;
    }

    /**
     * 
     * @return creation date of the {@link Video}.
     */
    public Date getCreated() {
	return this.created;
    }

    /**
     * Set creation date of the {@link Video}.
     * 
     * @param created
     *            to set.
     */
    public void setCreated(Date created) {
	this.created = created;
    }

    /**
     * 
     * @return last update date of the {@link Video}.
     */
    public Date getLastUpdated() {
	return this.lastUpdated;
    }

    /**
     * Set last update date of the {@link Video}.
     * 
     * @param lastUpdated
     *            to set.
     */
    public void setLastUpdated(Date lastUpdated) {
	this.lastUpdated = lastUpdated;
    }

    /**
     * 
     * @return version of the {@link Video}.
     */
    public Integer getVersion() {
	return this.version;
    }

    /**
     * Set version of the {@link Video}.
     * 
     * @param version
     *            to set.
     */
    public void setVersion(Integer version) {
	this.version = version;
    }

    /**
     * 
     * @return the {@link EParticipationRestriction} for the {@link Video}.
     */
    public EParticipationRestriction getParticipationRestriction() {
	return this.participationRestriction;
    }

    /**
     * Set the restriction how to get access to the {@link Video}.
     * 
     * @param participationRestriction
     *            to set.
     */
    public void setParticipationRestriction(EParticipationRestriction participationRestriction) {
	this.participationRestriction = participationRestriction;
    }

    /**
     * 
     * @return the id of the {@link Video}.
     */
    public Integer getId() {
	return this.id;
    }

    @Override
    public String toString() {
	return String.format("Video (%d) \"%s\"", this.id, this.title);
    }

    @Override
    public int hashCode() {
	return (this.id == null) ? 0 : this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (this.getClass() != obj.getClass()) {
	    return false;
	}
	Video other = (Video) obj;
	if (this.id == null) {
	    if (other.id != null) {
		return false;
	    }
	} else if (!this.id.equals(other.id)) {
	    return false;
	}
	return true;
    }

}
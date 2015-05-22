package hu.model.api;

import java.util.Date;


/**
 * This class represents a {@link SivaPlayerSession}.
 */
public class SivaPlayerSession {
	private Integer id;
	private String token;
	private String secondaryToken;
	private Integer clientId;
	private Integer userId;
	private Integer videoId;
	private Integer videoVersion;
	private Date start;
	private Date end;
	private Date expireDate;
	
	/**
     * Create a {@link SivaPlayerSession} with an id.
     * 
     * @param token
     *            to set.
     */
    public SivaPlayerSession(Integer id, String token) {
    	this.id = id;
    	this.token = token;
    }
    
    /**
     * Get the complete session token for the current {@link SivaPlayerSession}
     * consisting of the id and a security token.
     */
    public String getSessionToken(){
    	return this.getId() + "-" + this.getToken();
    }

    /**
     * 
     * @return the token of the {@link SivaPlayerSession}.
     */
    public String getToken() {
        return this.token;
    }
    
    /**
     * 
     * @return the token of the {@link SivaPlayerSession}.
     */
    public String getSecondaryToken() {
        return this.secondaryToken;
    }
    
    /**
     * Set the secondary token for the {@link SivaPlayerSession}.
     * @param secondaryToken to set.
     */
    public void setSecondaryToken(String secondaryToken) {
        this.secondaryToken = secondaryToken;
    }
    
    /**
     * 
     * @return the session id.
     */
    public Integer getId() {
        return this.id;
    }
    
    /**
     * 
     * @return the client's id.
     */
    public Integer getClientId() {
        return this.clientId;
    }

    /**
     * Set the client's id.
     * 
     * @param clientId
     *            to set.
     */
    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    
    /**
     * 
     * @return the user's id.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Set the user's id.
     * 
     * @param userId
     *            to set.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    /**
     * 
     * @return the video's id.
     */
    public Integer getVideoId() {
        return this.videoId;
    }

    /**
     * Set the video's id.
     * 
     * @param videId
     *            to set.
     */
    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }
    
    /**
     * 
     * @return the video's version.
     */
    public Integer getVideoVersion() {
        return this.videoVersion;
    }

    /**
     * Set the video's version.
     * 
     * @param videoVersion
     *            to set.
     */
    public void setVideoVersion(Integer videoVersion) {
        this.videoVersion = videoVersion;
    }
    
    /**
     * 
     * @return the start date of the {@link SivaPlayerSession}.
     */
    public Date getStart() {
        return this.start;
    }

    /**
     * Set start date of the {@link SivaPlayerSession}.
     * 
     * @param start date
     *            to set.
     */
    public void setStart(Date start) {
        this.start = start;
    }
    
    /**
     * 
     * @return the end date of the {@link SivaPlayerSession}.
     */
    public Date getEnd() {
        return this.end;
    }

    /**
     * Set end date of the {@link SivaPlayerSession}.
     * 
     * @param end date
     *            to set.
     */
    public void setEnd(Date end) {
        this.end = end;
    }
    
    /**
     * 
     * @return the expire date of the {@link SivaPlayerSession}.
     */
    public Date getExpireDate() {
        return this.expireDate;
    }

    /**
     * Set expire date of the {@link SivaPlayerSession}.
     * 
     * @param expire date
     *            to set.
     */
    public void setExpireDate(Date expiresAt) {
        this.expireDate = expiresAt;
    }

    @Override
    public String toString() {
        return String.format("SivaPlayerSession (%s)", this.id);
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
        SivaPlayerSession other = (SivaPlayerSession) obj;
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
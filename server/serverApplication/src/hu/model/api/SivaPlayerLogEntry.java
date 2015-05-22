package hu.model.api;

import java.util.Date;


/**
 * This class represents a {@link SivaPlayerLogEntry}.
 */
public class SivaPlayerLogEntry {
	private Integer id;
	private Integer sessionId;
	private Integer playerSequenceId = -1;
	private float sceneTimeOffset = 0;
	private Date time;
	private String type;
	private String element;
	private String additionalInformation;
	private Long clientTime = 0L;
	
    /**
     * 
     * @return the id of this log entry.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id of this log entry.
     * 
     * @param id
     *            to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    /**
     * 
     * @return the session id.
     */
    public Integer getSessionId() {
        return this.sessionId;
    }

    /**
     * Set the session id.
     * 
     * @param sessionId
     *            to set.
     */
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }
    
    /**
     * 
     * @return the sequence id from the player logs.
     */
    public Integer getPlayerSequenceId() {
        return this.playerSequenceId;
    }

    /**
     * Set the sequence id from the player logs.
     * 
     * @param playerSequenceId
     *            to set.
     */
    public void setPlayerSequenceId(Integer playerSequenceId) {
        this.playerSequenceId = playerSequenceId;
    }
    
    /**
     * 
     * @return the scene time offset.
     */
    public float getSceneTimeOffset() {
        return this.sceneTimeOffset;
    }

    /**
     * Set the scene time offset.
     * 
     * @param sceneTimeOffset
     *            to set.
     */
    public void setSceneTimeOffset(float sceneTimeOffset) {
        this.sceneTimeOffset = sceneTimeOffset;
    }
    
    /**
     * 
     * @return the creation time of the {@link SivaPlayerLogEntry}.
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * Set creation date of the {@link SivaPlayerLogEntry}.
     * 
     * @param creationTime
     *            to set.
     */
    public void setTime(Date creationTime) {
        this.time = creationTime;
    }
    
    /**
     * 
     * @return the type of the {@link SivaPlayerLogEntry}.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type of the {@link SivaPlayerLogEntry}.
     * 
     * @param type
     *            to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * 
     * @return the element of the {@link SivaPlayerLogEntry}.
     */
    public String getElement() {
        return this.element;
    }

    /**
     * Set the element of the {@link SivaPlayerLogEntry}.
     * 
     * @param element
     *            to set.
     */
    public void setElement(String element) {
        this.element = element;
    }
    
    /**
     * 
     * @return additional information of the {@link SivaPlayerLogEntry}.
     */
    public String getAdditionalInformation() {
        return this.additionalInformation;
    }

    /**
     * Set additional information for the {@link SivaPlayerLogEntry}.
     * 
     * @param additionalInformation
     *            to set.
     */
    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
    
    /**
     * 
     * @return the client time.
     */
    public Long getClientTime() {
        return this.clientTime;
    }

    /**
     * Set the client time.
     * 
     * @param clientTime
     *            to set.
     */
    public void setClientTime(Long clientTime) {
        this.clientTime = clientTime;
    }

    @Override
    public String toString() {
        return String.format("SivaPlayerLogEntry (%s)", this.id);
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
        SivaPlayerLogEntry other = (SivaPlayerLogEntry) obj;
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
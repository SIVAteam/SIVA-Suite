package hu.model.api;

/**
 * This class represents a {@link CollaborationThread}.
 */
public class CollaborationThread {
    private Integer id;
    private Integer videoId;
    private String scene;
    private String title;
    private Integer durationFrom;
    private Integer durationTo;
    private ECollaborationThreadVisibility visibility;
	
    /**
     * Create a {@link CollaborationThread} with an id.
     * 
     * @param id
     *            to set.
     */
    public CollaborationThread(Integer id) {
	this.id = id;
    }
    
    /**
     * 
     * @return the id of this thread.
     */
    public Integer getId() {
        return this.id;
    }
    
    /**
     * 
     * @return the id of the video.
     */
    public Integer getVideoId() {
        return this.videoId;
    }

    /**
     * Set the id of the video.
     * 
     * @param video
     *            to set.
     */
    public void setVideoId(Integer videoId) {
        this.videoId = videoId;
    }
    
    /**
     * 
     * @return the scene of this thread.
     */
    public String getScene() {
        return this.scene;
    }

    /**
     * Set the scene of this tread.
     * 
     * @param scene
     *            to set.
     */
    public void setScene(String scene) {
        this.scene = scene;
    }
    
    /**
     * 
     * @return the title of this thread.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of this tread.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * 
     * @return the display start time of this thread in seconds.
     */
    public Integer getDurationFrom() {
        return this.durationFrom;
    }

    /**
     * Set the display start time of this tread in seconds.
     * 
     * @param durationFrom
     *            to set.
     */
    public void setDurationFrom(Integer durationFrom) {
        this.durationFrom = durationFrom;
    }
    
    /**
     * 
     * @return the display end time of this thread in seconds.
     */
    public Integer getDurationTo() {
        return this.durationTo;
    }

    /**
     * Set the display end time of this tread in seconds.
     * 
     * @param durationTo
     *            to set.
     */
    public void setDurationTo(Integer durationTo) {
        this.durationTo = durationTo;
    }
    
    /**
     * 
     * @return the visibility of this thread.
     */
    public ECollaborationThreadVisibility getVisibility() {
        return this.visibility;
    }

    /**
     * Set the visibility of this thread.
     * 
     * @param visibility
     *            to set.
     */
    public void setVisibility(ECollaborationThreadVisibility visibility) {
        this.visibility = visibility;
    }
}
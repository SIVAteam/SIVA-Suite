package hu.model.api;

import java.util.Date;

/**
 * This class represents a {@link CollaborationPost}.
 */
public class CollaborationPost {
    private Integer id;
    private Integer threadId;
    private Integer userId;
    private Date date;
    private String post;
    private Boolean active;
	
    /**
     * Create a {@link CollaborationPost} with an id.
     * 
     * @param id
     *            to set.
     */
    public CollaborationPost(Integer id) {
	this.id = id;
    }
    
    /**
     * 
     * @return the id of this post.
     */
    public Integer getId() {
        return this.id;
    }
    
    /**
     * 
     * @return the date.
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Set the date.
     * 
     * @param date
     *            to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }
    
    /**
     * 
     * @return the post.
     */
    public String getPost() {
        return this.post;
    }

    /**
     * Set the post.
     * 
     * @param post
     *            to set.
     */
    public void setPost(String post) {
        this.post = post;
    }
    
    /**
     * 
     * @return the id of the owner.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Set the id of the owner.
     * 
     * @param user
     *            to set.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    /**
     * 
     * @return the id of the thread.
     */
    public Integer getThreadId() {
        return this.threadId;
    }

    /**
     * Set the id of the thread.
     * 
     * @param thread
     *            to set.
     */
    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }
    
    /**
     * 
     * @return if this thread is active.
     */
    public Boolean isActive() {
        return this.active;
    }

    /**
     * Set if this thread is active.
     * 
     * @param active
     *            to set.
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
}
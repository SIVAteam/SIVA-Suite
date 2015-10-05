package hu.model.api;

/**
 * This class represents a {@link CollaborationMedia}.
 */
public class CollaborationMedia {
    private Integer id;
    private Integer postId;
    private String filename;
	
    /**
     * Create a {@link CollaborationMedia} with an id.
     * 
     * @param id
     *            to set.
     */
    public CollaborationMedia(Integer id) {
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
     * @return the id of the post.
     */
    public Integer getPostId() {
        return this.postId;
    }

    /**
     * Set the id of the post.
     * 
     * @param postId
     *            to set.
     */
    public void setPostId(Integer postId) {
        this.postId = postId;
    }
    
    /**
     * 
     * @return the filename.
     */
    public String getFilename() {
        return this.filename;
    }

    /**
     * Set the filename.
     * 
     * @param filename
     *            to set.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
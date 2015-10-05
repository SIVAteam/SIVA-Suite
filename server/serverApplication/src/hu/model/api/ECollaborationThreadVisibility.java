package hu.model.api;

/**
 * This enum describes the visibility of a {@link CollaborationThread}..
 */
public enum ECollaborationThreadVisibility {
    
    /**
     * Visible for all everyone who is allowed to watch the video.
     */
    All,
    
    /**
     * Only visible for me. 
     */
    Me,
    
    /**
     * Only visible for administrators (and me).
     */
    Administrator
}

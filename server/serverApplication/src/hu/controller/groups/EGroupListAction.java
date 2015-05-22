package hu.controller.groups;


/**
 * This enum defines the different actions for group list.
 */
public enum EGroupListAction {
    
    /**
     * Add user as attendant to group.
     */
    addAttendant,

    /**
     * Remove user as attendant from group.
     */
    removeAttendant,
    
    /**
     * Add user as tutor to group.
     */
    addTutor,

    /**
     * Remove user as tutor from group.
     */
    removeTutor;
}

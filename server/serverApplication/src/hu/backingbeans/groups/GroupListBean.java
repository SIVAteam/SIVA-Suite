package hu.backingbeans.groups;

import hu.controller.groups.EGroupListAction;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.User;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for a {@link List} of {@link Group}s. It is used to
 * provide data to the view whenever a {@link List} of {@link Group}s needs to
 * be displayed.
 */
@ManagedBean
@RequestScoped
public class GroupListBean {
    private Integer page;
    private ESortDirection sortDirection;
    private String listShown = "all";
    private List<GroupListEntryBean> list;
    private Integer pages;
    private boolean visibleAvailable;
    private boolean attendedAvailable;
    private boolean ownedAvailable;
    private boolean allAvailable;
    private EGroupListAction action;

    /**
     * @return the current page of the list.
     */
    public Integer getPage() {
        return this.page;
    }

    /**
     * Set the current page of the list.
     * @param page
     *            to set.
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * @return the direction in which the list is sorted.
     */
    public ESortDirection getSortDirection() {
        return this.sortDirection;
    }

    /**
     * Set the direction in which the list is sorted.
     * 
     * @param sortDirection
     *            to set.
     */
    public void setSortDirection(ESortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * @return the list/tab that is currently shown. "all", "owned", "attended"
     *         or "visited".
     */
    public String getListShown() {
        return this.listShown;
    }

    /**
     * Set the list/tab that is currently shown. "all", "owned", "attended" or
     * "visited".
     * 
     * @param listShown
     *            to set.
     */
    public void setListShown(String listShown) {
        this.listShown = listShown;
    }

    /**
     * @return the {@link List} of {@link Group}s.
     */
    public List<GroupListEntryBean> getList() {
        return this.list;
    }

    /**
     * Set the {@link List} of {@link Group}s.
     * 
     * @param list
     *            to set.
     */
    public void setList(List<GroupListEntryBean> list) {
        this.list = list;
    }

    /**
     * @return the number of pages available.
     */
    public Integer getPages() {
        return this.pages;
    }

    /**
     * Set the number of pages available.
     * 
     * @param pages
     *            to set.
     */
    public void setPages(Integer pages) {
        this.pages = pages;
    }

    /**
     * @return true if the "visible groups" tab is shown.
     */
    public boolean isVisibleAvailable() {
        return visibleAvailable;
    }

    /**
     * Set whether the "visible groups" tab is shown.
     * 
     * @param visibleAvailable
     *            to set.
     */
    public void setVisibleAvailable(boolean visibleAvailable) {
        this.visibleAvailable = visibleAvailable;
    }

    /**
     * @return true if the "attended groups" tab is shown.
     */
    public boolean isAttendedAvailable() {
        return attendedAvailable;
    }

    /**
     * Set whether the "attended groups" tab is shown.
     * 
     * @param attendedAvailable
     *            to set.
     */
    public void setAttendedAvailable(boolean attendedAvailable) {
        this.attendedAvailable = attendedAvailable;
    }

    /**
     * @return true if the "owned groups" tab is shown.
     */
    public boolean isOwnedAvailable() {
        return ownedAvailable;
    }

    /**
     * Set whether the "owned groups" tab is shown.
     * 
     * @param ownedAvailable
     *            to set.
     */
    public void setOwnedAvailable(boolean ownedAvailable) {
        this.ownedAvailable = ownedAvailable;
    }

    /**
     * @return true if the "all groups" tab is shown.
     */
    public boolean isAllAvailable() {
        return allAvailable;
    }

    /**
     * Set whether the "all groups" tab is shown.
     * 
     * @param allAvailable
     *            to set.
     */
    public void setAllAvailable(boolean allAvailable) {
        this.allAvailable = allAvailable;
    }
    
    /**
     * 
     * @return the action type as {@link EGroupListAction}.
     */
    public EGroupListAction getAction() {
        return this.action;
    }

    /**
     * Set the action type as {@link EGroupListAction}.
     * 
     * @param action
     *            to set.
     */
    public void setAction(EGroupListAction action) {
        this.action = action;
    }

    /**
     * This backing bean holds the information about a single entry in a
     * {@link List} of {@link Group}s.
     */
    @ManagedBean
    @RequestScoped
    public static class GroupListEntryBean {
        private Group group;
        private List<User> owners;
        private boolean currentUserSignedUp;
        private boolean currentUserOwner;
        private boolean signUpAvailable;
        private boolean signOffAvailable;
        private boolean showVideosAvailable;
        private boolean editAvailable;
        private boolean deleteAvailable;

        /**
         * Helper function to format the owners of an {@link Group} for output
         * in a table cell.
         * 
         * @return the names of the owners of the {@link Group}, separated by
         *         semicolons.
         */
        public String getOwnersSeperated() {
            final int numOwners = this.owners.size();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < numOwners; i++) {
                User owner = this.owners.get(i);
                sb.append(owner.getLastName());
                sb.append(", ");
                sb.append(owner.getFirstName());
                if (owner.getTitle() != null) {
                    sb.append(", ");
                    sb.append(owner.getTitle());
                }
                if (i != numOwners - 1) {
                    sb.append("; ");
                }
            }

            return sb.toString();
        }

        /**
         * @return the {@link Group} of the entry.
         */
        public Group getGroup() {
            return this.group;
        }

        /**
         * Set the {@link Group} of the entry.
         * 
         * @param group
         *            to set.
         */
        public void setGroup(Group group) {
            this.group = group;
        }

        /**
         * @return the {@link List} of owners of the {@link Group}.
         */
        public List<User> getOwners() {
            return this.owners;
        }

        /**
         * Set the {@link List} of owners of the {@link Group}.
         * 
         * @param owners
         *            to set.
         */
        public void setOwners(List<User> owners) {
            this.owners = owners;
        }
        
        /**
         * @return true if the enrollment status of the current {@link User} is
         *         owner, false otherwise.
         */
        public boolean isCurrentUserOwner() {
            return this.currentUserOwner;
        }

        /**
         * Set whether the enrollment status of the current {@link User} is
         * owner or not.
         * 
         * @param currentUserOwner
         *            is true if the current {@link User} is owner, false
         *            otherwise.
         */
        public void setCurrentUserOwner(boolean currentUserOwner) {
            this.currentUserOwner = currentUserOwner;
        }

        /**
         * @return true if the enrollment status of the current {@link User} is
         *         signed up, false otherwise.
         */
        public boolean isCurrentUserSignedUp() {
            return this.currentUserSignedUp;
        }

        /**
         * Set whether the enrollment status of the current {@link User} is
         * signed up or not.
         * 
         * @param currentUserSignedUp
         *            is true if the current {@link User} is signed up, false
         *            otherwise.
         */
        public void setCurrentUserSignedUp(boolean currentUserSignedUp) {
            this.currentUserSignedUp = currentUserSignedUp;
        }

        /**
         * @return true if the "sign up" button is available.
         */
        public boolean isSignUpAvailable() {
            return signUpAvailable;
        }

        /**
         * Set whether to show the "sign up" button.
         * 
         * @param signUpAvailable
         *            to set.
         */
        public void setSignUpAvailable(boolean signUpAvailable) {
            this.signUpAvailable = signUpAvailable;
        }

        /**
         * @return true if the "sign off" button is available.
         */
        public boolean isSignOffAvailable() {
            return signOffAvailable;
        }

        /**
         * Set whether to show the "sign off" button.
         * 
         * @param signOffAvailable
         *            to set.
         */
        public void setSignOffAvailable(boolean signOffAvailable) {
            this.signOffAvailable = signOffAvailable;
        }

        /**
         * @return true if the "show videos" button is available.
         */
        public boolean isShowVideosAvailable() {
            return showVideosAvailable;
        }

        /**
         * Set whether to show the "show videos" button.
         * 
         * @param showVideosAvailable
         *            to set.
         */
        public void setShowVideosAvailable(
                boolean showVideosAvailable) {
            this.showVideosAvailable = showVideosAvailable;
        }

        /**
         * @return true if the "edit" button is available.
         */
        public boolean isEditAvailable() {
            return editAvailable;
        }

        /**
         * Set whether to show the "edit" button.
         * 
         * @param editAvailable
         *            to set.
         */
        public void setEditAvailable(boolean editAvailable) {
            this.editAvailable = editAvailable;
        }

        /**
         * @return true if the "delete" button is available.
         */
        public boolean isDeleteAvailable() {
            return deleteAvailable;
        }

        /**
         * Set whether to show the "delete" button.
         * 
         * @param deleteAvailable
         *            to set.
         */
        public void setDeleteAvailable(boolean deleteAvailable) {
            this.deleteAvailable = deleteAvailable;
        }
    }
}
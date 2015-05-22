package hu.backingbeans.users;

import hu.controller.users.EUserListAction;
import hu.model.ESortDirection;
import hu.model.users.ESortColumnUser;
import hu.model.users.EUserType;
import hu.model.users.User;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for a {@link List} of {@link User}s. It is used to
 * provide data to the view whenever a {@link List} of {@link User}s needs to be
 * displayed.
 */
@ManagedBean
@RequestScoped
public class UserListBean {
    private List<UserListEntryBean> list;
    private ESortDirection sortDirection = ESortDirection.ASC;
    private ESortColumnUser sortColumn = ESortColumnUser.Name;
    private int page = 0;
    private int pages = 0;
    private EUserType userType;
    private String searchQuery = "";
    private EUserListAction action;

    /**
     * 
     * @return the {@link List} of {@link User}s.
     */
    public List<UserListEntryBean> getList() {
        return this.list;
    }

    /**
     * Set the {@link List} of {@link User}s.
     * 
     * @param list
     *            of {@link User}s.
     */
    public void setList(List<UserListEntryBean> list) {
        this.list = list;
    }

    /**
     * 
     * @return the {@link ESortDirection}.
     */
    public ESortDirection getSortDirection() {
        return this.sortDirection;
    }

    /**
     * Set the {@link ESortDirection}.
     * 
     * @param sortDirection
     *            to set.
     */
    public void setSortDirection(ESortDirection sortDirection) {
        this.sortDirection = sortDirection;
    }

    /**
     * 
     * @return the {@link ESortColumnUser}.
     */
    public ESortColumnUser getSortColumn() {
        return this.sortColumn;
    }

    /**
     * Set the {@link ESortColumnUser}.
     * 
     * @param sortColumn
     *            to set.
     */
    public void setSortColumn(ESortColumnUser sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * 
     * @return the page number.
     */
    public int getPage() {
        return this.page;
    }

    /**
     * Set the page number.
     * 
     * @param page
     *            number to set.
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * 
     * @return the amount of pages.
     */
    public int getPages() {
        return this.pages;
    }

    /**
     * Set the amount of pages.
     * 
     * @param page
     *            amount to set.
     */
    public void setPages(int pages) {
        this.pages = pages;
    }

    /**
     * 
     * @return the user type as {@link EUserType}.
     */
    public EUserType getUserType() {
        return this.userType;
    }

    /**
     * Set the user type as {@link EUserType}.
     * 
     * @param userType
     *            to set.
     */
    public void setUserType(EUserType userType) {
        this.userType = userType;
    }

    /**
     * 
     * @return the search query as {@link String}.
     */
    public String getSearchQuery() {
        return this.searchQuery;
    }

    /**
     * Set the search query as {@link String}.
     * 
     * @param searchQuery
     *            to set.
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * 
     * @return the user type as {@link EUserType}.
     */
    public EUserListAction getAction() {
        return this.action;
    }

    /**
     * Set the user type as {@link EUserType}.
     * 
     * @param userType
     *            to set.
     */
    public void setAction(EUserListAction action) {
        this.action = action;
    }

    /**
     * This backing bean holds the information about a single entry in a
     * {@link List} of {@link User}s.
     */
    @ManagedBean
    @RequestScoped
    public static class UserListEntryBean {
        private User user;
        private boolean added;
        private boolean addable;
        private boolean removable;

        /**
         * 
         * @return the {@link User} of the entry.
         */
        public User getUser() {
            return this.user;
        }

        /**
         * Set the {@link User} of the entry.
         * 
         * @param user
         *            to set.
         */
        public void setUser(User user) {
            this.user = user;
        }

        /**
         * 
         * @return if the {@link User} is added to the current group or
         *         video.
         */
        public boolean isAdded() {
            return this.added;
        }

        /**
         * Set if the {@link User} is added to the current group or
         * video.
         * 
         * @param availableButtons
         *            to set.
         */
        public void setAdded(boolean added) {
            this.added = added;
        }

        /**
         * 
         * @return if the {@link User} can be added to the current group or
         *         video.
         */
        public boolean isAddable() {
            return this.addable;
        }

        /**
         * Set if the {@link User} can be added to the current group or
         * video.
         * 
         * @param addable
         *            to set.
         */
        public void setAddable(boolean addable) {
            this.addable = addable;
        }

        /**
         * 
         * @return if the {@link User} can be removed from the current group or
         *         video.
         */
        public boolean isRemovable() {
            return this.removable;
        }

        /**
         * Set if the {@link User} can be removed from the current group or
         * video.
         * 
         * @param removable
         *            to set.
         */
        public void setRemovable(boolean removable) {
            this.removable = removable;
        }
    }
}
package hu.backingbeans.videos;

import hu.model.EVideoType;
import hu.model.ESortColumnVideo;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.ESortColumnUser;
import hu.model.users.User;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for a {@link List} of {@link Video}s.
 */
@ManagedBean
@RequestScoped
public class VideoListBean {
    private List<VideoListEntryBean> list;
    private ESortDirection sortDirection = ESortDirection.ASC;
    private ESortColumnVideo sortColumn = ESortColumnVideo.Title;
    private int page = 0;
    private int pages = 0;
    private EVideoType type;
    private boolean createButtonAvailable;

    /**
     * 
     * @return the {@link List} of {@link VideoListEntryBean}s.
     */
    public List<VideoListEntryBean> getList() {
        return this.list;
    }

    /**
     * Set the {@link List} of {@link VideoListEntryBean}s.
     * 
     * @param list
     *            to set.
     */
    public void setList(List<VideoListEntryBean> list) {
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
    public ESortColumnVideo getSortColumn() {
        return this.sortColumn;
    }

    /**
     * Set the {@link ESortColumnUser}.
     * 
     * @param sortColumn
     *            to set.
     */
    public void setSortColumn(ESortColumnVideo sortColumn) {
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
     * @return the video type as {@link EVideoType}.
     */
    public EVideoType getType() {
        return this.type;
    }

    /**
     * Set the video type as {@link EVideoType}.
     * 
     * @param userType
     *            to set.
     */
    public void setType(EVideoType type) {
        this.type = type;
    }
    
    /**
     * 
     * @return the availability of the create button.
     */
    public boolean isCreateButtonAvailable() {
        return this.createButtonAvailable;
    }

    /**
     * Set the the availability of the create button.
     * 
     * @param availability
     *            to set.
     */
    public void setCreateButtonAvailable(boolean availablility) {
        this.createButtonAvailable = availablility;
    }

    /**
     * This backing bean holds the information about a single entry in a
     * {@link List} of {@link Video}s.
     */
    @ManagedBean
    @RequestScoped
    public static class VideoListEntryBean {
        private Video video;
        private Group group;
        private List<User> owners;
        private boolean completedByCurrentUser;
        private String active;
        private boolean participationButtonAvailable;
        private boolean zipDownloadButtonAvailable;
        private boolean chromeAppDownloadButtonAvailable;
        private boolean startButtonAvailable;
        private boolean stopButtonAvailable;
        private boolean editButtonAvailable;
        private boolean embeddButtonAvailable;
        private boolean deleteButtonAvailable;
        
        /**
         * 
         * @return the {@link Video} of the entry.
         */
        public Video getVideo() {
            return this.video;
        }

        /**
         * Set the {@link Video} of the entry.
         * 
         * @param video
         *            to set.
         */
        public void setVideo(Video video) {
            this.video = video;
        }

        /**
         * 
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
         * 
         * @return the {@link List} of {@link User}s that own the
         *         {@link Video}.
         */
        public List<User> getOwners() {
            return this.owners;
        }

        /**
         * Set the {@link List} of {@link User}s that own the
         * {@link Video}.
         * 
         * @param owners
         *            to set.
         */
        public void setOwners(List<User> owners) {
            this.owners = owners;
        }

        /**
         * 
         * @return true if the current user has already completed answering the
         *         {@link Video}, false otherwise.
         */
        public boolean isCompletedByCurrentUser() {
            return this.completedByCurrentUser;
        }

        /**
         * Set whether the current user has already completed answering the
         * {@link Video} or not.
         * 
         * @param completedByCurrentUser
         *            is true if the current user has already completed the
         *            {@link Video}, false otherwise.
         */
        public void setCompletedByCurrentUser(boolean completedByCurrentUser) {
            this.completedByCurrentUser = completedByCurrentUser;
        }
        
        /**
         * 
         * @return if the {@link Video} is active.
         */
        public String getActive() {
            return this.active;
        }

        /**
         * Set the {@link Video} is active.
         * 
         * @param activationStatus
         *            to set.
         */
        public void setActive(String active) {
            this.active = active;
        }
        
        /**
         * 
         * @return the availability of the participation button.
         */
        public boolean isParticipationButtonAvailable() {
            return this.participationButtonAvailable;
        }

        /**
         * Set the the availability of the participation button.
         * 
         * @param availability
         *            to set.
         */
        public void setParticipationButtonAvailable(boolean availablility) {
            this.participationButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the zip download button.
         */
        public boolean isZipDownloadButtonAvailable() {
            return this.zipDownloadButtonAvailable;
        }

        /**
         * Set the the availability of the zip download button.
         * 
         * @param availability
         *            to set.
         */
        public void setZipDownloadButtonAvailable(boolean availablility) {
            this.zipDownloadButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the ChromeApp download button.
         */
        public boolean isChromeAppDownloadButtonAvailable() {
            return this.chromeAppDownloadButtonAvailable;
        }

        /**
         * Set the the availability of the ChromeApp download button.
         * 
         * @param availability
         *            to set.
         */
        public void setChromeAppDownloadButtonAvailable(boolean availablility) {
            this.chromeAppDownloadButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the start button.
         */
        public boolean isStartButtonAvailable() {
            return this.startButtonAvailable;
        }

        /**
         * Set the the availability of the start button.
         * 
         * @param availability
         *            to set.
         */
        public void setStartButtonAvailable(boolean availablility) {
            this.startButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the stop button.
         */
        public boolean isStopButtonAvailable() {
            return this.stopButtonAvailable;
        }

        /**
         * Set the the availability of the stop button.
         * 
         * @param availability
         *            to set.
         */
        public void setStopButtonAvailable(boolean availablility) {
            this.stopButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the edit button.
         */
        public boolean isEditButtonAvailable() {
            return this.editButtonAvailable;
        }

        /**
         * Set the the availability of the edit button.
         * 
         * @param availability
         *            to set.
         */
        public void setEditButtonAvailable(boolean availablility) {
            this.editButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the embedd button.
         */
        public boolean isEmbeddButtonAvailable() {
            return this.embeddButtonAvailable;
        }

        /**
         * Set the the availability of the embedd button.
         * 
         * @param availability
         *            to set.
         */
        public void setEmbeddButtonAvailable(boolean availablility) {
            this.embeddButtonAvailable = availablility;
        }
        
        /**
         * 
         * @return the availability of the delete button.
         */
        public boolean isDeleteButtonAvailable() {
            return this.deleteButtonAvailable;
        }

        /**
         * Set the the availability of the delete button.
         * 
         * @param availability
         *            to set.
         */
        public void setDeleteButtonAvailable(boolean availablility) {
            this.deleteButtonAvailable = availablility;
        }
    }
}
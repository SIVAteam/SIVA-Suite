package hu.backingbeans.common;

import hu.model.users.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for all search forms of the application and holds
 * the entered search query.
 */
@ManagedBean
@RequestScoped
public class SearchBean {
    private String searchQuery;

    /**
     * 
     * @return the search query which was entered by the {@link User}.
     */
    public String getSearchQuery() {
        return this.searchQuery;
    }

    /**
     * Set the search query entered by the {@link User}.
     * 
     * @param searchQuery
     *            to set.
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}
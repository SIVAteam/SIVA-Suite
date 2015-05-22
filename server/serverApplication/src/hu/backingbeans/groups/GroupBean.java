package hu.backingbeans.groups;

import hu.model.Group;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.html.HtmlInputHidden;

/**
 * This is the backing bean for an {@link Group}. It is used whenever a new
 * {@link Group} is created or an existing {@link Group} is modified.
 */
@ManagedBean
@RequestScoped
public class GroupBean {
    private Integer id;
    private String title;
    private boolean visible;
    private HtmlInputHidden hiddenGroupId = new HtmlInputHidden();

    /**
     * @return the field containing the id of the {@link Group}.
     */
    public HtmlInputHidden getHiddenGroupId() {
        return hiddenGroupId;
        
    }

    /**
     * Set the field containing the id of the {@link Group}.
     * 
     * @param hiddenGroupId
     *            to set.
     */
    public void setHiddenGroupId(HtmlInputHidden hiddenGroupId) {
        this.hiddenGroupId = hiddenGroupId;
        
    }
    
    /**
     * Set the id of the {@link Group} kept in a hidden field.
     * 
     * @param hiddenGroupIdValue
     *            to set.
     */
    public void setHiddenGroupIdValue(Object hiddenGroupIdValue) {
        this.hiddenGroupId.setValue(hiddenGroupIdValue);
    }
 
    /**
     * @return the id of the {@link Group} kept in a hidden field.
     */
    public Object getHiddenGroupIdValue() {
        return this.hiddenGroupId.getValue();
    }

    /**
     * 
     * @return the id of the {@link Group}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id of the {@link Group}.
     * 
     * @param id
     *            to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return the title of the {@link Group}.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of the {@link Group}.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return the visibility of the {@link Group}.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Set the visibility of the {@link Group}.
     * 
     * @param visible
     *            is true if the {@link Group} is visible, false otherwise.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
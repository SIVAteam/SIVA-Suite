package hu.util;

import hu.model.users.EUserType;
import hu.model.users.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * This class stores the data associated with a {@link User}s session, namely
 * the it of the {@link User}.
 */
@ManagedBean
@SessionScoped
public class SessionData {
    private Integer userId;
    private EUserType userType;
    private ELocale locale;
    private boolean mobile = false;

    /**
     * @return the id of the {@link User} that is authorized in the current
     *         session.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Set the id of the {@link User} that is authorized in the current session.
     * 
     * @param userId
     *            to set.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    /**
     * @return the {@link EUserType} of the {@link User} that is authorized in the current
     *         session.
     */
    public EUserType getUserType() {
        return this.userType;
    }

    /**
     * Set the {@link EUserType} of the {@link User} that is authorized in the current session.
     * 
     * @param userType
     *            to set.
     */
    public void setUserType(EUserType userType) {
        this.userType = userType;
    }
    
    /**
     * @return the locale of the {@link User}.
     */
    public ELocale getLocale() {
        return this.locale;
    }

    /**
     * Set the locale of the {@link User}.
     * 
     * @param userId
     *            to set.
     */
    public void setLocale(ELocale locale) {
        this.locale = locale;
    }
    
    /**
     * @return whether its a mobile browser or not.
     */
    public boolean isMobile() {
        return this.mobile;
    }

    /**
     * Set whether its a mobile browser or not.
     * 
     * @param mobile
     *            to set.
     */
    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }
}

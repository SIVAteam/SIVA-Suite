package hu.backingbeans.common;

import hu.model.users.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;

/**
 * This is the backing bean for the login form. It holds the entered username,
 * password and a reference to the login form itself to display an error message
 * in case the login failed.
 */
@ManagedBean
@RequestScoped
public class LoginBean {
    private String username;
    private String password;
    private String redirectURL;
    private UIComponent loginForm;

    /**
     * @return the username of the {@link User} that wants to log in.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Set the username of the {@link User} that wants to log in.
     * 
     * @param username
     *            to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 
     * @return the password of the {@link User} that wants to log in.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password of the {@link User} that wants to log in.
     * 
     * @param password
     *            to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * 
     * @return the URL of the actual page the {@link User} wanted to visit.
     */
    public String getRedirectURL() {
        return this.redirectURL;
    }

    /**
     * Set the actual page the {@link User} that wanted to visit.
     * 
     * @param page url
     *            to set.
     */
    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    /**
     * 
     * @return the reference to the login form that received the entered
     *         credentials.
     */
    public UIComponent getLoginForm() {
        return this.loginForm;
    }

    /**
     * Set the reference to the login form that received the entered
     * credentials.
     * 
     * @param loginForm
     *            to set.
     */
    public void setLoginForm(UIComponent loginForm) {
        this.loginForm = loginForm;
    }
}
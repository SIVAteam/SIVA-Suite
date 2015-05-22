package hu.backingbeans.users;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for resetting password.
 */
@ManagedBean
@RequestScoped
public class ResetPasswordBean {
    private String hash;
    private String email;
    private String password;
    private String passwordRepeat;

    /**
     * 
     * @return the hash.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Set the hash for password recovery.
     * 
     * @param hash
     *            to set.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * 
     * @return the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email address for password recovery.
     * 
     * @param email
     *            address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return the newPassword.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the new password.
     * 
     * @param newPassword
     *            to set.
     */
    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 
     * @return the passwordRepeat.
     */
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * Set the repeated new password.
     * 
     * @param passwordRepeat
     *            to set.
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }
}
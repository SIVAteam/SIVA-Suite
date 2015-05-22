package hu.backingbeans.common;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for the contact form. It holds all the information
 * that is going to be sent to the administrator.
 */
@ManagedBean
@RequestScoped
public class ContactBean {
    private String academicTitle;
    private String firstName;
    private String lastName;
    private String email;
    private String subject;
    private String message;

    /**
     * 
     * @return the academic title of the sender.
     */
    public String getAcademicTitle() {
        return this.academicTitle;
    }

    /**
     * Set the academic title of the sender.
     * 
     * @param academicTitle
     *            to set.
     */
    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }

    /**
     * 
     * @return the first name of the sender.
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Set the first name of the sender.
     * 
     * @param firstName
     *            to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     * @return the last name of the sender.
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Set the last name of the sender.
     * 
     * @param lastName
     *            to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     * @return the email address of the sender.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Set the email address of the sender.
     * 
     * @param email
     *            to set.
     * 
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return the specified subject.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set the subject specified by the sender.
     * 
     * @param subject
     *            to set.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * 
     * @return the entered message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message entered by the sender.
     * 
     * @param message
     *            to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
package hu.backingbeans.users;

import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.util.ECountry;

import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.html.HtmlInputHidden;
import javax.servlet.http.Part;

/**
 * This backing bean holds all {@link User} related information that is
 * submitted through a form. It is used whenever a {@link User} is created,
 * edited or deleted. It is also used for registration of a {@link User}
 */
@ManagedBean
@RequestScoped
public class UserBean {	
    private Integer id;
    private String email;
    private String password;
    private String passwordRepeat;
    private boolean sendNewPassword;
    private String academicTitle;
    private String firstName;
    private String lastName;
    private Date birthday;
    private EGender gender;
    private String street;
    private String zip;
    private String city;
    private ECountry country;
    private String phone;
    private String fax;
    private String website;
    private boolean visible;
    private EUserType userType;
    private boolean banned;
    private HtmlInputHidden hiddenUserId = new HtmlInputHidden();
    private Part photo;
    private boolean photoAvailable;

    /**
     * 
     * @return the field containing the id of the {@link User}.
     */
    public HtmlInputHidden getHiddenUserId() {
        return hiddenUserId;
        
    }

    /**
     * Set the field containing the id of the {@link User}.
     * 
     * @param hiddenUserId
     *            to set.
     */
    public void setHiddenUserId(HtmlInputHidden hiddenUserId) {
        this.hiddenUserId = hiddenUserId;     
    }
    
    /**
     * Set the id of the {@link User} kept in a hidden field.
     * 
     * @param hiddenValue
     *            to set.
     */
    public void setHiddenUserIdValue(Object hiddenUserIdValue) {
        this.hiddenUserId.setValue(hiddenUserIdValue);
    }
 
    /**
     * 
     * @return the id of the {@link User} kept in a hidden field.
     */
    public Object getHiddenUserIdValue() {
        return this.hiddenUserId.getValue();
    }

    /**
     * 
     * @return the id of the {@link User}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Set the id of a {@link User}.
     * 
     * @param id
     *            to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return the email address of the {@link User}.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Set the email address of the {@link User}.
     * 
     * @param email
     *            to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return the password of the {@link User}.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password of the {@link User}.
     * 
     * @param password
     *            to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * @return the repetition of the password of the {@link User}.
     */
    public String getPasswordRepeat() {
        return this.passwordRepeat;
    }

    /**
     * Set the repetition of the password of the {@link User}.
     * 
     * @param passwordRepeat
     *            to set.
     */
    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
    }

    /**
     * 
     * @return true if a new password should be generated and sent to the
     *         {@link User}, false otherwise.
     */
    public boolean isSendNewPassword() {
        return this.sendNewPassword;
    }

    /**
     * Set whether a new password should be generated and sent to the
     * {@link User} or no.
     * 
     * @param sendNewPassword
     *            is true if a new password should be generated and sent to the
     *            {@link User}, false otherwise.
     */
    public void setSendNewPassword(boolean sendNewPassword) {
        this.sendNewPassword = sendNewPassword;
    }

    /**
     * 
     * @return the academic title of the {@link User}.
     */
    public String getAcademicTitle() {
        return this.academicTitle;
    }

    /**
     * Set the academic title of the {@link User}.
     * 
     * @param academicTitle
     *            to set.
     */
    public void setAcademicTitle(String academicTitle) {
        this.academicTitle = academicTitle;
    }

    /**
     * 
     * @return the first name of the {@link User}.
     */
    public String getFirstName() {
        return this.firstName;
    }

    /**
     * Set the first name of the {@link User}.
     * 
     * @param firstName
     *            to set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     * @return the last name of the {@link User}.
     */
    public String getLastName() {
        return this.lastName;
    }

    /**
     * Set the last name of the {@link User}.
     * 
     * @param lastName
     *            to set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     * @return the birthday of the {@link User}.
     */
    public Date getBirthday() {
        return this.birthday;
    }

    /**
     * Set the birthday of the {@link User}.
     * 
     * @param birthday
     *            to set.
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * 
     * @return the {@link EGender} of the {@link User}.
     */
    public EGender getGender() {
        return this.gender;
    }

    /**
     * Set the {@link EGender} of the {@link User}.
     * 
     * @param gender
     *            to set.
     */
    public void setGender(EGender gender) {
        this.gender = gender;
    }
    
    /**
     * 
     * @return the street of the {@link User}.
     */
    public String getStreet() {
        return this.street;
    }

    /**
     * Set the street of the {@link User}.
     * 
     * @param street
     *            to set.
     */
    public void setStreet(String street) {
        this.street = street;
    }
    
    /**
     * 
     * @return the zip code of the {@link User}.
     */
    public String getZip() {
        return this.zip;
    }

    /**
     * Set the zip code of the {@link User}.
     * 
     * @param zip code
     *            to set.
     */
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    /**
     * 
     * @return the city of the {@link User}.
     */
    public String getCity() {
        return this.city;
    }

    /**
     * Set the city of the {@link User}.
     * 
     * @param city
     *            to set.
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
     * 
     * @return the country of the {@link User}.
     */
    public ECountry getCountry() {
        return this.country;
    }

    /**
     * Set the country of the {@link User}.
     * 
     * @param country
     *            to set.
     */
    public void setCountry(ECountry country) {
        this.country = country;
    }
    
    /**
     * 
     * @return the phone number of the {@link User}.
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Set the phone number of the {@link User}.
     * 
     * @param phone number
     *            to set.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * 
     * @return the fax number of the {@link User}.
     */
    public String getFax() {
        return this.fax;
    }

    /**
     * Set the fax number of the {@link User}.
     * 
     * @param fax number
     *            to set.
     */
    public void setFax(String fax) {
        this.fax = fax;
    }
    
    /**
     * 
     * @return the website of the {@link User}.
     */
    public String getWebsite() {
        return this.website;
    }

    /**
     * Set the website of the {@link User}.
     * 
     * @param website
     *            to set.
     */
    public void setWebsite(String website) {
        this.website = website;
    }
    
    /**
     * 
     * @return if the {@link User}'s optional data is visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Set wheter the {@link User}'s optional data is visible or not.
     * 
     * @param visible
     *            is true if the {@link User} should be banned, false otherwise.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * 
     * @return the {@link EUserType} of the {@link User}.
     */
    public EUserType getUserType() {
        return this.userType;
    }

    /**
     * Set the {@link EUserType} of the {@link User}.
     * 
     * @param userType
     *            to set.
     */
    public void setUserType(EUserType userType) {
        this.userType = userType;
    }

    /**
     * 
     * @return true if the {@link User} is banned, false otherwise.
     */
    public boolean isBanned() {
        return this.banned;
    }

    /**
     * Set whether the {@link User} is banned or not.
     * 
     * @param banned
     *            is true if the {@link User} should be banned, false otherwise.
     */
    public void setBanned(boolean banned) {
        this.banned = banned;
    }
    
    /**
     * 
     * @return the uploaded photo of the {@link User}.
     */
    public Part getPhoto() {  
        return this.photo;  
    }  
  
    /**
     * Set the uploaded photo of the {@link User}.
     * 
     * @param phto
     *            to set.
     */
    public void setPhoto(Part photo) {  
        this.photo = photo;  
    }   
    
    /**
     * 
     * @return if the {@link User} has uploaded a photo.
     */
    public boolean isPhotoAvailable() {
        return this.photoAvailable;
    }

    /**
     * Set wheter the {@link User} has uploaded a photo.
     * 
     * @param photoAvailable
     *            is true if the {@link User} has uploaded a photo, false otherwise.
     */
    public void setPhotoAvailable(boolean photoAvailable) {
        this.photoAvailable = photoAvailable;
    }
}
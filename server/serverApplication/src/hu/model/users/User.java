package hu.model.users;

import hu.util.ECountry;
import hu.util.SecurityUtils;

import java.util.Date;

/**
 * This class represents a user registered within the system.
 */
public class User {
    private Integer id = null;
    private String passwordHash = null;
    private String email = null;
    private String title = null;
    private String firstName = null;
    private String lastName = null;
    private EUserType userType = null;
    private EGender gender = null;
    private Date birthday = null;
    private boolean banned = false;
    private boolean deletable = true;
    private String street;
    private String zip;
    private String city;
    private ECountry country;
    private String phone;
    private String fax;
    private String website;
    private boolean visible;
    private boolean photoAvailable;
    private String secretKey;
    
    /**
     * Create a {@link User} with a id.
     * 
     * @param id
     *            to set.
     */
    public User(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return the id of the {@link User}.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * 
     * @return the hashed password of the {@link User}.
     */
    public String getPasswordHash() {
        return this.passwordHash;
    }

    /**
     * Set the hashed password of the {@link User}.
     * 
     * @param passwordHash
     *            to set.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * 
     * @return always return null.
     */
    public String getPassword() {
        return null;
    }

    /**
     * Set the password of a {@link User}.
     * 
     * @param password
     *            to set.
     */
    public void setPassword(String password) {
        this.passwordHash = this.hash(password);
    }

    /**
     * Check if input matches the {@link User}s password.
     * 
     * @param password
     *            to check.
     * @return true if the input matches the {@link User}s password, false
     *         otherwise.
     */
    public boolean hasPassword(String password) {
        return this.passwordHash.equals(this.hash(password));
    }

    /**
     * 
     * @return the email address of the {@link User}.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Set the email of the {@link User}.
     * 
     * @param email
     *            to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     * @return the title of the {@link User}.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set the title of the {@link User}.
     * 
     * @param title
     *            to set.
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return the {@link EUserType} that defines the rights of the {@link User}
     *         .
     */
    public EUserType getUserType() {
        return this.userType;
    }

    /**
     * Set the {@link EUserType} that defines the rights of the {@link User}.
     * 
     * @param userType
     *            to set.
     */
    public void setUserType(EUserType userType) {
        this.userType = userType;
    }

    /**
     * 
     * @return the gender of the {@link User}.
     */
    public EGender getGender() {
        return this.gender;
    }

    /**
     * Set the gender of the {@link User}.
     * 
     * @param gender
     *            to set.
     */
    public void setGender(EGender gender) {
        this.gender = gender;
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
     * @return if the {@link User} has uploaded a photo.
     */
    public boolean isPhotoAvailable() {
        return this.photoAvailable;
    }

    /**
     * Set wheter the {@link User} has uploaded a photo.
     * 
     * @param visible
     *            is true if the {@link User} has uploaded a photo, false otherwise.
     */
    public void setPhotoAvailable(boolean photoAvailable) {
        this.photoAvailable = photoAvailable;
    }

    /**
     * 
     * @return the secret key of the {@link User}.
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * Set the secret key of the {@link User}.
     * 
     * @param website
     *            to set.
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
    
    /**
     * 
     * @return true if the {@link User} is banned from the web application,
     *         false otherwise.
     */
    public boolean isBanned() {
        return this.banned;
    }

    /**
     * Set whether the {@link User} is banned from the web application or not.
     * 
     * @param banned
     *            is true if the {@link User} is banned from the web
     *            application, false otherwise.
     */
    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    /**
     * 
     * @return true if an administrator can delete the {@link User}, false
     *         otherwise.
     */
    public boolean isDeletable() {
        return this.deletable;
    }

    /**
     * Set whether an administrator can delete the {@link User} or not.
     * 
     * @param deletable
     *            is true if an administrator can delete the {@link User}, false
     *            otherwise.
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @Override
    public String toString() {
        return String.format("User (%d) %s %s", this.id, this.firstName,
                this.lastName);
    }

    private String hash(String password) {
        return SecurityUtils.hash(password);
    }

    @Override
    public int hashCode() {
        return (this.id == null) ? 0 : this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
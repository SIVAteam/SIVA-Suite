package hu.controller.users;

import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.groups.GroupListBean;
import hu.backingbeans.groups.GroupListBean.GroupListEntryBean;
import hu.backingbeans.users.ResetPasswordBean;
import hu.backingbeans.users.UserBean;
import hu.controller.AController;
import hu.controller.groups.EGroupListAction;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.persistence.InconsistencyException;
import hu.persistence.NotFoundException;
import hu.util.BrandingConfiguration;
import hu.util.CommonUtils;
import hu.util.Configuration;
import hu.util.MailService;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.http.Part;

/**
 * This class provides all the {@link User} related functionality such as
 * creating/registering or deleting a {@link User}. In addition to that it
 * handles the process of profile editing and the function to ban/unban a
 * {@link User}.
 */
@ManagedBean
@RequestScoped
public class UserAction extends AController {
	
	private static final int PHOTO_WIDTH = 200;
	private static final int PHOTO_HEIGHT = 200;
	private static final String PHOTO_DESTINATION =  System.getProperty("user.home")
            + "/.sivaServer/photos/";

    private static final String REGISTER_USER_FACELET = "/xhtml/users/register";
    private static final String EDIT_PROFILE_FACELET = "/xhtml/users/editProfile";
    private static final String CREATE_USER_FACELET = "xhtml/users/createUser";
    private static final String EDIT_USER_FACELET = "/xhtml/users/editUser";
    private static final String LOGIN_FACELET = "/xhtml/users/login";
    private static final String LIST_USERS_FACELET = "/xhtml/users/listUsers";
    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
    private static final String HELP_FACELET = "/xhtml/common/help.jsf";
    private static final String SET_NEW_PASSWORD_FACELET = "/xhtml/users/setNewPassword.jsf";
    
    private static final String CREATE_USER_FORM = "createUserForm:email";
    private static final String LOGIN_FORM = "loginForm";
    private static final String REGISTER_FORM = "registerForm";
    private static final String LIST_USERS_TABLE = "listUsersTable";
    private static final String LIST_GROUPS_TABLE = "listGroupsTable";
    private static final String EDIT_PROFILE_FORM = "editProfileForm";
    private static final String EDIT_USER_FORM = "editUserForm";
    private static final String RECOVER_PASSWORD_FORM = "recoverPasswordForm";
    
    private static final String EMAIL_FIELD = ":email";
    private static final String PASSWORD_FIELD = ":password";
        
    private static final String MSG_USER_UNBAN_FAILED = "unbanUser_failed";
    private static final String MSG_USER_UNBANNED = "unbanUser_user_unbanned";
 
    private static final String MSG_USER_BAN_FAILED = "banUser_failed";
    private static final String MSG_USER_BANNED = "banUser_user_banned";

    private static final String MSG_USER_NOT_DELETED = "listUsers_user_not_deleted";
    private static final String MSG_USER_DELETED = "listUsers_user_deleted";
    private static final String MSG_EMAIL_EXISTS = "createUser_email_already_exists";
    private static final String MSG_PASSWORD_CONFIRM_FALSE = "password_confirm_false";
    private static final String MSG_USER_COULD_NOT_BE_REGISTERED = "user_could_not_be_registered";
    private static final String MSG_REGISTER_SUCCESS = "register_success";
    private static final String MSG_CREATE_USER_SUCCESS = "createUser_success";
    private static final String MSG_EDIT_PROFILE_SUCCESS = "editProfile_success";
    private static final String MSG_PROFILE_COULD_NOT_BE_EDITED = "editProfile_failed";
    private static final String MSG_SEND_EMAIL_PASSWORD_SUBJECT = "send_mail_create_user_password_subject";
    private static final String MSG_SEND_EMAIL_PASSWORD_BODY = "send_mail_create_user_password";
    private static final String MSG_SEND_EMAIL_UPDATE_PASSWORD_SUBJECT = "send_mail_update_user_password_subject";
    private static final String MSG_SEND_EMAIL_UPDATE_PASSWORD_BODY = "send_mail_update_user_password";
    private static final String MSG_ADMIN_MAY_NOT_DOWNGRADE = "not_deleteable_user_can_not_downgrade";
    private static final String MSG_RECOVER_PASSWORD_SUCCESS = "recoverPassword_success"; 
    private static final String MSG_RECOVER_PASSWORD_FAILED = "recoverPassword_failed";
    private static final String MSG_EMAIL_SUBJECT = "recoverPassword_email_subject";
    private static final String MSG_EMAIL_TEXT = "recoverPassword_email_text";
    private static final String MSG_SET_NEW_PASSWORD_FAILED = "setNewPassword_failed";
    private static final String MSG_SET_NEW_PASSWORD_SUCCESS = "setNewPassword_success";
    private static final String MSG_SET_NEW_PASSWORD_SAVE_FAILED = "setNewPassword_save_failed";
    private static final String MSG_EMAIL_SUBJECT_REGISTER = "email_subject_register";
    private static final String MSG_EMAIL_REGISTER_BODY = "email_register_message";
    private static final String MSG_PASSWORDS_NOT_EQUAL = "password_confirm_false";
    private static final String MSG_PHOTO_ERROR = "photoValidator_message";
    private static final String MSG_ATTENDANT_ADDED = "manageGroups_attendant_added";
    private static final String MSG_ATTENDANT_ADDED_FAILED = "manageGroups_attendant_not_added";
    private static final String MSG_ATTENDANT_REMOVED = "manageGroups_attendant_removed";
    private static final String MSG_ATTENDANT_REMOVED_FAILED = "manageGroups_attendant_not_removed";
    private static final String MSG_TUTOR_ADDED = "manageGroups_tutor_added";
    private static final String MSG_TUTOR_ADDED_FAILED = "manageGroups_tutor_not_added";
    private static final String MSG_TUTOR_REMOVED = "manageGroups_tutor_removed";
    private static final String MSG_TUTOR_REMOVED_FAILED = "manageGroups_tutor_not_removed";
    
    private static final String SQLERR_ADMIN_DOWNGRADE = "HuConstraint0002";
    
    @ManagedProperty("#{userBean}")
    private UserBean userBean;
    
    @ManagedProperty("#{groupBean}")
    private GroupBean groupBean;
    
    @ManagedProperty("#{groupListBean}")
    private GroupListBean groupListBean;
    
    @ManagedProperty("#{resetPasswordBean}")
    private ResetPasswordBean resetPasswordBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{mailService}")
    private MailService mailService;

    @ManagedProperty("#{brandingConfiguration}")
    private BrandingConfiguration brandingConfiguration;

    @ManagedProperty("#{sessionData}")
    private SessionData session;
    
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    private File mockedPhotoFile = null;

    /**
     * Set {@link ResetPasswordBean} using injection.
     * @param resetPasswordBean
     *            to inject.
     */
    public void setResetPasswordBean(ResetPasswordBean resetPasswordBean) {
        this.resetPasswordBean = resetPasswordBean;
    }

    /**
     * Set {@link UserBean} using injection.
     * 
     * @param userBean
     *            to inject.
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    /**
     * Set {@link GroupBean} using injection.
     * 
     * @param groupBean
     *            to inject.
     */
    public void setGroupBean(GroupBean groupBean) {
        this.groupBean = groupBean;
    }

    /**
     * Set {@link GroupListBean} using injection.
     * 
     * @param groupListBean
     *            to inject.
     */
    public void setGroupListBean(GroupListBean groupListBean) {
        this.groupListBean = groupListBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }
    
    /**
     * Set {@link MailService} using injection.
     * 
     * @param mailService
     *            to inject.
     */
    public void setBrandingConfiguration(BrandingConfiguration brandingConfiguration) {
        this.brandingConfiguration = brandingConfiguration;
    }

    /**
     * Set {@link MailService} using injection.
     * 
     * @param mailService
     *            to inject.
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Set {@link SessionData} using injection.
     * 
     * @param session
     *            to inject.
     */
    public void setSession(SessionData session) {
        this.session = session;
    }
    
    /**
     * Set {@link Configuration} using injection for configuration file access.
     * 
     * @param configuration
     *            to inject.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Save the id of the {@link User}.
     * 
     * @return always null.
     */
    public String recoverId() {
        Integer userId = null;
        if (userBean.getId() != null) {
            userId = userBean.getId();
        } else if (userBean.getHiddenUserIdValue() != null) {
            try {
                userId = Integer.parseInt((String) userBean.getHiddenUserIdValue());
            } catch (NumberFormatException ignored) {}
        }
        userBean.setId(userId);
        userBean.setHiddenUserIdValue(userId);
        return null;
    }

    /**
     * Let a user register by himself and create a new entry for him in the
     * database.
     * 
     * Only users of {@link EUserType#Anonymous} have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String registerUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        User newUser = null;

        // check for duplicate mail
        User checkMail = userStore.findByEmail(this.userBean.getEmail());

        if (checkMail != null
                && !checkMail.getId().equals(this.userBean.getId())) {
            // keep message
            if (this.getCurrentFcInstance().getExternalContext() != null) {
                this.getCurrentFcInstance().getExternalContext().getFlash()
                        .setKeepMessages(true);
            }
            this.sendMessageTo(REGISTER_FORM + EMAIL_FIELD,
                    this.getCommonMessage(MSG_EMAIL_EXISTS));
            this.redirectTo(REGISTER_USER_FACELET);
            return null;
        }

        // check password confirm
        if (!this.userBean.getPassword().equals(
                this.userBean.getPasswordRepeat())) {
            this.sendMessageTo(REGISTER_FORM + PASSWORD_FIELD,
                    this.getCommonMessage(MSG_PASSWORD_CONFIRM_FALSE));
            return REGISTER_USER_FACELET;
        }
        newUser = createNewUserObject();

        // create data record for the new user
        try {
            newUser = userStore.create(newUser);
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(MSG_USER_COULD_NOT_BE_REGISTERED);
            return LOGIN_FACELET;
        }
        
        // move uploaded photo
        if(this.userBean.getPhoto() != null || this.mockedPhotoFile != null){
        	if(!this.saveResizedPhoto(newUser.getId())){
        		this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_PHOTO_ERROR));
                return null;
        	}
        	newUser.setPhotoAvailable(true);
        	try {
				userStore.save(newUser);
			} catch (InconsistencyException e) {
				this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_PHOTO_ERROR));
                return null;
			}
        }
        
        // send confirmation email
        String path;
        try {
            path = CommonUtils.buildContextPath(HELP_FACELET, null);
        } catch (MalformedURLException e) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        } catch (URISyntaxException e) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        this.mailService.sendMail(this.userBean.getEmail(), String.format(this
                .getCommonMessage(MSG_EMAIL_SUBJECT_REGISTER), this.brandingConfiguration.getBrandingText("project_name")), String.format(
                this.getCommonMessage(MSG_EMAIL_REGISTER_BODY), this.brandingConfiguration.getBrandingText("project_name"),
                this.userBean.getEmail(), path));

        this.sendMessageTo(LOGIN_FORM,
                this.getCommonMessage(MSG_REGISTER_SUCCESS));
        return LOGIN_FACELET;
    }

    /**
     * Create a {@link User} and a new entry in the database manually.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} have permission to
     * use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String createUser() {

        IUserStore userStore = this.persistenceProvider.getUserStore();

        // check for duplicate mail
        User checkMail = userStore.findByEmail(this.userBean.getEmail());

        if (checkMail != null
                && !checkMail.getId().equals(this.userBean.getId())) {
            this.sendMessageTo(CREATE_USER_FORM,
                    this.getCommonMessage(MSG_EMAIL_EXISTS));
            return CREATE_USER_FACELET;
        }
        User newUser = createNewUserObject();

        //create data record for the new user
        try {
            userStore.create(newUser);
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(MSG_USER_COULD_NOT_BE_REGISTERED);
            return LIST_USERS_FACELET;
        }
        this.sendMessageTo(LIST_USERS_TABLE,
                this.getCommonMessage(MSG_CREATE_USER_SUCCESS));
        return LIST_USERS_FACELET;
    }

    /**
     * Let a {@link User} edit his own profile.
     * 
     * All {@link User}s have permission to edit the own profile.
     * 
     * {@link User}s of {@link EUserType#Administrator} have permission to edit
     * every profile.
     * 
     * @return the next page to show as {@link String}.
     */
    public String editProfile() {

        IUserStore userStore = this.persistenceProvider.getUserStore();

        User user = null;
        if (session.getUserId() != null) {
            user = userStore.getById(this.session.getUserId());
        } else {
            return RESTRICTION_ERROR_FACELET;
        }

        FacesContext fcxt = this.getCurrentFcInstance();

        // is prepopulation needed?
        if (!fcxt.isPostback() && !fcxt.isValidationFailed()) {
            this.populateUserBean(user);
        } else if (fcxt.isPostback() && fcxt.isValidationFailed()) {
            return null;
        } else {
            // check for duplicate mail
            User checkMail = userStore.findByEmail(this.userBean.getEmail());

            if (checkMail != null
                    && !this.session.getUserId().equals(checkMail.getId())) {
                this.sendMessageTo(EDIT_PROFILE_FORM + EMAIL_FIELD,
                        this.getCommonMessage(MSG_EMAIL_EXISTS));
                return EDIT_PROFILE_FACELET;
            }

            // check password confirm
            if (this.userBean.getPassword() != null
                    && !this.userBean.getPassword().equals(
                            this.userBean.getPasswordRepeat())) {
                this.sendMessageTo(EDIT_PROFILE_FORM + PASSWORD_FIELD,
                        this.getCommonMessage(MSG_PASSWORD_CONFIRM_FALSE));
                return EDIT_PROFILE_FACELET;
            } else {
                //all checks ok, update user
                user = new User(session.getUserId());
                user = this.updateUserObject(user);

                User oldDataset = userStore.getById(this.session.getUserId());
                user.setUserType(oldDataset.getUserType());
                user.setPhotoAvailable(oldDataset.isPhotoAvailable());
                
             // Move uploaded photo if there has been uploaded one or if it's a unit test
				if (this.userBean.getPhoto() != null || this.mockedPhotoFile != null) {
					if (!this.saveResizedPhoto(user.getId())) {
						this.sendGlobalMessageToFacelet(this
								.getCommonMessage(MSG_PHOTO_ERROR));
						return null;
					}
					user.setPhotoAvailable(true);
				}

                // store to database
                try {
                    userStore.save(user);
                    // keep message
                    if (this.getCurrentFcInstance().getExternalContext() != null) {
                        this.getCurrentFcInstance().getExternalContext().getFlash()
                                .setKeepMessages(true);
                    }
                    this.sendMessageTo(EDIT_PROFILE_FORM,
                            this.getCommonMessage(MSG_EDIT_PROFILE_SUCCESS));
                } catch (InconsistencyException e) {
                    this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_PROFILE_COULD_NOT_BE_EDITED));
                }
            }
        }
        return EDIT_PROFILE_FACELET + "?faces-redirect=true";
    }

    /**
     * Edit a {@link User}.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} have permission to
     * use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String editUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        User user = null;
        FacesContext fcxt = this.getCurrentFcInstance();
        
        if (this.userBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        user = userStore.findById(this.userBean.getId());
        
        if (user == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // is prepopulation needed?
        if (!fcxt.isPostback() && !fcxt.isValidationFailed()) {
            this.populateUserBean(user);
        } else if (fcxt.isPostback() && fcxt.isValidationFailed()) {
            return null;
        } else {

            // check for duplicate mail
            User checkMail = userStore.findByEmail(this.userBean.getEmail());

            if (checkMail != null
                    && !checkMail.getId().equals(userBean.getId())) {

                // keep messages for display
                if (this.getCurrentFcInstance().getExternalContext() != null) {
                    this.getCurrentFcInstance().getExternalContext().getFlash()
                            .setKeepMessages(true);
                }
                this.sendMessageTo(EDIT_USER_FORM + EMAIL_FIELD,
                        this.getCommonMessage(MSG_EMAIL_EXISTS));
                return EDIT_USER_FACELET;
            }

            user = new User(this.userBean.getId());
            user = this.updateUserObject(user);
            
            User oldDataset = userStore.getById(this.userBean.getId());
            user.setPhotoAvailable(oldDataset.isPhotoAvailable());
            
            // move uploaded photo
			if (this.userBean.getPhoto() != null) {
				if (!this.saveResizedPhoto(user.getId())) {
					this.sendGlobalMessageToFacelet(this
							.getCommonMessage(MSG_PHOTO_ERROR));
					return null;
				}
				user.setPhotoAvailable(true);
			}

            // store to database
            try {
                this.prepareForUpDowngrade(user);
                userStore.save(user);

                this.sendMessageTo(LIST_USERS_TABLE,
                        this.getCommonMessage(MSG_EDIT_PROFILE_SUCCESS));

                return LIST_USERS_FACELET;
            } catch (InconsistencyException e) {
                // keep message
                if (this.getCurrentFcInstance().getExternalContext() != null) {
                    this.getCurrentFcInstance().getExternalContext().getFlash()
                            .setKeepMessages(true);
                }
                if (e.getMessage().contains(SQLERR_ADMIN_DOWNGRADE)) {
                    this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_ADMIN_MAY_NOT_DOWNGRADE));
                }

                this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_PROFILE_COULD_NOT_BE_EDITED));
            }
        }
        this.redirectTo(EDIT_USER_FACELET);
        return null;
    }
    
    /**
	 * Provides the {@link User}'s public profile data.
	 * 
	 * If the {@link User} is a Administrator, he even can see private data.
	 * 
	 * @return an empty string.
	 */
	public String getPublicProfile() {
		IUserStore userStore = this.persistenceProvider.getUserStore();
		User user = null;
		User sessionUser = null;

		// Check if the url contains a user id and redirect if not
		if (this.userBean.getId() == null) {
			this.redirectTo(RESTRICTION_ERROR_FACELET);
			return null;
		}

		// Get the user's data from the database
		user = userStore.findById(this.userBean.getId());

		// Check if this users exists, redirect if not
		if (user == null) {
			this.redirectTo(RESTRICTION_ERROR_FACELET);
			return null;
		}

		if (this.session.getUserId() != null) {
			// Get the user who is currently watching the profile
			sessionUser = userStore.findById(this.session.getUserId());
			if (sessionUser == null) {
				this.redirectTo(RESTRICTION_ERROR_FACELET);
				return null;
			}
		}

		// Set profile data that every user is allowed to see
		this.userBean.setAcademicTitle(user.getTitle());
		this.userBean.setFirstName(user.getFirstName());
		this.userBean.setLastName(user.getLastName());
		this.userBean.setGender(user.getGender());
		this.userBean.setCountry(user.getCountry());
		this.userBean.setUserType(user.getUserType());

		// Check if user wants to publish further data or if the currently
		// watching user is a administrator
		if (user.isVisible()
				|| (sessionUser != null && sessionUser.getUserType().equals(
						EUserType.Administrator))) {
			this.userBean.setStreet(user.getStreet());
			this.userBean.setZip(user.getZip());
			this.userBean.setCity(user.getCity());
			this.userBean.setPhone(user.getPhone());
			this.userBean.setFax(user.getFax());
			this.userBean.setWebsite(("http://" + user.getWebsite()).replace(
					"http://http://", "http://"));
			this.userBean.setPhotoAvailable(user.isPhotoAvailable());
			this.userBean.setBirthday(user.getBirthday());
		}

		// Check if the currently watching user is an administrator and publish
		// the email address if so
		if (sessionUser != null
				&& sessionUser.getUserType().equals(EUserType.Administrator)) {
			this.userBean.setEmail(user.getEmail());
		}

		return "";
	}

    /**
     * Delete a {@link User} from the database.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} have permission to
     * use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String deleteUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
       
            try {
                userStore.delete(userBean.getId());
                File photo = new File(PHOTO_DESTINATION + userBean.getId() + ".jpg");
                photo.delete();
                this.sendMessageTo(LIST_USERS_TABLE,
                        this.getCommonMessage(MSG_USER_DELETED));

            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this.getCommonMessage(MSG_USER_NOT_DELETED));
                return LIST_USERS_FACELET;
            }

        return LIST_USERS_FACELET;

    }

    /**
     * Ban a {@link User} to restrain him from logging in.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} have permission to
     * use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String banUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();

        User userToBan = userStore.findById(userBean.getId());

        FacesContext fctx = this.getCurrentFcInstance();

        if (userToBan.isBanned()) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        if (!fctx.isPostback()) {
            return null;
        }

        userToBan.setBanned(true);
        try {
            userStore.save(userToBan);
            this.sendMessageTo(LIST_USERS_TABLE,
                    this.getCommonMessage(MSG_USER_BANNED));
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_USER_BAN_FAILED));
            return LIST_USERS_FACELET;
        }
        return LIST_USERS_FACELET;

    }

    /**
     * Check if the user has permission to use this page, and if he has,
     * populate the user who has to be edited (banned, unbanned, deleted).
     * 
     * @return always null
     */
    public String restriction() {
        IUserStore userStore = this.persistenceProvider.getUserStore();

        User user = userStore.getById(session.getUserId());

        if (userBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        User userToUnban = userStore.findById(userBean.getId());

        if (userToUnban == null
                || (user.equals(userToUnban) || !(user.getUserType() == EUserType.Administrator))) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        this.userBean.setAcademicTitle(userToUnban.getTitle());
        this.userBean.setFirstName(userToUnban.getFirstName());
        this.userBean.setLastName(userToUnban.getLastName());

        return null;
    }
    
    /**
     * Unban a {@link User} to give him back the possibility to login.
     * 
     * Only {@link User}s of {@link EUserType#Administrator} have permission to
     * use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String unbanUser() {
        IUserStore userStore = this.persistenceProvider.getUserStore();
  
        User userToUnban = userStore.findById(userBean.getId());

        FacesContext fctx = this.getCurrentFcInstance();
        
        //is user already unbanned
        if(!userToUnban.isBanned()){
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
            
        if (!fctx.isPostback()) {
            return null;
        }
            userToUnban.setBanned(false);
            try {
                userStore.save(userToUnban);
                this.sendMessageTo("listUsersTable",
                        this.getCommonMessage(MSG_USER_UNBANNED));
            } catch (InconsistencyException e) {
                this.sendGlobalMessageToFacelet(this
                        .getCommonMessage(MSG_USER_UNBAN_FAILED));
                return LIST_USERS_FACELET;
            }
            return LIST_USERS_FACELET;

    }

    
    /**
     * Send a unique link to the user for changing the password.
     * The user has two days after sending the link to set a new password.
     * The hash will be generated by {@link SecurityUtils}. The string which
     * will be hashed consits of the current date(DDMMJJJJ) two days later
     * the password and the old password of the user.
     *
     * Only users of {@link EUserType#Anonymous} have permission to use.
     * But the user have to be registered.
     *
     * @return the next page to show as {@link String}.
     */
    public String sendPasswordRecoveryLink() {
        IUserStore userStore = persistenceProvider.getUserStore();
        User user;
        try {
            user = userStore.getByEmail(userBean.getEmail());
        } catch (NotFoundException e) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_RECOVER_PASSWORD_FAILED));
            return null;
        }

        // Build string that is hashed
        Calendar date = new GregorianCalendar();
            // Set Scope to set a new password
        date.add(GregorianCalendar.DAY_OF_MONTH, +2);
        String email = user.getEmail();
        String key = generateHash(date, user);

        //Build link
        
        /*FacesContext ctx = this.getCurrentFcInstance();
        String path = ctx.getExternalContext().getRequestContextPath();
        String host = ctx.getExternalContext().getRequestServerName();
        String protocol = ctx.getExternalContext().getRequestScheme();
        Integer port = ctx.getExternalContext().getRequestServerPort();*/
        String parameter = "hash="+key+"&email="+email;
        String url;
        try {
            url = CommonUtils.buildContextPath(SET_NEW_PASSWORD_FACELET, parameter);
        } catch (MalformedURLException e) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        } catch (URISyntaxException e) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // use multiple mail to set Link inside the text.
        String link = url.replace("@", "%40");
        String subject = this.brandingConfiguration.getBrandingText("project_name")+" ";
        subject += this.getCommonMessage(MSG_EMAIL_SUBJECT);
        Map<String,String> recipient = new HashMap<String, String>();
        recipient.put(email, link);
        mailService.sendMultipleMail(recipient,
                subject,
                this.getCommonMessage(MSG_EMAIL_TEXT));

        this.sendMessageTo(RECOVER_PASSWORD_FORM,this
                .getCommonMessage(MSG_RECOVER_PASSWORD_SUCCESS));
        return null;
    }
    
    /**
     * Prepopulation-Function for setNewPassword.xhtml
     * Test if required parameters are given. If not redirect to
     * RESTRICTION_ERROR_FACELET else show page.
     * @return
     */
    public String prepopulateSaveNewPassword() {
        FacesContext fcxt = this.getCurrentFcInstance();
        if (fcxt.isPostback() && fcxt.isValidationFailed()) {
            return null;
        }
        String hash = resetPasswordBean.getHash();
        String email = resetPasswordBean.getEmail();
        if ((hash == null || email == null
                || hash.equals("") || email.equals("")) 
                && resetPasswordBean.getPassword() == null ) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        return null;
    }

    /**
     * Save the new password a user chose.
     *
     * Only users of {@link EUserType#Anonymous} have permission to use.
     *
     * @return the next page to show as {@link String}.
     */
    public String saveNewPassword() {
        // Check if given email exists
        String givenEmail = resetPasswordBean.getEmail();
        IUserStore userStore = persistenceProvider.getUserStore();
        User user;
        try {
            user = userStore.getByEmail(givenEmail);
        } catch (NotFoundException e) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_SET_NEW_PASSWORD_FAILED));
            return null;
        }

        // Check if passwords a equal
        String newPassword = resetPasswordBean.getPassword();
        String newPasswortRepeat = resetPasswordBean.getPasswordRepeat();
        if (!newPassword.equals(newPasswortRepeat)) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_PASSWORDS_NOT_EQUAL));
            return null;
        }

        // check if given hash is valid
        String givenHash = resetPasswordBean.getHash();
        Calendar date = new GregorianCalendar();
        String hashToday = this.generateHash(date,user);
        date.add(GregorianCalendar.DAY_OF_MONTH, +1);
        String hashTomorrow = this.generateHash(date,user);
        date.add(GregorianCalendar.DAY_OF_MONTH, +1);
        String hashTwoDays = this.generateHash(date, user);
        if (!givenHash.equals(hashToday)
                && !givenHash.equals(hashTomorrow)
                && !givenHash.equals(hashTwoDays)) {
            this.sendGlobalMessageToFacelet(this
                    .getCommonMessage(MSG_SET_NEW_PASSWORD_FAILED));
            return null;
        }

        //Save new Password
        user.setPassword(newPassword);
        try {
            userStore.save(user);
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(MSG_SET_NEW_PASSWORD_SAVE_FAILED);
        }

        if (FacesContext.getCurrentInstance() != null) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .getFlash().setKeepMessages(true);
        }

        this.sendMessageTo(LOGIN_FORM,this
                .getCommonMessage(MSG_SET_NEW_PASSWORD_SUCCESS));
        return LOGIN_FACELET;
    }
    
    /**
     * Show a list of {@link Group}s which can contain the
     * {@link user}.
     * 
     * @return the next page to show as {@link String}.
     */
    public String manageGroups() {
	IUserStore userStore = this.persistenceProvider.getUserStore();
	IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        
	// Verify that an user id is provided.
        if (this.userBean.getId() == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        // Check if group exists, redirect if not.
        User user = userStore.findById(this.userBean.getId());
        if (user == null) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }
        
        // Do not run if a search was submitted.
        if (!this.getCurrentFcInstance().isPostback()) {
            
            // Check if an action has to be performed and process action if so
            if(this.groupListBean.getAction() != null){
            
        	// Verify that an group id is provided.
                if (this.groupBean.getId() == null) {
                    this.redirectTo(RESTRICTION_ERROR_FACELET);
                    return null;
                }

                // Check if group exists, redirect if not.
                Group group = groupStore.findById(this.groupBean.getId());
                if (group == null) {
                    this.redirectTo(RESTRICTION_ERROR_FACELET);
                    return null;
                }
        	
                // Perform add/remove operation
                if (this.groupListBean.getAction() == EGroupListAction.addAttendant) {
                    try {
                        groupStore.addAttendant(group, user);
                        this.sendMessageTo(LIST_GROUPS_TABLE,
                                this.getCommonMessage(MSG_ATTENDANT_ADDED));
                    } catch (InconsistencyException e) {
                        this.sendGlobalMessageToFacelet(this
                                .getCommonMessage(MSG_ATTENDANT_ADDED_FAILED));
                        return LIST_USERS_FACELET;
                    }
                } else if (this.groupListBean.getAction() == EGroupListAction.removeAttendant) {
                    try {
                        groupStore.removeAttendant(group, user);
                        this.sendMessageTo(LIST_GROUPS_TABLE,
                                this.getCommonMessage(MSG_ATTENDANT_REMOVED));
                    } catch (InconsistencyException e) {
                        this.sendGlobalMessageToFacelet(this
                                .getCommonMessage(MSG_ATTENDANT_REMOVED_FAILED));
                        return LIST_USERS_FACELET;
                    }
                } else if (this.groupListBean.getAction() == EGroupListAction.addTutor) {
                    try {
                        groupStore.addOwner(group, user);
                        this.sendMessageTo(LIST_GROUPS_TABLE,
                                this.getCommonMessage(MSG_TUTOR_ADDED));
                    } catch (InconsistencyException e) {
                        this.sendGlobalMessageToFacelet(this
                                .getCommonMessage(MSG_TUTOR_ADDED_FAILED));
                        return LIST_USERS_FACELET;
                    }
                } else if (this.groupListBean.getAction() == EGroupListAction.removeTutor) {
                    try {
                        groupStore.removeOwner(group, user);
                        this.sendMessageTo(LIST_GROUPS_TABLE,
                                this.getCommonMessage(MSG_TUTOR_REMOVED));
                    } catch (InconsistencyException e) {
                        this.sendGlobalMessageToFacelet(this
                                .getCommonMessage(MSG_TUTOR_REMOVED_FAILED));
                        return LIST_USERS_FACELET;
                    }
                }
            }
        }
        
        // Fetch current user, to decide which buttons are visible and which
        // groups are attended.
        boolean isAdmin = user.getUserType() == EUserType.Administrator;
        boolean isTutor = user.getUserType() == EUserType.Tutor;

        // Decide which tabs to show.
         // Show attended groups for authenticated users.
        groupListBean.setAttendedAvailable(user != null);
        // Show owned groups for tutors and admins.
        groupListBean.setOwnedAvailable(isTutor || isAdmin);
        // Show all groups for all users
        groupListBean.setAllAvailable(true);

        // Parse GET parameters.
        ESortDirection direction = (this.groupListBean.getSortDirection() != null)
                ? this.groupListBean.getSortDirection() : ESortDirection.ASC; 
        boolean showVisible = "visible".equals(this.groupListBean.getListShown());
        boolean showAttended = "attended".equals(this.groupListBean.getListShown());
        boolean showOwned = "owned".equals(this.groupListBean.getListShown());
        boolean showAll = "all".equals(this.groupListBean.getListShown());
        int currentPage = (this.groupListBean.getPage() != null)
                ? this.groupListBean.getPage() : 0;

        // Only tutor and admin can own groups.
        if (!isAdmin && !isTutor) {
            showOwned = false;
        }

        // By default, show visible groups.
        if (!(showAttended || showOwned || showAll || showVisible)) {
            showAll = true;
            // Also reset parameter string to default value.
            // This is required because we use this parameter in the pagination tag
            // which does not filter data properly.
            this.groupListBean.setListShown("all");
        }

        // Calculate number of pages for the current tab.
        // Count groups.
        int numGroups = 0;
        if (showAll || showVisible) {
            numGroups = groupStore.getCountOfAll(true, true, user);
        } else if (showAttended) {
            numGroups = groupStore.getCountByAttendant(user);
        } else if (showOwned) {
            numGroups = groupStore.getCountByOwner(user);
        }
        // Determine how many pages are needed.
        int maxEntries = Math.max(1, this.configuration.getInteger("max_rows_per_table"));
        int numPages = numGroups / maxEntries
                + ((numGroups % maxEntries == 0) ? 0 : 1);
        this.groupListBean.setPages(numPages);
        // Bound the current page.
        currentPage = Math.max(0, Math.min(currentPage, numPages-(1-0)));
        this.groupListBean.setPage(currentPage);
        
        // Fetch selected groups and their owners.
        List<Group> groups = null;
        if (showVisible || showAll) {
            groups = groupStore.getAll(direction, true, true, maxEntries,
                    (currentPage - 0) * maxEntries, user);
        } else if (showOwned) {
            groups = groupStore.getByOwner(user, direction, maxEntries,
                    (currentPage - 0) * maxEntries);
        } else if (showAttended) {
            groups = groupStore.getByAttendant(user, direction,
                    maxEntries, (currentPage - 0) * maxEntries);
        }
        Map<Group, List<User>> owners = userStore.getUsersOwningGroups(groups);

        // Fetch the groups owned and attended by the current user.
        Set<Group> owned, attended;
        // Fetching groups is not necessary in the owned and attended groups tab.
        if (user != null && !showAttended && !showOwned) {
            owned = new HashSet<Group>(groupStore.getByOwner(user));
            attended = new HashSet<Group>(groupStore.getByAttendant(user));
        } else {
            owned = new HashSet<Group>();
            attended = new HashSet<Group>();
        }

        // Assemble the list to be displayed.
        List<GroupListEntryBean> listEntries = new LinkedList<GroupListEntryBean>();
        for (Group group : groups) {
            GroupListEntryBean entry = new GroupListEntryBean();

            entry.setSignOffAvailable(false);
            entry.setSignUpAvailable(false);
            
            // Show signOff button for attendants.
            if (showAttended || attended.contains(group)) {
        	entry.setSignOffAvailable(true);

            // Show signUp button for non-attendants/non-owners.
            } else if (!showOwned && !owned.contains(group)) {
                entry.setSignUpAvailable(true);
            }

            // Fill entry with data.
            entry.setGroup(group);
            entry.setOwners(owners.get(group));
            entry.setCurrentUserOwner(showOwned || owned.contains(group));
            entry.setCurrentUserSignedUp(showAttended || attended.contains(group));

            // Append entry to list.
            listEntries.add(entry);
        }

        this.groupListBean.setList(listEntries);
        
	return null;
    }
    
    /**
     * This method helps to generate a hash with the given parameters.
     * The Hash contains the following Elements:
     * <ul><li>Date in form DDMMYYYY.</li>
     * <li>The email of the user.</li>
     * <li>As secret the old password of the user</li></ul>
     * @param calendar for the date inside the {@link String} to hash.
     * @param email for email inside the {@link String} to hash.
     * @return
     */
    public String generateHash(Calendar calendar, User user) {
        String toHash = Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH));
        toHash += Integer.toString(calendar.get(GregorianCalendar.MONTH));
        toHash += Integer.toString(calendar.get(GregorianCalendar.YEAR));
        toHash += user.getEmail();
        toHash += user.getPasswordHash();
        String key = SecurityUtils.hash(toHash);
        return key;
    }

    /**
     * Create a user object for storing in the database
     * 
     * @return the new user object
     */
    private User createNewUserObject() {
        User newUserObject = new User(null);
        if (this.userBean.getPassword() == null) {

            String randomPw = SecurityUtils.randomString(8);
            newUserObject.setPasswordHash(SecurityUtils.hash(randomPw));

            String msg = String.format(
                    this.getCommonMessage(MSG_SEND_EMAIL_PASSWORD_BODY), this.brandingConfiguration.getBrandingText("project_name"), randomPw);

            this.mailService.sendMail(this.userBean.getEmail(),
        	    String.format(this.getCommonMessage(MSG_SEND_EMAIL_PASSWORD_SUBJECT), this.brandingConfiguration.getBrandingText("project_name")), msg);

        } else {
            newUserObject.setPasswordHash(SecurityUtils.hash(this.userBean
                    .getPassword()));
        }

        newUserObject.setEmail(this.userBean.getEmail());
        newUserObject.setTitle(this.userBean.getAcademicTitle());
        newUserObject.setFirstName(this.userBean.getFirstName());
        newUserObject.setLastName(this.userBean.getLastName());

        //on registration no usertype is populated to bean
        if (this.userBean.getUserType() == null) {
            newUserObject.setUserType(EUserType.Participant);
        } else {
            newUserObject.setUserType(this.userBean.getUserType());
        }
        newUserObject.setGender(this.userBean.getGender());
        newUserObject.setBirthday(this.userBean.getBirthday());
        newUserObject.setStreet(this.userBean.getStreet());
        newUserObject.setZip(this.userBean.getZip());
        newUserObject.setCity(this.userBean.getCity());
        newUserObject.setCountry(this.userBean.getCountry());
        newUserObject.setPhone(this.userBean.getPhone());
        newUserObject.setFax(this.userBean.getFax());
        newUserObject.setWebsite(this.userBean.getWebsite());
        newUserObject.setVisible(this.userBean.isVisible());
        newUserObject.setBanned(this.userBean.isBanned());
        newUserObject.setDeletable(true);
        return newUserObject;
    }

    /**
     * Update the user object with the data of the bean for storing in the
     * database
     * 
     * @param user
     * @return
     */
    private User updateUserObject(User user) {
        
        IUserStore uStore = this.persistenceProvider.getUserStore();
        
        User userBeforeUpdate = uStore.getById(user.getId());

        if (this.userBean.getPassword() != null
                && !this.userBean.getPassword().equals("")) {
            user.setPasswordHash(SecurityUtils.hash(userBean.getPassword()));
        } else if (this.userBean.isSendNewPassword()) {
            String randomPw = SecurityUtils.randomString(8);
            this.userBean.setPassword(randomPw);
            user.setPasswordHash(SecurityUtils.hash(randomPw));

            String msg = String.format(
                    this.getCommonMessage(MSG_SEND_EMAIL_UPDATE_PASSWORD_BODY),
                    this.userBean.getPassword());

            this.mailService.sendMail(this.userBean.getEmail(),
                    String.format(this.getCommonMessage(MSG_SEND_EMAIL_UPDATE_PASSWORD_SUBJECT), this.brandingConfiguration.getBrandingText("project_name")), msg);
        }

        user.setEmail(this.userBean.getEmail());
        user.setTitle(this.userBean.getAcademicTitle());
        user.setFirstName(this.userBean.getFirstName());
        user.setLastName(this.userBean.getLastName());
        user.setGender(this.userBean.getGender());
        user.setBirthday(this.userBean.getBirthday());
        user.setStreet(this.userBean.getStreet());
        user.setZip(this.userBean.getZip());
        user.setCity(this.userBean.getCity());
        user.setCountry(this.userBean.getCountry());
        user.setPhone(this.userBean.getPhone());
        user.setFax(this.userBean.getFax());
        user.setWebsite(this.userBean.getWebsite());
        user.setVisible(this.userBean.isVisible());
        user.setUserType(this.userBean.getUserType());
        user.setBanned(this.userBean.isBanned());
        user.setDeletable(userBeforeUpdate.isDeletable());

        return user;
    }

    /**
     * Populate the userBean with the data of an user object for viewing in the
     * facelet.
     * 
     * @param user
     */
    private void populateUserBean(User user) {
    	this.userBean.setId(user.getId());
        this.userBean.setEmail(user.getEmail());
        this.userBean.setAcademicTitle(user.getTitle());
        this.userBean.setFirstName(user.getFirstName());
        this.userBean.setLastName(user.getLastName());
        this.userBean.setUserType(user.getUserType());
        this.userBean.setGender(user.getGender());
        this.userBean.setBirthday(user.getBirthday());
        this.userBean.setStreet(user.getStreet());
        this.userBean.setZip(user.getZip());
        this.userBean.setCity(user.getCity());
        this.userBean.setCountry(user.getCountry());
        this.userBean.setPhone(user.getPhone());
        this.userBean.setFax(user.getFax());
        this.userBean.setWebsite(user.getWebsite());
        this.userBean.setVisible(user.isVisible());
        this.userBean.setPhotoAvailable(user.isPhotoAvailable());
        this.userBean.setBanned(user.isBanned());
    }

    /**
     * 
     * @return true if the logged in {@link User} is at least of
     *         {@link EUserType#Tutor}, otherwise return false.
     */
    public boolean isVisibleForTutors() {
        User user = persistenceProvider.getUserStore().getById(
                session.getUserId());
        return session.getUserId() != null
                && user.getUserType().getLevel() >= EUserType.Tutor.getLevel();
    }

    /**
     * 
     * @return true if the logged in {@link User} is at least of
     *         {@link EUserType#Administrator}, otherwise return false.
     */
    public boolean isVisibleForAdministrators() {
        User user = persistenceProvider.getUserStore().getById(
                session.getUserId());
        return session.getUserId() != null
                && user.getUserType().getLevel() >= EUserType.Administrator
                        .getLevel();
    }
    
    /**
     * Remove user from group ownership if he is downgraded to a
     * {@link EUserType#Participant}.
     * 
     * @param changedUser
     *            that should be saved
     */
    private void prepareForUpDowngrade(User changedUser) {
        IUserStore userStore = this.persistenceProvider.getUserStore();
        IGroupStore groupStore = this.persistenceProvider.getGroupStore();
        User unchangedUser = userStore.getById(changedUser.getId());

        if (unchangedUser.getUserType().getLevel() > changedUser.getUserType()
                .getLevel()
                && changedUser.getUserType() == EUserType.Participant) {
            List<Group> ownedGroups = groupStore.getByOwner(unchangedUser);

            try {
                for (Group e : ownedGroups) {
                    groupStore.removeOwner(e, unchangedUser);
                }
            } catch (InconsistencyException ignored) {
            }
        }
    }
    
    /**
     * Generate and save a resized version of the uploaded photo as JPG.
     * 
     * @param userId contains the users id
     * @return true if the photo was successfully saved, false otherwise.
     */
    private boolean saveResizedPhoto(int userId){
    	try {
			BufferedImage originalImage;
			
			// Check if its a unit test and get the input stream
			if(this.mockedPhotoFile == null){
				originalImage = ImageIO.read(this.userBean.getPhoto().getInputStream());
			}
			else{
				originalImage = ImageIO.read(new FileInputStream(this.mockedPhotoFile));
			}
			int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
			 
			BufferedImage resizeImageJpg = resizeImage(originalImage, type);
			ImageIO.write(resizeImageJpg, "jpg", new File(PHOTO_DESTINATION + userId + ".jpg"));
		} catch (IOException e) {
			return false;
		}
    	return true;
    }
    
    /**
     * Generate resized version of the uploaded photo.
     * 
     * @param originalImage
     * @param type
     * @return the resized photo.
     */
    private static BufferedImage resizeImage(BufferedImage originalImage, int type){
	int width = originalImage.getWidth();
	int height = originalImage.getHeight();
	if(width > PHOTO_WIDTH){
	    double factor = (double)width / PHOTO_WIDTH;
	    width /= factor;
	    height /= factor;	
	}
	if(height > PHOTO_HEIGHT){
	    double factor = (double)height / PHOTO_HEIGHT;
	    width /= factor;
	    height /= factor;
	}
	BufferedImage resizedImage = new BufferedImage(width, height, type);
    	Graphics2D g = resizedImage.createGraphics();
    	g.drawImage(originalImage, 0, 0, width, height, null);
    	g.dispose();
     
    	return resizedImage;
    }
    
    /**
	 * Set photo file for unit tests as it's not possible to read
	 * a file and convert it to a {@link Part} object.
	 * @param photoFile to upload.
	 */
	public void setPhotoUploadMock(File photoFile) {
		this.mockedPhotoFile  = photoFile;
	}
}
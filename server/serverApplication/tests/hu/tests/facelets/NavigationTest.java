package hu.tests.facelets;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;

import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test checks the page for broken links and invalid message keys.
 * 
 */
public class NavigationTest {

    // URLs
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";

    // IDs
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String EDIT_USER_FORM_ID = "editUserForm";
    private static final String LIST_USER_TABLE_ID = "listUsersTable";
    private static final String SEARCH_FORM_ID = "searchForm";

    // Link texts
    private static final String PW_FORGOTTEN_TXT = "Forgotten?";
    private static final String EDIT_PROFILE_HEADER_TXT = "edit profile";
    private static final String EDIT_PROFILE_TXT = "Edit Profile";
    private static final String HOME_TXT = "home";
    private static final String DASHBOARD_TXT = "dashboard";
    private static final String REGISTER_TXT = "register";
    private static final String VIDEOS_TXT = "videos";
    private static final String MANAGE_VIDEOS_TXT = "Manage Videos";
    private static final String CREATE_VIDEO_TXT = "Create Video";
    private static final String ACTIVE_VIDEOS_TXT = "Active Videos";
    private static final String INACTIVE_VIDEOS_TXT = "Inactive Videos";
    private static final String BACK_TO_VIDEOS_TXT = "back to videos";
    private static final String GROUPS_TXT = "groups";
    private static final String MANAGE_GROUPS_TXT = "Manage Groups";
    private static final String CREATE_GROUP_TXT = "Create Group";
    private static final String ATTENDED_GROUPS_TXT = "Signed Up Groups";
    private static final String OWNED_GROUPS_TXT = "Own Groups";
    private static final String ALL_GROUPS_TXT = "All Groups";
    private static final String BACK_TO_GROUPS_TXT = "back to groups";
    private static final String MANAGE_USERS_TXT = "Manage Users";

    private static final String HELP_TXT = "help";
    private static final String CONTACT_TXT = "contact";
    private static final String PRIVACY_TXT = "privacy";
    private static final String IMPRINT_TXT = "imprint";

    // Messages
    private static final String INVALID_MESSAGE_KEY_MSG = "! INVALID MESSAGE KEY !";

    // User data
    private static String tutorEmail;
    private static final String tutorPassword = "Test1";
    private static final String tutorAcademicTitle = "Prof";
    private static final String tutorFirstName = "Hans";
    private static final String tutorLastName = "Wurst";
    private static final String tutorBirthday = "01-02-2003";
    private static final String tutorGender = "male";
    private static String userEmail;
    private static final String userPassword = "Test1";
    private static final String userAcademicTitle = "PhD";
    private static final String userFirstName = "Sepp";
    private static final String userLastName = "Meier";
    private static final String userBirthday = "01-02-2003";
    private static final String userGender = "male";

    // Admin data
    private final static String title = "Dr.";
    private final static String password = "Test!1234";
    private static String email;
    private final static EUserType type = EUserType.Administrator;
    private final static String firstName = "Manuel";
    private final static String lastName = "Muster";
    private final static Date birthday = new Date();
    private final static EGender gender = EGender.Male;
    private final static boolean banned = false;
    private final static ECountry country = ECountry.Germany;
    private static User admin;

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    
    @BeforeClass
    public static void prepare() throws InterruptedException, InconsistencyException {
	setBaseUrl(Globals.getBaseUrl());
	
	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);

	// Generate new unique mail addresses
	tutorEmail = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	userEmail = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	email= "admin"+ System.currentTimeMillis() + "@mailinator.com";
	admin = new User(null);
	admin.setEmail(email);
	admin.setPassword(password);
	admin.setFirstName(firstName);
	admin.setLastName(lastName);
	admin.setBirthday(birthday);
	admin.setGender(gender);
	admin.setUserType(type);
	admin.setBanned(banned);
	admin.setTitle(title);
	admin.setCountry(country);
	admin = persistenceProvider.getUserStore().create(admin);
    }

    @Test
    public void testAsAnonymous(){

	beginAt(Globals.getBaseUrl());
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Start navigating through the page
	clickLinkWithExactText(PW_FORGOTTEN_TXT);
	assertTitleEquals("Password Recovery");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(REGISTER_TXT);
	assertTitleEquals("Register");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Test video listing
	clickLinkWithExactText(VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(INACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	clickLinkWithExactText(GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Common links
	clickLinkWithExactText(HELP_TXT, 1); // two appearances
	assertTitleEquals("Help");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CONTACT_TXT);
	assertTitleEquals("Contact");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(PRIVACY_TXT);
	assertTitleEquals("Privacy");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(IMPRINT_TXT);
	assertTitleEquals("Imprint");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(HOME_TXT);
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
    }

    
    @Test
    public void testAsRegistered(){

	beginAt(Globals.getBaseUrl());
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Register and login
	this.registerUser(userEmail, userPassword, userAcademicTitle, userFirstName, userLastName,
		userBirthday, userGender);
	this.login(userEmail, userPassword);

	clickLinkWithExactText(EDIT_PROFILE_HEADER_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Check Dashboard
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(EDIT_PROFILE_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(INACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ATTENDED_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Common links
	clickLinkWithExactText(HELP_TXT, 1); // two appearances
	assertTitleEquals("Help");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CONTACT_TXT);
	assertTitleEquals("Contact");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(PRIVACY_TXT);
	assertTitleEquals("Privacy");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(IMPRINT_TXT);
	assertTitleEquals("Imprint");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(HOME_TXT);
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	this.logoutCurrentUser();

	// Delete user that was used for this test
	this.login(email, password);
	clickLinkWithExactText(MANAGE_USERS_TXT);
	assertTitleEquals("Users");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Search for index of the correct "delete user" link (ugly)
	this.delete(userEmail);
	assertTextPresent("Selected user deleted.");

	this.logoutCurrentUser();
    }
  
    @Test
    public void testAsTutor(){

	beginAt(Globals.getBaseUrl());
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Register new user
	this.registerUser(tutorEmail, tutorPassword, tutorAcademicTitle, tutorFirstName,
		tutorLastName, tutorBirthday, tutorGender);

	// Login as administrator and set role of user to "Tutor"
	this.login(email, password);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText("Manage Users");

	// Search for index of the correct "edit user" link (ugly)
	assertFormPresent(SEARCH_FORM_ID);
	setWorkingForm(SEARCH_FORM_ID);
	setTextField(SEARCH_FORM_ID + ":searchQuery", tutorEmail);
	submit();
	
	assertTextInTable(LIST_USER_TABLE_ID, tutorEmail);
	clickLinkWithExactText("edit user");
	assertTitleEquals("Edit User");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	assertFormPresent(EDIT_USER_FORM_ID);
	setWorkingForm(EDIT_USER_FORM_ID);
	setTextField(EDIT_USER_FORM_ID + ":email", tutorEmail);
	selectOption(EDIT_USER_FORM_ID + ":academicTitle", tutorAcademicTitle);
	setTextField(EDIT_USER_FORM_ID + ":firstName", tutorFirstName);
	setTextField(EDIT_USER_FORM_ID + ":lastName", tutorLastName);
	setTextField(EDIT_USER_FORM_ID + ":birthday", tutorBirthday);
	selectOption(EDIT_USER_FORM_ID + ":gender", tutorGender);
	selectOption(EDIT_USER_FORM_ID + ":role", "Tutor");
	submit();
	assertTextPresent("Edited profile successfull!");

	this.logoutCurrentUser();

	// Login as tutor and start navigation through the page
	this.login(tutorEmail, tutorPassword);

	clickLinkWithExactText(EDIT_PROFILE_HEADER_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Check Dashboard
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(EDIT_PROFILE_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(INACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CREATE_VIDEO_TXT);
	assertTitleEquals("Create Video");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(BACK_TO_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ATTENDED_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(OWNED_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CREATE_GROUP_TXT);
	assertTitleEquals("Create Group");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(BACK_TO_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Common links
	clickLinkWithExactText(HELP_TXT, 1); // two appearances
	assertTitleEquals("Help");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CONTACT_TXT);
	assertTitleEquals("Contact");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(PRIVACY_TXT);
	assertTitleEquals("Privacy");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(IMPRINT_TXT);
	assertTitleEquals("Imprint");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(HOME_TXT);
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	this.logoutCurrentUser();

	// Delete user that was used for this test
	this.login(email, password);
	clickLinkWithExactText(MANAGE_USERS_TXT);
	assertTitleEquals("Users");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	this.delete(tutorEmail);
	assertTextPresent("Selected user deleted.");

	this.logoutCurrentUser();
    }
    
    /**
     * Delete the given user.
     * 
     * @param user
     *            to delete.
     */
    private void delete(String user) {

	this.searchUser(user);
	clickLinkWithExactText("delete user");
	assertTitleEquals("Delete User");
	setWorkingForm("deleteUserForm");
	submit();
    }
    
    /**
     * Search a user with a given string.
     * 
     * @param email
     *            is the string to search.
     */
    private void searchUser(String email) {
	assertFormPresent(SEARCH_FORM_ID);
	setWorkingForm(SEARCH_FORM_ID);
	setTextField(SEARCH_FORM_ID + ":searchQuery", email);
	submit();
    }

    @Test
    public void testAsAdministrator() {

	beginAt(Globals.getBaseUrl());
	assertTitleEquals("Welcome");

	// Login as administrator
	this.login(email, password);

	// Start navigating through the page
	clickLinkWithExactText(EDIT_PROFILE_HEADER_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Check Dashboard
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(EDIT_PROFILE_TXT);
	assertTitleEquals("Edit Profile");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(INACTIVE_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CREATE_VIDEO_TXT);
	assertTitleEquals("Create Video");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(BACK_TO_VIDEOS_TXT);
	assertTitleEquals("Videos");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ATTENDED_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(OWNED_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(ALL_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CREATE_GROUP_TXT);
	assertTitleEquals("Create Group");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(BACK_TO_GROUPS_TXT);
	assertTitleEquals("Groups");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(MANAGE_USERS_TXT);
	assertTitleEquals("Users");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(DASHBOARD_TXT);
	assertTitleEquals("Dashboard");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	// Common links
	clickLinkWithExactText(HELP_TXT, 1); // two appearances
	assertTitleEquals("Help");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(CONTACT_TXT);
	assertTitleEquals("Contact");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(PRIVACY_TXT);
	assertTitleEquals("Privacy");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(IMPRINT_TXT);
	assertTitleEquals("Imprint");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);
	clickLinkWithExactText(HOME_TXT);
	assertTitleEquals("Welcome");
	assertTextNotPresent(INVALID_MESSAGE_KEY_MSG);

	this.logoutCurrentUser();
    }

    /**
     * 
     * @param email
     * @param password
     * @param academicTitle
     * @param firstName
     * @param lastName
     * @param birthday
     * @param gender
     */
    private void registerUser(String email, String password, String academicTitle,
	    String firstName, String lastName, String birthday, String gender) {

	beginAt(REGISTER_JSF);
	assertTitleEquals("Register");
	assertFormPresent(REGISTER_FORM_ID);
	setWorkingForm(REGISTER_FORM_ID);

	// Register a new user
	setTextField(REGISTER_FORM_ID + ":email", email);
	setTextField(REGISTER_FORM_ID + ":password", password);
	setTextField(REGISTER_FORM_ID + ":passwordRepeat", password);
	selectOption(REGISTER_FORM_ID + ":academicTitle", academicTitle);
	setTextField(REGISTER_FORM_ID + ":firstName", firstName);
	setTextField(REGISTER_FORM_ID + ":lastName", lastName);
	setTextField(REGISTER_FORM_ID + ":birthday", birthday);
	selectOption(REGISTER_FORM_ID + ":gender", gender);
	submit();
	assertTextPresent("Registration sucessfull!");
    }

    /**
     * 
     * @param username
     * @param password
     */
    private void login(String username, String password) {

	// Login using the form in the header
	beginAt(Globals.getBaseUrl());
	assertTitleEquals("Welcome");
	assertFormPresent(HEADERLOGIN_FORM_ID);
	setWorkingForm(HEADERLOGIN_FORM_ID);
	setTextField(HEADERLOGIN_FORM_ID + ":username", username);
	setTextField(HEADERLOGIN_FORM_ID + ":password", password);
	submit();

	// Check if user is now logged in
	assertTitleEquals("Welcome");
	assertFormNotPresent(HEADERLOGIN_FORM_ID);
	assertFormPresent(HEADERLOGOUT_FORM_ID);
	clickLinkWithExactText("dashboard");
    }

    private void logoutCurrentUser() {

	// Logout user
	setWorkingForm(HEADERLOGOUT_FORM_ID);
	submit();
	assertTitleEquals("Login");
	assertFormNotPresent(HEADERLOGOUT_FORM_ID);
	assertFormPresent(HEADERLOGIN_FORM_ID);
    }
    
    @AfterClass
    public static void close() throws InconsistencyException {
	persistenceProvider.getUserStore().delete(admin);
	persistenceProvider.close();
    }
}
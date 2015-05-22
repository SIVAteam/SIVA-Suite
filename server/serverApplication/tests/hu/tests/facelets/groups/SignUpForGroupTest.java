package hu.tests.facelets.groups;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.getTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionSelected;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickRadioOption;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;

import java.util.Date;
import java.util.List;

import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.html.Table;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Concerning: /T161/, /T162/, /T170/, /T180/, /T190/, /T200/, /T201/, /T210/,
 * /T211/
 * 
 */

public class SignUpForGroupTest {

    // URLs
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";

    // IDs
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String EDIT_USER_FORM_ID = "editUserForm";
    private static final String LIST_USER_TABLE_ID = "listUsersTable";
    private static final String LIST_GROUPS_TABLE_ID = "listGroupsTable";
    private static final String CREATE_GROUP_FORM_ID = "createGroupForm";
    private static final String SIGN_UP_FORM_ID = "signUpForm";
    private static final String SIGN_OFF_FORM_ID = "signOffForm";
    private static final String DELETE_GROUP_FORM_ID = "deleteGroupForm";
    private static final String SEARCH_FORM_ID = "searchForm";

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

    // Group data
    private static final String groupTitle = "Fun with flags";

    @BeforeClass
    public static void prepare() throws InterruptedException, InconsistencyException {
	setBaseUrl(Globals.getBaseUrl());

	// Generate new unique mail addresses
	tutorEmail = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	userEmail = System.currentTimeMillis() + "@mailinator.com";
	
	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);
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

    @SuppressWarnings("unchecked")
    @Test
    public void testAssignToGroup() {

	// Register users for test
	this.registerUser(tutorEmail, tutorPassword, tutorAcademicTitle, tutorFirstName,
		tutorLastName, tutorBirthday, tutorGender);
	this.registerUser(userEmail, userPassword, userAcademicTitle, userFirstName, userLastName,
		userBirthday, userGender);

	// Login as administrator and set role of user to "Tutor"
	this.login(email, password);
	assertTitleEquals("Dashboard");
	clickLinkWithExactText("Manage Users");
	assertFormPresent(SEARCH_FORM_ID);
	setWorkingForm(SEARCH_FORM_ID);
	setTextField(SEARCH_FORM_ID + ":searchQuery", tutorEmail);
	submit();
	
	assertTextInTable(LIST_USER_TABLE_ID, tutorEmail);
	
	clickLinkWithExactText("edit user");
	assertTitleEquals("Edit User");
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

	// Login as tutor and create group
	this.login(tutorEmail, tutorPassword);
	assertTitleEquals("Dashboard");
	clickLinkWithExactText("Manage Groups");
	assertTitleEquals("Groups");
	clickLinkWithExactText("Create Group");
	assertTitleEquals("Create Group");
	assertFormPresent(CREATE_GROUP_FORM_ID);
	setWorkingForm(CREATE_GROUP_FORM_ID);
	setTextField(CREATE_GROUP_FORM_ID + ":title", groupTitle);
	assertRadioOptionPresent(CREATE_GROUP_FORM_ID+":j_idt60","false");
	assertRadioOptionPresent(CREATE_GROUP_FORM_ID+":j_idt60","true");
	clickRadioOption(CREATE_GROUP_FORM_ID+":j_idt60","true");
	assertRadioOptionSelected(CREATE_GROUP_FORM_ID + ":j_idt60","true");
	submit();
	assertTitleEquals("Groups");
	assertTextInTable(LIST_GROUPS_TABLE_ID, groupTitle);
	this.logoutCurrentUser();

	// Login as user and sign up to group
	this.login(userEmail, userPassword);
	assertTitleEquals("Dashboard");
	clickLinkWithExactText("Manage Groups");
	assertTitleEquals("Groups");

	// Search for index of the correct "sign up" link (ugly)
	int groupIndex = 0;
	Table groupTable = getTable(LIST_GROUPS_TABLE_ID);
	List<Row> groupRows = groupTable.getRows();
	for (Row r : groupRows) {
	    if (!r.hasText("Group") && !r.hasText("Tutors")) {
		if (!r.hasText(groupTitle)) {
		    groupIndex++;
		} else {
		    break;
		}
	    }
	}

	clickLinkWithExactText("sign up", groupIndex);
	assertTitleEquals("Sign up for Group");
	assertFormPresent(SIGN_UP_FORM_ID);
	setWorkingForm(SIGN_UP_FORM_ID);
	submit();
	assertTextPresent("You have signed up to the group.");

	// Sign off from group
	clickLinkWithExactText("Signed Up Groups");

	clickLinkWithExactText("sign off");
	assertTitleEquals("Sign off from Group");
	assertFormPresent(SIGN_OFF_FORM_ID);
	setWorkingForm(SIGN_OFF_FORM_ID);
	submit();
	assertTextPresent("You got successfull signed off.");
	this.logoutCurrentUser();

	// Login as tutor and delete the created group
	this.login(tutorEmail, tutorPassword);
	assertTitleEquals("Dashboard");
	clickLinkWithExactText("Manage Groups");
	assertTitleEquals("Groups");

	// Search for index of the correct "delete group" link (ugly)
	clickLinkWithExactText("Own Groups");

	clickLinkWithExactText("delete group");
	assertTitleEquals("Delete Group");
	assertFormPresent(DELETE_GROUP_FORM_ID);
	setWorkingForm(DELETE_GROUP_FORM_ID);
	submit();
	assertTextPresent("Group deleted.");
	this.logoutCurrentUser();

	// Login as administrator and delete registered users
	this.login(email, password);
	assertTitleEquals("Dashboard");
	clickLinkWithExactText("Manage Users");	

	assertFormPresent(SEARCH_FORM_ID);
	setWorkingForm(SEARCH_FORM_ID);
	setTextField(SEARCH_FORM_ID + ":searchQuery", tutorEmail);
	submit();
	assertTextInTable(LIST_USER_TABLE_ID, tutorEmail);

	clickLinkWithExactText("delete user");
	assertTitleEquals("Delete User");
	setWorkingForm("deleteUserForm");
	submit();
	assertTextPresent("Selected user deleted.");

	assertFormPresent(SEARCH_FORM_ID);
	setWorkingForm(SEARCH_FORM_ID);
	setTextField(SEARCH_FORM_ID + ":searchQuery", userEmail);
	submit();
	
	assertTextInTable(LIST_USER_TABLE_ID, userEmail);	
	clickLinkWithExactText("delete user");
	assertTitleEquals("Delete User");
	setWorkingForm("deleteUserForm");
	submit();
	assertTextPresent("Selected user deleted.");

	// Logout administrator
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
	assertTextNotPresent("Entered login does not fit.");
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
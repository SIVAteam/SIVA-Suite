package hu.tests.facelets.users;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.gotoPage;
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
 * Concerning: /T130/, /T140/, /T141/
 */
public class LoginTest {

    // URLs
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";  
    private static final String LOGIN_JSF = "/xhtml/users/login.jsf";

    // IDs
    private static final String SEARCH_FORM_ID = "searchForm";
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String LOGIN_FORM_ID = "loginForm";

    // User data
    private static String email;
    private static final String password = "Test1";
    private static final String academicTitle = "Prof";
    private static final String firstName = "Hans";
    private static final String lastName = "Wurst";
    private static final String birthday = "01-02-2003";
    private static final String gender = "male";
    
    // Admin data
    private final static String adminTitle = "Dr.";
    private final static String adminPassword = "Test!1234";
    private static String adminEmail;
    private final static EUserType adminType = EUserType.Administrator;
    private final static String adminFirstName = "Manuel";
    private final static String adminLastName = "Muster";
    private final static Date adminBirthday = new Date();
    private final static EGender adminGender = EGender.Male;
    private final static boolean adminBanned = false;
    private final static ECountry adminCountry = ECountry.Germany;
    private static User admin;

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;

    @BeforeClass
    public static void prepare() throws InconsistencyException {
        setBaseUrl(Globals.getBaseUrl());

        // Generate new "unique" mail address
        email = System.currentTimeMillis() + "@mailinator.com";
        
        configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);
	
	
	adminEmail="admin"+System.currentTimeMillis() + "@mailinator.com";
	admin = new User(null);
	admin.setEmail(adminEmail);
	admin.setPassword(adminPassword);
	admin.setFirstName(adminFirstName);
	admin.setLastName(adminLastName);
	admin.setBirthday(adminBirthday);
	admin.setGender(adminGender);
	admin.setUserType(adminType);
	admin.setBanned(adminBanned);
	admin.setTitle(adminTitle);
	admin.setCountry(adminCountry);
	admin = persistenceProvider.getUserStore().create(admin);
    }

    @Test
    public void testLoginForm() {

        // Login using invalid credentials
        beginAt(Globals.getBaseUrl());
        assertTitleEquals("Welcome");
        assertFormPresent(HEADERLOGIN_FORM_ID);
        setWorkingForm(HEADERLOGIN_FORM_ID);
        setTextField(HEADERLOGIN_FORM_ID + ":username", "abcdef");
        setTextField(HEADERLOGIN_FORM_ID + ":password", "123456");
        submit();
        assertTitleEquals("Login");

        // Register new user for test
        this.registerUser();

        // Login using the form in the header
        gotoPage(Globals.getBaseUrl());
        assertFormPresent(HEADERLOGIN_FORM_ID);
        setWorkingForm(HEADERLOGIN_FORM_ID);
        setTextField(HEADERLOGIN_FORM_ID + ":username", email);
        setTextField(HEADERLOGIN_FORM_ID + ":password", password);
        submit();

        // Check if user is now logged in
        assertTitleEquals("Welcome");
        assertFormNotPresent(HEADERLOGIN_FORM_ID);
        assertTextPresent(academicTitle + " " + firstName + " " + lastName);
        assertFormPresent(HEADERLOGOUT_FORM_ID);

        // Logout user
        this.logoutCurrentUser();

        // Login using the login page
        beginAt(LOGIN_JSF);
        assertTitleEquals("Login");
        assertFormPresent(HEADERLOGIN_FORM_ID);
        assertFormPresent(LOGIN_FORM_ID);
        setWorkingForm(LOGIN_FORM_ID);
        setTextField(LOGIN_FORM_ID + ":username", email);
        setTextField(LOGIN_FORM_ID + ":password", password);
        submit();

        // Check if user is now logged in
        assertTitleEquals("Dashboard");
        assertFormNotPresent(HEADERLOGIN_FORM_ID);
        assertTextPresent(academicTitle + " " + firstName + " " + lastName);
        assertFormPresent(HEADERLOGOUT_FORM_ID);

        // Logout user
        this.logoutCurrentUser();

        // Login with super administrator
        assertFormPresent(HEADERLOGIN_FORM_ID);
        setWorkingForm(HEADERLOGIN_FORM_ID);
        setTextField(HEADERLOGIN_FORM_ID + ":username",
                adminEmail);
        setTextField(HEADERLOGIN_FORM_ID + ":password",
                adminPassword);
        submit();
        assertTitleEquals("Dashboard");
        assertFormNotPresent(HEADERLOGIN_FORM_ID);

        // Delete user that was used for this test
        clickLinkWithExactText("Manage Users");
        searchUser(email);
        assertTextPresent(email);    

        clickLinkWithExactText("delete user");
        assertTitleEquals("Delete User");
        setWorkingForm("deleteUserForm");
        submit();
        assertTextPresent("Selected user deleted.");

        // Logout administrator
        this.logoutCurrentUser();
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
    
    private void registerUser() {

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
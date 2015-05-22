package hu.tests.facelets.users;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertSelectedOptionEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextFieldEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.checkCheckbox;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.getElementById;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;
import static org.junit.Assert.assertEquals;
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
 * Concerning: /T110/, /T120/
 */
public class RegistrationTest {

    // URLs  
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";

    // IDs
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String LIST_USER_TABLE_ID = "listUsersTable";
    private static final String SEARCH_FORM_ID = "searchForm";

    // User data
    private static String emailValid;
    private static final String emailInvalid = "google.de";
    private static final String password = "Test1";
    private static final String academicTitle = "Prof";
    private static final String firstName = "Hans";
    private static final String lastName = "Wurst";
    private static final String birthdayValid = "01-02-2003";
    private static final String birthdayInvalid = "01-01-3000";
    private static final String gender = "male";
    private final static String TEST_PICTURE_DIR = "./tests/resources/Test.png";
    
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
        emailValid = System.currentTimeMillis() + "@mailinator.com";
        
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
    public void testRegistrationForm() {
        beginAt(REGISTER_JSF);
        assertTitleEquals("Register");
        assertFormPresent(REGISTER_FORM_ID);
        setWorkingForm(REGISTER_FORM_ID);

        // Try to submit an empty form
        submit();
        assertEquals("error", getElementById(REGISTER_FORM_ID + ":email")
                .getAttribute("class"));
        assertEquals("error", getElementById(REGISTER_FORM_ID + ":password")
                .getAttribute("class"));
        assertEquals("error",
                getElementById(REGISTER_FORM_ID + ":passwordRepeat")
                        .getAttribute("class"));
        assertEquals("error", getElementById(REGISTER_FORM_ID + ":firstName")
                .getAttribute("class"));
        assertEquals("error", getElementById(REGISTER_FORM_ID + ":lastName")
                .getAttribute("class"));

        // Check for working validation
        setTextField(REGISTER_FORM_ID + ":email", emailInvalid);
        setTextField(REGISTER_FORM_ID + ":password", password);
        setTextField(REGISTER_FORM_ID + ":passwordRepeat", password + "x");
        selectOption(REGISTER_FORM_ID + ":academicTitle", academicTitle);
        setTextField(REGISTER_FORM_ID + ":firstName", firstName);
        setTextField(REGISTER_FORM_ID + ":lastName", lastName);
        setTextField(REGISTER_FORM_ID + ":birthday", birthdayInvalid);
        selectOption(REGISTER_FORM_ID + ":gender", gender);
        submit();

        assertEquals("error", getElementById(REGISTER_FORM_ID + ":email")
                .getAttribute("class"));
        assertEquals("error", getElementById(REGISTER_FORM_ID + ":birthday")
                .getAttribute("class"));
        assertEquals("", getElementById(REGISTER_FORM_ID + ":firstName")
                .getAttribute("class")); // spot test

        // Finally register with valid credentials
        assertTextFieldEquals(REGISTER_FORM_ID + ":email", emailInvalid);
        setTextField(REGISTER_FORM_ID + ":email", emailValid);
        setTextField(REGISTER_FORM_ID + ":password", password);
        setTextField(REGISTER_FORM_ID + ":passwordRepeat", password);
        assertSelectedOptionEquals(REGISTER_FORM_ID + ":academicTitle",
                academicTitle);
        assertTextFieldEquals(REGISTER_FORM_ID + ":firstName", firstName);
        assertTextFieldEquals(REGISTER_FORM_ID + ":lastName", lastName);
        assertTextFieldEquals(REGISTER_FORM_ID + ":birthday", birthdayInvalid);
        setTextField(REGISTER_FORM_ID + ":birthday", birthdayValid);
        assertSelectedOptionEquals(REGISTER_FORM_ID + ":gender", gender);
        setTextField(REGISTER_FORM_ID + ":street", "Sepperlweg");
        setTextField(REGISTER_FORM_ID + ":zip", "91453");
        setTextField(REGISTER_FORM_ID + ":city", "Josefsstadt");
        setTextField(REGISTER_FORM_ID + ":phone", "0171564362");
        setTextField(REGISTER_FORM_ID + ":fax", "0675828");
        setTextField(REGISTER_FORM_ID + ":website", "www.testsite.com");
        setTextField(REGISTER_FORM_ID + ":photo", TEST_PICTURE_DIR);
        checkCheckbox(REGISTER_FORM_ID + ":visible");
        submit();

        assertTextPresent("Registration sucessfull!");

        // Login as administrator and delete registered users
        this.login(adminEmail, adminPassword);
        clickLinkWithExactText("dashboard");
        assertTitleEquals("Dashboard");
        clickLinkWithExactText("Manage Users");
        searchUser(emailValid);
        assertTextInTable(LIST_USER_TABLE_ID, emailValid);
      
        clickLinkWithExactText("delete user");
        assertTitleEquals("Delete User");
        setWorkingForm("deleteUserForm");
        submit();
        assertTextPresent("Selected user deleted.");

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
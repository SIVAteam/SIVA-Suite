package hu.tests.facelets.users;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;
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
 * Concerning: /T145/, /T150/, /T160/
 */
public class EditProfileTest {

    // URLs
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";
    private final static String TEST_PICTURE_DIR = "./tests/resources/Test.png";
    // IDs
    private static final String SEARCH_FORM_ID = "searchForm";
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String EDIT_PROFILE_FORM_ID = "editProfileForm";
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String LIST_USER_TABLE_ID = "listUsersTable";

    // User data
    private static String email;
    private static final String password = "Test1";
    private static final String password2 = "Test2";
    private static final String academicTitle = "Prof";
    private static final String firstName = "Hans";
    private static final String lastName = "Wurst";
    private static final String birthdayValid = "01-02-2003";
    private static final String birthdayInvalid = "01-01-3000";
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

        // Generate new unique mail address
        email = System.currentTimeMillis() + "@mailinator.com";
        
        configuration = new Configuration();
       	persistenceProvider = new PgPersistenceProvider(configuration);
       	
       	adminEmail = "admin" + System.currentTimeMillis() + "@mailinator.com";
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
    public void testEditProfileForm() {

        // Register new user and login
        this.registerUser();
        this.login(email, password);

        clickLinkWithExactText("dashboard");
        assertTitleEquals("Dashboard");
        clickLinkWithExactText("Edit Profile");
        assertTitleEquals("Edit Profile");

        // Try to edit with invalid information
        setTextField(EDIT_PROFILE_FORM_ID + ":email", "");
        setTextField(EDIT_PROFILE_FORM_ID + ":firstName", "");
        setTextField(EDIT_PROFILE_FORM_ID + ":lastName", "");
        setTextField(EDIT_PROFILE_FORM_ID + ":birthday", birthdayInvalid);
        submit();
        assertEquals("error", getElementById(EDIT_PROFILE_FORM_ID + ":email")
                .getAttribute("class"));
        assertEquals("error",
                getElementById(EDIT_PROFILE_FORM_ID + ":firstName")
                        .getAttribute("class"));
        assertEquals("error",
                getElementById(EDIT_PROFILE_FORM_ID + ":lastName")
                        .getAttribute("class"));
        assertEquals("error",
                getElementById(EDIT_PROFILE_FORM_ID + ":birthday")
                        .getAttribute("class"));

        // Edit with valid information
        setTextField(EDIT_PROFILE_FORM_ID + ":email", email);
        setTextField(EDIT_PROFILE_FORM_ID + ":firstName", firstName);
        setTextField(EDIT_PROFILE_FORM_ID + ":lastName", lastName);        
        setTextField(EDIT_PROFILE_FORM_ID + ":password", password2);  
        setTextField(EDIT_PROFILE_FORM_ID + ":passwordRepeat", password2);  
        selectOption(EDIT_PROFILE_FORM_ID + ":academicTitle", "PhD");
        selectOption(EDIT_PROFILE_FORM_ID + ":gender", "female");
        selectOption(EDIT_PROFILE_FORM_ID + ":country", "Austria");
        
        setTextField(EDIT_PROFILE_FORM_ID + ":birthday", birthdayValid);
        setTextField(EDIT_PROFILE_FORM_ID + ":street", "Sepperlweg");
        setTextField(EDIT_PROFILE_FORM_ID + ":zip", "91453");
        setTextField(EDIT_PROFILE_FORM_ID + ":city", "Josefsstadt");
        setTextField(EDIT_PROFILE_FORM_ID + ":phone", "0171564362");
        setTextField(EDIT_PROFILE_FORM_ID + ":fax", "0675828");
        setTextField(EDIT_PROFILE_FORM_ID + ":website", "www.testsite.com");
        setTextField(EDIT_PROFILE_FORM_ID + ":photo", TEST_PICTURE_DIR);
        checkCheckbox(EDIT_PROFILE_FORM_ID + ":visible");
        submit();
        assertTextPresent("Edit Profile");
        assertTextNotPresent("Passwords do not match");
        assertTextPresent(email);
        assertTextPresent(firstName);
        assertTextPresent(lastName);
        assertTextPresent("PhD");
        assertTextPresent(birthdayValid);
        assertTextPresent("Austria");
        assertTextPresent("female");
        assertTextPresent("Sepperlweg");
        assertTextPresent("Josefsstadt");
        assertTextPresent("91453");
        assertTextPresent("0171564362");
        assertTextPresent("0675828");
        assertTextPresent("www.testsite.com");

        // Finally logout user
        this.logoutCurrentUser();

        // Login as administrator and delete registered users
        this.login(adminEmail, adminPassword);
        clickLinkWithExactText("dashboard");
        assertTitleEquals("Dashboard");
        clickLinkWithExactText("Manage Users");
        searchUser(email);
        assertTextInTable(LIST_USER_TABLE_ID, email);

        clickLinkWithExactText("delete user");
        assertTitleEquals("Delete User");
        setWorkingForm("deleteUserForm");
        submit();
        assertTextPresent("Selected user deleted.");

        this.logoutCurrentUser();
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
        setTextField(REGISTER_FORM_ID + ":birthday", birthdayValid);
        selectOption(REGISTER_FORM_ID + ":gender", gender);
        submit();
        assertTextPresent("Registration sucessfull!");
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
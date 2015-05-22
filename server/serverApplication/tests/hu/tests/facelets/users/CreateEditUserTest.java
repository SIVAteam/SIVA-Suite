package hu.tests.facelets.users;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.checkCheckbox;
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
 * Concerning: /T240/, /T250/, /T260/ 
 */
public class CreateEditUserTest {   

    // IDs
    private static final String SEARCH_FORM_ID = "searchForm";
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";
    private static final String CREATE_USER_FORM_ID = "createUserForm";
    private static final String EDIT_USER_FORM_ID = "editUserForm";
    private static final String LIST_USER_TABLE_ID = "listUsersTable";

    // User data
    private static String email;
    private static final String academicTitle = "Prof";
    private static final String firstName1 = "Hans";
    private static final String firstName2 = "Sepp";
    private static final String lastName = "Wurst";
    private static final String birthday = "01-02-2003";
    private static final String gender = "male";
    private static final String role = "Tutor";
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
    public void testCreateEditUser() {

        this.loginAdministrator();

        // Create new tutor
        clickLinkWithExactText("dashboard");
        clickLinkWithExactText("Manage Users");
        clickLinkWithExactText("Create User");
        assertFormPresent(CREATE_USER_FORM_ID);
        setWorkingForm(CREATE_USER_FORM_ID);
        setTextField(CREATE_USER_FORM_ID + ":email", email);
        selectOption(CREATE_USER_FORM_ID + ":academicTitle", academicTitle);
        setTextField(CREATE_USER_FORM_ID + ":firstName", firstName1);
        setTextField(CREATE_USER_FORM_ID + ":lastName", lastName);
        selectOption(CREATE_USER_FORM_ID + ":country", "Switzerland");
        selectOption(CREATE_USER_FORM_ID + ":gender", gender);
        selectOption(CREATE_USER_FORM_ID + ":role", role);
        submit();
        assertTextPresent("User created successfull!");
        assertTitleEquals("Users");
        searchUser(email);
        assertTextInTable(LIST_USER_TABLE_ID, new String[] {
                lastName + ", " + firstName1 + ", " + academicTitle, email });

        // Edit created tutor
        searchUser(email);
        clickLinkWithExactText("edit user");
        assertTitleEquals("Edit User");
        assertFormPresent(EDIT_USER_FORM_ID);
        setWorkingForm(EDIT_USER_FORM_ID);
        setTextField(EDIT_USER_FORM_ID + ":email", email);
        selectOption(EDIT_USER_FORM_ID + ":academicTitle", academicTitle);
        setTextField(EDIT_USER_FORM_ID + ":firstName", firstName2);
        setTextField(EDIT_USER_FORM_ID + ":lastName", lastName);       
        selectOption(EDIT_USER_FORM_ID + ":gender", gender);
        selectOption(EDIT_USER_FORM_ID + ":country", "Austria");
        selectOption(EDIT_USER_FORM_ID + ":role", role);
        //additional fields
        setTextField(EDIT_USER_FORM_ID + ":street", "Sepperlweg");
        setTextField(EDIT_USER_FORM_ID + ":zip", "91453");
        setTextField(EDIT_USER_FORM_ID + ":city", "Josefsstadt");
        setTextField(EDIT_USER_FORM_ID + ":phone", "0171564362");
        setTextField(EDIT_USER_FORM_ID + ":birthday", birthday);
        setTextField(EDIT_USER_FORM_ID + ":fax", "0675828");
        setTextField(EDIT_USER_FORM_ID + ":website", "www.testsite.com");
        setTextField(EDIT_USER_FORM_ID + ":photo", TEST_PICTURE_DIR);
        checkCheckbox(EDIT_USER_FORM_ID + ":visible");
        
        submit();

        assertTextPresent("Edited profile successfull!");
        searchUser(email);
        assertTextInTable(LIST_USER_TABLE_ID, new String[] {
                lastName + ", " + firstName2 + ", " + academicTitle, email });

        // Delete created tutor that was used for this test
        assertTextInTable(LIST_USER_TABLE_ID, email);

        // Search for index of the correct "delete user" link (ugly)
        searchUser(email);
        clickLinkWithExactText("delete user");
        assertTitleEquals("Delete User");
        setWorkingForm("deleteUserForm");
        submit();
        assertTextPresent("Selected user deleted.");
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

    private void loginAdministrator() {

        // Login using the form in the header
        beginAt(Globals.getBaseUrl());
        assertTitleEquals("Welcome");
        assertFormPresent(HEADERLOGIN_FORM_ID);
        setWorkingForm(HEADERLOGIN_FORM_ID);
        setTextField(HEADERLOGIN_FORM_ID + ":username",
                adminEmail);
        setTextField(HEADERLOGIN_FORM_ID + ":password",
                adminPassword);
        submit();

        // Check if user is now logged in
        assertTitleEquals("Welcome");
        assertFormNotPresent(HEADERLOGIN_FORM_ID);
        assertFormPresent(HEADERLOGOUT_FORM_ID);
    }
    
    @AfterClass
    public static void close() throws InconsistencyException {
	persistenceProvider.getUserStore().delete(admin);
	persistenceProvider.close();
    }
}
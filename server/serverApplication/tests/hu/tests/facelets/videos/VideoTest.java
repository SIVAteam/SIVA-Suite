package hu.tests.facelets.videos;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertLinkPresentWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionSelected;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickRadioOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.closeWindow;
import static net.sourceforge.jwebunit.junit.JWebUnit.getElementTextByXPath;
import static net.sourceforge.jwebunit.junit.JWebUnit.getTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setScriptingEnabled;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.html.Table;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This Test concerns all video related functions. The Pflichtenheft relating
 * Test Numbers can be found in the testing methode testCompleteVideo
 * 
 */
public class VideoTest {

    private static final String BASE_URL = Globals.getBaseUrl();

    // facelets
    private static final String ACCESS_VIDEO_JSF = "/xhtml/videos/accessVideo.jsf";

    // forms
    private static final String SEARCH_FORM_ID = "searchForm";
    private static final String HEADER_LOGIN_FORM = "headerLoginForm";
    private static final String HEADER_LOGOUT_FORM = "headerLogoutForm";
    private static final String REGISTER_USER_FORM = "registerForm";
    private static final String EDIT_USER_FORM = "editUserForm";
    private static final String DELETE_USER_FORM = "deleteUserForm";
    private static final String CREATE_GROUP_FORM = "createGroupForm";
    private static final String CREATE_VIDEO_FORM = "createVideoForm";
    private static final String GENERATE_TOKEN_FORM = "generateTokenForm";
    private static final String EDIT_VIDEO_FORM = "createVideoForm";
    private static final String STOP_VIDEO_FORM = "stopVideoForm";
    private static final String DELTE_VIDEO_FORM = "deleteVideoForm";

    // links
    
    private static final String MANAGE_USERS_LINK = "Manage Users";
    private static final String DELETE_USER_LINK = "delete user";
    private static final String REGISTER_LINK = "register";
    private static final String MANAGE_GROUPS_LINK = "Manage Groups";
    private static final String EDIT_GROUP_LINK = "edit group";
    private static final String ADD_REMOVE_USERS_LINK = "Add And Remove User(s)";
    private static final String ADD_USER_TO_GROUP_LINK = "add user to group";
    private static final String DASHBOARD_LINK = "dashboard";
    private static final String MANAGE_VIDEO_LINK = "Manage Videos";
    private static final String CREATE_VIDEO_LINK = "Create Video";
    private static final String VIDEOS_LINK = "videos";
    private static final String GENERATE_PRINTABLE_TOKEN_LINK = "Generate printable token list";
    private static final String STOP_VIDEO_LINK = "deactivate video";
    private static final String DELETE_VIDEO_LINK = "delete video";
    private static final String LIST_GROUPS_TABLE_ID = "listGroupsTable";
    private static final String DELETE_GROUP_FORM_ID = "deleteGroupForm";

    // input fields

    // login
    private static final String HEADER_LOGIN_USERNAME_FIELD = ":username";
    private static final String HEADER_LOGIN_PASSWORD_FIELD = ":password";

    // create group
    private static final String CREATE_GROUP_TITLE_FIELD = ":title";

    // create user
    private static final String REGISTER_USER_EMAIL_FIELD = ":email";
    private static final String REGISTER_USER_PASSWORD_FIELD = ":password";
    private static final String REGISTER_USER_PASSWORD_REPEAT_FIELD = ":passwordRepeat";
    private static final String REGISTER_USER_ACADEMIC_TITLE_FIELD = ":academicTitle";
    private static final String REGISTER_USER_FIRST_NAME_FIELD = ":firstName";
    private static final String REGISTER_USER_LAST_NAME_FIELD = ":lastName";
    private static final String REGISTER_USER_BIRTHDAY_FIELD = ":birthday";
    private static final String REGISTER_USER_GENDER_FIELD = ":gender";

    // create video
    private static final String VIDEO_TITLE_FIELD = ":title";
    private static final String VIDEO_GROUP_FIELD = ":group";
    private static final String VIDEO_PUBLICATION_FIELD = ":videoPublication";
    private static final String START_TIME = ":startTime";
    private static final String STOP_TIME = ":endTime";
    private static final String VIDEO_DESCRIPTION_FIELD = ":description";

    // etc
    private static final String AMOUNT_OF_TOKEN_FIELD = ":numberOfToken";

    // site titles
    private static final String LOGIN_TITLE = "Login";
    private static final String LIST_USERS_TITLE = "Users";
    private static final String REGISTER_USER_TITLE = "Register";
    private static final String LIST_GROUPS_TITLE = "Groups";
    private static final String ADD_USERS_TO_GROUP_TITLE = "Add Users to Group";
    private static final String LIST_VIDEO_TITLE = "Videos";
    private static final String WELCOME_TITLE = "Welcome";

    // messages
    private static final String DELETE_USER_SUCCESS = "Selected user deleted";
    private static final String REGISTER_USER_SUCCESS = "Registration sucessfull!";
    private static final String GROUP_DELETE_SUCCESS = "Group deleted.";
    private static final String ADD_USER_TO_GROUP_SUCCESS = "User was successfull added to the group.";   
    private static final String VIDEO_STOP_SUCCESS = "Video deactivated";
    private static final String VIDEO_DELETE_SUCCESS = "Video successfully deleted.";
    private static final String EDIT_VIDEO_SUCCESS = "Video successfully edited!";

    // test data

    // tutor
    private static String TUTOR_EMAIL;
    private static final String TUTOR_PASSWORD = "tutorPW123";
    private static final String TUTOR_ACADEMIC_TITLE = "PhD";
    private static final String TUTOR_FIRST_NAME = "Some";
    private static final String TUTOR_LAST_NAME = "Tutor";
    private static final String TUTOR_BIRTHDAY = "01-01-1970";
    private static final String TUTOR_GENDER = "female";

    // user
    private static String USER_EMAIL;
    private static final String USER_PASSWORD = "userPW123";
    private static final String USER_ACADEMIC_TITLE = "";
    private static final String USER_FIRST_NAME = "Some";
    private static final String USER_LAST_NAME = "User";
    private static final String USER_BIRTHDAY = "20-12-2012";
    private static final String USER_GENDER = "female";

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

    // group
    private static final String GROUP_TITLE = "testGroup";

    // video
    private static final String VIDEO_TITLE = "Studentenumfrage";
    private static final String VIDEO_TITLE_NEW = "Studentenbefragung";
    private static final String VIDEO_DESCRIPTION = "Allgemeine Umfrage über unsere Studenten.";
    private static final String VIDEO_PUBLICATION = "Make video visible for all users having a unique access token/link";
    private static final String AMOUNT_OF_TOKEN = "1";

    // token
    private static final String APPEND_TOKEN = "?token=";
    private static final String TOKEN_XPATH = "//*[@id=\"listTokenTable\"]/tbody/tr/td[1]";
    private static String token;

    @Before
    public void prepare() throws InconsistencyException, InterruptedException {
	setBaseUrl(BASE_URL);

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);
	adminEmail= "admin"+ System.currentTimeMillis() + "@mailinator.com";
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
	TUTOR_EMAIL = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	USER_EMAIL = System.currentTimeMillis() + "@mailinator.com";
    }

    @Test
    public void testCompleteVideos() throws InterruptedException {

	beginAt(Globals.getBaseUrl());

	this.loginAdmin();
	
	// test /T262/
	this.createTutor();
	
	// test /T263/
	this.logoutUser();
	this.registerUser();
	
	// test/T264/
	this.loginTutor();
	
	// test/T265/
	this.createGroup();
	
	// test/T266/
	this.addUserToGroup();
	
	// test/T270/
	this.createVideo();
	
	// test/T280/
	this.editVideo();
	
	// test/T295/
	this.generateToken();
	
	// test/T290/
	this.publishVideo();	
	
	// test/T320/
	this.logoutUser();
	this.loginUser();
	this.assertVideoListed();
	
	// test/T330/
	this.openVideoWithToken();
	
	// test/T345/
	closeWindow();
	
	// test/T350/
	this.openVideoWithToken();
	
	// test/T400/
	this.loginTutor();
	this.stopVideo();
	
	// test/T420/
	this.deleteVideos();
	
	// test/T430/
	this.loginAdmin();
	this.deleteGroup();
	
	// test/T440/
	this.deleteUser(USER_EMAIL);
	
	// test/T450/
	this.deleteUser(TUTOR_EMAIL);
    }

    private void loginAdmin() {

	beginAt(Globals.getBaseUrl());
	assertFormPresent(HEADER_LOGIN_FORM);
	setWorkingForm(HEADER_LOGIN_FORM);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_USERNAME_FIELD, adminEmail);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_PASSWORD_FIELD, adminPassword);
	submit();

	// Check if user is logged in
	assertTitleEquals(WELCOME_TITLE);
	assertFormNotPresent(HEADER_LOGIN_FORM);
	assertFormPresent(HEADER_LOGOUT_FORM);
	clickLinkWithExactText(DASHBOARD_LINK);
    }

    private void loginTutor() {

	beginAt(Globals.getBaseUrl());
	// Login using the form in the header
	assertFormPresent(HEADER_LOGIN_FORM);
	setWorkingForm(HEADER_LOGIN_FORM);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_USERNAME_FIELD, TUTOR_EMAIL);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_PASSWORD_FIELD, TUTOR_PASSWORD);
	submit();

	// Check if user is logged in
	assertTitleEquals(WELCOME_TITLE);
	assertFormNotPresent(HEADER_LOGIN_FORM);
	assertTextPresent(TUTOR_ACADEMIC_TITLE + " " + TUTOR_FIRST_NAME + " " + TUTOR_LAST_NAME);
	assertFormPresent(HEADER_LOGOUT_FORM);
	clickLinkWithExactText(DASHBOARD_LINK);
    }

    private void loginUser() {

	beginAt(Globals.getBaseUrl());
	// Login using the form in the header
	assertFormPresent(HEADER_LOGIN_FORM);
	setWorkingForm(HEADER_LOGIN_FORM);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_USERNAME_FIELD, USER_EMAIL);
	setTextField(HEADER_LOGIN_FORM + HEADER_LOGIN_PASSWORD_FIELD, USER_PASSWORD);
	submit();

	// Check if user is logged in
	assertTitleEquals(WELCOME_TITLE);
	assertFormNotPresent(HEADER_LOGIN_FORM);
	assertTextPresent(USER_ACADEMIC_TITLE + " " + USER_FIRST_NAME + " " + USER_LAST_NAME);
	assertFormPresent(HEADER_LOGOUT_FORM);
	clickLinkWithExactText(DASHBOARD_LINK);
    }

    private void createTutor() {
	// tutor has to register himself, that password is known for later login
	this.logoutUser();
	this.registerTutor();
	this.loginAdmin();

	// navigate to create user facelet
	assertLinkPresentWithExactText(MANAGE_USERS_LINK);
	clickLinkWithExactText(MANAGE_USERS_LINK);
	assertTitleEquals("Users");
	// search user1
	searchUser(TUTOR_EMAIL);
	clickLinkWithExactText("edit user");
	assertTitleEquals("Edit User");

	// change role
	assertFormPresent(EDIT_USER_FORM);
	setWorkingForm(EDIT_USER_FORM);
	selectOption(EDIT_USER_FORM + ":role", "Tutor");
	submit();
	assertTextPresent("Edited profile successfull!");
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

    private void registerTutor() {
	// navigate to register facelet
	assertLinkPresentWithExactText(REGISTER_LINK);
	clickLinkWithExactText(REGISTER_LINK);

	assertTitleEquals(REGISTER_USER_TITLE);
	assertFormPresent(REGISTER_USER_FORM);
	setWorkingForm(REGISTER_USER_FORM);

	// Register a new user
	setTextField(REGISTER_USER_FORM + REGISTER_USER_EMAIL_FIELD, TUTOR_EMAIL);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_PASSWORD_FIELD, TUTOR_PASSWORD);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_PASSWORD_REPEAT_FIELD, TUTOR_PASSWORD);
	selectOption(REGISTER_USER_FORM + REGISTER_USER_ACADEMIC_TITLE_FIELD, TUTOR_ACADEMIC_TITLE);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_FIRST_NAME_FIELD, TUTOR_FIRST_NAME);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_LAST_NAME_FIELD, TUTOR_LAST_NAME);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_BIRTHDAY_FIELD, TUTOR_BIRTHDAY);
	selectOption(REGISTER_USER_FORM + REGISTER_USER_GENDER_FIELD, TUTOR_GENDER);
	submit();
	assertTextPresent(REGISTER_USER_SUCCESS);
    }

    private void registerUser() {
	// navigate to register facelet
	assertLinkPresentWithExactText(REGISTER_LINK);
	clickLinkWithExactText(REGISTER_LINK);

	assertTitleEquals(REGISTER_USER_TITLE);
	assertFormPresent(REGISTER_USER_FORM);
	setWorkingForm(REGISTER_USER_FORM);

	// Register a new user
	setTextField(REGISTER_USER_FORM + REGISTER_USER_EMAIL_FIELD, USER_EMAIL);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_PASSWORD_FIELD, USER_PASSWORD);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_PASSWORD_REPEAT_FIELD, USER_PASSWORD);
	selectOption(REGISTER_USER_FORM + REGISTER_USER_ACADEMIC_TITLE_FIELD, USER_ACADEMIC_TITLE);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_FIRST_NAME_FIELD, USER_FIRST_NAME);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_LAST_NAME_FIELD, USER_LAST_NAME);
	setTextField(REGISTER_USER_FORM + REGISTER_USER_BIRTHDAY_FIELD, USER_BIRTHDAY);
	selectOption(REGISTER_USER_FORM + REGISTER_USER_GENDER_FIELD, USER_GENDER);
	submit();
	assertTextPresent(REGISTER_USER_SUCCESS);
    }

    private void deleteUser(String email) {
	// goto manage users facelet
	clickLinkWithExactText(DASHBOARD_LINK);
	assertLinkPresentWithExactText(MANAGE_USERS_LINK);
	clickLinkWithExactText(MANAGE_USERS_LINK);
	searchUser(email);
	assertLinkPresentWithExactText(DELETE_USER_LINK);
	clickLinkWithExactText(DELETE_USER_LINK);

	assertFormPresent(DELETE_USER_FORM);
	setWorkingForm(DELETE_USER_FORM);
	submit();

	assertTextPresent(DELETE_USER_SUCCESS);
	assertTitleEquals(LIST_USERS_TITLE);
    }

    private void logoutUser() {
	// Logout user
	assertFormPresent(HEADER_LOGOUT_FORM);
	setWorkingForm(HEADER_LOGOUT_FORM);
	submit();
	assertTitleEquals(LOGIN_TITLE);
	assertFormNotPresent(HEADER_LOGOUT_FORM);
	assertFormPresent(HEADER_LOGIN_FORM);
    }

    @SuppressWarnings("unchecked")
    private void addUserToGroup() {
	// navigate to add user to group
	assertLinkPresentWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(DASHBOARD_LINK);
	assertLinkPresentWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);		
	assertTitleEquals("Groups");
	clickLinkWithExactText("Own Groups");
	int groupIndex = 0;
	Table groupTable = getTable("listGroupsTable");	
	List<Row> groupRows = groupTable.getRows();
	for (Row r : groupRows) {
	    if (!r.hasText("Group") && !r.hasText("Tutors")) {
		if (!r.hasText(GROUP_TITLE)) {
		    groupIndex++;
		} else {
		    break;
		}
	    }
	}
	clickLinkWithExactText(EDIT_GROUP_LINK, groupIndex);
	assertLinkPresentWithExactText(ADD_REMOVE_USERS_LINK);
	clickLinkWithExactText(ADD_REMOVE_USERS_LINK);

	// add user to group
	assertLinkPresentWithExactText(ADD_USER_TO_GROUP_LINK);
	clickLinkWithExactText(ADD_USER_TO_GROUP_LINK, 1);

	assertTextPresent(ADD_USER_TO_GROUP_SUCCESS);
	assertTitleEquals(ADD_USERS_TO_GROUP_TITLE);
    }

    private void createGroup() {
	// navigate to create group
	assertLinkPresentWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	assertTitleEquals("Groups");
	clickLinkWithExactText("Create Group");

	// set title and visibility and create group
	setWorkingForm(CREATE_GROUP_FORM);
	setTextField(CREATE_GROUP_FORM + CREATE_GROUP_TITLE_FIELD, GROUP_TITLE);
	assertRadioOptionPresent(CREATE_GROUP_FORM+":j_idt60","false");
	assertRadioOptionPresent(CREATE_GROUP_FORM+":j_idt60","true");
	clickRadioOption(CREATE_GROUP_FORM+":j_idt60","false");
	assertRadioOptionSelected(CREATE_GROUP_FORM + ":j_idt60","false");
	submit();

	assertTitleEquals(LIST_GROUPS_TITLE);
    }

    private void assertVideoListed() {
	// navigate to list videos
	assertLinkPresentWithExactText(VIDEOS_LINK);
	clickLinkWithExactText(VIDEOS_LINK);
	clickLinkWithExactText("Active Videos");
	Table videoTable = getTable("listVideosTable");
	int counter = 1;
	clickLinkWithExactText("1");
	while(!videoTable.hasText(VIDEO_TITLE_NEW)){
	    ++counter;
	    clickLinkWithExactText(""+counter);
	    videoTable = getTable("listVideosTable");
	}
	assertTextInTable("listVideosTable", VIDEO_TITLE_NEW);
    }

    private void deleteGroup() {
	// nagivate to delete group
	assertLinkPresentWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(DASHBOARD_LINK);
	assertLinkPresentWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	int groupIndex = 0;
	Table groupTable = getTable(LIST_GROUPS_TABLE_ID);
	
	int counter = 1;
	clickLinkWithExactText("1");
	while(!groupTable.hasText(GROUP_TITLE)){
	    ++counter;
	    clickLinkWithExactText(""+counter);
	    groupTable = getTable(LIST_GROUPS_TABLE_ID);
	}
	@SuppressWarnings("unchecked")
	List<Row> groupRows = groupTable.getRows();
	for (Row r : groupRows) {
	    if (!r.hasText("Group") && !r.hasText("Tutors")) {
		if (!r.hasText(GROUP_TITLE)) {
		    groupIndex++;
		} else {
		    break;
		}
	    }
	}
	
	// confirm delete
	clickLinkWithExactText("delete group", groupIndex);	
	assertTitleEquals("Delete Group");
	assertTextPresent(GROUP_TITLE);

	assertFormPresent(DELETE_GROUP_FORM_ID);
	setWorkingForm(DELETE_GROUP_FORM_ID);
	submit();
	assertTitleEquals(LIST_GROUPS_TITLE);
	assertTextPresent(GROUP_DELETE_SUCCESS);
    }

    private void createVideo() {
	// navigate to create video
	assertLinkPresentWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(DASHBOARD_LINK);
	assertLinkPresentWithExactText(MANAGE_VIDEO_LINK);
	clickLinkWithExactText(MANAGE_VIDEO_LINK);
	assertLinkPresentWithExactText(CREATE_VIDEO_LINK);
	clickLinkWithExactText(CREATE_VIDEO_LINK);

	// create video
	setWorkingForm(CREATE_VIDEO_FORM);
	setTextField(CREATE_VIDEO_FORM + VIDEO_TITLE_FIELD, VIDEO_TITLE);
	selectOption(CREATE_VIDEO_FORM + VIDEO_GROUP_FIELD, GROUP_TITLE);
	selectOption(CREATE_VIDEO_FORM + VIDEO_PUBLICATION_FIELD, VIDEO_PUBLICATION);

	submit();
	assertTitleEquals("Edit Video");
    }

    private void publishVideo() {
	// navigate to edit video
	assertLinkPresentWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText("Own Groups");
	//owns only one group, so no need to search for an index
	clickLinkWithExactText("show videos", 0);
	clickLinkWithExactText("edit video", 0);	

	// set start time
	setWorkingForm(CREATE_VIDEO_FORM);

	// get today and tomorrow
	GregorianCalendar calendar = new GregorianCalendar();
	Date now = calendar.getTime();
	calendar.add(Calendar.DAY_OF_MONTH, 2);
	Date tomorrow = calendar.getTime();
	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	String todayFormat = sdf.format(now) + " 00:00";
	String tomorrowFormat = sdf.format(tomorrow) + " 00:00";

	setTextField(CREATE_VIDEO_FORM + START_TIME, todayFormat);
	setTextField(CREATE_VIDEO_FORM + STOP_TIME,
		tomorrowFormat);
	submit();

	assertTextPresent("Please upload a video file before activating the video.");
	
	setTextField(CREATE_VIDEO_FORM + ":video", "./tests/resources/brunoNodes.zip");
	submit();
	assertTextPresent(EDIT_VIDEO_SUCCESS);
    }

    private void editVideo() {
	// navigate to edit video	
	setWorkingForm(EDIT_VIDEO_FORM);
	setTextField(EDIT_VIDEO_FORM + VIDEO_TITLE_FIELD, VIDEO_TITLE_NEW);
	setTextField(EDIT_VIDEO_FORM + VIDEO_DESCRIPTION_FIELD, VIDEO_DESCRIPTION);

	submit();
	assertTextPresent(EDIT_VIDEO_SUCCESS);
    }
    
    private void generateToken() {
	assertLinkPresentWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText("Own Groups");
	//owns only one group, so no need to search for an index
	clickLinkWithExactText("show videos", 0);
	clickLinkWithExactText("edit video", 0);
	clickLinkWithExactText(GENERATE_PRINTABLE_TOKEN_LINK);

	setWorkingForm(GENERATE_TOKEN_FORM);
	setTextField(GENERATE_TOKEN_FORM + AMOUNT_OF_TOKEN_FIELD, AMOUNT_OF_TOKEN);
	submit();

	token = getElementTextByXPath(TOKEN_XPATH);

	Assert.assertFalse(token.equals(""));
    }

    private void openVideoWithToken() {
	// open browser with token url
	setScriptingEnabled(false);
	beginAt(ACCESS_VIDEO_JSF + APPEND_TOKEN + token);
	
	assertTitleEquals(VIDEO_TITLE_NEW);
	setScriptingEnabled(true);
    }

    /**
     * Can only be done by an owner of the video.
     */
    private void stopVideo() {
	// navigate to
	clickLinkWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText("Own Groups");
	//owns only one group, so no need to search for an index
	clickLinkWithExactText("show videos", 0);
	clickLinkWithExactText(STOP_VIDEO_LINK, 0);

	// confirm delete
	setWorkingForm(STOP_VIDEO_FORM);
	submit();

	assertTextPresent(VIDEO_STOP_SUCCESS);
	assertTitleEquals(LIST_VIDEO_TITLE);
    }

    /**
     * Can only be done by an owner of the video.
     */
    private void deleteVideos() {
	// navigate to list videos
	clickLinkWithExactText(DASHBOARD_LINK);
	clickLinkWithExactText(MANAGE_GROUPS_LINK);
	clickLinkWithExactText("Own Groups");
	//owns only one group, so no need to search for an index
	clickLinkWithExactText("show videos", 0);	

	// delete created video
	clickLinkWithExactText(DELETE_VIDEO_LINK);

	setWorkingForm(DELTE_VIDEO_FORM);
	submit();

	assertTitleEquals(LIST_VIDEO_TITLE);
	assertTextPresent(VIDEO_DELETE_SUCCESS);	
    }

    @AfterClass
    public static void close() throws InconsistencyException {
	persistenceProvider.getUserStore().delete(admin);
	persistenceProvider.close();
    }
}
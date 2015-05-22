package hu.tests.facelets.testscenarios;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertRadioOptionSelected;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertSubmitButtonPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextInTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickRadioOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.getTable;
import static net.sourceforge.jwebunit.junit.JWebUnit.gotoWindowByTitle;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setScriptingEnabled;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.common.TokenBean;
import hu.backingbeans.videos.VideoBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.controller.videos.VideoAction;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SessionData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import net.sourceforge.jwebunit.exception.TestingEngineResponseException;
import net.sourceforge.jwebunit.html.Row;
import net.sourceforge.jwebunit.html.Table;
import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
import net.sourceforge.jwebunit.util.TestContext;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * This class is testing some function which are not present in the
 * "Pflichtenheft".
 * 
 */
public class additionalTests {

    private static final String MANAGE_USERS_LINK = "Manage Users";

    private static final String GROUPS_TITLE = "Groups";

    private static final String GROUPS_NAVIGATION = "groups";

    private static final String VIDEOS_NAVIGATION = "videos";

    private static final String VIDEO_PASSWORD_FORM_ID = "accessForm";

    private static final String DASHBOARD_TITLE = "Dashboard";

    private static final String ATTENDANTS_VIDEO = "4Staedtequiz";

    private static final String ANONYM_VIDEO = "3Städtequiz";

    private static final String PASSWORD_VIDEO = "2Städtequiz";

    private static final String TOKEN_VIDEO = "1Städtequiz";

    private static final String PASSWORD = "Test123";

    private static final String VIDEOS = "Videos";

    static final String VIDEOS_TITLE = VIDEOS;

    private static final String EDIT_GROUP_TITLE = "Edit Group";

    private static final String CREATE_VIDEO_FORM_ID = "createVideoForm";

    private static final String EDIT_GROUP_FORM_ID = "editGroupForm";

    private static final String LOGIN_FORM_ID = "loginForm";

    private static final String LOGIN_JSF = "/xhtml/users/login.jsf";

    private static final String SEARCH_FORM_ID = "searchForm";

    private static final String BASE_URL = Globals.getBaseUrl();

    // URL's
    private static final String REGISTER_JSF = "/xhtml/users/register.jsf";

    // ID's
    private static final String REGISTER_FORM_ID = "registerForm";
    private static final String EDIT_USER_FORM_ID = "editUserForm";
    private static final String LIST_GROUPS_TABLE_ID = "listGroupsTable";
    private static final String CREATE_GROUP_FORM_ID = "createGroupForm";
    private static final String SIGN_UP_FORM_ID = "signUpForm";
    private static final String DELETE_GROUP_FORM_ID = "deleteGroupForm";

    // User1
    private static String user1email;
    private static final String user1password = "muster!123";
    private static final String user1firstName = "Max";
    private static final String user1name = "Mustermann";
    private static final String user1title = "PhD";
    private static final String user1gender = "male";
    private static final String user1birthdate = "1-1-1990";

    // User2
    private static String user2email;

    // User3
    private static String user3email;

    // User4
    private static String user4email;

    // Group
    private static final String groupTitle = "Deutsche Städte";
    private static final String groupTitleEdit = "deutsche Städte";

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

    private static VideoBean videoBean;
    private static VideoPublicationBean videoPublicationBean;
    private static TokenBean tokenBean;
    private static VideoAction videoAction;
    private static FacesContext cxtMock;
    private static UIViewRoot uiViewRoot;
    private static Locale locale = new Locale("de");
    private static SessionData session;
    
    private static int id1;
    private static int id2;
    private static int id3;
    private static int id4;

    private static final String TEST_VIDEO_DIRECTORY = "./tests/resources/brunoNodes.zip";

    /**
     * Prepare for the test.
     * 
     * @throws InconsistencyException
     * @throws InterruptedException
     */
    @Before
    public void prepare() throws InconsistencyException, InterruptedException {

	videoBean = new VideoBean();
	videoPublicationBean = new VideoPublicationBean();

	tokenBean = new TokenBean();
	videoAction = new VideoAction();
	session = new SessionData();
	videoAction.setPersistenceProvider(persistenceProvider);
	videoAction.setVideoBean(videoBean);
	videoAction.setVideoPublicationBean(videoPublicationBean);

	videoAction.setTokenBean(tokenBean);
	cxtMock = mock(FacesContext.class);
	uiViewRoot = mock(UIViewRoot.class);
	videoAction.setMock(cxtMock);
	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

	setBaseUrl(BASE_URL);

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);
	email = "admin" + System.currentTimeMillis() + "@mailinator.com";
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
	user1email = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	user2email = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	user3email = System.currentTimeMillis() + "@mailinator.com";
	Thread.sleep(5);
	user4email = System.currentTimeMillis() + "@mailinator.com";
    }

    /**
     * This is the test method with all additional tests.
     * 
     * @throws MalformedURLException
     * @throws TestingEngineResponseException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAdditional() throws TestingEngineResponseException, MalformedURLException {

	// register user1
	registerUser(user1email, user1password, user1title, user1firstName, user1name,
		user1birthdate, user1gender);

	// register user2
	registerUser(user2email, user1password, user1title, user1firstName, user1name,
		user1birthdate, user1gender);

	// register user3
	registerUser(user3email, user1password, user1title, user1firstName, user1name,
		user1birthdate, user1gender);

	// register user
	registerUser(user4email, user1password, user1title, user1firstName, user1name,
		user1birthdate, user1gender);

	// user1 to tutor1
	// login admin
	this.login(email, password);
	changeUserRole("Tutor", user1email);
	assertTextPresent("Edited profile successfull!");

	// user 2 to tutor2
	changeUserRole("Tutor", user2email);
	assertTextPresent("Edited profile successfull!");

	// user 4 to tutor4
	changeUserRole("Tutor", user4email);
	assertTextPresent("Edited profile successfull!");

	// search user1;
	this.searchUser(user1email);
	assertTextPresent(user1email);
	assertTextNotPresent(user2email);

	this.logout();

	// login tutor1
	this.login(user1email, user1password);
	assertTitleEquals(DASHBOARD_TITLE);

	// tutor1 create group
	createGroup(groupTitle);
	assertTextPresent("Group successfully created!");
	// add tutor2 as tutor

	// Search for index of the correct "sign up" link (ugly)
	int groupIndex = 0;
	clickLinkWithExactText("Own Groups");
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

	clickLinkWithExactText("edit group", groupIndex);
	assertTitleEquals(EDIT_GROUP_TITLE);
	clickLinkWithExactText("Add And Remove Tutor(s)");
	assertTitleEquals("Add Tutors To Group");

	// search tutor2 to add
	this.searchUser(user2email);
	clickLinkWithExactText("add tutor to group");
	assertTextPresent("Tutor added to group!");

	clickLinkWithExactText("remove tutor from group");
	assertTextPresent("Tutor removed from group!");

	clickLinkWithExactText("add users");
	clickLinkWithExactText("add user to group");

	// edit group
	clickLinkWithExactText("back to group");
	assertTitleEquals(EDIT_GROUP_TITLE);

	assertFormPresent(EDIT_GROUP_FORM_ID);
	setWorkingForm(EDIT_GROUP_FORM_ID);
	setTextField(EDIT_GROUP_FORM_ID + ":title", groupTitleEdit);
	submit();
	assertTextPresent("Group successfully edited!");

	// create video
	groupIndex = 0;
	clickLinkWithExactText("Own Groups");
	groupTable = getTable(LIST_GROUPS_TABLE_ID);
	groupRows = groupTable.getRows();
	for (Row r : groupRows) {
	    if (!r.hasText("Group") && !r.hasText("Tutors")) {
		if (!r.hasText(groupTitle)) {
		    groupIndex++;
		} else {
		    break;
		}
	    }
	}
	clickLinkWithExactText("show videos", groupIndex - 1);
	assertTitleEquals(VIDEOS_TITLE);
	clickLinkWithExactText("Create Video");
	assertTitleEquals("Create Video");
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":title", TOKEN_VIDEO);
	selectOption(CREATE_VIDEO_FORM_ID + ":group", groupTitleEdit);
	selectOption(CREATE_VIDEO_FORM_ID + ":videoPublication",
		"Make video visible for all users having a unique access token/link");
	submit();

	assertTitleEquals("Edit Video");

	// send mails to all users of the group

	clickLinkWithExactText("Add and remove Users");
	assertTitleEquals("Add Users to Video");
	clickLinkWithExactText("Other");
	assertSubmitButtonPresent("sendTokenToAll:j_idt113");
	submit("sendTokenToAll:j_idt113", "Send access tokens to all users of the group");
	assertTextPresent("Sending mails was successful.");

	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	assertTextPresent("Please upload a video file before activating the video.");

	upload(TOKEN_VIDEO,1);

	// start
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	
	clickLinkWithExactText("activate video");
	setWorkingForm("startVideoForm");
	submit();
	assertTextPresent("Video activated");

	// for password
	clickLinkWithExactText(VIDEOS_NAVIGATION);
	assertTitleEquals(VIDEOS_TITLE);
	clickLinkWithExactText("Create Video");
	assertTitleEquals("Create Video");
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":title", PASSWORD_VIDEO);
	selectOption(CREATE_VIDEO_FORM_ID + ":group", groupTitleEdit);
	selectOption(CREATE_VIDEO_FORM_ID + ":videoPublication",
		"Make video visible for all users knowing a specified password");
	submit();
	assertTitleEquals("Edit Video");

	// set password
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":password", PASSWORD);
	submit();
	assertTextPresent("Video successfully edited!");

	// start this
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	assertTextPresent("Please upload a video file before activating the video.");

	upload(PASSWORD_VIDEO,2);

	// start
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	int videoIndex = 0;
	Table videoTable = getTable("listVideosTable");
	List<Row> videoRows = videoTable.getRows();
	for (Row r : videoRows) {
	    if (!r.hasText("Video") && !r.hasText("Group")) {
		if (!r.hasText(PASSWORD_VIDEO)) {
		    videoIndex++;
		} else {
		    break;
		}
	    }
	}
	clickLinkWithExactText("edit video", videoIndex - 1);
	assertTextPresent(PASSWORD_VIDEO);
	// set password
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":password", PASSWORD);
	submit();
	assertTextPresent("Video successfully edited!");
	
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	setWorkingForm("startVideoForm");
	submit();
	assertTextPresent("Video activated");

	// duplicate for anonym
	clickLinkWithExactText(VIDEOS_NAVIGATION);
	assertTitleEquals(VIDEOS_TITLE);
	clickLinkWithExactText("Create Video");
	assertTitleEquals("Create Video");
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":title", ANONYM_VIDEO);
	selectOption(CREATE_VIDEO_FORM_ID + ":group", groupTitleEdit);
	selectOption(CREATE_VIDEO_FORM_ID + ":videoPublication",
		"Make video visible for all registered and anonymous users");
	submit();
	assertTitleEquals("Edit Video");

	// start this
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	assertTextPresent("Please upload a video file before activating the video.");

	upload(ANONYM_VIDEO,3);

	// start
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	setWorkingForm("startVideoForm");
	submit();
	assertTextPresent("Video activated");

	// duplicate for group attendants
	clickLinkWithExactText(VIDEOS_NAVIGATION);
	assertTitleEquals(VIDEOS_TITLE);
	clickLinkWithExactText("Create Video");
	assertTitleEquals("Create Video");
	assertFormPresent(CREATE_VIDEO_FORM_ID);
	setWorkingForm(CREATE_VIDEO_FORM_ID);
	setTextField(CREATE_VIDEO_FORM_ID + ":title", ATTENDANTS_VIDEO);
	selectOption(CREATE_VIDEO_FORM_ID + ":group", groupTitleEdit);
	selectOption(CREATE_VIDEO_FORM_ID + ":videoPublication",
		"Make video visible for all registered group attendees");
	submit();
	assertTitleEquals("Edit Video");

	// start this
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	assertTextPresent("Please upload a video file before activating the video.");

	upload(ATTENDANTS_VIDEO,4);

	// start
	clickLinkWithExactText("groups");
	clickLinkWithExactText("Own Groups");
	clickLinkWithExactText("show videos");
	clickLinkWithExactText("activate video");
	setWorkingForm("startVideoForm");
	submit();
	assertTextPresent("Video activated");

	this.logout();

	// login user3
	this.login(user3email, user1password);
	assertTitleEquals(DASHBOARD_TITLE);
	clickLinkWithExactText(GROUPS_NAVIGATION);
	assertTitleEquals(GROUPS_TITLE);
	clickLinkWithExactText("help");
	gotoWindowByTitle("Help");
	assertTitleEquals("Help");
	gotoWindowByTitle(GROUPS_TITLE);
	assertTitleEquals(GROUPS_TITLE);

	// sign up to group
	clickLinkWithExactText("sign up");
	assertTitleEquals("Sign up for Group");
	assertFormPresent(SIGN_UP_FORM_ID);
	setWorkingForm(SIGN_UP_FORM_ID);
	submit();

	assertTextPresent("You have signed up to the group.");

	this.logout();

	// login tutor1
	// remove user from group
	this.login(user1email, user1password);
	clickLinkWithExactText(GROUPS_NAVIGATION);
	assertTitleEquals(GROUPS_TITLE);
	clickLinkWithExactText("edit group");
	assertTitleEquals("Edit Group");
	clickLinkWithExactText("Add And Remove User(s)");

	// add
	clickLinkWithExactText("add tutors");
	this.searchUser(user4email);
	assertTitleEquals("Add Tutors To Group");
	clickLinkWithExactText("add tutor to group");
	assertTextPresent("Tutor added to group!");

	this.searchUser(user2email);
	assertTitleEquals("Add Tutors To Group");
	clickLinkWithExactText("add tutor to group");
	assertTextPresent("Tutor added to group!");

	// remove tutor from group

	assertTitleEquals("Add Tutors To Group");
	this.searchUser(user4email);
	clickLinkWithExactText("remove tutor from group");
	assertTextPresent("Tutor removed from group!");

	// add him again
	assertTitleEquals("Add Tutors To Group");
	this.searchUser(user4email);
	clickLinkWithExactText("add tutor to group");
	assertTextPresent("Tutor added to group!");

	this.logout();

	// get access to video with password
	clickLinkWithExactText(VIDEOS_NAVIGATION);
	assertTitleEquals(VIDEOS_TITLE);
	
	videoIndex = 0;
	videoTable = getTable("listVideosTable");	
	
	int counter = 1;
	clickLinkWithExactText("1");
	while(!videoTable.hasText(PASSWORD_VIDEO)){
	    ++counter;
	    clickLinkWithExactText(""+counter);
	    videoTable = getTable("listVideosTable");
	}
	
	videoRows = videoTable.getRows();
	assertTextInTable("listVideosTable", PASSWORD_VIDEO);
	for (Row r : videoRows) {
	    if (!r.hasText("Video") && !r.hasText("Group")) {
		if (!r.hasText(PASSWORD_VIDEO)) {
		    videoIndex++;
		} else {
		    break;
		}
	    }
	}
	clickLinkWithExactText("watch", videoIndex - 1);
	assertTitleEquals("Access video");
	setWorkingForm(VIDEO_PASSWORD_FORM_ID);
	setTextField("accessForm:password", PASSWORD);
	setScriptingEnabled(false);
	submit();
	assertTextNotPresent("Inserted password was not correct.");
	assertTitleEquals(PASSWORD_VIDEO);
	setScriptingEnabled(true);

	// login tutor2
	this.login(user2email, user1password);

	clickLinkWithExactText(GROUPS_NAVIGATION);
	assertTitleEquals(GROUPS_TITLE);
	clickLinkWithExactText("Own Groups");
	groupIndex = 0;
	groupTable = getTable(LIST_GROUPS_TABLE_ID);
	groupRows = groupTable.getRows();
	for (Row r : groupRows) {
	    if (!r.hasText("Group") && !r.hasText("Tutors")) {
		if (!r.hasText(groupTitle)) {
		    groupIndex++;
		} else {
		    break;
		}
	    }
	}
	clickLinkWithExactText("delete group", groupIndex - 1);
	assertTitleEquals("Delete Group");

	assertFormPresent(DELETE_GROUP_FORM_ID);
	setWorkingForm(DELETE_GROUP_FORM_ID);
	submit();
	assertTextPresent("Group deleted.");

	this.logout();

	// login as admin
	this.login(email, password);
	assertTitleEquals(DASHBOARD_TITLE);

	// ban user3
	clickLinkWithExactText(MANAGE_USERS_LINK);
	assertTitleEquals("Users");
	this.searchUser(user3email);
	clickLinkWithExactText("ban user");
	assertTitleEquals("Ban User");
	setWorkingForm("banUserForm");
	submit();
	assertTextPresent("User banned successfully.");
	this.logout();

	// try login user3
	this.login(user3email, user1password);
	assertTextPresent("You are banned.");

	// login as admin
	this.login(email, password);
	assertTitleEquals(DASHBOARD_TITLE);

	// unban user3
	clickLinkWithExactText(MANAGE_USERS_LINK);
	assertTitleEquals("Users");
	this.searchUser(user3email);
	clickLinkWithExactText("unban user");
	assertTitleEquals("Unban User");
	setWorkingForm("unbanUserForm");
	submit();
	assertTextPresent("User unbanned successfully.");
	this.logout();

	// user3 password forgotten
	beginAt(LOGIN_JSF);
	clickLinkWithExactText("Forgotten?");
	assertTitleEquals("Password Recovery");

	assertFormPresent("recoverPasswordForm");
	setWorkingForm("recoverPasswordForm");
	setTextField("recoverPasswordForm" + ":email", user3email + "a");
	submit();
	assertTextPresent("This email does not exist in this system!");

	setTextField("recoverPasswordForm" + ":email", user3email);
	submit();
	assertTextPresent("An email has been sent to the given address. Please click the link inside the mail to reset your password.");

	// login user3
	this.login(user3email, user1password);
	assertTitleEquals(DASHBOARD_TITLE);

	this.logout();

	// login as admin
	this.login(email, password);
	clickLinkWithExactText(MANAGE_USERS_LINK);
	assertTitleEquals("Users");
	// delete user1
	this.delete(user1email);
	assertTextPresent("Selected user deleted.");
	// delete user2
	this.delete(user2email);
	assertTextPresent("Selected user deleted.");
	// delete user3
	this.delete(user3email);
	assertTextPresent("Selected user deleted.");
	// delete user4
	this.delete(user4email);
	assertTextPresent("Selected user deleted.");

	this.logout();

    }

    private void upload(String videoName, int number) throws TestingEngineResponseException,
	    MalformedURLException {
	HtmlUnitTestingEngineImpl testEngine = new HtmlUnitTestingEngineImpl();
	testEngine.beginAt(new URL(BASE_URL + LOGIN_JSF), new TestContext());

	assertTrue(testEngine.hasForm("headerLoginForm"));
	testEngine.setWorkingForm(LOGIN_FORM_ID, 0);
	testEngine.setTextField(LOGIN_FORM_ID + ":username", user1email);
	testEngine.setTextField(LOGIN_FORM_ID + ":password", user1password);
	testEngine.submit();
	assertTrue(testEngine.hasForm("headerLogoutForm"));
	assertFalse(testEngine.getCurrentPageTitle().equals("Error"));
	testEngine.clickLinkWithExactText(GROUPS_NAVIGATION, 0);
	testEngine.clickLinkWithExactText("Own Groups", 0);
	testEngine.clickLinkWithExactText("show videos", 0);
	assertTrue(testEngine.hasTable("listVideosTable"));	
	Table videoTable = testEngine.getTable("listVideosTable");
	
	int counter = 1;
	clickLinkWithExactText("1");
	while(!videoTable.hasText(videoName)){
	    ++counter;
	    testEngine.clickLinkWithExactText(""+counter,0);
	    videoTable = testEngine.getTable("listVideosTable");
	}
	assertTrue(testEngine.isTextInTable("listVideosTable", videoName));
	int videoIndex = 0;
	
	
	@SuppressWarnings("unchecked")
	List<Row> videoRows = videoTable.getRows();
	for (Row r : videoRows) {
	    if (!r.hasText("Video") && !r.hasText("Group")) {
		if (!r.hasText(videoName)) {
		    videoIndex++;
		} else {
		    break;
		}
	    }
	}
	testEngine.clickLinkWithExactText("edit video", videoIndex - 1);
	assertTrue(testEngine.getPageTitle().equals("Edit Video"));
	
	URL url = testEngine.getPageURL();
	String urlString = url.toString();
	char c = urlString.charAt(0);
	int index = 0;
	while (c != '=') {
	    index++;
	    c = urlString.charAt(index);
	}
	urlString = urlString.substring(index + 1, urlString.length());

	int id = Integer.parseInt(urlString);
	switch(number){
	    case(1):
		id1=id;
	    	break;
	    case(2):
		id2=id;
	    	break;
	    case(3):
		id3=id;
	    	break;
	    case(4):
		id4=id;
	    	break;	    
	}
	// "Upload"
	File file = new File(TEST_VIDEO_DIRECTORY);
	when(cxtMock.isPostback()).thenReturn(true);
	session.setUserId(admin.getId());
	videoBean = new VideoBean();
	videoBean.setId(id);
	videoBean.setTitle(videoName);
	videoBean.setGroupId(persistenceProvider.getGroupStore()
		.getByOwner(persistenceProvider.getUserStore().getByEmail(user1email)).get(0)
		.getId());
	videoAction.setSessionData(session);
	videoAction.setPersistenceProvider(persistenceProvider);
	videoAction.setVideoBean(videoBean);
	videoAction.setVideoUploadMock(file);
	videoAction.clearLogs();
	videoAction.editVideo();
    }

    /**
     * Logout a user.
     */
    private void logout() {
	setWorkingForm("headerLogoutForm");
	submit();
	assertTitleEquals("Login");
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
     * Create a group with a given title.
     * 
     * @param title
     *            to set.
     */
    private void createGroup(String title) {
	clickLinkWithExactText(GROUPS_NAVIGATION);
	assertTitleEquals(GROUPS_TITLE);
	clickLinkWithExactText("Create Group");
	assertTitleEquals("Create Group");

	assertFormPresent(CREATE_GROUP_FORM_ID);
	setWorkingForm(CREATE_GROUP_FORM_ID);
	setTextField(CREATE_GROUP_FORM_ID + ":title", title);
	setTextField(CREATE_GROUP_FORM_ID + ":title", groupTitle);
	assertRadioOptionPresent(CREATE_GROUP_FORM_ID + ":j_idt60", "false");
	assertRadioOptionPresent(CREATE_GROUP_FORM_ID + ":j_idt60", "true");
	clickRadioOption(CREATE_GROUP_FORM_ID + ":j_idt60", "true");
	assertRadioOptionSelected(CREATE_GROUP_FORM_ID + ":j_idt60", "true");
	submit();
    }

    /**
     * Change the role of a user.
     * 
     * @param role
     *            to set.
     * @param user
     *            to change.
     */
    private void changeUserRole(String role, String user) {
	clickLinkWithExactText("dashboard");
	clickLinkWithExactText(MANAGE_USERS_LINK);
	assertTitleEquals("Users");

	// search user1
	searchUser(user);

	clickLinkWithExactText("edit user");
	assertTitleEquals("Edit User");

	// change role
	assertFormPresent(EDIT_USER_FORM_ID);
	setWorkingForm(EDIT_USER_FORM_ID);
	selectOption(EDIT_USER_FORM_ID + ":role", role);
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

    /**
     * Register a new user.
     * 
     * @param email
     *            to set.
     * @param password
     *            to set.
     * @param academicTitle
     *            to set.
     * @param firstName
     *            to set.
     * @param lastName
     *            to set.
     * @param birthday
     *            to set.
     * @param gender
     *            to set.
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
     * Login a user with a user name and a user password.
     * 
     * @param username
     *            for the login.
     * @param password
     *            for the login.
     */
    private void login(String username, String password) {

	// Login
	beginAt(LOGIN_JSF);
	assertTitleEquals("Login");
	assertFormPresent(LOGIN_FORM_ID);
	setWorkingForm(LOGIN_FORM_ID);
	setTextField(LOGIN_FORM_ID + ":username", username);
	setTextField(LOGIN_FORM_ID + ":password", password);
	submit();
    }

    @AfterClass
    public static void close() throws InconsistencyException {
	session.setUserId(admin.getId());
	videoAction.setSessionData(session);
	videoAction.setVideoBean(videoBean);
	videoBean.setId(id1);
	videoAction.deleteVideo();
	assertNull(persistenceProvider.getVideoStore().findById(id1));
	videoBean.setId(id2);
	videoAction.deleteVideo();
	assertNull(persistenceProvider.getVideoStore().findById(id2));
	videoBean.setId(id3);
	videoAction.deleteVideo();
	assertNull(persistenceProvider.getVideoStore().findById(id3));
	videoBean.setId(id4);
	videoAction.deleteVideo();
	assertNull(persistenceProvider.getVideoStore().findById(id4));
	persistenceProvider.getUserStore().delete(admin);
	persistenceProvider.close();
    }
}

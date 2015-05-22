package hu.tests.facelets;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextNotPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleNotEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.clickLinkWithExactText;
import static net.sourceforge.jwebunit.junit.JWebUnit.getPageSource;
import static net.sourceforge.jwebunit.junit.JWebUnit.gotoPage;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the url manipulations defined in the validation report. To
 * run this test the sql script of the usability test must first be executed.
 */
public class UrlManipulationTest {

    // Outcome
    private static final int RESTRICTION_ERROR = 1;
    private static final int CORRECT_PAGE = 2;
    private static final int CORRECT_SEARCH = 3;
    private static final int NO_SCRIPT_INJECTION = 4;
    private static final int NO_COMMON_ERROR = 5;

    // URL
    private static final String prefix = "http://localhost:8080/MTT/xhtml/";

    // IDs
    private static final String HEADERLOGIN_FORM_ID = "headerLoginForm";
    private static final String HEADERLOGOUT_FORM_ID = "headerLogoutForm";

    // Page titles
    private static final String ERROR_TITLE = "Error";

    // Messages
    private static final String RESTRICTION_ERROR_MSG = "You do not have permission to use this page.";
    private static final String COMMON_ERROR_MSG = "Your request could not be completed. One or more errors occurred.";

    // Credentials
    private static String tutorEmail;
    private static final String tutorPassword = "Test!1234";
    private static String participantEmail;
    private static final String participantPassword = "Test!1234";

    // Test params
    private static Map<String, Integer> testUrlsForAdmin;
    private static Map<String, Integer> testUrlsForTutor;
    private static Map<String, Integer> testUrlsForParticipant;

    // admin params
    private final static String title = "Dr.";
    private final static String adminPassword = "Test!1234";
    private static String email;
    private final static EUserType type = EUserType.Administrator;
    private final static String firstName = "Manuel";
    private final static String lastName = "Muster";
    private final static Date birthday = new Date();
    private final static EGender gender = EGender.Male;
    private final static boolean banned = false;
    private final static ECountry country = ECountry.Germany;
    private static User admin;
    private final static int adminId = 402;

    // params Tutor
    private static User tutor;
    private final static String tutorFirstName = "Hans";
    private final static String tutorLastName = "Wurst";
    private final static String tutorTitle = "";
    private final static EUserType tutorType = EUserType.Tutor;
    private final static EGender tutorGender = EGender.Male;
    private final static ECountry tutorCountry = ECountry.Germany;
    private final static int tutorId = 401;

    // params Participant
    private static User participant;
    private final static String participantFirstName = "Emma";
    private final static String participantLastName = "Kaese";
    private final static String participantTitle = "";
    private final static EUserType participantType = EUserType.Participant;
    private final static EGender participantGender = EGender.Female;
    private final static Date participantBirthday = new Date(665912387);
    private final static ECountry participantCountry = ECountry.Switzerland;
    private final static int participantId = 400;

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;

    // Test parameters
    private static String injectedScript = "<script>window.onload = alert(1);</script>";

    @BeforeClass
    public static void prepare() throws InconsistencyException, InterruptedException {
	setBaseUrl(Globals.getBaseUrl());

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);

	testUrlsForAdmin = UrlManipulationTest.splitParams(new File("").getAbsolutePath()
		+ "/tests/hu/tests/facelets/configuration/TestUrlsAdmin.txt");
	testUrlsForTutor = UrlManipulationTest.splitParams(new File("").getAbsolutePath()
		+ "/tests/hu/tests/facelets/configuration/TestUrlsTutor.txt");
	testUrlsForParticipant = UrlManipulationTest.splitParams(new File("").getAbsolutePath()
		+ "/tests/hu/tests/facelets/configuration/TestUrlsParticipant.txt");

	email = "admin" + System.currentTimeMillis() + "@mailinator.com";
	admin = new User(adminId);
	admin.setEmail(email);
	admin.setPassword(adminPassword);
	admin.setFirstName(firstName);
	admin.setLastName(lastName);
	admin.setBirthday(birthday);
	admin.setGender(gender);
	admin.setUserType(type);
	admin.setBanned(banned);
	admin.setTitle(title);
	admin.setCountry(country);
	admin = persistenceProvider.getUserStore().create(admin);

	Thread.sleep(5);
	tutorEmail = "tutor" + System.currentTimeMillis() + "@mailinator.com";
	tutor = new User(tutorId);
	tutor.setFirstName(tutorFirstName);
	tutor.setLastName(tutorLastName);
	tutor.setTitle(tutorTitle);
	tutor.setPassword(tutorPassword);
	tutor.setEmail(tutorEmail);
	tutor.setUserType(tutorType);
	tutor.setGender(tutorGender);
	tutor.setCountry(tutorCountry);
	tutor = persistenceProvider.getUserStore().create(tutor);
	assertNotNull(persistenceProvider.getUserStore().findByEmail(tutorEmail));

	Thread.sleep(5);
	participantEmail = "participant" + System.currentTimeMillis() + "@mailinator.com";
	participant = new User(participantId);
	participant.setFirstName(participantFirstName);
	participant.setLastName(participantLastName);
	participant.setTitle(participantTitle);
	participant.setPassword(participantPassword);
	participant.setEmail(participantEmail);
	participant.setUserType(participantType);
	participant.setGender(participantGender);
	participant.setBirthday(participantBirthday);
	participant.setCountry(participantCountry);
	participant = persistenceProvider.getUserStore().create(participant);

	if (persistenceProvider.getUserStore().findById(313) != null) {
	    persistenceProvider.getUserStore().delete(
		    persistenceProvider.getUserStore().findById(313));
	}
	if (persistenceProvider.getUserStore().findById(314) != null) {
	    persistenceProvider.getUserStore().delete(
		    persistenceProvider.getUserStore().findById(314));
	}
	if (persistenceProvider.getGroupStore().findById(182) != null) {
	    persistenceProvider.getGroupStore().delete(
		    persistenceProvider.getGroupStore().findById(182));
	}
    }

    @Test
    public void testUrlManipulation() {

	this.login(admin.getEmail(), adminPassword);

	for (Map.Entry<String, Integer> url : testUrlsForAdmin.entrySet()) {
	    this.testUrlWithParams(url.getKey(), url.getValue());
	}
	this.logoutCurrentUser();

	gotoPage("http://localhost:8080/MTT/xhtml/common/start.jsf");
	this.login(tutor.getEmail(), tutorPassword);
	for (Map.Entry<String, Integer> url : testUrlsForTutor.entrySet()) {
	    this.testUrlWithParams(url.getKey(), url.getValue());
	}
	this.logoutCurrentUser();

	this.login(participant.getEmail(), participantPassword);
	for (Map.Entry<String, Integer> url : testUrlsForParticipant.entrySet()) {
	    this.testUrlWithParams(url.getKey(), url.getValue());
	}
	this.logoutCurrentUser();
    }

    private void testUrlWithParams(String url, int outcome) {

	gotoPage(prefix + url);

	switch (outcome) {

	case RESTRICTION_ERROR:
	    assertTitleEquals(ERROR_TITLE);
	    assertTextPresent(RESTRICTION_ERROR_MSG);
	    break;

	case CORRECT_PAGE:
	    assertTitleNotEquals(ERROR_TITLE);
	    break;

	case NO_COMMON_ERROR:
	    assertTextNotPresent(COMMON_ERROR_MSG);
	    break;

	case CORRECT_SEARCH:
	    String validSource = getPageSource();
	    gotoPage(url);
	    String injectedSource = getPageSource();
	    assertTrue(validSource.equals(injectedSource));
	    break;

	case NO_SCRIPT_INJECTION:
	    assertFalse(getPageSource().contains(injectedScript));
	    break;

	default:
	}
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

    private static Map<String, Integer> splitParams(String fileLocation) {

	Map<String, Integer> params = new LinkedHashMap<String, Integer>();

	try {
	    BufferedReader in = new BufferedReader(new FileReader(fileLocation));
	    String line = null;

	    while ((line = in.readLine()) != null) {
		if (!line.startsWith("//")) {
		    String[] split = line.split(":");
		    params.put(split[0], Integer.parseInt(split[1]));
		}
	    }

	    in.close();
	} catch (IOException ignored) {
	}

	return params;
    }

    @AfterClass
    public static void close() throws InconsistencyException {
	persistenceProvider.getUserStore().delete(admin);
	persistenceProvider.getUserStore().delete(tutor);
	persistenceProvider.getUserStore().delete(participant);
	persistenceProvider.close();
    }
}
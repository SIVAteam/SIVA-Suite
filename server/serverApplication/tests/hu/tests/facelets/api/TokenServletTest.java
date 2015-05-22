package hu.tests.facelets.api;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertTextPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setIgnoreFailingStatusCodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sun.misc.BASE64Encoder;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class TokenServletTest {

    // User data
    private final static String banTitle = "Dr.";
    private static String banEmail;
    private final static String banPassword = "Test!123";
    private final static String banFirstName = "Max";
    private final static String banLastName = "Muster";
    private final static EGender banGender = EGender.Male;
    private final static EUserType banType = EUserType.Tutor;
    private final static boolean banBanned = true;
    private final static ECountry banCountry = ECountry.Germany;

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

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private User bannedUser;
    private User user;

    private static final String UC_AUTH_BASIC = "androidApp:DAzf5631O-fgUJ-3jhUI6d";

    @Before
    public void prepare() throws InconsistencyException {
	setBaseUrl(Globals.getBaseUrl());

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);
	banEmail= "ban"+ System.currentTimeMillis() + "@mailinator.com";
	bannedUser = new User(null);
	bannedUser.setTitle(banTitle);
	bannedUser.setEmail(banEmail);
	bannedUser.setPassword(banPassword);
	bannedUser.setFirstName(banFirstName);
	bannedUser.setLastName(banLastName);
	bannedUser.setGender(banGender);
	bannedUser.setUserType(banType);
	bannedUser.setBanned(banBanned);
	bannedUser.setCountry(banCountry);
	bannedUser = persistenceProvider.getUserStore().create(bannedUser);
	
	email= "admin"+ System.currentTimeMillis() + "@mailinator.com";
	user = new User(null);
	user.setEmail(email);
	user.setPassword(password);
	user.setFirstName(firstName);
	user.setLastName(lastName);
	user.setBirthday(birthday);
	user.setGender(gender);
	user.setUserType(type);
	user.setBanned(banned);
	user.setTitle(title);
	user.setCountry(country);
	user = persistenceProvider.getUserStore().create(user);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void tokenTest() throws FailingHttpStatusCodeException, IOException, JSONException {

	WebClient webclient = new WebClient();
	webclient.setThrowExceptionOnFailingStatusCode(false);
	WebRequest requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/token"),
		HttpMethod.POST);
	setIgnoreFailingStatusCodes(true);
	String codedAuthBasic = new BASE64Encoder().encode(UC_AUTH_BASIC.getBytes());
	JSONObject json;

	// test without authorization header and without HTTP POST
	beginAt("/token");
	assertTextPresent("HTTP POST");

	// test without authorization header and with HTTP POST
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("No authorization header set", json.getString("description"));

	// test using incorrect authorization header
	requestSettings.setAdditionalHeader("Authorization", "Bearer " + codedAuthBasic);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Malformed authorization header", json.getString("description"));

	// test using correct authorization header with malformed client
	// information
	requestSettings.setAdditionalHeader("Authorization", "Basic " + "test");
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Malformed client credentials", json.getString("description"));

	// test using correct authorization header with wrong client information
	requestSettings.setAdditionalHeader("Authorization",
		"Basic " + new BASE64Encoder().encode("test:test".getBytes()));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("No client found having these credentials", json.getString("description"));

	// test using correct authorization header with existing client
	// information
	requestSettings.setAdditionalHeader("Authorization", "Basic " + codedAuthBasic);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("One or more required parameters missing in POST body",
		json.getString("description"));

	// test with required POST values but using wrong values
	ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
	parameters.add(new NameValuePair("username", "test"));
	parameters.add(new NameValuePair("password", "test"));
	parameters.add(new NameValuePair("grant_type", "test"));
	requestSettings.setRequestParameters(parameters);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Use 'password' as grant type", json.getString("description"));

	parameters.clear();
	parameters.add(new NameValuePair("username", "test"));
	parameters.add(new NameValuePair("password", "test"));
	parameters.add(new NameValuePair("grant_type", "password"));
	requestSettings.setRequestParameters(parameters);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("No user found having these credentials", json.getString("description"));

	// test with required POST values but banned user
	parameters.clear();
	parameters.add(new NameValuePair("username", bannedUser.getEmail()));
	parameters.add(new NameValuePair("password", banPassword));
	parameters.add(new NameValuePair("grant_type", "password"));
	requestSettings.setRequestParameters(parameters);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("You have been banned", json.getString("description"));

	// test with required POST values and correct user
	parameters.clear();
	parameters.add(new NameValuePair("username", user.getEmail()));
	parameters.add(new NameValuePair("password", password));
	parameters.add(new NameValuePair("grant_type", "password"));
	requestSettings.setRequestParameters(parameters);	
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertFalse(json.getString("access_token") == null);

	webclient.closeAllWindows();
    }

    @After
    public void clean() throws InconsistencyException {
	persistenceProvider.getUserStore().delete(user.getId());
	persistenceProvider.getUserStore().delete(bannedUser.getId());
	persistenceProvider.close();
    }
}
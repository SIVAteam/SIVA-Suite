package hu.tests.facelets.api;

import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setIgnoreFailingStatusCodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Video;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.IVideoStore;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.tests.facelets.configuration.Globals;
import hu.util.Configuration;
import hu.util.ECountry;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
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

/**
 * Testclass to test the InstructionServlet
 * 
 */
public class InstructionServletTest {

    private static final String UC_AUTH_BASIC = "androidApp:DAzf5631O-fgUJ-3jhUI6d";
    private User user;
    private User altUser;

    // User data
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

    private final static String altTitle = "Prof.";
    private final static String altPassword = "Test!1234";
    private static String altEmail;
    private final static EUserType altType = EUserType.Tutor;
    private final static String altFirstName = "Max";
    private final static String altLastName = "Maler";
    private final static Date altBirthday = new Date();
    private final static EGender altGender = EGender.Male;
    private final static boolean altBanned = false;
    private final static ECountry altCountry = ECountry.Germany;

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static IVideoStore videoStore;
    private static IGroupStore groupStore;

    private static Group group1;
    private final static String group1Title = "test";
    private final static boolean group1Visibility = true;

    private static Video v1;
    private final static String v1Title = "Bruno Nodes";
    private final static String v1Description = "First Test Video";
    private final static EParticipationRestriction v1PartRest = EParticipationRestriction.Registered;

    private static Video v2;
    private final static String v2Title = "Hallo";
    private final static String v2Description = "Second Test Video";
    private final static EParticipationRestriction v2PartRest = EParticipationRestriction.Registered;

    private static Video v3;
    private final static String v3Title = "Bye";
    private final static String v3Description = "First Test Video";
    private final static EParticipationRestriction v3PartRest = EParticipationRestriction.Registered;

    @Before
    public void prepare() throws InconsistencyException, IOException {
	setBaseUrl(Globals.getBaseUrl());

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);

	videoStore = persistenceProvider.getVideoStore();
	groupStore = persistenceProvider.getGroupStore();
	email = "admin" + System.currentTimeMillis() + "@mailinator.com";
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

	altEmail = "alt" + System.currentTimeMillis() + "@mailinator.com";
	altUser = new User(null);
	altUser.setEmail(altEmail);
	altUser.setPassword(altPassword);
	altUser.setFirstName(altFirstName);
	altUser.setLastName(altLastName);
	altUser.setBirthday(altBirthday);
	altUser.setGender(altGender);
	altUser.setUserType(altType);
	altUser.setBanned(altBanned);
	altUser.setTitle(altTitle);
	altUser.setCountry(altCountry);
	altUser = persistenceProvider.getUserStore().create(altUser);

	group1 = new Group(null);
	group1.setTitle(group1Title);
	group1.setVisible(group1Visibility);
	group1 = groupStore.create(group1, user);

	v1 = new Video(null);
	v1.setTitle(v1Title);
	v1.setAuthorId(user.getId());
	v1.setDescription(v1Description);
	v1.setParticipationRestriction(v1PartRest);
	v1.setDirectory("TESTVIDEODIR");
	v1 = videoStore.create(v1, group1);
	v1.setSize((long) 2);
	v1 = videoStore.save(v1);

	v2 = new Video(null);
	v2.setTitle(v2Title);
	v2.setAuthorId(user.getId());
	v2.setDescription(v2Description);
	v2.setParticipationRestriction(v2PartRest);
	v2.setDirectory("TESTVIDEODIR");
	v2 = videoStore.create(v2, group1);
	v2.setSize((long) 3);
	v2 = videoStore.save(v2);

	v3 = new Video(null);
	v3.setTitle(v3Title);
	v3.setAuthorId(altUser.getId());
	v3.setDescription(v3Description);
	v3.setParticipationRestriction(v3PartRest);
	v3.setDirectory("TESTVIDEODIR2");
	v3 = videoStore.create(v3, group1);
	v3.setSize((long) 2);
	v3 = videoStore.save(v3);
    }

    /**
     * @throws IOException
     * @throws JSONException
     * @throws FailingHttpStatusCodeException
     * 
     */
    @SuppressWarnings("deprecation")
    @Test
    public void instuctionTest() throws FailingHttpStatusCodeException, JSONException, IOException {

	WebClient webclient = new WebClient();
	webclient.setThrowExceptionOnFailingStatusCode(false);
	WebRequest requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/instruction"),
		HttpMethod.POST);
	setIgnoreFailingStatusCodes(true);
	String codedAuthBasic = new BASE64Encoder().encode(UC_AUTH_BASIC.getBytes());
	JSONObject json;

	// test with HTTP POST
	assertTrue(webclient.getPage(requestSettings).getWebResponse().getContentAsString()
		.contains("HTTP method POST is not supported by this URL"));

	// test without authorization header and with HTTP GET
	requestSettings.setHttpMethod(HttpMethod.GET);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("No authorization header set", json.getString("description"));

	// test using incorrect authorization header
	requestSettings.setAdditionalHeader("Authorization", "Basic " + codedAuthBasic);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Malformed authorization header", json.getString("description"));

	// test using correct authorization header with malformed client
	// information
	requestSettings.setAdditionalHeader("Authorization", "Bearer " + "test");
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("401", json.getString("code"));
	assertEquals("Token expired or unknown", json.getString("description"));

	// getting authorization information from the token site
	requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/token"), HttpMethod.POST);
	requestSettings.setAdditionalHeader("Authorization", "Basic " + codedAuthBasic);
	ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
	parameters.add(new NameValuePair("username", user.getEmail()));
	parameters.add(new NameValuePair("password", password));
	parameters.add(new NameValuePair("grant_type", "password"));
	requestSettings.setRequestParameters(parameters);
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());

	String accessToken = json.getString("access_token");
	assertFalse(accessToken == null);

	// test with correct Header
	requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/instruction"),
		HttpMethod.GET);
	requestSettings.setAdditionalHeader("Authorization",
		"Bearer " + new BASE64Encoder().encode(accessToken.getBytes()));
	JSONArray jsonArray = new JSONArray(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());

	int numberOfTestVideos = 0;
	int index1 = 0;
	int index2 = 0;
	int index3 = 0;
	for (int i = 0; i < jsonArray.length(); i++) {

	    if (jsonArray.getJSONObject(i).get("id").equals("" + v1.getId())) {
		numberOfTestVideos++;
		index1 = i;
	    } else if (jsonArray.getJSONObject(i).get("id").equals("" + v2.getId())) {
		numberOfTestVideos++;
		index2 = i;
	    } else if (jsonArray.getJSONObject(i).get("id").equals("" + v3.getId())) {
		numberOfTestVideos++;
		index3 = i;
	    }
	}

	assertEquals(3, numberOfTestVideos);

	assertEquals("" + v1.getId(), jsonArray.getJSONObject(index1).get("id"));
	assertEquals("" + v1.getViews(), jsonArray.getJSONObject(index1).get("views"));
	assertEquals("" + v1.getRatings(), jsonArray.getJSONObject(index1).get("rating"));
	assertEquals("" + v1.getDownloads(), jsonArray.getJSONObject(index1).get("downloads"));
	assertEquals("" + v1.getSize(), jsonArray.getJSONObject(index1).get("size"));
	assertEquals("" + v1.getVersion(), jsonArray.getJSONObject(index1).get("version"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v1.getCreated()), jsonArray
		.getJSONObject(index1).get("created"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v1.getLastUpdated()),
		jsonArray.getJSONObject(index1).get("lastUpdated"));
	assertEquals("Bruno Nodes", jsonArray.getJSONObject(index1).get("name"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR/thumbnail.jpg", jsonArray
		.getJSONObject(index1).get("thumbnail"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR/video.zip", jsonArray
		.getJSONObject(index1).get("archive"));

	json = (JSONObject) jsonArray.getJSONObject(index1).get("author");
	assertEquals(email, json.getString("username"));
	assertEquals(firstName + " " + lastName, json.getString("name"));

	assertEquals("" + v2.getId(), jsonArray.getJSONObject(index2).get("id"));
	assertEquals("" + v2.getViews(), jsonArray.getJSONObject(index2).get("views"));
	assertEquals("" + v2.getRatings(), jsonArray.getJSONObject(index2).get("rating"));
	assertEquals("" + v2.getDownloads(), jsonArray.getJSONObject(index2).get("downloads"));
	assertEquals("" + v2.getSize(), jsonArray.getJSONObject(index2).get("size"));
	assertEquals("" + v2.getVersion(), jsonArray.getJSONObject(index2).get("version"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v2.getCreated()), jsonArray
		.getJSONObject(index2).get("created"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v2.getLastUpdated()),
		jsonArray.getJSONObject(index2).get("lastUpdated"));
	assertEquals("Hallo", jsonArray.getJSONObject(index2).get("name"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR/thumbnail.jpg", jsonArray
		.getJSONObject(index2).get("thumbnail"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR/video.zip", jsonArray
		.getJSONObject(index2).get("archive"));

	json = (JSONObject) jsonArray.getJSONObject(index2).get("author");
	assertEquals(email, json.getString("username"));
	assertEquals(firstName + " " + lastName, json.getString("name"));

	assertEquals("" + v3.getId(), jsonArray.getJSONObject(index3).get("id"));
	assertEquals("" + v3.getViews(), jsonArray.getJSONObject(index3).get("views"));
	assertEquals("" + v3.getRatings(), jsonArray.getJSONObject(index3).get("rating"));
	assertEquals("" + v3.getDownloads(), jsonArray.getJSONObject(index3).get("downloads"));
	assertEquals("" + v3.getSize(), jsonArray.getJSONObject(index3).get("size"));
	assertEquals("" + v3.getVersion(), jsonArray.getJSONObject(index3).get("version"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v3.getCreated()), jsonArray
		.getJSONObject(index3).get("created"));
	assertEquals("" + (new SimpleDateFormat("yyyy-MM-dd")).format(v3.getLastUpdated()),
		jsonArray.getJSONObject(index3).get("lastUpdated"));
	assertEquals("Bye", jsonArray.getJSONObject(index3).get("name"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR2/thumbnail.jpg", jsonArray
		.getJSONObject(index3).get("thumbnail"));
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR2/video.zip", jsonArray
		.getJSONObject(index3).get("archive"));

	json = (JSONObject) jsonArray.getJSONObject(index3).get("author");
	assertEquals(altEmail, json.getString("username"));
	assertEquals(altFirstName + " " + altLastName, json.getString("name"));

	// test with correct Header, but not enough parameters
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction" + "/0"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Parameter 'minimum rating', 'minimum views' or 'maximum size' not set",
		json.getString("description"));

	// test with correct Header, but too many parameters
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction" + "/0/2/4/3/4/5"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals("Too many parameters set", json.getString("description"));

	// test with correct Header, and first three parameters for v1 and v3
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction/" + v1.getRatings()
		+ "/" + v1.getViews() + "/" + v1.getSize()));
	jsonArray = new JSONArray(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());

	numberOfTestVideos = 0;
	index1 = 0;
	index3 = 0;
	for (int i = 0; i < jsonArray.length(); i++) {
	    if (jsonArray.getJSONObject(i).get("id").equals("" + v1.getId())) {
		numberOfTestVideos++;
		index1 = i;
	    } else if (jsonArray.getJSONObject(i).get("id").equals("" + v3.getId())) {
		numberOfTestVideos++;
		index3 = i;
	    }
	}
	assertEquals(2, numberOfTestVideos);
	assertEquals("Bruno Nodes", jsonArray.getJSONObject(index1).get("name"));
	assertEquals("Bye", jsonArray.getJSONObject(index3).get("name"));

	// test with correct Header, and first three parameters plus email for
	// v1
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction/" + v1.getRatings()
		+ "/" + v1.getViews() + "/" + v1.getSize() + "/" + user.getEmail()));
	jsonArray = new JSONArray(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());

	numberOfTestVideos = 0;
	index1 = 0;
	for (int i = 0; i < jsonArray.length(); i++) {
	    if (jsonArray.getJSONObject(i).get("id").equals("" + v1.getId())) {
		numberOfTestVideos++;
		index1 = i;
	    }
	}

	assertEquals(1, numberOfTestVideos);
	assertEquals("Bruno Nodes", jsonArray.getJSONObject(index1).get("name"));

	// test with correct Header, and parameter for rating is not integer
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction" + "/a/0/2/"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals(
		"Parameters 'minimum rating', 'minimum views' and 'maximum size' have to be integers",
		json.getString("description"));

	// test with correct Header, and parameter for views is not integer
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction" + "/0/a0/2/"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals(
		"Parameters 'minimum rating', 'minimum views' and 'maximum size' have to be integers",
		json.getString("description"));

	// test with correct Header, and parameter for size is not integer
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction" + "/a/0/a2/"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("400", json.getString("code"));
	assertEquals(
		"Parameters 'minimum rating', 'minimum views' and 'maximum size' have to be integers",
		json.getString("description"));

	webclient.closeAllWindows();
    }

    @After
    public void clean() throws InconsistencyException {
	videoStore.delete(v1.getId());
	videoStore.delete(v2.getId());
	videoStore.delete(v3.getId());
	persistenceProvider.getUserStore().delete(user.getId());
	persistenceProvider.getUserStore().delete(altUser.getId());
	persistenceProvider.close();
    }
}
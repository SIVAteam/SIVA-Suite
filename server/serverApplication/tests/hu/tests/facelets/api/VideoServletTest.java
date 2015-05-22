package hu.tests.facelets.api;

import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setIgnoreFailingStatusCodes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.common.TokenBean;
import hu.backingbeans.videos.VideoBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.controller.videos.VideoAction;
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
import hu.util.SessionData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

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

public class VideoServletTest {

    private static final String VIDEO_DESTINATION = System.getProperty("user.home")
	    + "/.sivaServer/videos/";
    private static final String TEST_VIDEO_DIRECTORY = "./tests/resources/brunoNodes.zip";

    private static IVideoStore videoStore;
    private static IGroupStore groupStore;
    private static VideoBean videoBean;
    private static VideoPublicationBean videoPublicationBean;
    private static TokenBean tokenBean;
    private static VideoAction videoAction;
    private static FacesContext cxtMock;
    private static UIViewRoot uiViewRoot;
    private static Locale locale = new Locale("de");

    private static SessionData session;
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

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private User user;

    private static Video v1;
    private final static String v1Title = "Bruno Nodes";
    private final static String v1Description = "First Test Video";
    private final static EParticipationRestriction v1PartRest = EParticipationRestriction.Registered;
    private static Group group1;
    private final static String group1Title = "test";
    private final static boolean group1Visibility = true;

    private static Video v2;
    private final static String v2Title = "Hallo";
    private final static String v2Description = "Second Test Video";
    private final static EParticipationRestriction v2PartRest = EParticipationRestriction.Registered;

    private static final String UC_AUTH_BASIC = "androidApp:DAzf5631O-fgUJ-3jhUI6d";

    private static File testVideoFile;

    @Before
    public void prepare() throws InconsistencyException, IOException {
	setBaseUrl(Globals.getBaseUrl());

	configuration = new Configuration();
	persistenceProvider = new PgPersistenceProvider(configuration);

	videoStore = persistenceProvider.getVideoStore();
	groupStore = persistenceProvider.getGroupStore();
	
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

	group1 = new Group(null);
	group1.setTitle(group1Title);
	group1.setVisible(group1Visibility);
	group1 = groupStore.create(group1, user);

	videoBean = new VideoBean();
	videoPublicationBean = new VideoPublicationBean();

	tokenBean = new TokenBean();
	videoAction = new VideoAction();
	session = new SessionData();

	videoAction.setPersistenceProvider(persistenceProvider);
	videoAction.setVideoBean(videoBean);
	videoAction.setVideoPublicationBean(videoPublicationBean);

	videoAction.setTokenBean(tokenBean);
	videoAction.setSessionData(session);

	cxtMock = mock(FacesContext.class);
	uiViewRoot = mock(UIViewRoot.class);
	videoAction.setMock(cxtMock);

	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

	// Prepare beans
	videoBean.setTitle(v1Title);
	videoBean.setDescription(v1Description);
	videoBean.setParticipationRestriction(v1PartRest);

	testVideoFile = new File(TEST_VIDEO_DIRECTORY);

	v1 = new Video(null);
	v1.setTitle(v1Title);
	v1.setAuthorId(user.getId());
	v1.setDescription(v1Description);
	v1.setParticipationRestriction(v1PartRest);
	v1.setDirectory("TESTVIDEODIR");
	v1 = videoStore.create(v1, group1);

	when(cxtMock.isPostback()).thenReturn(true);
	session.setUserId(user.getId());
	videoBean = new VideoBean();
	videoBean.setId(v1.getId());
	videoBean.setTitle(v1Title);
	videoBean.setGroupId(group1.getId());
	videoAction.setSessionData(session);
	videoAction.setVideoBean(videoBean);
	videoAction.setVideoUploadMock(testVideoFile);
	videoAction.clearLogs();
	videoAction.editVideo();

	v1 = videoStore.findById(v1.getId());

	v2 = new Video(null);
	v2.setTitle(v2Title);
	v2.setAuthorId(user.getId());
	v2.setDescription(v2Description);
	v2.setParticipationRestriction(v2PartRest);
	v2.setDirectory("TESTVIDEODIR2");
	v2 = videoStore.create(v2, group1);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void videoTest() throws FailingHttpStatusCodeException, IOException, JSONException {

	WebClient webclient = new WebClient();
	webclient.setThrowExceptionOnFailingStatusCode(false);
	WebRequest requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/videos"),
		HttpMethod.POST);
	setIgnoreFailingStatusCodes(true);
	String codedAuthBasic = new BASE64Encoder().encode(UC_AUTH_BASIC.getBytes());
	JSONObject json;
	// only if problems occur

	//webclient.setJavaScriptEnabled(false);

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

	// test with correct Header, but no file specified
	requestSettings = new WebRequest(new URL(Globals.getBaseUrl() + "/videos"), HttpMethod.GET);
	requestSettings.setAdditionalHeader("Authorization",
		"Bearer " + new BASE64Encoder().encode(accessToken.getBytes()));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("404", json.getString("code"));
	assertEquals("No file specified", json.getString("description"));

	// test with correct Header, but no file available
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/videos" + "/test"));
	json = new JSONObject(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());
	assertEquals("404", json.getString("code"));
	assertEquals("File not found", json.getString("description"));

	// get Video link from the instruction servlet
	requestSettings.setUrl(new URL(Globals.getBaseUrl() + "/instruction/" + v1.getRatings()
		+ "/" + v1.getViews() + "/" + v1.getSize()));
	JSONArray jsonArray = new JSONArray(webclient.getPage(requestSettings).getWebResponse()
		.getContentAsString());

	int numberOfTestVideos = 0;
	int index = 0;

	for (int i = 0; i < jsonArray.length(); i++) {
	    if (jsonArray.getJSONObject(i).get("id").equals("" + v1.getId())) {
		numberOfTestVideos++;
		index = i;
	    }
	}
	assertEquals(1, numberOfTestVideos);
	assertEquals("Bruno Nodes", jsonArray.getJSONObject(index).get("name"));
	String archive = jsonArray.getJSONObject(index).get("archive").toString();
	assertEquals("https://localhost:8080/MTT/videos/TESTVIDEODIR/video.zip", archive);

	archive = archive.replace("https://", "http://");
	requestSettings.setUrl(new URL(archive));
	File file = new File(VIDEO_DESTINATION + "TESTVIDEODIR/video.zip");
	String contentType = URLConnection.guessContentTypeFromName(file.getName());
	
	assertEquals(contentType, webclient.getPage(requestSettings).getWebResponse()
		.getContentType());

	assertEquals(file.length(), Long.parseLong(webclient.getPage(requestSettings).getWebResponse().getResponseHeaderValue("Content-Length")));
	
	webclient.closeAllWindows();
    }

    @After
    public void clean() throws InconsistencyException {
	session.setUserId(user.getId());
	videoBean.setId(v1.getId());
	videoAction.deleteVideo();
	assertNull(videoStore.findById(v1.getId()));
	videoStore.delete(v2.getId());
	persistenceProvider.getUserStore().delete(user.getId());
	persistenceProvider.close();
    }
}
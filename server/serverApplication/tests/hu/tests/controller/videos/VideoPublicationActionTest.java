package hu.tests.controller.videos;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.videos.UsersInvitedToVideoBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.controller.videos.VideoPublicationAction;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Token;
import hu.model.Video;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.MailService;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functionality of the {@link VideoPublicationAction}
 * controller.
 */
public class VideoPublicationActionTest {

    private static Configuration configuration;
    private static PgPersistenceProvider pers;
    private static VideoPublicationAction vPubAction;

    // globals for invite/uninvite
    private static List<User> users = new LinkedList<User>();
    private static Integer naireId;
    private static Integer inTutorId;
    private static Integer inTutorNoOwnerId;
    private static Integer inPartic1Id;
    private static Integer inPartic2Id;
    private static Integer inAdminId;
    private static Integer inGroupId;
    private static Integer tokenNaireId;

    // globals for generateToken
    private static Integer gtTutorId;
    private static Integer gtVdId;
    private static Integer gtGroupId;

    /**
     * 
     * @throws InconsistencyException
     */
    @BeforeClass
    public static void prepare() throws Exception {
	configuration = new Configuration();
	pers = new PgPersistenceProvider(configuration);

	vPubAction = new VideoPublicationAction();
	vPubAction.setPersistenceProvider(pers);

	prepareForInvite();

	prepareForGenerateToken();
    }

    /**
     * 
     * @throws InconsistencyException
     */
    @AfterClass
    public static void cleanUp() throws Exception {
	cleanupForInvite();

	cleanupForGenerateToken();
	pers.close();
    }

    /**
     * Clean up the db.
     * 
     * @throws InconsistencyException
     */
    private static void cleanupForInvite() throws InconsistencyException {
	for (int i = 0; i < 5; i++) {
	    pers.getUserStore().delete(
		    pers.getUserStore().findByEmail(String.format("invite%02d@test.de", i)));
	}
	pers.getGroupStore().delete(inGroupId);

    }

    /**
     * Set the db for the following tests.
     * 
     * @throws InconsistencyException
     */
    private static void prepareForInvite() throws InconsistencyException {

	for (int i = 0; i < 5; i++) {
	    User tutor = new User(null);
	    tutor.setBanned(false);

	    tutor.setBirthday(new Date());
	    tutor.setEmail(String.format("invite%02d@test.de", i));
	    tutor.setFirstName("test");
	    tutor.setGender(EGender.Male);
	    tutor.setLastName("test");
	    tutor.setPasswordHash(SecurityUtils.hash("Test!123"));
	    tutor.setUserType(EUserType.Participant);
	    tutor.setCountry(ECountry.Germany);
	    tutor.setTitle("Dr.");
	    tutor.setStreet("Mustergasse");
	    tutor.setZip("45056");
	    tutor.setCity("Musterberg");
	    tutor.setPhone("09001456");
	    tutor.setFax("08001456");
	    tutor.setWebsite("www.musterweb.de");
	    User user = pers.getUserStore().create(tutor);
	    users.add(user);
	}

	User user = new User(null);
	// save as tutor and get id
	user = pers.getUserStore().findByEmail("invite00@test.de");
	user.setUserType(EUserType.Tutor);
	inTutorId = pers.getUserStore().save(user).getId();

	inPartic1Id = pers.getUserStore().findByEmail("invite01@test.de").getId();
	inPartic2Id = pers.getUserStore().findByEmail("invite02@test.de").getId();

	user = pers.getUserStore().findByEmail("invite04@test.de");
	user.setUserType(EUserType.Administrator);
	inAdminId = pers.getUserStore().save(user).getId();

	user = pers.getUserStore().findByEmail("invite03@test.de");
	user.setUserType(EUserType.Tutor);
	inTutorNoOwnerId = pers.getUserStore().save(user).getId();

	Group group = new Group(null);
	group.setTitle("creatVidTestGroup");
	group.setVisible(true);
	group = pers.getGroupStore().create(group, inTutorId);
	inGroupId = group.getId();

	Video vid = new Video(null);

	vid.setDescription("test description");
	vid.setTitle("test video");
	vid.setDirectory("/xhtml/videos/listVideos");
	vid.setAuthorId(inAdminId);
	vid.setParticipationRestriction(EParticipationRestriction.Public);

	naireId = pers.getVideoStore().create(vid, group).getId();

	vid.setParticipationRestriction(EParticipationRestriction.Token);

	tokenNaireId = pers.getVideoStore().create(vid, group).getId();

	VideoPublicationBean videoPublicationBean = new VideoPublicationBean();
	videoPublicationBean.setId(naireId);

	vPubAction.setConfiguration(configuration);
	vPubAction.setVideoPublicationBean(videoPublicationBean);

	FacesContext cxtMock = mock(FacesContext.class);
	UIViewRoot uiViewRoot = mock(UIViewRoot.class);
	Locale locale = new Locale("de");
	vPubAction.setMock(cxtMock);
	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

	// mock mailservice
	MailService mailService = mock(MailService.class);
	vPubAction.setMailService(mailService);
	when(mailService.sendMail("", "", "")).thenReturn(true);

    }

    /**
     * Test the listing of users for invitation to a video.
     * 
     */
    @Test
    public void testManageInvitations() {

	FacesContext cxtMock = mock(FacesContext.class);
	UIViewRoot uiViewRoot = mock(UIViewRoot.class);
	Locale locale = new Locale("de");
	vPubAction.setMock(cxtMock);
	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

	when(cxtMock.isPostback()).thenReturn(false);
	when(cxtMock.isValidationFailed()).thenReturn(false);

	UserBean userBean = new UserBean();
	UserListBean listBean = new UserListBean();
	SessionData session = new SessionData();
	UsersInvitedToVideoBean invitedUsersBean = new UsersInvitedToVideoBean();

	vPubAction.setUserBean(userBean);
	vPubAction.setUserListBean(listBean);
	vPubAction.setSession(session);

	// visit as admin
	session.setUserId(inAdminId);
	vPubAction.setInvitedUsersBean(invitedUsersBean);
	vPubAction.manageInvitations();

	// visit add user page as participant
	session.setUserId(inPartic1Id);
	assertNull(vPubAction.manageInvitations());

    }

    /**
     * 
     * Test invitation to a video.
     * 
     * @throws InconsistencyException
     */
    @Test
    public void testInviteUser() throws InconsistencyException {

	VideoPublicationBean vBean = new VideoPublicationBean();
	vBean.setId(naireId);
	vPubAction.setVideoPublicationBean(vBean);
	UsersInvitedToVideoBean invitedUsers = new UsersInvitedToVideoBean();
	vPubAction.setInvitedUsersBean(invitedUsers);

	SessionData session = new SessionData();
	vPubAction.setSession(session);

	UserBean userBean = new UserBean();
	vPubAction.setUserBean(userBean);
	userBean.setId(inPartic1Id);

	// invite as participant
	session.setUserId(inPartic1Id);
	vPubAction.inviteUser();
	assertNull(invitedUsers.getInvitedUsers());

	// invite as tutor without permission
	session.setUserId(inTutorNoOwnerId);
	assertNull(invitedUsers.getInvitedUsers());

	// invite as admin
	session.setUserId(inAdminId);

	vPubAction.inviteUser();
	assertTrue(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic1Id));

	// invite as tutor
	session.setUserId(inTutorId);
	userBean.setId(inPartic2Id);

	vPubAction.inviteUser();
	assertTrue(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic2Id));

	// invite a already invited
	vPubAction.inviteUser();
	assertEquals("Benutzer wurde bereits hinzugefügt.", vPubAction.getLastFacesMessage());

	Set<Integer> set = new HashSet<Integer>();
	set.add(inPartic1Id);
	set.add(inPartic2Id);
	assertEquals(invitedUsers.getInvitedUsers().get(naireId), set);
	invitedUsers.setInvitedUsers(null);
    }

    /**
     * 
     * Test univitation to a video.
     * 
     */
    @Test
    public void testUninviteUser() {

	UsersInvitedToVideoBean invitedUsers = new UsersInvitedToVideoBean();
	vPubAction.setInvitedUsersBean(invitedUsers);

	VideoPublicationBean videoPublicationBean = new VideoPublicationBean();
	videoPublicationBean.setId(naireId);
	vPubAction.setVideoPublicationBean(videoPublicationBean);

	SessionData session = new SessionData();
	vPubAction.setSession(session);

	UserBean userBean = new UserBean();
	// user to uninvite
	userBean.setId(inPartic1Id);
	vPubAction.setUserBean(userBean);

	Set<Integer> set = new HashSet<Integer>();
	set.add(inPartic1Id);
	set.add(inPartic2Id);
	Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
	map.put(naireId, set);
	invitedUsers.setInvitedUsers(map);

	// uninvite as participant
	session.setUserId(inPartic2Id);
	vPubAction.uninviteUser();
	assertTrue(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic1Id));

	// uninvite as tutor without permission
	session.setUserId(inTutorNoOwnerId);
	vPubAction.uninviteUser();
	assertTrue(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic1Id));

	// uninvite as tutor
	session.setUserId(inTutorId);
	vPubAction.uninviteUser();
	assertFalse(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic1Id));

	// uninvite as admin
	userBean.setId(inPartic2Id);
	session.setUserId(inAdminId);
	vPubAction.uninviteUser();
	assertFalse(invitedUsers.getInvitedUsers().get(naireId).contains(inPartic2Id));

	// both uninvited
	assertTrue(invitedUsers.getInvitedUsers().get(naireId).isEmpty());

	// uninvite a already uninvited
	vPubAction.uninviteUser();
	assertEquals("Benutzer ist nicht eingeladen.", vPubAction.getLastFacesMessage());
    }

    /**
     * 
     * @throws InconsistencyException
     * 
     */
    private static void prepareForGenerateToken() throws InconsistencyException {
	// Create tutor for testing.
	User tutor = new User(null);
	tutor.setBanned(false);
	tutor.setBirthday(new Date());
	tutor.setDeletable(true);
	tutor.setEmail("asd@mailinator.com");
	tutor.setFirstName("tokenTest");
	tutor.setGender(EGender.Male);
	tutor.setLastName("Tutor");
	tutor.setPassword("pw");
	tutor.setUserType(EUserType.Tutor);
	tutor.setStreet("Mustergasse");
	tutor.setZip("45056");
	tutor.setCity("Musterberg");
	tutor.setPhone("09001456");
	tutor.setFax("08001456");
	tutor.setWebsite("www.musterweb.de");
	tutor.setCountry(ECountry.Germany);
	tutor = pers.getUserStore().create(tutor);

	gtTutorId = tutor.getId();

	// create group
	Group group = new Group(null);
	group.setTitle("creatQuestTestGroup");
	group.setVisible(true);
	group = pers.getGroupStore().create(group, gtTutorId);
	gtGroupId = group.getId();

	// create video
	Video vid = new Video(null);
	vid.setDescription("test description");
	vid.setTitle("test video");
	vid.setAuthorId(gtTutorId);
	vid.setDirectory("/xhtml/videos/listVideos");
	vid.setParticipationRestriction(EParticipationRestriction.Token);

	gtVdId = pers.getVideoStore().create(vid, group).getId();
    }

    /**
     * Clean up database after test
     * 
     * @throws InconsistencyException
     */
    private static void cleanupForGenerateToken() throws InconsistencyException {
	pers.getUserStore().delete(gtTutorId);
	pers.getGroupStore().delete(gtGroupId);
    }

    /**
     * Create test data
     * 
     * @throws InconsistencyException
     */
    @Test
    public void testGenerateParticipationToken() throws InconsistencyException {

	FacesContext cxtMock = mock(FacesContext.class);
	UIViewRoot uiViewRoot = mock(UIViewRoot.class);
	Locale locale = new Locale("de");
	vPubAction.setMock(cxtMock);
	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

	// set tutor id
	SessionData session = new SessionData();
	session.setUserId(gtTutorId);
	vPubAction.setSession(session);

	// set videoPublicationBean
	int numTokens = 10;
	VideoPublicationBean videoPublicationBean = new VideoPublicationBean();
	vPubAction.setVideoPublicationBean(videoPublicationBean);
	videoPublicationBean.setId(gtVdId);
	videoPublicationBean.setNumTokens(numTokens);
	when(cxtMock.isPostback()).thenReturn(true);
	when(cxtMock.isValidationFailed()).thenReturn(false);

	vPubAction.generateParticipationToken();

	List<Token> tokens = pers.getTokenStore().getForVideo(gtVdId);

	// test if all token are stored in database
	assertTrue(tokens.size() == numTokens);

	// test if token are unique
	for (Token tmp : tokens) {
	    int hit = 0;
	    for (Token found : tokens) {
		if (tmp.getToken().equals(found.getToken())) {
		    hit = hit + 1;
		}
	    }
	    assertTrue(hit == 1);
	}

	// delete token
	for (Token token : tokens) {
	    pers.getTokenStore().delete(token);
	}
    }

    /**
     * Test if the correct number of token is generated.
     * 
     */
    @Test
    public void testSendToken() {

	mailMock();

	UsersInvitedToVideoBean invitedUsers = new UsersInvitedToVideoBean();
	vPubAction.setInvitedUsersBean(invitedUsers);
	SessionData session = new SessionData();

	VideoPublicationBean qpb = new VideoPublicationBean();
	qpb.setId(tokenNaireId);

	vPubAction.setVideoPublicationBean(qpb);

	// send token to invited
	session.setUserId(inTutorId);
	vPubAction.setSession(session);

	Set<Integer> set = new HashSet<Integer>();
	set.add(inPartic1Id);
	set.add(inPartic2Id);
	Map<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
	map.put(tokenNaireId, set);
	invitedUsers.setInvitedUsers(map);

	vPubAction.sendToken();
	assertEquals("Die Mails wurden erfolgreich versandt.", vPubAction.getLastFacesMessage());

	session.setUserId(inPartic1Id);

	assertEquals("/xhtml/errors/restrictionError", vPubAction.sendToken());

	// send token to list
	qpb.setEmailList("email1 email2 email3");
	vPubAction.sendToList();
	assertEquals("Die Mails wurden erfolgreich versandt.", vPubAction.getLastFacesMessage());

	// send token to all
	session.setUserId(inTutorId);
	vPubAction.sendToAll();
	assertEquals("Die Mails wurden erfolgreich versandt.", vPubAction.getLastFacesMessage());
    }

    /**
     * mock the context for sending mails.
     */
    private void mailMock() {
	FacesContext cxtMock = mock(FacesContext.class);
	UIViewRoot uiViewRoot = mock(UIViewRoot.class);
	ExternalContext exCxt = mock(ExternalContext.class);

	Locale locale = new Locale("de");
	vPubAction.setMock(cxtMock);
	when(uiViewRoot.getLocale()).thenReturn(locale);
	when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
	when(cxtMock.getExternalContext()).thenReturn(exCxt);
	when(exCxt.getRequestContextPath()).thenReturn("contextPath");
	when(exCxt.getRequestServerName()).thenReturn("server");
	when(exCxt.getRequestScheme()).thenReturn("schema");
	when(exCxt.getRequestServerPort()).thenReturn(1234);
	when(cxtMock.isPostback()).thenReturn(true);
    }

    /**
     * Test in other order.
     * 
     * @throws InconsistencyException
     */
    @Test
    public void helper() throws InconsistencyException {
	this.testSendToken();
	this.testManageInvitations();
	this.testGenerateParticipationToken();
	this.testInviteUser();
    }
}
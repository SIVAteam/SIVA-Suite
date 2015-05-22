package hu.tests.controller.videos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.common.TokenBean;
import hu.backingbeans.videos.VideoBean;
import hu.backingbeans.videos.VideoPublicationBean;
import hu.controller.videos.VideoAction;
import hu.controller.videos.VideoPublicationAction;
import hu.model.EParticipationRestriction;
import hu.model.Group;
import hu.model.Token;
import hu.model.Video;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IGroupStore;
import hu.persistence.ITokenStore;
import hu.persistence.IUserStore;
import hu.persistence.IVideoStore;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functionality of the {@link VideoAction} controller.
 */

public class VideoActionTest {

    private static final String LIST_VIDEO_FACELET = "/xhtml/videos/listVideos";
    private static final String ACCESS_RESTRICTION_FACELET = "/xhtml/errors/restrictionError";
    private static final String WATCH_VIDEO_FACELET = "/xhtml/videos/watchVideo";
    private static final String TEST_VIDEO_DIRECTORY = "./tests/resources/brunoNodes.zip";
    private static final String VIDEO_DESTINATION =  System.getProperty("user.home")
            + "/.sivaServer/videos/";

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static IUserStore userStore;
    private static IVideoStore videoStore;
    private static IGroupStore groupStore;
    private static ITokenStore tokenStore;
    private static VideoBean videoBean;
    private static VideoPublicationBean videoPublicationBean;
    private static TokenBean tokenBean;
    private static VideoAction videoAction;
    private static FacesContext cxtMock;
    private static UIViewRoot uiViewRoot;
    private static Locale locale = new Locale("de");

    private static SessionData session;

    // User > Administrator
    private static User admin;
    private final static String adminFirstName = "Max";
    private final static String adminLastName = "Mustermann";
    private final static String adminTitle = "";
    private final static String adminPassword = SecurityUtils.hash("123");
    private final static String adminEmail = "g@h.i";
    private final static EUserType adminType = EUserType.Administrator;
    private final static EGender adminGender = EGender.Male;
    private final static Date adminBirthday = new Date(650012387);
    private final static String adminStreet = "Musterstrasse";
    private final static String adminZip = "55555";
    private final static String adminCity = "Musterstadt";
    private final static ECountry adminCountry = ECountry.Germany;
    private final static String adminPhone = "0151555";
    private final static String adminFax = "0151554";
    private final static String adminWebsite = "www.musterweb.de";

    // User > Tutor
    private static User tutor;
    private final static String tutorFirstName = "Max";
    private final static String tutorLastName = "Mistermann";
    private final static String tutorTitle = "";
    private final static String tutorPassword = SecurityUtils.hash("123");
    private final static String tutorEmail = "a@b.c";
    private final static EUserType tutorType = EUserType.Tutor;
    private final static EGender tutorGender = EGender.Male;
    private final static Date tutorBirthday = new Date(659912387);
    private final static ECountry tutorCountry= ECountry.Germany;
    private final static String tutorStreet = "Musterstrasse";
    private final static String tutorZip = "55555";
    private final static String tutorCity = "Musterstadt";
    private final static String tutorPhone = "0151555";
    private final static String tutorFax = "0151554";
    private final static String tutorWebsite = "www.musterweb.de";

    // User > Participant
    private static User participant;
    private final static String participantFirstName = "Emma";
    private final static String participantLastName = "Musterfrau";
    private final static String participantTitle = "";
    private final static String participantPassword = SecurityUtils.hash("234");
    private final static String participantEmail = "b@c.d";
    private final static EUserType participantType = EUserType.Participant;
    private final static EGender participantGender = EGender.Female;
    private final static Date participantBirthday = new Date(665912387);
    private final static ECountry participantCountry= ECountry.Switzerland;
    private final static String participantStreet = "Mustergasse";
    private final static String participantZip = "5545";
    private final static String participantCity = "Musterdorf";
    private final static String participantPhone = "01511235";
    private final static String participantFax = "01511234";
    private final static String participantWebsite = "www.musterweb.ch";

    // Set of a video attributes
    private static Video v1;
    private final static String v1Title = "Does SEP smell to high heaven?";
    private final static String v1TitleNew = "I have never...";
    private final static String v1Description = "Seriously...";
    private final static String v1DescriptionNew = "...been in...";
    private final static EParticipationRestriction v1PartRest = EParticipationRestriction.GroupAttendants;
   
    // Set of group attributes
    private static Group group1;
    private final static String group1Title = "test";
    private final static boolean group1Visibility = true;
    private static Group group2;
    private final static String group2Title = "Fun with Flags 2";
    private final static boolean group2Visibility = false;

    private static Token token1;
    private final static String token1Identifier = "aBcDe12345";
    
    // globals for editPublicationSettings
    private static Integer epsAdminId;
    private static Integer epsGroupId;
    private static Integer epsVPubId;
    private static Integer epsQTokenId;
    private static Integer epsQPasswId;
    private static Integer epsQTokenPasswId;
    
    private static File testVideoFile;
    

    /**
     * Prepare testing: Check that the database is clean and insert test data.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void prepare() throws Exception {
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);

        userStore = persistenceProvider.getUserStore();
        videoStore = persistenceProvider.getVideoStore();
       
        groupStore = persistenceProvider.getGroupStore();
        
        tokenStore = persistenceProvider.getTokenStore();
       
        videoBean = new VideoBean();
        videoPublicationBean = new VideoPublicationBean();
        
        tokenBean = new TokenBean();
        videoAction = new VideoAction();
        session = new SessionData();

        videoAction.setPersistenceProvider(persistenceProvider);
        videoAction.setVideoBean(videoBean);
        videoAction
                .setVideoPublicationBean(videoPublicationBean);
        
        videoAction.setTokenBean(tokenBean);
        videoAction.setSessionData(session);

        cxtMock = mock(FacesContext.class);
        uiViewRoot = mock(UIViewRoot.class);
        videoAction.setMock(cxtMock);

        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // Users
        tutor = new User(null);
        tutor.setFirstName(tutorFirstName);
        tutor.setLastName(tutorLastName);
        tutor.setTitle(tutorTitle);
        tutor.setPasswordHash(tutorPassword);
        tutor.setEmail(tutorEmail);
        tutor.setUserType(tutorType);
        tutor.setGender(tutorGender);
        tutor.setBirthday(tutorBirthday);
        tutor.setCountry(tutorCountry);
        tutor.setStreet(tutorStreet);
        tutor.setZip(tutorZip);
        tutor.setCity(tutorCity);
        tutor.setPhone(tutorPhone);
        tutor.setFax(tutorFax);
        tutor.setWebsite(tutorWebsite);

        participant = new User(null);
        participant.setFirstName(participantFirstName);
        participant.setLastName(participantLastName);
        participant.setTitle(participantTitle);
        participant.setPasswordHash(participantPassword);
        participant.setEmail(participantEmail);
        participant.setUserType(participantType);
        participant.setGender(participantGender);
        participant.setBirthday(participantBirthday);
        participant.setCountry(participantCountry);
        participant.setStreet(participantStreet);
        participant.setZip(participantZip);
        participant.setCity(participantCity);
        participant.setPhone(participantPhone);
        participant.setFax(participantFax);
        participant.setWebsite(participantWebsite);

        admin = new User(null);
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setTitle(adminTitle);
        admin.setPasswordHash(adminPassword);
        admin.setEmail(adminEmail);
        admin.setUserType(adminType);
        admin.setGender(adminGender);
        admin.setBirthday(adminBirthday);
        admin.setCountry(adminCountry);
        admin.setStreet(adminStreet);
        admin.setZip(adminZip);
        admin.setCity(adminCity);
        admin.setPhone(adminPhone);
        admin.setFax(adminFax);
        admin.setWebsite(adminWebsite);

        // Groups
        group1 = new Group(null);
        group1.setTitle(group1Title);
        group1.setVisible(group1Visibility);
        group2 = new Group(null);
        group2.setTitle(group2Title);
        group2.setVisible(group2Visibility);

        // Videos
        v1 = new Video(null);
        v1.setTitle(v1Title);
        v1.setAuthorId(admin.getId());
        v1.setDescription(v1Description);
        v1.setParticipationRestriction(v1PartRest);
        v1.setDirectory("TESTVIDEODIR");
        
        // Prepare beans
        videoBean.setTitle(v1Title);
        videoBean.setDescription(v1Description);
        videoBean.setParticipationRestriction(v1PartRest);
              
        testVideoFile = new File(TEST_VIDEO_DIRECTORY).getCanonicalFile();   
    }
    
    /**
     * 
     * @throws InconsistencyException
     */
    @Test
    public void createVideoTest() throws InconsistencyException {

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // Create video with anonymous
        session.setUserId(null);
        videoBean.setGroupId(group1.getId());
        videoAction.setVideoBean(videoBean);
 
        assertEquals(null,
                videoAction.createVideo());       
        assertEquals("/xhtml/errors/restrictionError",
                videoAction.getLastRedirect());

        // Create video with participant
        assertNotNull(participant.getId());
        session.setUserId(participant.getId());
        videoBean.setGroupId(group1.getId());
        videoAction.setVideoBean(videoBean);
        assertEquals(null,
                videoAction.createVideo());
        assertEquals("/xhtml/errors/restrictionError",
                videoAction.getLastRedirect());

        // Create video with tutor
        assertNotNull(tutor.getId());
        session.setUserId(tutor.getId());
        videoBean.setGroupId(group1.getId());
        videoAction.setVideoBean(videoBean);
        videoAction.createVideo();

        // Create video with administrator
        assertNotNull(admin.getId());
        session.setUserId(admin.getId());
        videoBean.setGroupId(group1.getId());       
        videoAction.setVideoBean(videoBean);
        assertTrue(videoAction.createVideo().startsWith(
                "/xhtml/videos/editVideo"));       
    }
    
    /**
     * Prepare the database for testing
     * {@link VideoPublicationAction#editPublicationSettings()}.
     */
    public static void prepareEditPublicationSettings() throws Exception {
        //cleanupEditPublicationSettings();

        // Create administrator for testing.
        User admin = new User(null);
        admin.setBanned(false);
        admin.setBirthday(new Date());
        admin.setDeletable(true);
        admin.setEmail("VideoPublicationActionTest.admin@mailinator.com");
        admin.setFirstName("VideoPublicationActionTest");
        admin.setGender(EGender.Male);
        admin.setLastName("Admin");
        admin.setPassword("test");
        admin.setUserType(EUserType.Administrator);
        admin.setStreet("Musterstrasse");
        admin.setZip("45336");
        admin.setCity("Musterhausen");
        admin.setPhone("09007456");
        admin.setFax("08006656");
        admin.setWebsite("www.muster-web.de");
        admin.setCountry(ECountry.Austria);
        admin.setStreet("Wandstraße");
        admin.setCity("Einoede");
        admin.setZip("013");
        admin.setFax("0456234");
        admin.setPhone("0456234");
        admin.setWebsite("www.test.at");
        admin = persistenceProvider.getUserStore().create(admin);
        epsAdminId = admin.getId();

        // Create group for testing.
        Group group = new Group(null);
        group.setTitle("VideoPublicationActionTest");
        group.setVisible(true);
        group = persistenceProvider.getGroupStore().create(group, admin);
        epsGroupId = group.getId();
        

        // Create publicly accessible video.
        Video vPub = createVideo(
                "VideoPublicationActionTest Public", epsGroupId,
                EParticipationRestriction.Public, epsAdminId);
        epsVPubId = vPub.getId();     

        // Create video accessible by token.
        Video vToken = createVideo(
                "VideoPublicationActionTest Token", epsGroupId,
                EParticipationRestriction.Token, epsAdminId);
        epsQTokenId = vToken.getId();
        
        // Create video accessible by password.
        Video vPassw = createVideo(
                "VideoPublicationActionTest Password", epsGroupId,
                EParticipationRestriction.Password, epsAdminId);
        epsQPasswId = vPassw.getId();
       
        // Create video accessible by token and password.
        Video vTokenPassw = createVideo(
                "VideoPublicationActionTest Token Password",
                epsGroupId, EParticipationRestriction.Token, epsAdminId);
        
        epsQTokenPasswId = vTokenPassw.getId();       
    }
    
    /**
     * A Test that checks, if after another upload the version number gets increased 
     * and if the zip- and xml-files last modified times get changed.
     * 
     * @throws Exception
     */
    @Test
    public void afterUploadTest() throws Exception {
    	
         admin = userStore.create(admin);
         
         // Create group for test
         group1 = groupStore.create(group1, admin);
         
    	Video v1 = new Video(null);
        v1.setTitle(v1Title);
        v1.setAuthorId(admin.getId());
        v1.setDescription(v1Description);
        v1.setParticipationRestriction(v1PartRest);
        v1.setDirectory("TESTVIDEODIR");
        v1 = videoStore.create(v1, group1);  
        
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(admin.getId());
        videoBean = new VideoBean();
        videoBean.setId(v1.getId());
        videoBean.setTitle(v1Title);
        videoBean.setGroupId(group1.getId());
        videoAction.setSessionData(session);
        videoAction.setVideoBean(videoBean);
        videoAction.setVideoUploadMock(testVideoFile);
        videoAction.clearLogs();
        videoAction.editVideo();
        
        v1=videoStore.findById(v1.getId());
        int version = v1.getVersion();
        assertTrue(version>0);             
        File zip = new File(VIDEO_DESTINATION + "/" + v1.getDirectory() + "/video.zip");
        File xml = new File(VIDEO_DESTINATION + "/" + v1.getDirectory() + "/XML/export.xml");
        assertTrue(zip.exists());
    	assertTrue(xml.exists());
        Date zipDate =new Date(zip.lastModified());
        Date xmlDate = new Date(xml.lastModified());
        
        // Neuer Upload und Version und  Änderungsdatum prüfen       
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(admin.getId());
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

        assertTrue(v1.getVersion() > version);
        zip = new File(VIDEO_DESTINATION + "/" + v1.getDirectory() + "/video.zip");
        xml = new File(VIDEO_DESTINATION + "/" + v1.getDirectory() + "/XML/export.xml");
        assertTrue(zipDate.compareTo(new Date(zip.lastModified())) == -1);
        assertTrue(xmlDate.compareTo(new Date(xml.lastModified())) == -1);
        
        session.setUserId(admin.getId());
        videoAction.deleteVideo();
        assertNull(videoStore.findById(v1.getId()));        
    }
    
    /**
     * Test for trying to start a video. After a video is
     * started a user may participate by answering questions.
     * 
     * @throws Exception
     */
    @Test
    public void startVideoTest() throws Exception {   
                
        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        
       Video v1 = new Video(null);
        v1.setTitle(v1Title);
        v1.setAuthorId(tutor.getId());
        v1.setDescription(v1Description);
        v1.setParticipationRestriction(v1PartRest);
        v1.setDirectory("TESTVIDEODIR");
        v1 = videoStore.create(v1, group1);
               
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(admin.getId());
        videoBean = new VideoBean();
        videoBean.setId(v1.getId());
        videoBean.setTitle(v1Title);
        videoBean.setGroupId(group1.getId());
        videoAction.setSessionData(session);
        videoAction.setVideoBean(videoBean);
        videoAction.setVideoUploadMock(testVideoFile);
        videoAction.clearLogs();
        videoAction.editVideo();
        
        // video has not already started
        assertNull(v1.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(v1.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);   
        videoAction.startVideo();
        
        v1 = videoStore.getById(videoBean.getId());
       
        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(v1.getStart().compareTo(new Date()) == -1);

        session.setUserId(admin.getId());
        videoAction.deleteVideo();
        assertNull(videoStore.findById(v1.getId()));
    }

    /**
     * Test for stopping an video. After stop no user may participate in the video.
     * 
     * @throws Exception
     */
    @Test
    public void stopVideoTest() throws Exception {

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        
        Video v1 = new Video(null);
        v1.setTitle(v1Title);
        v1.setAuthorId(tutor.getId());
        v1.setDescription(v1Description);
        v1.setParticipationRestriction(v1PartRest);
        v1.setDirectory("TESTVIDEODIR");
        v1 = videoStore.create(v1, group1);
        
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(admin.getId());
        videoBean = new VideoBean();
        videoBean.setId(v1.getId());
        videoBean.setTitle(v1Title);
        videoBean.setGroupId(group1.getId());
        videoAction.setSessionData(session);
        videoAction.setVideoBean(videoBean);
        videoAction.setVideoUploadMock(testVideoFile);
        videoAction.clearLogs();
        videoAction.editVideo();
        
        v1=videoStore.findById(v1.getId());
        // video.start is null as long as not video has not
        // started
        assertNull(v1.getStart());

        // Set necessary bean data and start video

        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(v1.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);
        videoAction.startVideo();
        
        v1 = videoStore.getById(v1.getId());

        // verify that a new date is newer than start date
        Thread.sleep(1000);
        
        assertNotNull(v1.getStart().compareTo(new Date()) < 0);

        assertNull(v1.getStop());

        // stop video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(v1.getId());
        videoAction.stopVideo();

        v1 = videoStore.getById(v1.getId());

        // verify that a new date is newer than stop date
        Thread.sleep(1000);
        
        assertTrue(v1.getStop().compareTo(new Date()) < 0);
        
        session.setUserId(admin.getId());
        videoAction.deleteVideo();
        assertNull(videoStore.findById(v1.getId()));
    }

    /**
     * 
     * @throws InconsistencyException
     */
    @Test
    public void editVideoTest() throws InconsistencyException {

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create groups for test
        group1 = groupStore.create(group1, tutor);
        group2 = groupStore.create(group2, tutor);       
        		
        // Create video for test
        v1.setAuthorId(admin.getId());
        v1 = videoStore.create(v1, group1);
        videoAction.setSessionData(session);
        
        // Edit video with anonymous
        session.setUserId(null);
        videoAction.setVideoBean(videoBean);       
        videoAction.editVideo();
        assertEquals(ACCESS_RESTRICTION_FACELET,
                videoAction.getLastRedirect());

        // Edit video with participant
        assertNotNull(participant.getId());
        session.setUserId(participant.getId());
        videoAction.setVideoBean(videoBean);
        videoAction.editVideo();
        assertEquals(ACCESS_RESTRICTION_FACELET,
                videoAction.getLastRedirect());

        /*
         * Edit video with tutor that is an owner of the corresponding
         * group
         */
        assertNotNull(tutor.getId());
        session.setUserId(tutor.getId());
        videoBean = new VideoBean();
        assertNull(videoBean.getId());
        assertNull(videoBean.getTitle());
        videoBean.setId(v1.getId());
        videoAction.setVideoBean(videoBean);        
        
        // Test Prepopulation
        when(cxtMock.isPostback()).thenReturn(false);
        videoAction.editVideo();
        assertEquals(v1.getId(), videoBean.getId());
        assertEquals(v1Title, videoBean.getTitle());
        assertEquals(group1.getId(), videoBean.getGroupId());
        assertEquals(v1Description, videoBean.getDescription());
        assertEquals(v1PartRest,
                videoBean.getParticipationRestriction()); 
        
        
        v1.setDirectory("TESTVIDEODIR");
        
               
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(admin.getId());
       
        videoBean.setId(v1.getId());
        videoBean.setTitle(v1Title);
        videoBean.setGroupId(group1.getId());
        videoAction.setSessionData(session);
        videoAction.setVideoBean(videoBean);
        videoAction.setVideoUploadMock(testVideoFile);
        videoAction.clearLogs();
        videoAction.editVideo();
        
        Date vStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date vEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60  * 1000);
       
        // Prepare bean for editing
        videoBean.setTitle(v1TitleNew);
        videoBean.setGroupId(group2.getId());
        videoBean.setDescription(v1DescriptionNew);
        videoBean.setStart(vStart);
        videoBean.setStop(vEnd);
        
        // Edit video
        when(cxtMock.isPostback()).thenReturn(true);
        videoAction.editVideo();
        
        // Check if video was successfully edited
        Video editedv1 = videoStore.findById(videoBean.getId());
        assertNotNull(editedv1);
        
        assertEquals(v1TitleNew, editedv1.getTitle());
        Group groupEditedv1 = groupStore.getForVideo(v1);
        assertEquals(group2.getId(), groupEditedv1.getId());
        assertEquals(v1DescriptionNew, editedv1.getDescription());
        assertEquals(vStart, editedv1.getStart());
        assertEquals(vEnd, editedv1.getStop());
        	
        session.setUserId(admin.getId());
        videoAction.deleteVideo();
        assertNull(videoStore.findById(v1.getId()));        
    }

    /**
     * 
     * @throws InconsistencyException
     */
    @Test
    public void deleteVideoTest() throws InconsistencyException {

        // Check if administrator exists and create him in database
        assertNotNull(admin);
        admin = userStore.create(admin);
        assertNotNull(userStore.findById(admin.getId()));

        // Check if group exists and create it in database
        assertNotNull(group1);
        group1 = groupStore.create(group1, admin);
        assertNotNull(groupStore.findById(group1.getId()));

        // Create video in database
        Video v1 = new Video(null);
        v1.setTitle("test video");
        v1.setAuthorId(admin.getId());
        v1.setDirectory("/xhtml/videos/listVideos");
        v1.setDescription("test description");
        v1.setParticipationRestriction(EParticipationRestriction.Public);
        v1.setPublished(false);
        v1.setPassword(null);
        v1.setStart(null);
        v1.setStop(null);
        v1 = videoStore.create(v1, group1);

        // Check if creation of video was successful
        assertNotNull(v1);

        // Set session for administrator
        session.setUserId(admin.getId());

        // Prepare bean and controller
        videoBean.setId(v1.getId());
        videoAction.setVideoBean(videoBean);

        // Delete video with controller
        String result = videoAction.deleteVideo();

        // Check if video and its questions and answers were really
        // deleted from database
        assertNull(videoStore.findById(v1.getId()));   
        result.startsWith(LIST_VIDEO_FACELET);
    }

    /**
     * Access a video with a token.
     * 
     * @throws Exception
     */
    @Test
    public void accessVideoToken() throws Exception {

        String videoTitle = "testVideo";

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        Video video = new Video(null);
        video.setTitle(videoTitle);
        video.setAuthorId(admin.getId());
        video.setDirectory("/xhtml/videos/listVideos");
        video
                .setParticipationRestriction(EParticipationRestriction.Token);

        video = videoStore.create(video, group1);

        // video has not already started
        assertNull(video.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(video.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);

        // Start and end time
        Date qStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date qEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60
                * 1000);

        // Start video
        video.setStart(qStart);
        video.setStop(qEnd);
        videoStore.save(video);

        video = videoStore.getById(videoBean.getId());

        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(video.getStart().compareTo(new Date()) < 0);

        //
        // test with token as anonymous
        //
        session.setUserId(null);

        // verify token is not yet in database
        assertNull(tokenStore.find(token1Identifier));
        token1 = new Token(token1Identifier);

        // create test token
        token1 = tokenStore.create(token1, video.getId());
        assertNotNull(token1);

        tokenBean.setToken(token1);
        when(cxtMock.isPostback()).thenReturn(true);

        // access video
        videoAction.accessVideo();
        
        String redirect = videoAction.getLastRedirect();

        // first question of the video is a singleChoiceQuestion
        assertTrue(redirect.startsWith(WATCH_VIDEO_FACELET));
    }

    /**
     * Access a video with a password.
     * 
     * @throws Exception
     */
    @Test
    public void accessVideoPassword() throws Exception {

        String videoTitle = "testVideo";
        String videoPassword = "testPassword123";

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        Video video = new Video(null);
        video.setTitle(videoTitle);
        video.setAuthorId(admin.getId());
        video.setDirectory("/xhtml/videos/listVideos");
        video.setParticipationRestriction(EParticipationRestriction.Password);
        
        video.setPassword(videoPassword);

        video = videoStore.create(video, group1);

        // video has not already started
        assertNull(video.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(video.getId());
        videoAction.setVideoBean(videoBean);
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);

        // Start and end time
        Date qStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date qEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60
                * 1000);

        // Start video
        video.setStart(qStart);
        video.setStop(qEnd);
        videoStore.save(video);

        video = videoStore.getById(videoBean.getId());

        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(video.getStart().compareTo(new Date()) < 0);

        //
        // test with password as participant
        //
        session.setUserId(participant.getId());
        
        videoBean.setPassword(videoPassword);
       
        when(cxtMock.isPostback()).thenReturn(true);

        // access video
        videoAction.accessVideo();
        String redirect = videoAction.getLastRedirect();
        
        assertTrue(redirect.startsWith(WATCH_VIDEO_FACELET));
    }

    /**
     * Access video with attendant restrictions.
     * 
     * @throws Exception
     */
    @Test
    public void accessVideoAttendantRestriction() throws Exception {

        String videoTitle = "testVideo";

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        Video video = new Video(null);
        video.setTitle(videoTitle);
        video.setAuthorId(admin.getId());
        video.setDirectory("/xhtml/videos/listVideos");
        video.setParticipationRestriction(EParticipationRestriction.GroupAttendants);
      
        video = videoStore.create(video, group1);

        // add participant to group
        groupStore.addAttendant(group1, participant);
        
        // video has not already started
        assertNull(video.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(video.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);

        // Start and end time
        Date qStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date qEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60
                * 1000);

        // Start video
        video.setStart(qStart);
        video.setStop(qEnd);
        videoStore.save(video);

        video = videoStore.getById(videoBean.getId());

        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(video.getStart().compareTo(new Date()) < 0);
     
        // test as anonymous      
        session.setUserId(null);
        when(cxtMock.isPostback()).thenReturn(true);

        // access video
        videoAction.accessVideo();
        String redirect = videoAction.getLastRedirect();

        // access is restricted for anonymous
        assertTrue(redirect.startsWith(ACCESS_RESTRICTION_FACELET));

        // test as group attendant
        session.setUserId(participant.getId());
        videoAction.accessVideo();

        redirect = videoAction.getLastRedirect();
        
        // access granted, first question is a numberInputQuestion
        assertTrue(redirect.startsWith(WATCH_VIDEO_FACELET));
    }

    /**
     * Access Video with registered User Restriction.
     * 
     * @throws Exception
     */
    @Test
    public void accessVideoAttendanRegisteredUsertRestriction() throws Exception {
    	String videoTitle = "testVideo";

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        Video video = new Video(null);
        video.setTitle(videoTitle);
        video.setAuthorId(admin.getId());
        video.setDirectory("/xhtml/videos/listVideos");
        video.setParticipationRestriction(EParticipationRestriction.Registered);
        
        video = videoStore.create(video, group1);
        
        //video has not already started
        assertNull(video.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(video.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);

        // Start and end time
        Date qStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date qEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60
                * 1000);

        // Start video
        video.setStart(qStart);
        video.setStop(qEnd);
        videoStore.save(video);

        video = videoStore.getById(videoBean.getId());

        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(video.getStart().compareTo(new Date()) < 0);
      
        // test as anonymous      
        session.setUserId(null);
        when(cxtMock.isPostback()).thenReturn(true);

        // access video
        videoAction.accessVideo();
        String redirect = videoAction.getLastRedirect();

        // access is restricted for anonymous
        assertTrue(redirect.startsWith(ACCESS_RESTRICTION_FACELET));
        
        // test as group attendant       
        session.setUserId(participant.getId());
        videoAction.accessVideo();

        redirect = videoAction.getLastRedirect();
        
        // access granted
        assertTrue(redirect.startsWith(WATCH_VIDEO_FACELET));
    }
    /**
     * Access a public video. Only owners are not allowed to participate.
     * 
     * @throws Exception
     */
    @Test
    public void accessVideoPublic() throws Exception {

        String videoTitle = "testVideo";

        // Create users for test
        participant = userStore.create(participant);
        tutor = userStore.create(tutor);
        admin = userStore.create(admin);

        // Create group for test
        group1 = groupStore.create(group1, tutor);

        // set necessary video data and create video
        Video video = new Video(null);
        video.setTitle(videoTitle);
        video.setAuthorId(admin.getId());
        video.setDirectory("/xhtml/videos/listVideos");
        video
                .setParticipationRestriction(EParticipationRestriction.Public);

        video = videoStore.create(video, group1);

        // video has not already started
        assertNull(video.getStart());

        // Set necessary bean data and start video
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);

        videoBean.setId(video.getId());
        session.setUserId(tutor.getId());
        videoAction.setSessionData(session);

        // Start and end time
        Date qStart = new Date(System.currentTimeMillis() - 1 * 60 * 60 * 1000);
        Date qEnd = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60
                * 1000);

        // Start video
        video.setStart(qStart);
        video.setStop(qEnd);
        videoStore.save(video);

        video = videoStore.getById(videoBean.getId());

        // verify that a new Date is newer than start date
        Thread.sleep(5);
        assertNotNull(video.getStart().compareTo(new Date()) < 0);
 
        // test as group owner
        session.setUserId(tutor.getId());
        videoAction.accessVideo();
        String redirect = videoAction.getLastRedirect();

        redirect = videoAction.getLastRedirect();
        
        // access is restricted to tutors
        assertTrue(redirect.startsWith(WATCH_VIDEO_FACELET));
    }

    /**
     * 
     * @throws Exception 
     */
    @After
    public void cleanDB() throws Exception {
	
        // Clean database from created users
        List<User> users = new LinkedList<User>();
        users.add(participant);
        users.add(tutor);
        users.add(admin);

        for (User u : users) {
            if (u.getId() != null && userStore.findById(u.getId()) != null) {
                userStore.delete(u);
            }
        }

        // Clean database from created groups
        List<Group> groups = new LinkedList<Group>();
        groups.add(group1);
        groups.add(group2);

        for (Group e : groups) {
            if (e.getId() != null && groupStore.findById(e.getId()) != null) {
                groupStore.delete(e);
            }
        }
        // clean database from created token
        List<Token> tokens = new LinkedList<Token>();

        tokens.add(token1);

        for (Token token : tokens) {
            if (token != null && tokenStore.find(token.getToken()) != null) {
                tokenStore.delete(token);
            }
        }
       
    }

    /**
     * Clean up the database after testing
     * {@link VideoPublicationAction#editPublicationSettings()}.
     */
    public static void cleanupEditPublicationSettings() throws Exception {
	 
	 
        if (epsGroupId != null) {
            persistenceProvider.getGroupStore().delete(epsGroupId);
        } else {
            List<Group> groups = persistenceProvider.getGroupStore().getAll(persistenceProvider.getUserStore().findById(1));
            for (Group e : groups) {
                if (e.getTitle().contains("VideoPublicationActionTest")) {
                    persistenceProvider.getGroupStore().delete(e);
                    break;
                }
            }
        }

        if (epsAdminId != null) {
            persistenceProvider.getUserStore().delete(epsAdminId);
        } else {
            User admin = persistenceProvider.getUserStore().findByEmail(
                    "VideoPublicationActionTest.admin@mailinator.com");
            if (admin != null) {
                persistenceProvider.getUserStore().delete(admin);
            }
        }
    }

    /**
     * Create a new {@link Video}.
     */
    private static Video createVideo(String title,
            Integer group, EParticipationRestriction participation, int authorId) throws Exception {
        Video v = new Video(null);
        v.setParticipationRestriction(participation);
        v.setTitle(title);
        v.setAuthorId(authorId);
        v.setDirectory("TESTVIDEODIR");
        v = persistenceProvider.getVideoStore().create(v, group);
        
         
        
        when(cxtMock.isPostback()).thenReturn(true);
        session.setUserId(authorId);
        videoBean = new VideoBean();
        videoBean.setId(v.getId());
        videoBean.setTitle(title);
        videoBean.setGroupId(group);
        videoAction.setSessionData(session);
        videoAction.setVideoBean(videoBean);
        videoAction.setVideoUploadMock(testVideoFile);
        videoAction.clearLogs();
        videoAction.editVideo();
        v=persistenceProvider.getVideoStore().findById(v.getId());
        
        return v;
    }

    /**
     * Test {@link VideoPublicationAction#editPublicationSettings()}.
     * @throws Exception 
     * 
     */
    @Test
    public void testEditPublicationSettings() throws Exception {
	 prepareEditPublicationSettings();

        VideoBean vpb;       
        // Read publicly accessible video using controller.
        vpb = editPublicationSettingsRead(epsAdminId, epsVPubId);
        assertEquals(epsVPubId, vpb.getId());
        assertNull(vpb.getStart());
        assertNull(vpb.getStop());
        assertFalse(vpb.isPasswordAvailable());
        assertFalse(vpb.isTokenAvailable());

        // Read video accessible by token.
        vpb = editPublicationSettingsRead(epsAdminId, epsQTokenId);
        assertEquals(epsQTokenId, vpb.getId());
        assertNull(vpb.getStart());
        assertNull(vpb.getStop());
        assertFalse(vpb.isPasswordAvailable());
        assertTrue(vpb.isTokenAvailable());

        // Read video accessible by password.
        vpb = editPublicationSettingsRead(epsAdminId, epsQPasswId);
        assertEquals(epsQPasswId, vpb.getId());
        assertNull(vpb.getStart());
        assertNull(vpb.getStop());
        assertNull(vpb.getPassword());
        assertTrue(vpb.isPasswordAvailable());
        assertFalse(vpb.isTokenAvailable());

        // Read video accessible by password and token.
        vpb = editPublicationSettingsRead(epsAdminId, epsQTokenPasswId);       
        assertEquals(epsQTokenPasswId, vpb.getId());
        assertNull(vpb.getStart());
        assertNull(vpb.getStop());
        assertNull(vpb.getPassword());
        assertTrue(vpb.isTokenAvailable());

        // Create start and stop date.
        GregorianCalendar start = new GregorianCalendar();
        start.add(GregorianCalendar.HOUR_OF_DAY, 1);

        GregorianCalendar stop = new GregorianCalendar();
        stop.add(GregorianCalendar.HOUR_OF_DAY, 2);

        // Start videos.
        editPublicationSettingsWrite(epsAdminId, epsVPubId, start.getTime(),
                stop.getTime(), null);
        editPublicationSettingsWrite(epsAdminId, epsQTokenId, start.getTime(),
                stop.getTime(), null);
        editPublicationSettingsWrite(epsAdminId, epsQPasswId, start.getTime(),
                stop.getTime(), "test");
        editPublicationSettingsWrite(epsAdminId, epsQTokenPasswId,
                start.getTime(), stop.getTime(), "test");

        // Verify that all videos were started.
        Video q;
        q = persistenceProvider.getVideoStore().getById(epsVPubId);
        assertEquals(start.getTime(), q.getStart());
        assertEquals(stop.getTime(), q.getStop());
        assertNull(q.getPassword());

        q = persistenceProvider.getVideoStore().getById(epsQTokenId);
        assertEquals(start.getTime(), q.getStart());
        assertEquals(stop.getTime(), q.getStop());
        assertNull(q.getPassword());

        q = persistenceProvider.getVideoStore().getById(epsQPasswId);
        assertEquals(start.getTime(), q.getStart());
        assertEquals(stop.getTime(), q.getStop());
        assertEquals("test", q.getPassword());

        q = persistenceProvider.getVideoStore().getById(epsQTokenPasswId);
        assertEquals(start.getTime(), q.getStart());
        assertEquals(stop.getTime(), q.getStop()); 
        
        session.setUserId(epsAdminId);
	 VideoBean vb =new VideoBean();
	 videoAction.setVideoBean(vb);
	 vb.setId(epsVPubId);	 
	 videoAction.deleteVideo();
	 assertNull(videoStore.findById( epsVPubId)); 
	 vb.setId(epsQPasswId);	 
	 videoAction.deleteVideo();
	 assertNull(videoStore.findById( epsQPasswId)); 
	 vb.setId(epsQTokenPasswId);	 
	 videoAction.deleteVideo();
	 assertNull(videoStore.findById( epsQTokenPasswId)); 
	 vb.setId(epsQTokenId);	 
	 videoAction.deleteVideo();
	 assertNull(videoStore.findById( epsQTokenId)); 
    }

    /**
     * Read the publication settings of a {@link Video} using the
     * controller.
     */
    private static VideoBean editPublicationSettingsRead(
            Integer currentUser, Integer video) {
        SessionData session = new SessionData();
        session.setUserId(currentUser);

        FacesContext fctxMock = mock(FacesContext.class);
        UIViewRoot uvr = mock(UIViewRoot.class);
        when(fctxMock.isPostback()).thenReturn(false);
        when(fctxMock.isValidationFailed()).thenReturn(false);
        when(fctxMock.getViewRoot()).thenReturn(uvr);
        when(uvr.getLocale()).thenReturn(Locale.ENGLISH);

        VideoBean vpb = new VideoBean();
        vpb.setId(video);

        VideoAction ctrl = new VideoAction();
        ctrl.setConfiguration(configuration);
        ctrl.setMock(fctxMock);
        ctrl.setPersistenceProvider(persistenceProvider);
        ctrl.setVideoBean(vpb);
        ctrl.setSessionData(session);

        ctrl.editVideo();

        return vpb;
    }

    /**
     * Change the publication settings of a {@link Video} using the
     * controller.
     */
    private static VideoBean editPublicationSettingsWrite(
            Integer currentUser, Integer video, Date start, Date stop,
            String password) {
        SessionData session = new SessionData();
        session.setUserId(currentUser);

        FacesContext fctxMock = mock(FacesContext.class);
        UIViewRoot uvr = mock(UIViewRoot.class);
        when(fctxMock.isPostback()).thenReturn(true);
        when(fctxMock.isValidationFailed()).thenReturn(false);
        when(fctxMock.getViewRoot()).thenReturn(uvr);
        when(uvr.getLocale()).thenReturn(Locale.ENGLISH);

        assertNotNull(start);
        assertNotNull(stop);       
     
     
        VideoBean vpb = new VideoBean();
        vpb.setId(video);
        vpb.setTitle(videoStore.findById(video).getTitle());
        vpb.setGroupId(epsGroupId);
        VideoAction ctrl = new VideoAction();
        ctrl.setConfiguration(configuration);
        ctrl.setMock(fctxMock);
        ctrl.setPersistenceProvider(persistenceProvider);
        ctrl.setVideoBean(vpb);
        ctrl.setSessionData(session);                       
        
        vpb.setPassword(password);
        vpb.setStart(start);
        vpb.setStop(stop);     
       
        ctrl.editVideo();
        
        Video vid= videoStore.findById(video);
       
        assertNotNull(vid.getStart());
        assertNotNull(vid.getStop());
        return vpb;
    }
    
    @AfterClass
    public static void releasePersistence() throws Exception {
    	cleanupEditPublicationSettings();
        persistenceProvider.close();
    }
}

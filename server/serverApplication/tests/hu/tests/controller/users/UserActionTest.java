package hu.tests.controller.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.users.ResetPasswordBean;
import hu.backingbeans.users.UserBean;
import hu.controller.AController;
import hu.controller.users.UserAction;
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

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functionality of the {@link UserAction} controller.
 */
public class UserActionTest extends AController {

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static SessionData sessionData;
    private static UserBean userBean;
    private static UserAction userAction;

    // User data
    private final static String title = "Dr.";
    private final static String email = "test.user@mail.com";
    private final static String password = "Test!123";
    private final static String firstName = "Max";
    private final static String lastName = "Muster";
    @SuppressWarnings("deprecation")
	private final static Date birthday = new Date(1990, 12, 12);
    private final static EGender gender = EGender.Male;
    private final static EUserType type = EUserType.Administrator;
    private final static boolean banned = false;
    private final static String street = "Musterstrasse";
    private final static String zip = "55555";
    private final static String city = "Musterstadt";
    private final static ECountry country = ECountry.Germany;
    private final static String phone = "0151555";
    private final static String fax = "0151554";
    private final static String website = "www.musterweb.de";
    private final static boolean visible = true;
    
    private final static String titleChange = "Prof.";    
    private final static String emailChange = "change@mail.com";
    private final static String passwordChange = "Testchange!123";
    private final static String firstNameChange = "Maxnew";
    private final static String lastNameChange = "Musternew";
    @SuppressWarnings("deprecation")
	private final static Date birthdayChange = new Date(1990, 12, 13);
    private final static EGender genderChange = EGender.Female;;
    private final static boolean bannedChange = false;
    private final static String streetChange = "Musterstrassenew";
    private final static String zipChange = "5555";
    private final static String cityChange = "Musterdorf";
    private final static ECountry countryChange = ECountry.Austria;
    private final static String phoneChange = "01515553";
    private final static String faxChange = "01515543";
    private final static String websiteChange = "www.musterweb-new.at";
    private final static boolean visibleChange = false;
    
    private static Integer adminId;
    private static Integer attendantId;
    
    private final static String LOGIN_FACELET = "/xhtml/users/login";
    
    private final static String TEST_PICTURE_DIR = "./tests/resources/Test.png";
    private final static String TEST_ALT_PICTURE_DIR = "./tests/resources/Test2.png";
    private static final String PHOTO_DESTINATION =  System.getProperty("user.home")
            + "/.sivaServer/photos/";
    
    private static File picture;

    /**
     * @throws InconsistencyException
     */
    @BeforeClass
    public static void prepare() throws InconsistencyException {
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);
        sessionData = new SessionData();

        userBean = new UserBean();
        userBean.setAcademicTitle(title);
        userBean.setEmail(email);
        userBean.setPassword(SecurityUtils.hash(password));
        userBean.setFirstName(firstName);
        userBean.setLastName(lastName);
        userBean.setBirthday(birthday);
        userBean.setGender(gender);
        userBean.setUserType(type);
        userBean.setBanned(banned);    
        userBean.setStreet(street);
        userBean.setZip(zip);
        userBean.setCity(city);
        userBean.setCountry(country);
        userBean.setPhone(phone);
        userBean.setFax(fax);
        userBean.setWebsite(website);
        userBean.setVisible(visible);
       
        userAction = new UserAction();
        userAction.setPersistenceProvider(persistenceProvider);
        userAction.setSession(sessionData);
        userAction.setUserBean(userBean);        

        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        prepareForBanUnban();     
    }

    /**
     * @throws InconsistencyException
     */
    @BeforeClass
    public static void preprepare() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
        userAction = new UserAction();
        userAction.setMock(cxtMock);
        
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);

        userAction.setPersistenceProvider(persistenceProvider);
        userAction.setSession(sessionData);
        userAction.setUserBean(userBean);

        
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void cleanup() throws Exception {
        cleanupForBanUnban();
        persistenceProvider.close();
    }

    /**
     * @throws InconsistencyException
     */
    private static void prepareForBanUnban() throws InconsistencyException {

        for (int i = 0; i < 3; i++) {
        	User tutor = new User(null);
            tutor.setCountry(ECountry.Germany);
            tutor.setBanned(false);
            tutor.setBirthday(new Date());
            tutor.setEmail(String.format("banUnBan%02d@test.de",i));
            tutor.setFirstName("test");
            tutor.setGender(EGender.Male);
            tutor.setLastName("test");
            tutor.setPasswordHash(SecurityUtils.hash("Test!123"));
            tutor.setUserType(EUserType.Tutor);
            tutor.setTitle("Dr.");
            tutor.setStreet("Mustergasse");
            tutor.setZip("45056");
            tutor.setCity("Musterberg");
            tutor.setPhone("09001456");
            tutor.setFax("08001456");
            tutor.setVisible(true);
            tutor.setWebsite("www.musterweb.de");
            
            persistenceProvider.getUserStore().create(tutor);
        }

        User user1 = new User(null);
        user1 = persistenceProvider.getUserStore().findByEmail(
        		"banUnBan00@test.de");
        user1.setUserType(EUserType.Participant);
        attendantId = persistenceProvider.getUserStore().save(user1).getId();

        User user2 = new User(null);
        user2 = persistenceProvider.getUserStore().findByEmail(
        		"banUnBan01@test.de");
        user2.setUserType(EUserType.Administrator);
        adminId = persistenceProvider.getUserStore().save(user2).getId();

        persistenceProvider.getUserStore()
                .findByEmail("banUnBan02@test.de").getId();

    }

    /**
     * @throws InconsistencyException
     */
    private static void cleanupForBanUnban() throws InconsistencyException {
        for (int i = 0; i < 3; i++) {
            persistenceProvider.getUserStore().delete(
                    persistenceProvider.getUserStore().findByEmail(
                            String.format("banUnBan%02d@test.de", i)));
        }
    }

    /**
     * @throws InconsistencyException
     */
    @Test
    public void testDeleteUser() throws InconsistencyException {
    	 // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        User user = persistenceProvider.getUserStore().findByEmail(
                "email@test.de");
        if (user != null) {
        	userBean.setId(user.getId());
            userAction.deleteUser();
        }

        user = new User(null);
        user.setEmail("email@test.de");
        user.setPassword(SecurityUtils.hash(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBirthday(birthday);
        user.setGender(gender);
        user.setUserType(type);
        user.setBanned(banned);
        user.setCountry(country);
        user.setStreet(street);
        user.setZip(zip);
        user.setCity(city);
        user.setPhone(phone);
        user.setFax(fax);
        user.setWebsite(website);
        user.setVisible(visible);
        user = persistenceProvider.getUserStore().create(user);
        userBean.setId(user.getId());

        assertNotNull(user);
        // delete as attendant
        sessionData.setUserId(attendantId);

        // user still in DB?
        assertNotNull(user);

        // delete yourself
        sessionData.setUserId(user.getId());
        userAction.restriction();
        assertEquals("/xhtml/errors/restrictionError", userAction.getLastRedirect());

        // delete as admin
        sessionData.setUserId(adminId);
        userAction.deleteUser();
        assertNull(persistenceProvider.getUserStore().findById(user.getId()));
    }

    /**
     * @throws InconsistencyException
     */
    @Test
    public void testBanUnbanUser() throws InconsistencyException {

        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        User user = persistenceProvider.getUserStore().findByEmail(
                "email@test.de");
        if (user != null) {
        	userBean.setId(user.getId());
            userAction.deleteUser();
        }

        user = new User(null);
        user.setEmail("email@test.de");
        user.setPassword(SecurityUtils.hash(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBirthday(birthday);
        user.setGender(gender);
        user.setUserType(type);
        user.setBanned(banned);
        user.setCountry(country);
        user.setStreet(street);
        user.setZip(zip);
        user.setCity(city);
        user.setPhone(phone);
        user.setFax(fax);
        user.setWebsite(website);
        user.setVisible(visible);
        user = persistenceProvider.getUserStore().create(user);
        userBean.setId(user.getId());

        // ban tests
        // ban as attendant
        sessionData.setUserId(attendantId);
        userAction.banUser();
        assertFalse(persistenceProvider.getUserStore().getById(user.getId())
                .isBanned());

        // unban unbanned
        sessionData.setUserId(adminId);
        userAction.unbanUser();
        assertEquals("/xhtml/errors/restrictionError", userAction.getLastRedirect());

        // ban yourself
        userBean.setId(adminId);
        userAction.restriction();
        assertEquals("/xhtml/errors/restrictionError", userAction.getLastRedirect());
        userBean.setId(user.getId());

        // ban as admin
        when(cxtMock.isPostback()).thenReturn(true);
        sessionData.setUserId(adminId);
        userAction.banUser();
        assertTrue(persistenceProvider.getUserStore().getById(user.getId())
                .isBanned());

        // unban as attendant
        sessionData.setUserId(attendantId);
        userAction.restriction();
        assertEquals("/xhtml/errors/restrictionError", userAction.getLastRedirect());

        // unban as admin
        sessionData.setUserId(adminId);
        userAction.unbanUser();
        assertFalse(persistenceProvider.getUserStore().getById(user.getId())
                .isBanned());

        // delete user
        userBean.setId(user.getId());
        userAction.deleteUser();
    }

    /**
     * @throws InconsistencyException
     */
    @Test
    public void testRegister() throws InconsistencyException {

        // delete possible existing user
        User userExists = persistenceProvider.getUserStore().findByEmail(email);
        if (userExists != null) {
        	userBean.setId(userExists.getId());
            userAction.deleteUser();
        }
        userExists = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(userExists);

        // set test data
        userBean.setAcademicTitle(title);
        userBean.setEmail(email);
        userBean.setFirstName(firstName);
        userBean.setLastName(lastName);
        userBean.setBirthday(birthday);
        userBean.setGender(gender);
        userBean.setUserType(type);
        userBean.setBanned(banned);
        userBean.setPassword(password);
        userBean.setPasswordRepeat(password);
        userBean.setCountry(country);
        userBean.setStreet(street);
        userBean.setZip(zip);
        userBean.setCity(city);
        userBean.setCountry(country);
        userBean.setPhone(phone);
        userBean.setFax(fax);
        userBean.setWebsite(website);
        userBean.setVisible(visible);
        
        picture = new File(TEST_PICTURE_DIR);
        userAction.setPhotoUploadMock(picture);
       
        //mock mailservice
        MailService mailService = mock(MailService.class);
        userAction.setMailService(mailService);
        when(mailService.sendMail(anyString(), anyString(), anyString())).thenReturn(true);

        // test register
        userAction.registerUser();
        User user = persistenceProvider.getUserStore().findByEmail(email);       
        
        assertNotNull(user);
        assertEquals(email, user.getEmail());
        assertEquals(SecurityUtils.hash(password), user.getPasswordHash());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(gender, user.getGender());
        assertEquals(type, user.getUserType());
        assertEquals(country, user.getCountry());
        assertEquals(zip, user.getZip());
        assertEquals(website, user.getWebsite());
        assertEquals(street, user.getStreet());
        assertEquals(phone, user.getPhone());
        assertEquals(fax, user.getFax());
        assertEquals(city, user.getCity());
        assertEquals(visible, user.isVisible());
        assertTrue(user.isPhotoAvailable());
        assertTrue(banned == user.isBanned());
        assertTrue(new File(PHOTO_DESTINATION + user.getId() + ".jpg").exists());
        
        // delete user after test
        userBean.setId(user.getId());
        userAction.deleteUser();
        
        user = persistenceProvider.getUserStore().findByEmail(email);        
        assertNull(user);       
    }

    /** 
     * @throws InconsistencyException
     */
    @Test
    public void testCreateUser() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // delete possible existing user
        User userExists = persistenceProvider.getUserStore().findByEmail(email);

        if (userExists != null) {
        	userBean.setId(userExists.getId());
            userAction.deleteUser();
        }
        userExists = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(userExists);

        // delete possible existing user

        String adminMail = "admin@local.host";
        User userExistsAdmin = persistenceProvider.getUserStore().findByEmail(
                adminMail);

        if (userExistsAdmin != null) {
        	userBean.setId(userExistsAdmin.getId());
            userAction.deleteUser();
        }
        userExistsAdmin = persistenceProvider.getUserStore().findByEmail(
                adminMail);
        assertNull(userExistsAdmin);

        // delete possible existing user

        String tutorMail = "tutor@nice.at";
        User userExistsTutor = persistenceProvider.getUserStore().findByEmail(
                tutorMail);

        if (userExistsTutor != null) {
        	userBean.setId(userExistsTutor.getId());
            userAction.deleteUser();
        }
        userExistsTutor = persistenceProvider.getUserStore().findByEmail(
                tutorMail);
        assertNull(userExistsTutor);

        // test as admin
        User admin = createUser(EUserType.Administrator, adminMail);

        sessionData.setUserId(admin.getId());

        // set userData in bean
        userBean.setEmail(email);

        userAction.createUser();

        User createdUser = persistenceProvider.getUserStore().getByEmail(email);
        assertNotNull(createdUser);

        // delete user after test
        User user = persistenceProvider.getUserStore().getByEmail(email);
        userBean.setId(user.getId());
        userAction.deleteUser();
        
        user = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(user);

        // test as tutor
        User tutor = createUser(EUserType.Tutor, tutorMail);

        sessionData.setUserId(tutor.getId());

        // set data in bean
        userBean.setEmail(email);
        userAction.createUser();

        User tutorCreatedUser = persistenceProvider.getUserStore().getByEmail(
                email);
        assertNotNull(tutorCreatedUser);

        // delete user after test
        User tutorUser = persistenceProvider.getUserStore().getByEmail(email);
       
        userBean.setId(tutorUser.getId());
        userAction.deleteUser();
        tutorUser = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(tutorUser);

        // delete user after test
        User tutorDelete = persistenceProvider.getUserStore().getByEmail(
                tutorMail);
        userBean.setId(tutorDelete.getId());
        userAction.deleteUser();
        tutorDelete = persistenceProvider.getUserStore().findByEmail(tutorMail);
        assertNull(tutorDelete);

        // delete user after test
        User adminDelete = persistenceProvider.getUserStore().getByEmail(
                adminMail);
        userBean.setId(adminDelete.getId());
        userAction.deleteUser();
        adminDelete = persistenceProvider.getUserStore().findByEmail(adminMail);
        assertNull(adminDelete);

    }

    /**
     * @throws InconsistencyException
     */
    @Test
    public void testEditProfile() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // delete possible existing user with email
        User userExists = persistenceProvider.getUserStore().findByEmail(email);

        if (userExists != null) {
        	userBean.setId(userExists.getId());
            userAction.deleteUser();
        }
        userExists = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(userExists);

        // delete possible existing user with emailChange
        User userExistsEmailChange = persistenceProvider.getUserStore()
                .findByEmail(emailChange);

        if (userExistsEmailChange != null) {
        	userBean.setId(userExistsEmailChange.getId());
            userAction.deleteUser();
        }
        userExistsEmailChange = persistenceProvider.getUserStore().findByEmail(
                emailChange);
        assertNull(userExistsEmailChange);

        // create User
        
        User user = createUser(EUserType.Participant, email);
        sessionData.setUserId(user.getId());
        userBean.setId(user.getId());
        assertEquals(user.getEmail(), email);

        // Test form prepopulation               
        assertEquals(user.getId(), userBean.getId());
        assertEquals(title, userBean.getAcademicTitle());
        assertEquals(email, userBean.getEmail());
        assertEquals(firstName, userBean.getFirstName());
        assertEquals(lastName, userBean.getLastName());
        assertEquals(birthday, userBean.getBirthday());
        assertEquals(gender, userBean.getGender());
        assertEquals(country, userBean.getCountry());
        assertEquals(street, userBean.getStreet());
        assertEquals(city, userBean.getCity());
        assertEquals(phone, userBean.getPhone());
        assertEquals(fax, userBean.getFax());
        assertEquals(website, userBean.getWebsite());
        assertTrue(!user.isPhotoAvailable());
        
        // Test for edit
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);
        userBean.setEmail(emailChange);
        userBean.setPassword(passwordChange);
        userBean.setPasswordRepeat(passwordChange);
        userBean.setAcademicTitle(titleChange);
        userBean.setFirstName(firstNameChange);
        userBean.setLastName(lastNameChange);
        userBean.setBirthday(birthdayChange);
        userBean.setGender(genderChange);      
        userBean.setBanned(bannedChange);
        userBean.setCountry(countryChange);
        userBean.setStreet(streetChange);
        userBean.setZip(zipChange);
        userBean.setCity(cityChange);
        userBean.setCountry(countryChange);
        userBean.setPhone(phoneChange);
        userBean.setFax(faxChange);
        userBean.setWebsite(websiteChange);
        userBean.setVisible(visibleChange);
        
        // "upload" photo
        picture = new File(TEST_PICTURE_DIR);
        userAction.setPhotoUploadMock(picture);
        
        userAction.setMock(cxtMock);
        userAction.editProfile();
        
        user = persistenceProvider.getUserStore().getById(
                sessionData.getUserId());
        
        Date picDate =new Date(new File(PHOTO_DESTINATION + user.getId() + ".jpg").lastModified());
             
        assertEquals(emailChange, user.getEmail());       
        assertEquals(titleChange, user.getTitle());
        assertEquals(emailChange, user.getEmail());
        assertEquals(firstNameChange, user.getFirstName());
        assertEquals(lastNameChange, user.getLastName());
        assertEquals(birthdayChange, user.getBirthday());
        assertEquals(genderChange, user.getGender());
        assertEquals(countryChange, user.getCountry());
        assertEquals(streetChange, user.getStreet());
        assertEquals(cityChange, user.getCity());
        assertEquals(phoneChange, user.getPhone());
        assertEquals(faxChange, user.getFax());
        assertEquals(websiteChange, user.getWebsite());
        assertEquals(SecurityUtils.hash(passwordChange), user.getPasswordHash());
        assertTrue(user.isPhotoAvailable());
        
        // overwrite existing photo
        picture = new File(TEST_ALT_PICTURE_DIR);
        userAction.setPhotoUploadMock(picture);
        userAction.editProfile();
        // look if new photo is "newer"
        assertTrue(user.isPhotoAvailable());
        assertTrue(picDate.compareTo(new Date(new File(PHOTO_DESTINATION + user.getId() + ".jpg").lastModified())) == -1);
        user = persistenceProvider.getUserStore().findByEmail(emailChange);
              
        assertNotNull(user);

        // delete user after test
        userBean.setId(user.getId());
        userAction.deleteUser();
        user = persistenceProvider.getUserStore().findByEmail(emailChange);
        assertNull(user);

    }

    /** 
     * @throws InconsistencyException
     */
    @Test
    public void testEditUser() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // delete possible existing user with email
        User userExists = persistenceProvider.getUserStore().findByEmail(email);

        if (userExists != null) {
        	userBean.setId(userExists.getId());
            userAction.deleteUser();
        }
        userExists = persistenceProvider.getUserStore().findByEmail(email);
        assertNull(userExists);

        // delete possible existing user with emailChange
        User userExistsEmailChange = persistenceProvider.getUserStore()
                .findByEmail(emailChange);

        if (userExistsEmailChange != null) {
        	userBean.setId(userExistsEmailChange.getId());
            userAction.deleteUser();
        }
        userExistsEmailChange = persistenceProvider.getUserStore().findByEmail(
                emailChange);
        assertNull(userExistsEmailChange);

        // delete possible existing user with "cool@admin.com"
        String coolAdminMail = "cool@admin.com";
        User coolAdmin = persistenceProvider.getUserStore().findByEmail(
                coolAdminMail);

        if (coolAdmin != null) {
        	userBean.setId(coolAdmin.getId());
            userAction.deleteUser();
        }
        coolAdmin = persistenceProvider.getUserStore().findByEmail(
                coolAdminMail);
        assertNull(coolAdmin);

        // test as admin
        User admin = createUser(EUserType.Administrator, coolAdminMail);
        sessionData.setUserId(admin.getId());

        // create UserData
        User userCreated = createUser(EUserType.Participant, email);
        assertEquals(userCreated.getEmail(), email);
        userBean.setId(userCreated.getId());

        // Test form prepopulation
        userAction.editUser();
        assertEquals(userCreated.getId(), userBean.getId());
        assertEquals(email, userBean.getEmail());

        // Test for edit
        when(cxtMock.isPostback()).thenReturn(true);
        when(cxtMock.isValidationFailed()).thenReturn(false);
        userBean.setEmail(emailChange);

        userAction.setMock(cxtMock);
        userAction.editUser();
        userCreated = persistenceProvider.getUserStore().getById(
                userCreated.getId());

        assertEquals(emailChange, userCreated.getEmail());

        // delete user after test
        User userChange = persistenceProvider.getUserStore().findByEmail(
                emailChange);
        userBean.setId(userChange.getId());
        userAction.deleteUser();
        userChange = persistenceProvider.getUserStore().findByEmail(emailChange);
        assertNull(userChange);
        
        // delete user after test
        User userCool = persistenceProvider.getUserStore().findByEmail(
                coolAdminMail);
        userBean.setId(userCool.getId());
        userAction.deleteUser();
        userCool = persistenceProvider.getUserStore().findByEmail(coolAdminMail);
        assertNull(userCool);

    }

    /**
     * Create an testUser with static data from UserActionTest and userType type
     * 
     * @param type
     * @return
     * @throws InconsistencyException
     */
    private User createUser(EUserType type, String email)
            throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
        User user = new User(null);
        user.setEmail(email);
        user.setPasswordHash(SecurityUtils.hash(password));
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBirthday(birthday);
        user.setGender(gender);
        user.setUserType(type);
        user.setBanned(banned);
        user.setTitle(title);
        user.setCountry(country);
        user.setStreet(street);
        user.setZip(zip);
        user.setCity(city);
        user.setPhone(phone);
        user.setFax(fax);
        user.setWebsite(website);
        user.setVisible(visible);        
        user = persistenceProvider.getUserStore().create(user);
        return user;
    }
    
    /**
     * This test check the method generateHash.
     * @throws InconsistencyException
     */
    @Test
    public void testGenerateHash() throws InconsistencyException {
        Calendar c1 = new GregorianCalendar(2013, 1, 1);
        Calendar c2 = new GregorianCalendar(2013, 1, 2);
        Calendar c3 = new GregorianCalendar(2013, 1, 3);
        Calendar c4 = new GregorianCalendar(2013, 1, 4);
        
        String h1 = "5faab6ad3bdbf919f9cae250196a31828862c97847623acc8d87da707ad6a788";
        String h2 = "1df3ec1ed197d8e8a495132d0a2ab8e00cb50605cfe6408baeba3e69b15be355";
        String h3 = "7bdbbf7eceae4116bb6a0f2cec0e28d8bd09b7ca6b6741c1c70e2e62842d29a3";
        String h4 = "e14a96cecaad10cd4d685c35ae055d5ba08281b92c65a134671d1c6425f658fa";
        
        String testUserEmail = "tester@mailinator.com";
        String password = "Test123";
        String wrongPassword = "Test456";
        String wrongUserEmail = "wrong@mailinator.com";
        
        // delete possible existing user
        User testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        User wrongUser = persistenceProvider.getUserStore().findByEmail(wrongUserEmail);

        if (testUser != null) {
            persistenceProvider.getUserStore().delete(testUser);
        }
        
        if (wrongUser != null) {
            persistenceProvider.getUserStore().delete(wrongUser);
        }
        
        testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(testUser);
        wrongUser = persistenceProvider.getUserStore().findByEmail(wrongUserEmail);
        assertNull(wrongUser);
        
        // create User
        testUser = createUser(EUserType.Participant, testUserEmail);
        testUser.setEmail(testUserEmail);
        testUser.setPassword(password);
        persistenceProvider.getUserStore().save(testUser);
        wrongUser = createUser(EUserType.Participant, wrongUserEmail);
        wrongUser.setEmail(wrongUserEmail);
        wrongUser.setPassword(wrongPassword);
        persistenceProvider.getUserStore().save(wrongUser);
        
        testUser = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        assertNotNull(testUser);
        wrongUser = persistenceProvider.getUserStore().getByEmail(wrongUserEmail);
        assertNotNull(wrongUser);
        
        // Test
        assertEquals(h1, userAction.generateHash(c1, testUser));
        assertEquals(h2, userAction.generateHash(c2, testUser));
        assertEquals(h3, userAction.generateHash(c3, testUser));
        assertEquals(h4, userAction.generateHash(c4, testUser));
        
        assertFalse(h1.equals(userAction.generateHash(c2, testUser)));
        assertFalse(h1.equals(userAction.generateHash(c3, testUser)));
        assertFalse(h1.equals(userAction.generateHash(c4, testUser)));

        assertFalse(h1.equals(userAction.generateHash(c2, wrongUser)));
        assertFalse(h1.equals(userAction.generateHash(c3, wrongUser)));
        assertFalse(h1.equals(userAction.generateHash(c4, wrongUser)));
        
        assertFalse(h2.equals(userAction.generateHash(c1, wrongUser)));
        assertFalse(h2.equals(userAction.generateHash(c2, wrongUser)));
        assertFalse(h2.equals(userAction.generateHash(c3, wrongUser)));
        assertFalse(h2.equals(userAction.generateHash(c4, wrongUser)));
        
        assertFalse(h3.equals(userAction.generateHash(c1, wrongUser)));
        assertFalse(h3.equals(userAction.generateHash(c2, wrongUser)));
        assertFalse(h3.equals(userAction.generateHash(c3, wrongUser)));
        assertFalse(h3.equals(userAction.generateHash(c4, wrongUser)));
        
        assertFalse(h4.equals(userAction.generateHash(c1, wrongUser)));
        assertFalse(h4.equals(userAction.generateHash(c2, wrongUser)));
        assertFalse(h4.equals(userAction.generateHash(c3, wrongUser)));
        assertFalse(h4.equals(userAction.generateHash(c4, wrongUser)));
        
        
        // delete user after test
        User user = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        userBean.setId(user.getId());
        userAction.deleteUser();
        user = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(user);
        user = persistenceProvider.getUserStore().getByEmail(wrongUserEmail);
        userBean.setId(user.getId());
        userAction.deleteUser();
        user = persistenceProvider.getUserStore().findByEmail(wrongUserEmail);
        assertNull(user);
    }
    
    /**
     * This test check if the method sendPasswordRecoveryLink works correctly.
     * @throws InconsistencyException
     */
    @Test
    public void testSendPasswordRecoveryLink() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
        
        String testUserEmail = "tester@mailinator.com";
        String password = "Test123";
        
        // delete possible existing user
        User testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);

        if (testUser != null) {
        	userBean.setId(testUser.getId());
            userAction.deleteUser();
        }
        testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(testUser);

        // create User
        testUser = createUser(EUserType.Participant, testUserEmail);
        userBean.setId(testUser.getId());
        userBean.setEmail(testUserEmail);
        userBean.setPassword(password);
        testUser.setEmail(userBean.getEmail());
        testUser.setPassword(userBean.getPassword());
        testUser.setCountry(userBean.getCountry());
        testUser.setZip(userBean.getZip());
        testUser.setStreet(userBean.getStreet());
        testUser.setCity(userBean.getCity());
        testUser.setWebsite(userBean.getWebsite());
        testUser.setPhone(userBean.getPhone());
        testUser.setFax(userBean.getFax());
        testUser.setVisible(userBean.isVisible());
        persistenceProvider.getUserStore().save(testUser);
        
        testUser = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        assertNotNull(testUser);
        
        //mock mailservice
        MailService mailService = mock(MailService.class);
        userAction.setMailService(mailService);
        when(mailService.sendMail(anyString(), anyString(), anyString())).thenReturn(true);
        
        //Test
        sessionData.setUserId(testUser.getId());
        userAction.setSession(sessionData);
        String s = userAction.sendPasswordRecoveryLink();
        assertNull(s);
        
        // delete user after test
        User user = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        userBean.setId(user.getId());
        userAction.deleteUser();
        user = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(user);
    }
    
    /**
     * This test checks if a reseted password is really reseted.
     * @throws InconsistencyException
     */
    @Test
    public void testSaveNewPassword() throws InconsistencyException {
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        userAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
             
        String testUserEmail = "tester@mailinator.com";
        String password1 = "Test123";
        String password2 = "Test456";
        
        // delete possible existing user
        User testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);

        if (testUser != null) {
        	userBean.setId(testUser.getId());
            userAction.deleteUser();
        }
        testUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(testUser);

        // create User
        testUser = createUser(EUserType.Participant, testUserEmail);
        userBean.setId(testUser.getId());
        userBean.setEmail(testUserEmail);
        userBean.setPassword(password1);
        testUser.setEmail(userBean.getEmail());
        testUser.setPassword(userBean.getPassword());
        testUser.setCountry(userBean.getCountry());
        testUser.setZip(userBean.getZip());
        testUser.setStreet(userBean.getStreet());
        testUser.setCity(userBean.getCity());
        testUser.setWebsite(userBean.getWebsite());
        testUser.setPhone(userBean.getPhone());
        testUser.setFax(userBean.getFax());
        testUser.setVisible(userBean.isVisible());
        persistenceProvider.getUserStore().save(testUser);
        
        testUser = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        assertNotNull(testUser);
        assertEquals(SecurityUtils.hash(password1), testUser.getPasswordHash());
        
        // Reset users password
        sessionData.setUserId(testUser.getId());
        ResetPasswordBean pBean = new ResetPasswordBean();
        pBean.setEmail(testUser.getEmail());
        Calendar c = new GregorianCalendar();
        String hash = userAction.generateHash(c, testUser);
        pBean.setHash(hash);
        pBean.setPassword(password2);
        pBean.setPasswordRepeat(password2);
        userAction.setResetPasswordBean(pBean);
        userAction.setSession(sessionData);
        String s = userAction.saveNewPassword();
        
        // check if correct site is redirected
        assertEquals(LOGIN_FACELET, s);
        
        // check new password
        User checkUser = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertEquals(SecurityUtils.hash(password2), checkUser.getPasswordHash());

        // delete user after test
        User user = persistenceProvider.getUserStore().getByEmail(testUserEmail);
        userBean.setId(user.getId());
        userAction.deleteUser();
        user = persistenceProvider.getUserStore().findByEmail(testUserEmail);
        assertNull(user);
    }    
}
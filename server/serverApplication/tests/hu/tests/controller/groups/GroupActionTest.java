package hu.tests.controller.groups;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.groups.GroupAction;
import hu.controller.users.UserAction;
import hu.model.Group;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functionality of the {@link GroupAction} controller.
 */
public class GroupActionTest {

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static SessionData sessionData;
    private static GroupBean groupBean;
    private static UserListBean userListBean;
    private static GroupAction groupAction;

    // User > Tutor
    private static User tutor;
    private final static String tutorFirstName = "Hans";
    private final static String tutorLastName = "Wurst";
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
    private final static boolean tutorVisible = true;

    // User > Participant
    private static User participant;
    private final static String participantFirstName = "Emma";
    private final static String participantLastName = "Käse";
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
    private final static boolean participantVisible = true;

 // User > Administrator
    private static User admin;
    private final static String adminFirstName = "Sepp";
    private final static String adminLastName = "Meier";
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
    
    // Attributes of an group
    private final static String groupTitle = "Fun with Flags";
    private final static String groupTitleNew = "Fun with two Flags";
    private final static boolean groupVisibility = true;
    private final static boolean groupVisibilityNew = false;
    
    private static UserBean userBean;
    private static UserAction userAction;

    /**
     * 
     * @throws InconsistencyException
     */
    @BeforeClass
    public static void prepare() throws InconsistencyException {   	
    	
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
        tutor.setVisible(tutorVisible);
        
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
        participant.setVisible(participantVisible);
        
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
        
        // Set up persistence
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);

        // Create users in DB
        tutor = persistenceProvider.getUserStore().create(tutor);
        participant = persistenceProvider.getUserStore().create(participant);
        admin = persistenceProvider.getUserStore().create(admin);

        // Set up controller
        sessionData = new SessionData();
        sessionData.setUserId(admin.getId());
        groupBean = new GroupBean();
        groupBean.setTitle(groupTitle);
        groupBean.setVisible(groupVisibility);
        userListBean = new UserListBean();

        userBean = new UserBean();
        
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
        
        groupAction = new GroupAction();
        groupAction.setSessionData(sessionData);
        groupAction.setPersistenceProvider(persistenceProvider);
        groupAction.setGroupBean(groupBean);
        groupAction.setUserListBean(userListBean);
    }

    /**
     * 
     * @throws InconsistencyException
     */
    @Test
    public void testCreateGroup() throws InconsistencyException {
    	
        SessionData sessionData = new SessionData();
        sessionData.setUserId(tutor.getId());
        GroupBean groupBean = new GroupBean();
        groupBean.setTitle(groupTitle);
        groupBean.setVisible(groupVisibility);
        UserListBean userListBean = new UserListBean();

        GroupAction groupAction = new GroupAction();
        groupAction.setSessionData(sessionData);
        groupAction.setPersistenceProvider(persistenceProvider);
        groupAction.setGroupBean(groupBean);
        groupAction.setUserListBean(userListBean);
        
        // Prepare FacesContext Mock
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        groupAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
       
        groupAction.createGroup();
        
        assertNotNull(persistenceProvider.getGroupStore().findById(
                groupBean.getId()));
        
        
        persistenceProvider.getGroupStore().delete(groupBean.getId());
        
        assertNull(persistenceProvider.getGroupStore().findById(
        		groupBean.getId()));
    }

    /**
     * @throws InconsistencyException
     * 
     */
    @Test
    public void testEditGroup() throws InconsistencyException {
        
        Group group = new Group(null);
        group.setTitle(groupTitle);
        group.setVisible(groupVisibility);
        group = persistenceProvider.getGroupStore()
                .create(group, tutor.getId());
        assertNotNull(persistenceProvider.getGroupStore().findById(
        		group.getId()));
        groupBean.setId(group.getId());

        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        groupAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // Test form prepopulation
        groupBean.setTitle(null);
        groupBean.setVisible(!groupVisibility);
        groupAction.editGroup();
        assertEquals(group.getId(), groupBean.getId());
        assertEquals(groupTitle, groupBean.getTitle());
        assertEquals(groupVisibility, groupBean.isVisible());

        // Test tutor prepopulation
        List<User> users = persistenceProvider.getUserStore().getByOwnership(
        		groupBean.getId());
        List<User> tutors = new LinkedList<User>();
        for (UserListEntryBean u : userListBean.getList()) {
            tutors.add(new User(u.getUser().getId()));
        }
        assertTrue(tutors.containsAll(users));

        // Prepare FacesContext Mock for editing
        when(cxtMock.isPostback()).thenReturn(true);

        // Test editing
        groupBean.setTitle(groupTitleNew);
        groupBean.setVisible(groupVisibilityNew);
        groupAction.editGroup();
        Group editedGroup = persistenceProvider.getGroupStore().getById(
        		groupBean.getId());
        assertEquals(group.getId(), editedGroup.getId());
        assertEquals(groupTitleNew, editedGroup.getTitle());
        assertEquals(groupVisibilityNew, editedGroup.isVisible());

        // Test permission check
        sessionData.setUserId(participant.getId());
        groupAction.editGroup();
        assertEquals("/xhtml/errors/restrictionError",
        		groupAction.getLastRedirect());
        
        sessionData.setUserId(admin.getId());
        groupAction.deleteGroup();
        assertNull(persistenceProvider.getGroupStore().findById(
        		groupBean.getId()));
    }

  
    /**
     * @throws InconsistencyException
     */
    @Test
    public void testDeleteGroup() throws InconsistencyException {
        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        groupAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
        
        when(cxtMock.isPostback()).thenReturn(true);
        
       
        if (persistenceProvider.getUserStore().findByEmail("tutorEmail@de.de") != null) {
        	userBean.setId(persistenceProvider.getUserStore().findByEmail("tutorEmail@de.de").getId());
            userAction.deleteUser();
            assertNull(persistenceProvider.getUserStore().findByEmail("tutorEmail@de.de"));
        }

        User tutor1 = new User(null);
        tutor1.setFirstName(tutorFirstName);
        tutor1.setLastName(tutorLastName);
        tutor1.setTitle(tutorTitle);
        tutor1.setPasswordHash(tutorPassword);
        tutor1.setEmail("tutorEmail@de.de");
        tutor1.setUserType(tutorType);
        tutor1.setGender(tutorGender);
        tutor1.setBirthday(tutorBirthday);
        tutor1.setCountry(tutorCountry);
        tutor1.setStreet(tutorStreet);
        tutor1.setZip(tutorZip);
        tutor1.setCity(tutorCity);
        tutor1.setPhone(tutorPhone);
        tutor1.setFax(tutorFax);
        tutor1.setWebsite(tutorWebsite);
        tutor1.setVisible(tutorVisible);
        int noOwnerTutorId 
            = persistenceProvider.getUserStore().create(tutor1).getId();
        
        // create group
        sessionData.setUserId(tutor.getId());
        Group group = new Group(null);
        group.setTitle("jufe");
        group.setVisible(true);
        group = persistenceProvider.getGroupStore().create(group, tutor.getId());
        groupBean.setId(group.getId());

        // delete group as attendant
        sessionData.setUserId(participant.getId());
        groupAction.deleteGroup();
        assertNotNull(persistenceProvider.getGroupStore().findById(
        		group.getId()));

        // delete group as tutor who is not owner
        sessionData.setUserId(noOwnerTutorId);
        groupAction.deleteGroup();
        assertNotNull(persistenceProvider.getGroupStore().findById(
        		group.getId()));

        // delete group
        sessionData.setUserId(tutor.getId());
        groupAction.deleteGroup();
        assertNull(persistenceProvider.getGroupStore().findById(group.getId()));

        // delete not existing group
        when(cxtMock.isPostback()).thenReturn(false);
        groupAction.deleteGroup();
        assertEquals("/xhtml/errors/restrictionError",
        		groupAction.getLastRedirect());
        
        
    	userBean.setId(noOwnerTutorId);
        userAction.deleteUser();
        assertNull(persistenceProvider.getUserStore().findById(noOwnerTutorId));

    }

    /**
     * 
     * @throws InconsistencyException
     */
    @AfterClass
    public static void cleanDB() throws InconsistencyException {
        if (persistenceProvider.getUserStore().findById(tutor.getId()) != null) {
        	int id= tutor.getId();
        	userBean.setId(id);
            userAction.deleteUser();
            assertNull(persistenceProvider.getUserStore().findById(id));
        }

        if (persistenceProvider.getUserStore().findById(participant.getId()) != null) {
        	int id= participant.getId();
        	userBean.setId(id);
            userAction.deleteUser();
            assertNull(persistenceProvider.getUserStore().findById(id));
        }
        
        if (persistenceProvider.getUserStore().findById(admin.getId()) != null) {
        	int id= admin.getId();
        	userBean.setId(id);
            userAction.deleteUser();
            assertNull(persistenceProvider.getUserStore().findById(id));
        }
        persistenceProvider.close();
    }

}

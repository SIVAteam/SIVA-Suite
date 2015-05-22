package hu.tests.controller.groups;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.groups.GroupAttendanceAction;
import hu.model.ESortDirection;
import hu.model.Group;
import hu.model.users.EGender;
import hu.model.users.ESortColumnUser;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.InconsistencyException;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.SecurityUtils;
import hu.util.SessionData;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link GroupAttendanceAction}.
 * Running this test requires that max_rows_per_table is set to 20.
 */


public class GroupAttendanceActionTest {
    
    // Globals for all tests.
    private static final String TEST_IN_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static Configuration config;
    private static PgPersistenceProvider pers;

    // Globals for testListAssociatedUsers
    private static Group listGroup;
    private static User listOwner;
    private static List<User> listAttendants;
    private static List<User> listUsers;
    
    // Globals for testAddUser and testRemoveUser
    private static SessionData addRemSessionData;
    private static GroupBean addRemGroupBean;
   // private static UserListBean addUserListBean;
    private static UserBean addUserBean;
    private static GroupAttendanceAction addGroupAttendanceAction;
    private static int addRemTutorId;
    private static int addRemTutorNoOwnerId;
    private static int addAdminId;
    private static int addParticipantOnlyId;
    private static int addParticipantId2;
    private static int addParticipantId3;


    /**
     * Prepare tests: Initialize configuration and persistence.
     */
    @BeforeClass
    public static void prepare() throws Exception {
        config = new Configuration();
        pers = new PgPersistenceProvider(config);
        prepareForAddRemove();
        prepareForList();
    }

    /**
     * Clean up after testing: Close persistence.
     */
    @AfterClass
    public static void cleanup() throws Exception {
        cleanupForAddRemove();
        cleanupForList();

        // Close persistence last, since the methods above may need it.
        pers.close();
    }

    /**
     * @throws InconsistencyException 
     */
    @Test
    public void testAddUser() throws InconsistencyException {
     // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        addGroupAttendanceAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
      //for permission check
        
        when(cxtMock.isPostback()).thenReturn(true);
        
        
        Group group = new Group(null);
        group.setTitle("addUserTest");
        group.setVisible(true);
        
        group = pers.getGroupStore().create(group, addRemTutorId);
        addRemGroupBean.setId(group.getId());
           
        //self sign up
        addRemSessionData.setUserId(addParticipantId2);
        
        addUserBean.setId(null);
        addGroupAttendanceAction.addUser();
        assertTrue(pers.getGroupStore()
                .getByAttendant(addParticipantId2).contains(group));
        
        //self sign up, already signed up
        when(cxtMock.isPostback()).thenReturn(false);
        addGroupAttendanceAction.addUser();
        assertEquals("/xhtml/errors/restrictionError",
                addGroupAttendanceAction.getLastRedirect());
        
        //self sign up, tutor of group
        addRemSessionData.setUserId(addRemTutorId);
        addGroupAttendanceAction.addUser();
        assertEquals("/xhtml/errors/restrictionError",
                addGroupAttendanceAction.getLastRedirect());
        
        //add user who is already attendant
        addUserBean.setId(addParticipantId2);
        addGroupAttendanceAction.addUser();
        assertEquals("/xhtml/errors/restrictionError",
                addGroupAttendanceAction.getLastRedirect());
        
        //add user who is already tutor
        addRemSessionData.setUserId(addAdminId);
        addUserBean.setId(addRemTutorId);
        addGroupAttendanceAction.addUser();
        assertEquals("/xhtml/errors/restrictionError",
                addGroupAttendanceAction.getLastRedirect());
        
        //add user as tutor who is no owner
        addRemSessionData.setUserId(addRemTutorNoOwnerId);
        addUserBean.setId(addParticipantId3);
        addGroupAttendanceAction.addUser();
        assertEquals("/xhtml/errors/restrictionError",
                addGroupAttendanceAction.getLastRedirect());
        
        //add user as tutor who is owner
        when(cxtMock.isPostback()).thenReturn(true);
        addRemSessionData.setUserId(addRemTutorId);
        addGroupAttendanceAction.addUser();
        assertTrue(pers.getGroupStore()
                .getByAttendant(addParticipantId3).contains(group));
        
        //add user as admin
        addRemSessionData.setUserId(addAdminId);
        addUserBean.setId(addParticipantOnlyId);
        addGroupAttendanceAction.addUser();
        assertTrue(pers.getGroupStore()
                .getByAttendant(addParticipantOnlyId).contains(group));
        
        //delete created user and group
        pers.getGroupStore().delete(group.getId());        
       
    }

    /**
     * @throws InconsistencyException 
     */
    private static void prepareForAddRemove() throws InconsistencyException {
        
        addRemSessionData = new SessionData();
        addRemGroupBean = new GroupBean();
        addUserBean = new UserBean();
        addGroupAttendanceAction = new GroupAttendanceAction();
        addGroupAttendanceAction.setGroupBean(addRemGroupBean);
        addGroupAttendanceAction.setSessionData(addRemSessionData);
        addGroupAttendanceAction.setUserBean(addUserBean);
        addGroupAttendanceAction.setPersistenceProvider(pers);
        
        for(int i = 0; i <6; i++){
        User tutor = new User(null);
        tutor.setCountry(ECountry.Germany);
        tutor.setBanned(false);
        tutor.setBirthday(new Date());
        tutor.setEmail(String.format("addTutor%02d@test.de",i));
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
        tutor.setWebsite("www.musterweb.de");
        tutor.setVisible(true);
        pers.getUserStore().create(tutor);
        }
        
        
        User user1 = new User(null);
        user1 = pers.getUserStore().findByEmail("addTutor00@test.de");
        user1.setUserType(EUserType.Participant);
       
        
        addParticipantOnlyId = pers.getUserStore().save(user1).getId();
        
        User user2 = new User(null);
        user2 = pers.getUserStore().findByEmail("addTutor01@test.de");
        user2.setUserType(EUserType.Administrator);
        addAdminId = pers.getUserStore().save(user2).getId();
        
        addRemTutorId = pers.getUserStore().findByEmail("addTutor04@test.de").getId();
        addRemTutorNoOwnerId = pers.getUserStore().findByEmail("addTutor00@test.de").getId();
        addParticipantId2 = pers.getUserStore().findByEmail("addTutor02@test.de").getId();
        addParticipantId3 = pers.getUserStore().findByEmail("addTutor03@test.de").getId();       
    }

    /**
     * @throws InconsistencyException 
     */
    private static void cleanupForAddRemove() throws InconsistencyException {

        for(int i = 0; i < 6; i++) {
            pers.getUserStore().delete(pers.getUserStore().findByEmail(
                    String.format("addTutor%02d@test.de",i)));
        }       
    }

    /**
     * @throws InconsistencyException 
     */
    @Test
    public void testRemoveUser() throws InconsistencyException {
        
        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        
        addGroupAttendanceAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);
      //for permission check
        
        when(cxtMock.isPostback()).thenReturn(true);
      
        Group group = new Group(null);
        group.setTitle("removeUserTest");
        group.setVisible(true);
        
        group = pers.getGroupStore().create(group, addRemTutorId);
        pers.getGroupStore().addAttendant(group.getId(), addParticipantOnlyId);
        pers.getGroupStore().addAttendant(group.getId(), addParticipantId2);
        pers.getGroupStore().addAttendant(group.getId(), addParticipantId3);
  
        addRemGroupBean.setId(group.getId());

        //self sign off
        addRemSessionData.setUserId(addParticipantOnlyId);
        
        addUserBean.setId(null);
        addGroupAttendanceAction.removeUser();
        assertFalse(pers.getGroupStore()
                .getByAttendant(addParticipantOnlyId).contains(group));
        
        //self sign up, already signed off
        when(cxtMock.isPostback()).thenReturn(false);
        addGroupAttendanceAction.removeUser();
        assertEquals("/xhtml/errors/restrictionError", addGroupAttendanceAction.getLastRedirect());      
        
        //remove user who is already removed
        addRemSessionData.setUserId(addRemTutorId);
        addUserBean.setId(addParticipantOnlyId);
        addGroupAttendanceAction.removeUser();
        assertEquals("/xhtml/errors/restrictionError", addGroupAttendanceAction.getLastRedirect()); 
        
        //remove user as tutor who is no owner
        addRemSessionData.setUserId(addRemTutorNoOwnerId);
        addUserBean.setId(addParticipantId2);
        addGroupAttendanceAction.removeUser();
        assertEquals("/xhtml/errors/restrictionError", addGroupAttendanceAction.getLastRedirect()); 
        
        //remove user as tutor who is owner
        when(cxtMock.isPostback()).thenReturn(true);
        addRemSessionData.setUserId(addRemTutorId);
        addGroupAttendanceAction.removeUser();
        assertFalse(pers.getGroupStore()
                .getByAttendant(addParticipantId2).contains(group));
        
        //remove user as admin
        addRemSessionData.setUserId(addAdminId);
        addUserBean.setId(addParticipantId3);
        addGroupAttendanceAction.removeUser();
        assertFalse(pers.getGroupStore()
                .getByAttendant(addParticipantId3).contains(group));
        
        //delete created user and group
        pers.getGroupStore().delete(group.getId());
    }


    /**
     * Test {@link GroupAttendanceAction#listAssociatedUsers()}.
     */
    @Test
    public void testListAssociatedUsers() {
        UserListBean ulb;
        List<User> fetchedAttendants = new ArrayList<User>();
        List<User> fetchedAddable = new ArrayList<User>();
        List<User> fetchedRemovable = new ArrayList<User>();

        // Fetch all users in the table.
        int page = 0;
        while (true) {
            ulb = listAssociatedUsers(listOwner.getId(),
                    listGroup.getId(), page, "", ESortColumnUser.Name,
                    ESortDirection.ASC, null);
            
            for (UserListEntryBean uleb : ulb.getList()) {
                User user = uleb.getUser();
                
                // Filter relevant users.
                String fullName = user.getFirstName() + " " + user.getLastName();
                if (!fullName.contains("Attendance TestAttendant")
                        && !fullName.contains("Attendance TestUser")) {
                    continue;
                }

                // Put each users into buckets.
                if (uleb.isAdded()) {
                    fetchedAttendants.add(user);
                }
                if (uleb.isAddable()) {
                    fetchedAddable.add(user);
                }
                if (uleb.isRemovable()) {
                    fetchedRemovable.add(user);
                }
            }
            
            if (ulb.getPage() == ulb.getPages()-1) {
                break;
            }
            page++;
        }

        // Check size of buckets.
        assertEquals(listAttendants.size(), fetchedAttendants.size());
        assertEquals(listAttendants.size(), fetchedRemovable.size());
        assertEquals(listUsers.size(), fetchedAddable.size());

        // Check contents of buckets.
        for (int i = 0; i < listAttendants.size(); i++) {
            assertEquals(listAttendants.get(i), fetchedAttendants.get(i));
            assertEquals(listAttendants.get(i), fetchedRemovable.get(i));
        }
        for (int i = 0; i < listUsers.size(); i++) {
            assertEquals(listUsers.get(i), fetchedAddable.get(i));
        }
    }

    /**
     * Create test data for testing the list.
     */
    private static void prepareForList() throws Exception {
        cleanupForList();
    
        listGroup = new Group(null);
        listGroup.setTitle("AttendanceTestGroup");
        listGroup.setVisible(true);
        
        listOwner = new User(null);
        listOwner.setBanned(false);
        listOwner.setBirthday(new Date());
        listOwner.setDeletable(true);
        listOwner.setEmail("attendance.test.owner@mailinator.com");
        listOwner.setFirstName("Attendance");
        listOwner.setLastName("TestOwner");
        listOwner.setGender(EGender.Male);
        listOwner.setPasswordHash(TEST_IN_SHA256);
        listOwner.setTitle(null);
        listOwner.setUserType(EUserType.Tutor);
        listOwner.setCountry(ECountry.Germany);
        listOwner.setStreet("Neuestraße");
        listOwner.setZip("50463");
        listOwner.setCity("Weisswurststadt");
        listOwner.setPhone("06785463");
        listOwner.setFax("06785464");
        listOwner.setWebsite("www.testen.de");
        listOwner.setVisible(false);
        listOwner = pers.getUserStore().create(listOwner);
        listGroup = pers.getGroupStore().create(listGroup, listOwner);
    
        listAttendants = new LinkedList<User>();
        for (int i = 1; i <= 15; i++) {
            User attendant = new User(null);
            attendant.setBanned(false);
            attendant.setBirthday(new Date());
            attendant.setDeletable(true);
            attendant.setEmail(String.format("attendance.test.attendant%02d@mailinator.com", i));
            attendant.setFirstName("Attendance");
            attendant.setLastName(String.format("TestAttendant%02d", i));
            attendant.setGender(EGender.Male);
            attendant.setPasswordHash(TEST_IN_SHA256);
            attendant.setTitle(null);
            attendant.setCountry(ECountry.Austria);
            attendant.setStreet("Altestraße");
            attendant.setZip("5063");
            attendant.setCity("Wienerstadt");
            attendant.setPhone("678463");
            attendant.setFax("678464");
            attendant.setWebsite("www.testen.at");
            attendant.setUserType(EUserType.Participant);
            attendant.setVisible(true);
            attendant = pers.getUserStore().create(attendant);
            pers.getGroupStore().addAttendant(listGroup, attendant);
            listAttendants.add(attendant);
        }
        
        listUsers = new LinkedList<User>();
        for (int i = 1; i <= 15; i++) {
            User user = new User(null);
            user.setBanned(false);
            user.setBirthday(new Date());
            user.setDeletable(true);
            user.setEmail(String.format("attendance.test.user%02d@mailinator.com", i));
            user.setFirstName("Attendance");
            user.setLastName(String.format("TestUser%02d", i));
            user.setGender(EGender.Male);
            user.setPasswordHash(TEST_IN_SHA256);
            user.setTitle(null);
            user.setStreet("Mühlenstraße");
            user.setZip("5063");
            user.setCity("Teststadt");
            user.setPhone("67846356");
            user.setFax("67846357");
            user.setWebsite("www.testen.ch");
            user.setCountry(ECountry.Switzerland);
            user.setUserType(EUserType.Participant);
            user.setVisible(true);
            user = pers.getUserStore().create(user);
            listUsers.add(user);
        }
    }

    /**
     * Remove the test data from the database.
     */
    private static void cleanupForList() throws Exception {
        if (listGroup == null) {
            List<Group> groups = pers.getGroupStore().getAll(pers.getUserStore().findById(1));
            for (Group group : groups) {
                if (group.getTitle().equals("AttendanceTestGroup")) {
                    listGroup = group;
                    break;
                }
            }
        }

        if (listGroup != null) {
            pers.getGroupStore().delete(listGroup);
        }

        if (listOwner == null) {
            listOwner = pers.getUserStore().findByEmail(
                    "attendance.test.owner@mailinator.com");
        }

        if (listOwner != null) {
            pers.getUserStore().delete(listOwner);
        }

        if (listAttendants == null) {
            listAttendants = new LinkedList<User>();
            for (int i = 0; i <= 15; i++) {
                User user = pers.getUserStore().findByEmail(
                        String.format(
                                "attendance.test.attendant%02d@mailinator.com",
                                i));
                if (user != null) {
                    listAttendants.add(user);
                }
            }
        }

        if (listAttendants != null) {
            for (User attendant : listAttendants) {
                pers.getUserStore().delete(attendant);
            }
        }

        if (listUsers == null) {
            listUsers = new LinkedList<User>();
            for (int i = 0; i <= 15; i++) {
                User user = pers.getUserStore().findByEmail(
                        String.format(
                                "attendance.test.user%02d@mailinator.com", i));
                if (user != null) {
                    listUsers.add(user);
                }
            }
        }

        if (listUsers != null) {
            for (User user : listUsers) {
                pers.getUserStore().delete(user);
            }
        }
    }

    /**
     * Invoke {@link GroupAttendanceAction#listAssociatedUsers()}.
     */
    private static UserListBean listAssociatedUsers(Integer userId,
            Integer groupId, Integer page, String searchQuery,
            ESortColumnUser sortColumn, ESortDirection sortDirection,
            EUserType userType) {
        SessionData session = new SessionData();
        session.setUserId(userId);

        GroupBean gb = new GroupBean();
        gb.setId(groupId);

        UserListBean ulb = new UserListBean();
        ulb.setPage(page);
        ulb.setSearchQuery(searchQuery);
        ulb.setSortColumn(sortColumn);
        ulb.setSortDirection(sortDirection);
        ulb.setUserType(userType);

        FacesContext fctxMock = mock(FacesContext.class);
        when(fctxMock.isPostback()).thenReturn(false);

        GroupAttendanceAction ctrl = new GroupAttendanceAction();
        ctrl.setMock(fctxMock);
        ctrl.setConfiguration(config);
        ctrl.setGroupBean(gb);
        ctrl.setPersistenceProvider(pers);
        ctrl.setSessionData(session);
        ctrl.setUserListBean(ulb);

        ctrl.listAssociatedUsers();

        return ulb;
    }
}

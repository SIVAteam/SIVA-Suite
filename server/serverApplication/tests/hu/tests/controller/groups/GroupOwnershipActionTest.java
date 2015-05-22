package hu.tests.controller.groups;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.groups.GroupBean;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.groups.GroupAction;
import hu.controller.groups.GroupOwnershipAction;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the functionality of the {@link GroupOwnershipAction}
 * controller.
 */

public class GroupOwnershipActionTest {

    private static ArrayList<User> users = new ArrayList<User>();

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static SessionData sessionData;
    private static GroupBean groupBean;
    private static GroupBean listGroupBean;
    private static UserBean userBean;
    private static UserListBean userListBean;
    private static GroupAction groupAction;
    private static GroupOwnershipAction groupOwnershipAction;
    
    // User data
    private final static String title = "Dr.";
    private final static String password = "Test!123";
    private final static String firstName = "Max";
    private final static String lastName = "Muster";
    private final static Date birthday = new Date();
    private final static EGender gender = EGender.Male;
    private final static boolean banned = false;
    private final static ECountry country = ECountry.Germany;
    private final static String street = "Musterstrasse";
    private final static String zip = "55555";
    private final static String city = "Musterstadt";
    private final static String phone = "0151555";
    private final static String fax = "0151554";
    private final static String website = "www.musterweb.de";
    private final static boolean visible = true;
    // IDs of logged in users
    private final static int tutorId = 3;
    private static int sessionId;

    // Attributes of an group
    private final static String groupTitle = "Fun with Flags";
    private final static boolean groupVisibility = true;

    // Needed for testing

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void prepare() throws Exception {
        prepareList();
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);
        sessionData = new SessionData();
        sessionData.setUserId(tutorId);
        groupBean = new GroupBean();
        groupBean.setTitle(groupTitle);
        groupBean.setVisible(groupVisibility);
        userBean = new UserBean();
        userListBean = new UserListBean();
        groupOwnershipAction = new GroupOwnershipAction();
        groupOwnershipAction.setPersistenceProvider(persistenceProvider);

        groupAction = new GroupAction();
        groupAction.setSessionData(sessionData);
        groupAction.setPersistenceProvider(persistenceProvider);
        groupAction.setGroupBean(groupBean);
        groupAction.setUserListBean(userListBean);
    }

    /**
    private final static EUserType type = EUserType.Administrator;
     * 
     * @throws InconsistencyException
     */
    @Test
    public void testAddUser() throws InconsistencyException {
        // delete possible existing user with email
        User userExists = persistenceProvider.getUserStore().findByEmail(
                "user@some.mail");

        if (userExists != null) {
            persistenceProvider.getUserStore().delete(userExists);
        }
        userExists = persistenceProvider.getUserStore().findByEmail(
                "user@some.mail");
        assertNull(userExists);

        // delete possible existing user with email
        User userExistsAgain = persistenceProvider.getUserStore().findByEmail(
                "anotherUser@email.com");

        if (userExistsAgain != null) {
            persistenceProvider.getUserStore().delete(userExistsAgain);
        }
        userExists = persistenceProvider.getUserStore().findByEmail(
                "anotherUser@email.com");
        assertNull(userExists);
        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        groupAction.setMock(cxtMock);
        groupOwnershipAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // create test data
        User owner = createUser(EUserType.Tutor,
                "user@some.mail");
        Group group = createGroup(owner);

        User anotherOwner = createUser(EUserType.Tutor,
                "anotherUser@email.com");
        userBean.setId(anotherOwner.getId());
        assertTrue(!checkIfOwner(anotherOwner, group));
        groupOwnershipAction.setUserBean(userBean);
        groupBean.setId(group.getId());
        groupOwnershipAction.setGroupBean(groupBean);
        groupOwnershipAction.addUser();

        assertTrue(checkIfOwner(anotherOwner, group));

        // delete users and group
        deleteGroup(group);
        deleteUser(owner);
        deleteUser(anotherOwner);
    }

    @Test
    public void testDeleteUser() throws InconsistencyException {
        // delete possible existing user with email
        User userExists = persistenceProvider.getUserStore().findByEmail(
                "user@some.mail");

        if (userExists != null) {
            persistenceProvider.getUserStore().delete(userExists);
        }
        userExists = persistenceProvider.getUserStore().findByEmail(
                "user@some.mail");
        assertNull(userExists);

        // delete possible existing user with email
        User userExistsAgain = persistenceProvider.getUserStore().findByEmail(
                "anotherUser@email.com");

        if (userExistsAgain != null) {
            persistenceProvider.getUserStore().delete(userExistsAgain);
        }
        userExists = persistenceProvider.getUserStore().findByEmail(
                "anotherUser@email.com");
        assertNull(userExists);
        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        groupAction.setMock(cxtMock);
        groupOwnershipAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // create and set test data
        User owner = createUser(EUserType.Tutor,
                "user@some.mail");
        Group group = createGroup(owner);

        User anotherOwner = createUser(EUserType.Tutor,
                "anotherUser@email.com");
        userBean.setId(anotherOwner.getId());
        assertTrue(!checkIfOwner(anotherOwner, group));
        groupOwnershipAction.setUserBean(userBean);
        groupOwnershipAction.setGroupBean(groupBean);

        // add owner to group
        groupBean.setId(group.getId());
        groupOwnershipAction.addUser();

        assertTrue(checkIfOwner(anotherOwner, group));

        // delete owner from group
        userBean.setId(anotherOwner.getId());
        groupOwnershipAction.removeUser();

        assertTrue(!checkIfOwner(anotherOwner, group));

        // delete users and group
        deleteGroup(group);
        deleteUser(owner);
        deleteUser(anotherOwner);
    }

    /**
     * 
     * @param user
     * @param group
     * @return
     */
    private boolean checkIfOwner(User user, Group group) {
        java.util.List<Group> list = persistenceProvider.getGroupStore()
                .getByOwner(user);

        for (Group g : list) {
            if (g.getId().equals(group.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param user
     * @return
     * @throws InconsistencyException
     */
    private Group createGroup(User user) throws InconsistencyException {
    	Group group = new Group(null);
        group.setTitle(groupTitle);
        group.setVisible(groupVisibility);
        group = persistenceProvider.getGroupStore().create(group, user.getId());
        assertNotNull(persistenceProvider.getGroupStore().findById(
                group.getId()));
        return group;
    }

    /**
     * 
     * @param group
     * @throws InconsistencyException
     */
    private void deleteGroup(Group group) throws InconsistencyException {
        persistenceProvider.getGroupStore().delete(group);
    }

    /**
     * Prepare testing: Check that the database is clean and insert test data.
     * 
     * @throws Exception
     */
    public static void prepareList() throws Exception {
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);

        assertEquals(20,
                (int) configuration.getInteger("max_rows_per_table"));

        users = new ArrayList<User>();
        
        // Add test users to current user list
        int setNewAdmins = 2;
        int setNewDummies = 1;
        int setNewTutors = 7;
        for (int i = 0; i < setNewAdmins; i++) {
            addUserToDatabase(i, "Admin", "Prof.", EUserType.Administrator,
                    EGender.Male);
        }

        for (int i = 0; i < setNewDummies; i++) {
            addUserToDatabase(i, "Dummy", "Dr.", EUserType.Tutor, EGender.Male);
        }

        for (int i = 0; i < setNewTutors; i++) {
            addUserToDatabase(i, "Tutor", "Dr.", EUserType.Tutor,
                    EGender.Female);
        }

        User tutor = new User(null);
        tutor.setFirstName("Max"+"GroupOwnership" );
        tutor.setLastName("Musterman"+"GroupOwnership");
        tutor.setTitle(null);
        tutor.setBanned(false);
        tutor.setDeletable(true);
        tutor.setUserType(EUserType.Administrator);
        tutor.setGender(EGender.Male);
        tutor.setEmail("test"+"GroupOwnership"+ "@test.com");
        tutor.setBirthday(new Date());
        tutor.setCountry(ECountry.Switzerland);
        tutor.setStreet("Kindlistraße");
        tutor.setZip("9945");
        tutor.setCity("Wunderstadt");
        tutor.setPhone("65784");
        tutor.setFax("5666");
        tutor.setWebsite("www.test.ch");
        tutor.setPasswordHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        tutor.setVisible(visible);
        tutor = persistenceProvider.getUserStore().create(tutor);
        users.add(tutor);

        sessionId = tutor.getId();

        Group group = new Group(null);
        group.setTitle(groupTitle);
        group.setVisible(groupVisibility);
        group = persistenceProvider.getGroupStore()
                .create(group, tutor.getId());
        listGroupBean = new GroupBean();
        listGroupBean.setId(group.getId());
        listGroupBean.setTitle(group.getTitle());
        listGroupBean.setVisible(groupVisibility);

    }

    private static void addUserToDatabase(int i, String testName,
            String testTitle, EUserType testType, EGender testGender)
            throws Exception {
        assertEquals(null, persistenceProvider.getUserStore()
                .findByEmail("us." + testName + "@mailinator.com" + i));

        User user = new User(null);
        user.setBanned(i % 2 == 0);
        user.setCountry(ECountry.Germany);
        user.setBirthday(new Date());
        user.setDeletable(true);
        user.setEmail("us." + testName +"GroupOwnership" +"@mailinator.com" + i);
        user.setFirstName("Us" + testName +"GroupOwnership" + "F" + i);
        user.setLastName("Us" + testName +"GroupOwnership" +"N" + i);
        user.setPasswordHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        user.setTitle(testTitle);
        user.setUserType(testType);
        user.setGender(testGender);
        user.setStreet("Teststraße");
        user.setZip("19953");
        user.setCity("Teststadt");
        user.setPhone("7865784");
        user.setFax("785666");
        user.setWebsite("www.test.de");
        user.setVisible(true);
        user = persistenceProvider.getUserStore().create(user);
        users.add(i, user);
    }
   
    private static String sortByName(ArrayList<User> users,
            ESortDirection direction) {
        if (direction != null && direction == ESortDirection.DESC) {
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User entry1, User entry2) {
                    String name1 = entry1.getLastName()
                            + entry1.getFirstName()
                            + ((entry1.getTitle() != null) ? entry1.getTitle()
                                    : "");
                    String name2 = entry2.getLastName()
                            + entry2.getFirstName()
                            + ((entry2.getTitle() != null) ? entry2.getTitle()
                                    : "");
                    return name2.compareTo(name1);
                }
            });
        } else {
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User entry1, User entry2) {
                    String name1 = entry1.getLastName()
                            + entry1.getFirstName()
                            + ((entry1.getTitle() != null) ? entry1.getTitle()
                                    : "");
                    String name2 = entry2.getLastName()
                            + entry2.getFirstName()
                            + ((entry2.getTitle() != null) ? entry2.getTitle()
                                    : "");
                    return name1.compareTo(name2);
                }
            });
        }
        return null;
    }
  
    private static String sortByEmail(ArrayList<User> users,
            ESortDirection direction) {
        if (direction != null && direction == ESortDirection.DESC) {
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User entry1, User entry2) {
                    return entry2.getEmail().compareTo(entry1.getEmail());
                }
            });
        } else {
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User entry1, User entry2) {
                    return entry1.getEmail().compareTo(entry2.getEmail());
                }
            });
        }
        return null;
    }

    /**
     * Delete test data from the database.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void destroyList() throws Exception {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).isDeletable()) {
                persistenceProvider.getUserStore().delete(users.get(i));
            }
        }
        persistenceProvider.getGroupStore().delete(listGroupBean.getId());
        persistenceProvider.close();
    }

    /**
     * Test the list of tutors sorted by name.
     */
    @Test
    public void testTutorsSortedByName() {
        ArrayList<User> tutors = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Tutor) {
                tutors.add(users.get(i));
            }
        }
        matchTutors(tutors, ESortColumnUser.Name, "");
    }

    /**
     * Test the list of administrators sorted by name.
     */
    @Test
    public void testAdministratorsSortedByName() {
        ArrayList<User> administrators = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Administrator) {
                administrators.add(users.get(i));
            }
        }
        matchAdministrators(administrators, ESortColumnUser.Name, "");
    }

    /**
     * Test the list of tutors sorted by email.
     */

    @Test
    public void testTutorsSortedByEmail() {
        ArrayList<User> tutors = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Tutor) {
                tutors.add(users.get(i));
            }
        }
        matchTutors(tutors, ESortColumnUser.Email, "");
    }

    /**
     * Test the list of administrators sorted by email.
     */
    @Test
    public void testAdministratorsSortedByEmail() {
        ArrayList<User> administrators = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Administrator) {
                administrators.add(users.get(i));
            }
        }
        matchAdministrators(administrators, ESortColumnUser.Email, "");
    }

    /**
     * Test the search for tutors sorted by name.
     */
    @Test
    public void testSearchTutorsSortedByName() {
        String searchQuery = "UsTutorL0";
        ArrayList<User> tutors = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Tutor
                    && users.get(i).getLastName().equals(searchQuery)) {
                tutors.add(users.get(i));
            }
        }
        matchTutors(tutors, ESortColumnUser.Name, searchQuery);
    }

    /**
     * Test the search for administrators sorted by name.
     */
    @Test
    public void testSearchAdministratorsSortedByName() {
        String searchQuery = "UsAdminL0";
        ArrayList<User> administrators = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Administrator
                    && users.get(i).getLastName().equals(searchQuery)) {
                administrators.add(users.get(i));
            }
        }
        matchAdministrators(administrators, ESortColumnUser.Name, searchQuery);
    }

    /**
     * Test the search for tutors sorted by email.
     */

    @Test
    public void testSearchTutorsSortedByEmail() {
        String searchQuery = "UsTutorL0";
        ArrayList<User> tutors = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Tutor
                    && users.get(i).getLastName().equals(searchQuery)) {
                tutors.add(users.get(i));
            }
        }
        matchTutors(tutors, ESortColumnUser.Email, searchQuery);
    }

    /**
     * Test the search for administrators sorted by email.
     */
    @Test
    public void testSearchAdministratorsSortedByEmail() {
        String searchQuery = "UsAdminL0";
        ArrayList<User> administrators = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserType() == EUserType.Administrator
                    && users.get(i).getLastName().equals(searchQuery)) {
                administrators.add(users.get(i));
            }
        }
        matchAdministrators(administrators, ESortColumnUser.Email, searchQuery);
    }

    private static UserListBean listUsers(Integer page,
            ESortColumnUser sortColumn, ESortDirection sortDirection,
            String searchQuery, EUserType list) {
        UserListBean ulb = new UserListBean();
        ulb.setPage(page - 1);
        ulb.setSortColumn(sortColumn);
        ulb.setSortDirection(sortDirection);
        ulb.setSearchQuery(searchQuery);
        ulb.setUserType(list);

        SessionData session = new SessionData();
        session.setUserId(sessionId);

        FacesContext fctxMock = mock(FacesContext.class);
        when(fctxMock.isPostback()).thenReturn(false);

        GroupOwnershipAction ctrl = new GroupOwnershipAction();
        ctrl.setSessionData(session);
        ctrl.setGroupBean(listGroupBean);
        ctrl.setMock(fctxMock);
        ctrl.setConfiguration(configuration);
        ctrl.setUserListBean(ulb);
        ctrl.setPersistenceProvider(persistenceProvider);

        ctrl.listAssociatedUsers();

        return ulb;
    }

    private static void matchTutors(ArrayList<User> users,
            ESortColumnUser sortColumn, String searchQuery) {
        UserListBean ulb;

        // Test all users tab in ascending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.ASC);
        } else {
            sortByEmail(users, ESortDirection.ASC);
        }
        List<User> us = new ArrayList<User>();
        ulb = listUsers(1, sortColumn, ESortDirection.ASC, searchQuery,
                EUserType.Tutor);
        for (int i = 1; i < ulb.getPages(); i++) {
            ulb = listUsers(i, sortColumn, ESortDirection.ASC, searchQuery,
                    EUserType.Tutor);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("GroupOwnership" )) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable());
        }

        // Test all users tab in descending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.DESC);
        } else {
            sortByEmail(users, ESortDirection.DESC);
        }
        us = new ArrayList<User>();
        ulb = listUsers(1, sortColumn, ESortDirection.DESC, searchQuery,
                EUserType.Tutor);
        for (int i = 1; i < ulb.getPages(); i++) {
            ulb = listUsers(i, sortColumn, ESortDirection.DESC, searchQuery,
                    EUserType.Tutor);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("GroupOwnership")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable());
        }
    }

    private static void matchAdministrators(ArrayList<User> users,
            ESortColumnUser sortColumn, String searchQuery) {
        UserListBean ulb;

        // Test all users tab in ascending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.ASC);
        } else {
            sortByEmail(users, ESortDirection.ASC);
        }
        ulb = listUsers(1, sortColumn, ESortDirection.ASC, searchQuery,
                EUserType.Administrator);
        List<User> us = new ArrayList<User>();
        
        for (int i = 1; i < ulb.getPages()+1; i++) {
            
            ulb = listUsers(i, sortColumn, ESortDirection.ASC, searchQuery,
                    EUserType.Administrator);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("GroupOwnership")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable());
        }

        // Test all users tab in descending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.DESC);
        } else {
            sortByEmail(users, ESortDirection.DESC);
        }
        ulb = listUsers(1, sortColumn, ESortDirection.DESC, searchQuery,
                EUserType.Administrator);
        us = new ArrayList<User>();
        
        for (int i = 1; i < ulb.getPages(); i++) {
            ulb = listUsers(i, sortColumn, ESortDirection.DESC, searchQuery,
                    EUserType.Administrator);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("GroupOwnership")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable());
        }
    }

    private static void matchRow(User row, String title,
            String firstName, String lastName, EUserType userType,
            boolean banned, boolean deletable) {
        if (banned && !deletable) {
            fail();
        }

        if (userType != EUserType.Administrator && !deletable) {
            fail();
        }

        assertEquals(title, row.getTitle());
        assertNotNull(firstName);
        if (firstName != null) {
            assertEquals(firstName, row.getFirstName());
        }
        assertNotNull(lastName);
        if (lastName != null) {
            assertEquals(lastName, row.getLastName());
        }
        	assertNotNull(userType);
        if (userType != null) {
            assertEquals(userType, row.getUserType());
        }
        assertEquals(banned, row.isBanned());
        assertEquals(deletable, row.isDeletable());
    }
    
    /**
     * Create an testUser
     * 
     * @param type
     * @return
     * @throws InconsistencyException
     */
    private User createUser(EUserType type, String email)
            throws InconsistencyException {
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
        user = persistenceProvider.getUserStore().create(user);
        return user;
    }
    
    /**
     * Delete an testUser.
     * 
     * @param user
     * @throws InconsistencyException
     * 
     */
    private void deleteUser(User user) throws InconsistencyException {
        persistenceProvider.getUserStore().delete(user);
    }
}

package hu.tests.controller.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.users.UserBean;
import hu.backingbeans.users.UserListBean;
import hu.backingbeans.users.UserListBean.UserListEntryBean;
import hu.controller.users.UserAction;
import hu.controller.users.UserListAction;
import hu.model.ESortDirection;
import hu.model.users.EGender;
import hu.model.users.ESortColumnUser;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
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
 * Unit tests for {@link UserListAction#listUsers()}. Running this test requires
 * that max_rows_per_table is set to 20. 
 */
public class UserListActionTest {
    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;

    private static ArrayList<User> users = new ArrayList<User>();

    private static SessionData sessionData;
    private static UserBean userBean;
    private static UserAction userAction;
    
    private static int setNewAdmins = 2;
    private static int setNewDummies = 1;
    private static int setNewTutors = 7;
    private static int setNewUsers = 10;
    
    /**
     * Prepare testing:  insert test data.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void prepare() throws Exception {
        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);
        sessionData = new SessionData();
        
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
        
        assertEquals(20,
                (int) configuration.getInteger("max_rows_per_table"));

        // Add test users to current user list
       

        for (int i = 0; i < setNewAdmins; i++) {
            addUserToDatabase(i, "Admin", "Prof.", EUserType.Administrator,
                    EGender.Male, false);
        }

        for (int i = 0; i < setNewDummies; i++) {
            addUserToDatabase(i, "Dummy", "Dr.", EUserType.Tutor, EGender.Male, true);
        }

        for (int i = 0; i < setNewTutors; i++) {
            addUserToDatabase(i, "Tutor", "Dr.", EUserType.Tutor,
                    EGender.Female, true);
        }

        for (int i = 0; i < setNewUsers; i++) {
            addUserToDatabase(i, "User", "Dr.", EUserType.Participant,
                    EGender.Male, true);
        }
    }

    private static void addUserToDatabase(int i, String testName,
            String testTitle, EUserType testType, EGender testGender, boolean visible)
            throws Exception {
        assertEquals(null, persistenceProvider.getUserStore()
                .findByEmail("us." + testName + "@mailinator.com" + i));

        User user = new User(null);
        user.setBanned(i % 2 == 0);
        user.setBirthday(new Date());
        user.setDeletable(true);
        user.setEmail("us." + testName + "@UserListActionTestmailinator.com" + i);
        user.setFirstName("Us" + testName + "FUserListActionTest" + i);
        user.setLastName("Us" + testName + "NUserListActionTest" + i);
        user.setPasswordHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        user.setTitle(testTitle);
        user.setUserType(testType);
        user.setGender(testGender);
        user.setCountry(ECountry.Austria);
        user.setStreet("Wandstrasse"+i);
        user.setCity("EinÃ¶de");
        user.setZip("013"+i);
        user.setFax("0456234"+i);
        user.setPhone("0456234"+(i*5));
        user.setWebsite("www.test.at");
        user.setVisible(visible);
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
    public static void destroy() throws Exception {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).isDeletable()) {
            	int id = users.get(i).getId();
            	userBean.setId(users.get(i).getId());
                userAction.deleteUser();
                assertNull(persistenceProvider.getUserStore().findById(id));
            }
        }
        persistenceProvider.close();
    }

    /**
     * Test the list of all users sorted by name.
     */
    @Test
    public void testAllUsersSortedByName() {
        matchAll(users, ESortColumnUser.Name, "");
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
     * Test the list search for users sorted by name.
     */
    @Test
    public void testSearchAllUsersSortedByName() {
        String searchQuery = "UsUserL0";
        ArrayList<User> allUsers = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getLastName().equals(searchQuery)) {
                allUsers.add(users.get(i));
            }
        }
        matchAll(allUsers, ESortColumnUser.Name, searchQuery);
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
                    && users.get(i).getLastName() == searchQuery) {
                administrators.add(users.get(i));
            }
        }
        matchAdministrators(administrators, ESortColumnUser.Name, searchQuery);
    }

    /**
     * Test the search for all users sorted by email.
     */

    @Test
    public void testSearchAllUsersSortedByEmail() {
        String searchQuery = "UsUserL0";
        ArrayList<User> allUsers = new ArrayList<User>();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getLastName().equals(searchQuery)) {
                allUsers.add(users.get(i));
            }
        }
        matchAll(users, ESortColumnUser.Email, "UsUserL0");
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

        FacesContext fctxMock = mock(FacesContext.class);
        when(fctxMock.isPostback()).thenReturn(false);

        UserListAction ctrl = new UserListAction();
        ctrl.setMock(fctxMock);
        ctrl.setConfiguration(configuration);
        ctrl.setUserListBean(ulb);
        ctrl.setPersistenceProvider(persistenceProvider);

        ctrl.listUsers();

        return ulb;
    }

    private static void matchAll(ArrayList<User> users,
            ESortColumnUser sortColumn, String searchQuery) {
        UserListBean ulb;

        // Test all users tab in ascending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.ASC);
        } else {
            sortByEmail(users, ESortDirection.ASC);
        }
        
        List<User> us = new ArrayList<User>();
        ulb = listUsers(1, sortColumn, ESortDirection.ASC, searchQuery, null);
        for (int i = 1; i < ulb.getPages(); i++) {
            ulb = listUsers(i, sortColumn, ESortDirection.ASC, searchQuery, null);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
       
        for (int i = 0; i < us.size(); i++) {
            
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
        }

        // Test all users tab in descending order.
        if (sortColumn == ESortColumnUser.Name) {
            sortByName(users, ESortDirection.DESC);
        } else {
            sortByEmail(users, ESortDirection.DESC);
        }
        us = new ArrayList<User>();
        ulb = listUsers(1, sortColumn, ESortDirection.DESC, searchQuery, null);
        for (int i = 1; i < ulb.getPages(); i++) {
            ulb = listUsers(i, sortColumn, ESortDirection.DESC, searchQuery, null);
            List<UserListEntryBean> list = ulb.getList();
            for (UserListEntryBean entry : list) {
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
        }
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
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
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
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
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
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
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
		if (entry.getUser().getLastName().contains("UserListActionTest")) {
		us.add(entry.getUser());
		}
	    }
	}
        assertTrue(users.size() >= us.size());
        for (int i = 0; i < us.size(); i++) {
            matchRow(us.get(i), users.get(i).getTitle(), users
                    .get(i).getFirstName(), users.get(i).getLastName(), users
                    .get(i).getUserType(), users.get(i).isBanned(), users
                    .get(i).isDeletable(), users.get(i).isVisible());
        }
    }

    private static void matchRow(User row, String title,
            String firstName, String lastName, EUserType userType,
            boolean banned, boolean deletable, boolean visible) {
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
        assertEquals(visible, row.isVisible());
    }
}
package hu.tests.controller.common;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import hu.backingbeans.common.LoginBean;
import hu.backingbeans.users.AuthenticatedUserBean;
import hu.controller.common.AuthenticationAction;
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
 * This class tests the login functionality of the system.
 * 
 */
public class AuthenticationActionTest {

    private static Configuration configuration;
    private static PgPersistenceProvider persistenceProvider;
    private static AuthenticationAction authenticationAction;
    private static SessionData sessionData;
    private static AuthenticatedUserBean authenticatedUser;
    private static LoginBean loginBeanValid1;
    private static LoginBean loginBeanValid2;
    private static LoginBean loginBeanInvalid1;
    private static LoginBean loginBeanInvalid2;

    // Valid Credentials
    private final static String validMail1 = "addTutor01@test.de";
    private final static String validPw1 = "Test!123";

    private final static String validMail2 = "addTutor00@test.de";
    private final static String validPw2 = "Test!123";

    // Invalid Credentials
    private final static String invalidMail1 = "stromberg@chef.de";
    private final static String invalidPw1 = "Chef!123";
    private final static String invalidMail2 = "marshall.m@mail.de";
    private final static String invalidPw2 = "Marshall!123";

    /**
     * Prepare for testing.
     * @throws InconsistencyException
     */
    @BeforeClass
    public static void prepare() throws InconsistencyException {

        configuration = new Configuration();
        persistenceProvider = new PgPersistenceProvider(configuration);
        sessionData = new SessionData();
        authenticatedUser = new AuthenticatedUserBean();
        authenticationAction = new AuthenticationAction();
        authenticationAction.setPersistenceProvider(persistenceProvider);
        authenticationAction.setSession(sessionData);
        authenticationAction.setAuthenticatedUserBean(authenticatedUser);

        for (int i = 0; i < 2; i++) {
            User tutor = new User(null);
            tutor.setBanned(false);
            tutor.setCountry(ECountry.Austria);
            tutor.setBirthday(new Date());
            tutor.setEmail(String.format("addTutor%02d@test.de", i));
            tutor.setFirstName("test");
            tutor.setGender(EGender.Male);
            tutor.setLastName("test");
            tutor.setPasswordHash(SecurityUtils.hash("Test!123"));
            tutor.setUserType(EUserType.Tutor);
            tutor.setTitle("Dr.");
            tutor.setStreet("Wandstraße"+i);
            tutor.setCity("Einöde");
            tutor.setZip("013"+i);
            tutor.setFax("0456234"+i);
            tutor.setPhone("0456234"+(i*5));
            tutor.setVisible(true);
            tutor.setWebsite("www.test.at");
            persistenceProvider.getUserStore().create(tutor);
        }

        loginBeanValid1 = new LoginBean();
        loginBeanValid1.setUsername(validMail1);
        loginBeanValid1.setPassword(validPw1);
        loginBeanValid2 = new LoginBean();
        loginBeanValid2.setUsername(validMail2);
        loginBeanValid2.setPassword(validPw2);

        loginBeanInvalid1 = new LoginBean();
        loginBeanInvalid1.setUsername(invalidMail1);
        loginBeanInvalid1.setPassword(invalidPw1);
        loginBeanInvalid2 = new LoginBean();
        loginBeanInvalid2.setUsername(invalidMail2);
        loginBeanInvalid2.setPassword(invalidPw2);

    }

    /**
     * Test the login function.
     * @throws IOException 
     */
    @Test
    public void testLogin() throws IOException {
        // Prepare FacesContext Mock for prepopulation
        FacesContext cxtMock = mock(FacesContext.class);
        UIViewRoot uiViewRoot = mock(UIViewRoot.class);
        Locale locale = new Locale("de");
        authenticationAction.setMock(cxtMock);
        when(uiViewRoot.getLocale()).thenReturn(locale);
        when(cxtMock.getViewRoot()).thenReturn(uiViewRoot);

        // Test with valid credentials
        authenticationAction.setLoginBean(loginBeanValid1);
        assertEquals("/xhtml/users/dashboard", authenticationAction.login());
        authenticationAction.setLoginBean(loginBeanValid2);
        assertEquals("/xhtml/users/dashboard", authenticationAction.login());

        // Test with invalid credentials
        authenticationAction.setLoginBean(loginBeanInvalid1);
        assertEquals("/xhtml/users/login", authenticationAction.login());
        assertEquals("Eingegebene Benutzerdaten stimmen nicht überein.",
                authenticationAction.getLastFacesMessage());
        authenticationAction.setLoginBean(loginBeanInvalid2);
        assertEquals("/xhtml/users/login", authenticationAction.login());

    }

    /**
     * Test the prepopulation for the username.
     * @throws IOException 
     */
    @Test
    public void testPrePopulateUsername() throws IOException {
        authenticationAction.setLoginBean(loginBeanValid1);
        authenticationAction.login();
        authenticationAction.prePopulateUsername();
        assertNotNull(authenticatedUser.getUser());

    }

    /**
     * Test the logout function.
     * @throws IOException 
     */
    @Test
    public void testLogout() throws IOException {
        authenticationAction.setLoginBean(loginBeanValid1);
        assertEquals("/xhtml/users/dashboard", authenticationAction.login());
        assertNotNull(sessionData.getUserId());

        assertEquals("/xhtml/users/logout?faces-redirect=true",
                authenticationAction.logout());
        assertEquals(null, sessionData.getUserId());
    }

    /**
     * Clean uo after test.
     * 
     * @throws InconsistencyException
     */
    @AfterClass
    public static void clean() throws InconsistencyException {
        for (int i = 0; i < 2; i++) {
            persistenceProvider.getUserStore().delete(
                    persistenceProvider.getUserStore().findByEmail(
                            String.format("addTutor%02d@test.de", i)));
        }
        persistenceProvider.close();
    }
}
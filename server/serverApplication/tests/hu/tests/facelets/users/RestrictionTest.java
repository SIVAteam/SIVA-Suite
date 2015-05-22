package hu.tests.facelets.users;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;

import hu.tests.facelets.configuration.Globals;

import org.junit.*;

/**
 * Concerning: /T100/
 */
public class RestrictionTest {

    // URLs
    private static final String LIST_USERS_JSF = "/xhtml/users/listUsers.jsf";

    @BeforeClass
    public static void prepare() {
        setBaseUrl(Globals.getBaseUrl());
    }

    @Test
    public void testAccessRestrictions() {
        beginAt(Globals.getBaseUrl());
        assertTitleEquals("Welcome");

        gotoPage(LIST_USERS_JSF);
        assertTitleEquals("Login");
    }
}
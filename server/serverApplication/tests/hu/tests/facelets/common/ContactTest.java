package hu.tests.facelets.common;

import static net.sourceforge.jwebunit.junit.JWebUnit.*;
import static org.junit.Assert.*;
import hu.tests.facelets.configuration.Globals;

import org.junit.*;

/**
 * This class tests the systems contact form.
 */
public class ContactTest {  

    // IDs
    private static final String CONTACT_FORM_ID = "contactForm";

    @BeforeClass
    public static void prepare() {
        setBaseUrl(Globals.getBaseUrl());
    }

    @Test
    public void testContactForm() {

        beginAt(Globals.getBaseUrl());
        assertTitleEquals("Welcome");

        clickLinkWithExactText("contact");
        assertTitleEquals("Contact");
        assertFormPresent(CONTACT_FORM_ID);
        setWorkingForm(CONTACT_FORM_ID);

        // Try to submit an empty form
        submit();
        assertEquals("error", getElementById(CONTACT_FORM_ID + ":firstName")
                .getAttribute("class"));
        assertEquals("error", getElementById(CONTACT_FORM_ID + ":lastName")
                .getAttribute("class"));
        assertEquals("error", getElementById(CONTACT_FORM_ID + ":email")
                .getAttribute("class"));
        assertEquals("error", getElementById(CONTACT_FORM_ID + ":subject")
                .getAttribute("class"));
        assertEquals("error", getElementById(CONTACT_FORM_ID + ":message")
                .getAttribute("class"));

        // Check for working validation
        setTextField(CONTACT_FORM_ID + ":firstName", "Max");
        submit();
        assertEquals("", getElementById(CONTACT_FORM_ID + ":firstName")
                .getAttribute("class"));

        // Fill form correctly and submit
        setTextField(CONTACT_FORM_ID + ":lastName", "Mustermann");
        setTextField(CONTACT_FORM_ID + ":email", "mustermann@fim.uni-passau.de");
        setTextField(CONTACT_FORM_ID + ":subject", "JWebUnit Test");
        setTextField(CONTACT_FORM_ID + ":message",
                "Hier laeuft gerade ein Test!");
        submit();
        assertTextPresent("Message was sent.");
    }
}

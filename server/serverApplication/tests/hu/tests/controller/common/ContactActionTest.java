package hu.tests.controller.common;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.common.ContactBean;
import hu.controller.common.ContactAction;
import hu.model.users.EGender;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;
import hu.util.MailService;

import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link ContactAction}.
 * 
 */

public class ContactActionTest {

    private static final String MSG_MESSAGE = "Message!123";
    private static final String MSG_SUBJECT = "Subject!!!";
    private static final String MSG_LAST_NAME = "LastName";
    private static final String MSG_FIRST_NAME = "FirstName";
    private static final String MSG_ACADEMIC_TITLE = "Prof.";
    private static final String MSG_MAIL = "test@mailinator.com";
    private static final String TEST_IN_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static PgPersistenceProvider pers;
    private static Integer adminId;
    private static MailMock mailMock;
    private final static ECountry COUNTRY = ECountry.Germany;
    private final static String STREET = "Musterstrasse";
    private final static String ZIP = "55555";
    private final static String CITY = "Musterstadt";
    private final static String PHONE = "0151555";
    private final static String FAX = "0151554";
    private final static String WEBSITE = "www.musterweb.de";
    private final static boolean VISIBLE = false;

    /**
     * Mock for the mail service, which allows access to the last message sent.
     */
    private static class MailMock extends MailService {

        public String lastSubject;
        public String lastBody;

        @Override
        public void setConfiguration(Configuration configuration) {
            throw new RuntimeException("Not mocked");
        }

        @Override
        public synchronized boolean sendMail(String recipient,
                String subject, String body) {
            throw new RuntimeException("Not mocked");
        }

        @Override
        public synchronized boolean sendMultipleMail(
                Map<String, String> recipientMap, String subject,
                String body) {
            this.lastSubject = subject;
            this.lastBody = body;
            return true;
        }
    };

    /**
     * Prepare testing: Check that the database is clean and insert test data.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void prepare() throws Exception {
        pers = new PgPersistenceProvider(new Configuration());
        mailMock = new MailMock();

        User admin = pers.getUserStore().findByEmail("contact.admin@mailinator.com");
        if (admin != null) {
            pers.getUserStore().delete(admin);
        }

        admin = new User(null);
        admin.setBanned(false);
        admin.setCountry(COUNTRY);
        admin.setDeletable(true);
        admin.setEmail("contact.admin@mailinator.com");
        admin.setFirstName("ContactAdmin");
        admin.setLastName("ContactAdmin");
        admin.setPasswordHash(TEST_IN_SHA256);
        admin.setTitle("Tst");
        admin.setUserType(EUserType.Administrator);
        admin.setGender(EGender.Male);
        admin.setStreet(STREET);
        admin.setZip(ZIP);
        admin.setCity(CITY);
        admin.setPhone(PHONE);
        admin.setFax(FAX);
        admin.setWebsite(WEBSITE);
        admin.setVisible(VISIBLE);
        admin = pers.getUserStore().create(admin);

        adminId = admin.getId();
    }

    /**
     * Delete test data from the database.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void destroy() throws Exception {
        pers.getUserStore().delete(adminId);
        pers.close();
    }

    /**
     * Test sending a message with the contact form.
     */
    @Test
    public void testSendMessage() {
        String result = this.sendMessage(MSG_MAIL, MSG_ACADEMIC_TITLE,
                MSG_FIRST_NAME, MSG_LAST_NAME, MSG_SUBJECT, MSG_MESSAGE);
        assertNull(result);
        String body = mailMock.lastBody;
        assertTrue(body.contains(MSG_MAIL));
        assertTrue(body.contains(MSG_ACADEMIC_TITLE));
        assertTrue(body.contains(MSG_FIRST_NAME));
        assertTrue(body.contains(MSG_LAST_NAME));
        assertTrue(body.contains(MSG_SUBJECT));
        assertTrue(body.contains(MSG_MESSAGE));
        assertTrue(mailMock.lastSubject.contains(MSG_SUBJECT));
    }

    private String sendMessage(String email, String academicTitle,
            String firstName, String lastName, String subject, String message) {
        FacesContext fctxMock = mock(FacesContext.class);
        when(fctxMock.isPostback()).thenReturn(true);
        when(fctxMock.isValidationFailed()).thenReturn(false);
        UIViewRoot uvr = mock(UIViewRoot.class);
        when(fctxMock.getViewRoot()).thenReturn(uvr);
        when(uvr.getLocale()).thenReturn(Locale.ENGLISH);

        ContactBean cbean = new ContactBean();
        cbean.setAcademicTitle(academicTitle);
        cbean.setFirstName(firstName);
        cbean.setLastName(lastName);
        cbean.setEmail(email);
        cbean.setMessage(message);
        cbean.setSubject(subject);

        ContactAction ctrl = new ContactAction();
        ctrl.setMock(fctxMock);
        ctrl.setContactBean(cbean);
        ctrl.setMailService(mailMock);
        ctrl.setPersistenceProvider(pers);

        return ctrl.sendMessage();
    }
}

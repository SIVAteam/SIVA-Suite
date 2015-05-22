package hu.tests.facelets.common;

import static net.sourceforge.jwebunit.junit.JWebUnit.assertFormPresent;
import static net.sourceforge.jwebunit.junit.JWebUnit.assertTitleEquals;
import static net.sourceforge.jwebunit.junit.JWebUnit.beginAt;
import static net.sourceforge.jwebunit.junit.JWebUnit.selectOption;
import static net.sourceforge.jwebunit.junit.JWebUnit.setBaseUrl;
import static net.sourceforge.jwebunit.junit.JWebUnit.setTextField;
import static net.sourceforge.jwebunit.junit.JWebUnit.setWorkingForm;
import static net.sourceforge.jwebunit.junit.JWebUnit.submit;

import org.junit.Before;
import org.junit.Test;

import hu.tests.facelets.configuration.Globals;


/**
 * Attention!
 * For this test the Configuration.properties in the user home folder has to be
 * deleted and tomcat has to be restarted!
 * 
 */
public class InstallationTest {


    private static final String BASE_URL = Globals.getBaseUrl();
    
    //form
    private static final String INSTALLATION_FORM = "installationForm";

    //fields
    private static final String INSTALLATION_DATABASE_HOST_FIELD = ":databaseHost";
    private static final String INSTALLATION_DATABASE_PORT_FIELD = ":databasePort";
    private static final String INSTALLATION_DATABASE_NAME_FIELD = ":databaseName";
    private static final String INSTALLATION_DATABASE_USER_FIELD = ":databaseUser";
    private static final String INSTALLATION_DATABASE_PASSWORD_FIELD = ":databasePassword";
    private static final String INSTALLATION_SMTP_HOST_FIELD = ":smtpHost";
    private static final String INSTALLATION_SMTP_PORT_FIELD = ":smtpPort";
    private static final String INSTALLATION_SMTP_USER_FIELD = ":smtpUser";
    private static final String INSTALLATION_SMTP_PASSWORD_FIELD = ":smtpPassword";
    private static final String INSTALLATION_SMTP_SYSTEM_MAIL_FIELD = ":smtpSender";
    private static final String INSTALLATION_ADMIN_EMAIL_FIELD = ":email";
    private static final String INSTALLATION_ADMIN_PASSWORD_FIELD = ":password";
    private static final String INSTALLATION_ADMIN_PASSWORD_REPEAT_FIELD = ":passwordRepeat";
    private static final String INSTALLATION_ADMIN_ACADEMIC_TITLE_FIELD = ":academicTitle";
    private static final String INSTALLATION_ADMIN_FIRST_NAME_FIELD = ":firstName";
    private static final String INSTALLATION_ADMIN_LAST_NAME_FIELD = ":lastName";
   
    private static final String INSTALLATION_ADMIN_GENDER_FIELD = ":gender";
    
    //site titles
    private static final String INSTALLATION_TITLE = "Installation";
    private static final String HOMEPAGE_TITLE = "Welcome";

    //test data
    private static final String INSTALLATION_DATABASE_HOST = "localhost";
    private static final String INSTALLATION_DATABASE_PORT = "5432";
    private static final String INSTALLATION_DATABASE_NAME = "mtt";
    private static final String INSTALLATION_DATABASE_USER = "postgres";
    private static final String INSTALLATION_DATABASE_PASSWORD = "D8spSKfq";
    private static final String INSTALLATION_SMTP_HOST = "smtp.gmail.com";
    private static final String INSTALLATION_SMTP_PORT = "587";
    private static final String INSTALLATION_SMTP_USER = "sep.handsup";
    private static final String INSTALLATION_SMTP_PASSWORD = "sepHandsUpPW";
    private static final String INSTALLATION_SMTP_SYSTEM_MAIL = "sep.handsup@gmail.com";
    private static final String ADMIN_EMAIL = Globals.getAdminUsername();
    private static final String ADMIN_PASSWORD = Globals.getAdminPassword();
    private static final String ADMIN_PASSWORD_REPEAT = Globals.getAdminPassword();
    private static final String ADMIN_ACADEMIC_TITLE = "Prof";
    private static final String ADMIN_FIRST_NAME = "Some";
    private static final String ADMIN_LAST_NAME = "God";
    private static final String ADMIN_GENDER = "male";

    @Before
    public void prepare() {
        setBaseUrl(BASE_URL);
    }
    
    @Test
    public void installDatabase() {
        
        beginAt("");
        assertTitleEquals(INSTALLATION_TITLE);
        assertFormPresent(INSTALLATION_FORM);
        setWorkingForm(INSTALLATION_FORM);
        
        setTextField(INSTALLATION_FORM + INSTALLATION_DATABASE_HOST_FIELD, INSTALLATION_DATABASE_HOST);
        setTextField(INSTALLATION_FORM + INSTALLATION_DATABASE_PORT_FIELD, INSTALLATION_DATABASE_PORT);
        setTextField(INSTALLATION_FORM + INSTALLATION_DATABASE_NAME_FIELD, INSTALLATION_DATABASE_NAME);
        setTextField(INSTALLATION_FORM + INSTALLATION_DATABASE_USER_FIELD, INSTALLATION_DATABASE_USER);
        setTextField(INSTALLATION_FORM + INSTALLATION_DATABASE_PASSWORD_FIELD, INSTALLATION_DATABASE_PASSWORD);
        setTextField(INSTALLATION_FORM + INSTALLATION_SMTP_HOST_FIELD, INSTALLATION_SMTP_HOST);
        setTextField(INSTALLATION_FORM + INSTALLATION_SMTP_PORT_FIELD, INSTALLATION_SMTP_PORT);
        setTextField(INSTALLATION_FORM + INSTALLATION_SMTP_USER_FIELD, INSTALLATION_SMTP_USER);
        setTextField(INSTALLATION_FORM + INSTALLATION_SMTP_PASSWORD_FIELD, INSTALLATION_SMTP_PASSWORD);
        setTextField(INSTALLATION_FORM + INSTALLATION_SMTP_SYSTEM_MAIL_FIELD, INSTALLATION_SMTP_SYSTEM_MAIL);
        setTextField(INSTALLATION_FORM + INSTALLATION_ADMIN_EMAIL_FIELD, ADMIN_EMAIL);
        setTextField(INSTALLATION_FORM + INSTALLATION_ADMIN_PASSWORD_FIELD, ADMIN_PASSWORD);
        setTextField(INSTALLATION_FORM + INSTALLATION_ADMIN_PASSWORD_REPEAT_FIELD, ADMIN_PASSWORD_REPEAT);
        selectOption(INSTALLATION_FORM + INSTALLATION_ADMIN_ACADEMIC_TITLE_FIELD, ADMIN_ACADEMIC_TITLE);
        setTextField(INSTALLATION_FORM + INSTALLATION_ADMIN_FIRST_NAME_FIELD, ADMIN_FIRST_NAME);
        setTextField(INSTALLATION_FORM + INSTALLATION_ADMIN_LAST_NAME_FIELD, ADMIN_LAST_NAME);
        selectOption(INSTALLATION_FORM + INSTALLATION_ADMIN_GENDER_FIELD, ADMIN_GENDER);
        submit();
        
        assertTitleEquals(HOMEPAGE_TITLE);
    }
}
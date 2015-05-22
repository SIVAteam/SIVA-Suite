package hu.tests.controller.common;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hu.backingbeans.common.InstallationBean;
import hu.backingbeans.users.UserBean;
import hu.controller.common.InstallationAction;
import hu.model.users.EGender;
import hu.persistence.IPersistenceProvider;
import hu.persistence.postgres.PgPersistenceProvider;
import hu.util.Configuration;
import hu.util.ECountry;

import java.util.Date;
import java.util.Locale;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for {@link InstallationAction#setup()}.
 * 
 */
public class InstallationActionTest {

    /**
     * Since no empty database is available during testing, only the
     * verification of the database connectivity is verified.
     */
    @Test
    public void testSetup() {
        UserBean ab = new UserBean();
        ab.setAcademicTitle("");
        ab.setBirthday(new Date());
        ab.setEmail("installation.admin@mailinator.com");
        ab.setFirstName("Installation");
        ab.setGender(EGender.Male);
        ab.setLastName("Admin");
        ab.setPassword("test");
        ab.setPasswordRepeat("test");
        ab.setCountry(ECountry.Switzerland);
        ab.setStreet("Kindlistraße");
        ab.setZip("9945");
        ab.setCity("Wunderstadt");
        ab.setPhone("65784");
        ab.setFax("5666");
        ab.setWebsite("www.test.ch");
        ab.setVisible(true);

        FacesContext fctxMock = mock(FacesContext.class);
        when(fctxMock.isPostback()).thenReturn(true);
        when(fctxMock.isValidationFailed()).thenReturn(false);
        UIViewRoot uvr = mock(UIViewRoot.class);
        when(fctxMock.getViewRoot()).thenReturn(uvr);
        when(uvr.getLocale()).thenReturn(Locale.ENGLISH);

        Configuration config = new Configuration() {
            @Override
            public String getString(String key) {
                if (key.equals("is_installed")) {
                    return "false";
                } else if (key.contains("port")){
                    return "1234";
                } else if (key.contains("host")) {
                    return "localhost";
                } else if (key.contains("user")) {
                    return "root";
                } else if (key.contains("passw")) {
                    return "wrong";
                } else if (key.contains("database_connections")) {
                        return "1";
                } else {
                    return "";
                }
            }

            @Override
            public void setString(String key, String value) {
            }

            @Override
            public Integer getInteger(String key) {
                String str =this.getString(key);
                if (str.length() == 0) {
                    return 0;
                } else {
                    return Integer.parseInt(str);
                }
            }

            @Override
            public void setInteger(String key, Integer value) {
            }

            @Override
            public Boolean getBoolean(String key) {
                return Boolean.parseBoolean(this.getString(key));
            }

            @Override
            public void setBoolean(String key, Boolean value) {
            }
        };

        InstallationBean ib = new InstallationBean();
        ib.setDatabaseHost("localhost");
        ib.setDatabaseName("postgres");
        ib.setDatabasePassword("wrongPassword");
        ib.setDatabasePort(5432);
        ib.setDatabaseUser("postgres");
        ib.setSmtpHost("localhost");
        ib.setSmtpPassword("wrongPassword");
        ib.setSmtpPort(123);
        ib.setSmtpSender("root@localhost");
        ib.setSmtpUser("root");
        
        IPersistenceProvider pers = new PgPersistenceProvider(config);

        InstallationAction ctrl = new InstallationAction();
        ctrl.setAdministratorBean(ab);
        ctrl.setConfiguration(config);
        ctrl.setInstallationBean(ib);
        ctrl.setPersistenceProvider(pers);
        ctrl.setMock(fctxMock);
        
        assertEquals(null, ctrl.setup());
        
        assertEquals("Could not connect to the specified database.",
                ctrl.getLastFacesMessage());

        pers.close();
    }

}

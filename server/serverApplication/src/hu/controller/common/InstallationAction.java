package hu.controller.common;

import hu.backingbeans.common.InstallationBean;
import hu.backingbeans.users.UserBean;
import hu.controller.AController;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IPersistenceSetup;
import hu.persistence.InconsistencyException;
import hu.persistence.PersistenceSetupException;
import hu.util.Configuration;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

/**
 * This class provides the functionality for the installation wizard.
 */
@ManagedBean
@RequestScoped
public class InstallationAction extends AController {
    private static final String CFG_IS_INSTALLED = "is_installed";
    
    private static final String START_FACELET = "/xhtml/common/start";
    private static final String RESTRICTION_ERROR_FACELET = "/xhtml/errors/restrictionError";
    
    private static final String MSG_DB_SETUP = "installation_db_setup_failed";
    private static final String MSG_ADMIN_CREATION = "installation_admin_creation_failed";
    private static final String MSG_PASSWORD_MATCH = "installation_passwort_repeat_failed";
    private static final String MSG_NO_DB_CONNECTIVITY = "installation_no_db_connectivity";
    
    private static final String INSTALLATION_FORM_PASSWORD_REPEAT = "installationForm:passwordRepeat";
    
    @ManagedProperty("#{installationBean}")
    private InstallationBean installationBean;

    @ManagedProperty("#{userBean}")
    private UserBean administratorBean;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    /**
     * Set {@link InstallationBean} using injection.
     * @param installationBean
     *            to inject.
     */
    public void setInstallationBean(InstallationBean installationBean) {
        this.installationBean = installationBean;
    }

    /**
     * Set {@link UserBean} using injection.
     * @param administratorBean
     *            to inject.
     */
    public void setAdministratorBean(UserBean administratorBean) {
        this.administratorBean = administratorBean;
    }

    /**
     * Set {@link IPersistenceProvider} using injection for database access.
     * 
     * @param persistenceProvider
     *            to inject.
     */
    public void setPersistenceProvider(IPersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Set {@link Configuration} using injection.
     * 
     * @param configuration
     *            to inject.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Set the application up with the details provided in the installation
     * routine.
     * 
     * Only users of {@link EUserType#Administrator} have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String setup() {
        // Installation procedure is only available if the system is not already installed.
        if (this.configuration.getBoolean(CFG_IS_INSTALLED)) {
            this.redirectTo(RESTRICTION_ERROR_FACELET);
            return null;
        }

        FacesContext fctx = this.getCurrentFcInstance();
        // Pre-populate installation form with default values from configuration file.
        if (!fctx.isPostback() && !fctx.isValidationFailed()) {
            return this.prepopulateSettings();
        } else if (fctx.isPostback() && fctx.isValidationFailed()) {
            return null;
        }

        // Verify that both entered passwords match.
        if (!this.administratorBean.getPassword().equals(this.administratorBean.getPasswordRepeat())) {
            this.sendMessageTo(INSTALLATION_FORM_PASSWORD_REPEAT,
                    this.getCommonMessage(MSG_PASSWORD_MATCH));
            return null;
        }

        // Save and verify the configuration.
        this.saveConfiguration();
        if (!this.persistenceProvider.testConnectivity()) {
            this.sendGlobalMessageToFacelet(
                    this.getCommonMessage(MSG_NO_DB_CONNECTIVITY));
            return null;
        }

        // Install the persistence layer.
        this.persistenceProvider.restart();
        IPersistenceSetup setup = this.persistenceProvider.getSetup();
        try {
            setup.uninstall();
            setup.install();
            //setup.fill();
        } catch (PersistenceSetupException e) {
            this.sendGlobalMessageToFacelet(
                    this.getCommonMessage(MSG_DB_SETUP) + e.getMessage());
            return null;
        }

        // Add administrator to database.
        User admin = this.createAdministrator();
        try {
            this.persistenceProvider.getUserStore().create(admin);
        } catch (InconsistencyException e) {
            this.sendGlobalMessageToFacelet(
                    this.getCommonMessage(MSG_ADMIN_CREATION));
            return null;
        }

        // Show start-page after successful installation.
        this.configuration.setBoolean(CFG_IS_INSTALLED, true);
        return START_FACELET;
    }

    private User createAdministrator() {
        User admin = new User(null);
        admin.setBanned(false);
        admin.setDeletable(false);
        admin.setUserType(EUserType.Administrator);
        admin.setBirthday(this.administratorBean.getBirthday());
        admin.setEmail(this.administratorBean.getEmail());
        admin.setFirstName(this.administratorBean.getFirstName());
        admin.setGender(this.administratorBean.getGender());
        admin.setLastName(this.administratorBean.getLastName());
        admin.setPassword(this.administratorBean.getPassword());
        admin.setTitle((this.administratorBean.getAcademicTitle() == null)
                ? "" : this.administratorBean.getAcademicTitle());
        admin.setCountry(this.administratorBean.getCountry());
        return admin;
    }

    private void saveConfiguration() {
        this.configuration.setString("database_host", this.installationBean.getDatabaseHost());
        this.configuration.setInteger("database_port", this.installationBean.getDatabasePort());
        this.configuration.setString("database_user", this.installationBean.getDatabaseUser());
        this.configuration.setString("database_name", this.installationBean.getDatabaseName());
        this.configuration.setString("database_password", this.installationBean.getDatabasePassword());
        this.configuration.setString("smtp_host", this.installationBean.getSmtpHost());
        this.configuration.setInteger("smtp_port", this.installationBean.getSmtpPort());
        this.configuration.setBoolean("smtp_start_tls_enable", this.installationBean.getSmtpTLS());
        this.configuration.setString("smtp_user", this.installationBean.getSmtpUser());
        this.configuration.setString("smtp_password", this.installationBean.getSmtpPassword());
        this.configuration.setString("smtp_sender_email", this.installationBean.getSmtpSender());
        this.configuration.setString("smtp_sender_name", this.installationBean.getSmtpSenderName());
    }

    private String prepopulateSettings() {
        this.installationBean.setDatabaseHost(this.configuration.getString("database_host"));
        this.installationBean.setDatabasePort(this.configuration.getInteger("database_port"));
        this.installationBean.setDatabaseName(this.configuration.getString("database_name"));
        this.installationBean.setDatabaseUser(this.configuration.getString("database_user"));
        this.installationBean.setDatabasePassword(this.configuration.getString("database_password"));
        this.installationBean.setSmtpHost(this.configuration.getString("smtp_host"));
        this.installationBean.setSmtpPassword(this.configuration.getString("smtp_password"));
        this.installationBean.setSmtpPort(this.configuration.getInteger("smtp_port"));
        this.installationBean.setSmtpSender(this.configuration.getString("smtp_sender_email"));
        this.installationBean.setSmtpUser(this.configuration.getString("smtp_user"));
        return null;
    }
}
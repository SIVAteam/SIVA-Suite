package hu.backingbeans.common;

import hu.controller.common.InstallationAction;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * This is the backing bean for the {@link InstallationAction}. It holds the
 * application configuration entered during the installation process.
 */
@ManagedBean
@RequestScoped
public class InstallationBean {
    private String databaseHost;
    private Integer databasePort;
    private String databaseUser;
    private String databasePassword;
    private String databaseName;

    private String smtpHost;
    private Integer smtpPort;
    private Boolean smtpTLS;
    private String smtpUser;
    private String smtpPassword;
    private String smtpSender;
    private String smtpSenderName;

    /**
     * 
     * @return the host of the database which was set during the installation
     *         routine.
     */
    public String getDatabaseHost() {
        return this.databaseHost;
    }

    /**
     * Set the host of the database which is set during the installation
     * routine.
     * @param databaseHost
     *            to set.
     */
    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    /**
     * 
     * @return the port of the database which was set during the installation
     *         routine.
     */
    public Integer getDatabasePort() {
        return this.databasePort;
    }

    /**
     * Set the port of the database which is set during the installation
     * routine.
     * 
     * @param databasePort
     *            to set.
     */
    public void setDatabasePort(Integer databasePort) {
        this.databasePort = databasePort;
    }

    /**
     * 
     * @return the user of the database which was set during the installation
     *         routine.
     */
    public String getDatabaseUser() {
        return this.databaseUser;
    }

    /**
     * Set the user of the database which is set during the installation
     * routine.
     * 
     * @param databaseUser
     *            to set.
     */
    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    /**
     * 
     * @return the password of the database which was set during the
     *         installation routine.
     */
    public String getDatabasePassword() {
        return this.databasePassword;
    }

    /**
     * Set the password of the database which is set during the installation
     * routine.
     * 
     * @param databasePassword
     *            to set.
     */
    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    /**
     * 
     * @return the name of the database which was set during the installation
     *         routine.
     */
    public String getDatabaseName() {
        return this.databaseName;
    }

    /**
     * Set the name of the database which is set during the installation
     * routine.
     * 
     * @param databaseName
     *            to set.
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * 
     * @return the host of the SMTP server which was set during the installation
     *         routine.
     */
    public String getSmtpHost() {
        return this.smtpHost;
    }

    /**
     * Set the host of the SMTP server which is set during the installation
     * routine.
     * 
     * @param smtpHost
     *            to set.
     */
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    /**
     * 
     * @return the port of the SMTP server which was set during the installation
     *         routine.
     */
    public Integer getSmtpPort() {
        return this.smtpPort;
    }

    /**
     * Set the port of the SMTP server which is set during the installation
     * routine.
     * 
     * @param smtpPort
     *            to set.
     */
    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }
    
    /**
     * 
     * @return true of the TLS for SMTP connection is necessary.
     */
    public Boolean getSmtpTLS() {
        return this.smtpTLS;
    }

    /**
     * Set true of the TLS for SMTP connection is necessary.
     * 
     * @param smtpTLS
     *            true if TLS is necessary, false otherwise.
     */
    public void setSmtpTLS(Boolean smtpTLS) {
        this.smtpTLS = smtpTLS;
    }

    /**
     * 
     * @return the user of the SMTP server which was set during the installation
     *         routine.
     */
    public String getSmtpUser() {
        return this.smtpUser;
    }

    /**
     * Set the user of the SMTP server which is set during the installation
     * routine.
     * 
     * @param smtpUser
     *            to set.
     */
    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    /**
     * 
     * @return the password of the SMTP server which was set during the
     *         installation routine.
     */
    public String getSmtpPassword() {
        return this.smtpPassword;
    }

    /**
     * Set the password of the SMTP server which is set during the installation
     * routine.
     * 
     * @param smtpPassword
     *            to set.
     */
    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    /**
     * 
     * @return the sender address of the SMTP server which was set during the
     *         installation routine.
     */
    public String getSmtpSender() {
        return this.smtpSender;
    }

    /**
     * Set the sender address of the SMTP server which is set during the
     * installation routine.
     * 
     * @param smtpSender
     *            to set.
     */
    public void setSmtpSender(String smtpSender) {
        this.smtpSender = smtpSender;
    }
    
    /**
     * 
     * @return the sender address of the SMTP server which was set during the
     *         installation routine.
     */
    public String getSmtpSenderName() {
        return this.smtpSenderName;
    }

    /**
     * Set the sender address of the SMTP server which is set during the
     * installation routine.
     * 
     * @param smtpSender
     *            to set.
     */
    public void setSmtpSenderName(String smtpSenderName) {
        this.smtpSenderName = smtpSenderName;
    }
}
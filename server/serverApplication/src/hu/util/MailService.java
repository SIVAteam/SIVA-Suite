package hu.util;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * This class provides functionality to send emails. It connects to a SMTP mail
 * server given by the {@link Configuration} file to send emails to the given
 * address. All necessary information to connect to a mail server is stored in
 * the {@link Configuration} file. This information includes server address,
 * username, password and the SMTP-port.
 * Timeout for a connection to the mail server are 20 seconds.
 */
@ManagedBean(name = "mailService", eager = true)
@ApplicationScoped
public class MailService {
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    private static final String MAIL_HOST = "mail.smtp.host";
    private static final String MAIL_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_PASSWORD = "mail.smtp.password";
    private static final String SENDER_EMAIL = "sender.email";
    private static final String SENDER_NAME = "sender.name";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.connectiontimeout";
    
    private static final String CONF_SMTP_HOST = "smtp_host";
    private static final String CONF_SMTP_PORT = "smtp_port";
    private static final String CONF_SMTP_USER = "smtp_user";
    private static final String CONF_SMTP_PASSWORD = "smtp_password";
    private static final String CONF_SMTP_SENDER_EMAIL = "smtp_sender_email";
    private static final String CONF_SMTP_SENDER_NAME = "smtp_sender_name";
    private static final String CONF_SMTP_MAIL_AUTH = "smtp_mail_auth";
    private static final String CONF_SMTP_STARTTLS_ENABLE = "smtp_start_tls_enable";
    private static final int SMTP_TIMEOUT = 20000;
    private static final String EXCEPTION_WRONG_LOGIN_DATA = "Username and Password not accepted";
    private static final String WRONG_LOGIN_ERROR_MSG = "Username and/or password are wrong.";
    private static final String EXCEPTION_WRONG_HOST = "Unknown SMTP host";
    private static final String WRONG_HOST_ERROR_MSG = "Host not reachable. Please check hostname.";
    private static final String EXCEPTION_WRONG_PORT = "Could not connect to SMTP host";
    private static final String WRONG_PORT_ERROR_MSG = "It seems the given port is wrong.";
    private static final String EXCEPTION_TIMEOUT = "connect timed out";
    private static final String TIMEOUT_ERROR_MSG = "Could not establish connection. Maybe hostname and/or port are wrong.";
    private static final String NOT_CLOSED_ERROR_MSG = "The mail connection could not be closed. Normally emails was be send correctly. Please check if emails were received.";
    private String smtpHost;
    private int smtpPort;
    private int smtpTimeout;
    private String smtpUsername;
    private String smtpPassword;
    private String senderEmail;
    private String senderName;
    private boolean mailAuth;
    private boolean mailStartTlsEnable;

    private Properties mailProperties;
    private Session mailSession;
    private Transport mailTransport;

    /**
     * Set {@link Configuration} using injection to retrieve mail server
     * connection information.
     * 
     * @param configuration
     *            to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        smtpHost = configuration.getString(CONF_SMTP_HOST);
        smtpPort = configuration.getInteger(CONF_SMTP_PORT);
        smtpUsername = configuration.getString(CONF_SMTP_USER);
        smtpTimeout = SMTP_TIMEOUT;
        smtpPassword = configuration.getString(CONF_SMTP_PASSWORD);
        senderEmail = configuration.getString(CONF_SMTP_SENDER_EMAIL);
        senderName = configuration.getString(CONF_SMTP_SENDER_NAME);
        mailAuth = Boolean.parseBoolean(configuration
                .getString(CONF_SMTP_MAIL_AUTH));
        mailStartTlsEnable = Boolean.parseBoolean(configuration
                .getString(CONF_SMTP_STARTTLS_ENABLE));
    }

    /**
     * Send an email to given recipient and close the connection.
     * 
     * @param recipient
     *            of the email.
     * @param subject
     *            of the email.
     * @param body
     *            is the content of the email.
     * @return true if sending was successful, throw 
     * {@link MailServiceException} if not.
     * 
     * @throws MailServiceException 
     */
    public synchronized boolean sendMail(String recipient, String subject, String body) {
        if (openConnection()) {
            try {
                InternetAddress[] mailAddresses = InternetAddress
                        .parse(recipient);
                Message message = new MimeMessage(mailSession);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, mailAddresses);
                message.setSubject(subject);
                message.setText(body);
                mailTransport.sendMessage(message, mailAddresses);
            } catch (MessagingException e) {
                throw new MailServiceException();
            }
        }
        try {
            mailTransport.close();
        } catch (MessagingException e) {
            throw new MailServiceException(NOT_CLOSED_ERROR_MSG);
        }
        return true;
    }
    
    /**
     * This method is for multiple mail sending. All e-mail adresses are given 
     * in a {@link Map} as key. The Value is for a specific text-element which 
     * will be replaced in the body string. 
     * 
     * @param recipientMap 
     *          stores in the key a e-mail adress, in the value e.g.
     *          a token.
     * @param subject 
     *          for every e-mail.
     * @param body 
     *          text for every e-mail. The pattern $input$ will be replaced
     *          by the specific value. 
     * @return true if all mails was send without Exceptions, otherwise throw
     *         {@link MailServiceException};      
     * @throws MailServiceException 
     */
    public synchronized boolean sendMultipleMail(Map<String, String> recipientMap, String subject, String body) {
        if (openConnection()) {
            Message message = new MimeMessage(mailSession);
            
            for (Entry<String, String> recipient : recipientMap.entrySet()) {
                try {
                    InternetAddress[] mailAddresses = InternetAddress
                            .parse(recipient.getKey());
                    message.setFrom(new InternetAddress(senderEmail, senderName));
                    message.setRecipients(Message.RecipientType.TO, mailAddresses);
                    message.setSubject(subject);
                    message.setText(body.replace("$input$", recipient.getValue()));
                    mailTransport.sendMessage(message, mailAddresses);
                } catch (MessagingException e) {
                    throw new MailServiceException();
                } catch (UnsupportedEncodingException e) {
                    throw new MailServiceException();
		}
            }
        }
        try {
            mailTransport.close();
        } catch (MessagingException e) {
            throw new MailServiceException(NOT_CLOSED_ERROR_MSG);
        }
        return true;
    }
    
    /**
     * Open a connection to the mail-server.
     * @return true if the connection is ready for transmission, otherwise
     *         throw {@link MailServiceException}.
     * 
     * @throws MailServiceException 
     */
    private synchronized boolean openConnection() {
        mailProperties = new Properties();
        mailProperties.put(MAIL_HOST, smtpHost);
        mailProperties.put(MAIL_SMTP_AUTH, mailAuth);
        mailProperties.put(MAIL_PORT, smtpPort);
        mailProperties.put(MAIL_SMTP_STARTTLS_ENABLE, mailStartTlsEnable);
        mailProperties.put(SENDER_EMAIL, senderEmail);
        mailProperties.put(SENDER_NAME, senderName);
        mailProperties.put(MAIL_SMTP_PASSWORD, smtpPassword);
        mailProperties.put(MAIL_SMTP_TIMEOUT, smtpTimeout);

        mailProperties.put("mail.debug", "false");
        mailProperties.put("mail.debug.auth", "false");
        mailSession = Session.getInstance(mailProperties);

        try {
            mailTransport = mailSession.getTransport("smtp");
            mailTransport.connect(smtpHost, smtpPort, smtpUsername,
                    smtpPassword);
        } catch (MessagingException e) {
            String error = e.toString();
            if (error.contains(EXCEPTION_WRONG_LOGIN_DATA)) {
                throw new MailServiceException(WRONG_LOGIN_ERROR_MSG);
            } else if (error.contains(EXCEPTION_WRONG_HOST)) {
                throw new MailServiceException(WRONG_HOST_ERROR_MSG);
            } else if (error.contains(EXCEPTION_WRONG_PORT)) {
                throw new MailServiceException(WRONG_PORT_ERROR_MSG);
            } else if (error.contains(EXCEPTION_TIMEOUT)) {
                throw new MailServiceException(TIMEOUT_ERROR_MSG);
            } else {
                throw new MailServiceException();
            }
        }
        return true;
    }
}
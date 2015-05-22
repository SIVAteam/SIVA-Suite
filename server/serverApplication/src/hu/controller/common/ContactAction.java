package hu.controller.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.backingbeans.common.ContactBean;
import hu.controller.AController;
import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.IUserStore;
import hu.util.MailService;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

/**
 * This class provides the functionality to send the input of the contact form
 * to a predefined email address.
 */
@ManagedBean
@RequestScoped
public class ContactAction extends AController {
    private static final String MSG_CONTACT_MAIL_SUBJECT_PREFIX = "contact_mail_subject_prefix";
    private static final String MSG_CONTACT_MAIL_BODY = "contact_mail_body";
    private static final String MSG_CONTACT_MESSAGE_SENT = "contact_message_sent";
    
    private static final String CONTACT_FORM = "contactForm";
    
    @ManagedProperty("#{contactBean}")
    private ContactBean contactBean;

    @ManagedProperty("#{mailService}")
    private MailService mailService;

    @ManagedProperty("#{PersistenceProvider}")
    private IPersistenceProvider persistenceProvider;

    /**
     * Set {@link ContactBean} using injection.
     * 
     * @param contactBean
     *            to inject.
     */
    public void setContactBean(ContactBean contactBean) {
        this.contactBean = contactBean;
    }

    /**
     * Set {@link MailService} using injection.
     * 
     * @param mailService
     *            to inject.
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
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
     * Send a message entered in the contact form to the system administrator.
     * 
     * All {@link EUserType}s of users have permission to use.
     * 
     * @return the next page to show as {@link String}.
     */
    public String sendMessage() {
        IUserStore ustore = this.persistenceProvider.getUserStore();
        ContactBean cb = this.contactBean;

        // Fetch all administrators to send contact email to.
        List<User> administrators = ustore.getByType(EUserType.Administrator);
        Map<String, String> recipients = new HashMap<String, String>();
        for (User admin : administrators) {
            recipients.put(admin.getEmail(), "");
        }

        // Format given name.
        String name = null;
        if (cb.getAcademicTitle() != null && !cb.getAcademicTitle().equals("")) {
            name = String.format("%s, %s, %s",
                    cb.getLastName(), cb.getFirstName(), cb.getAcademicTitle());
        } else {
            name = String.format("%s, %s", cb.getLastName(), cb.getFirstName());
        }

        // Create subject and body for contact email.
        String subject = this.getCommonMessage(MSG_CONTACT_MAIL_SUBJECT_PREFIX) + cb.getSubject();
        String body = String.format(this.getCommonMessage(MSG_CONTACT_MAIL_BODY),
                name, cb.getEmail(), cb.getSubject(), cb.getMessage());

        // Send contact email.
        this.mailService.sendMultipleMail(recipients, subject, body);
        this.sendMessageTo(CONTACT_FORM, this.getCommonMessage(MSG_CONTACT_MESSAGE_SENT));

        // Clear form.
        cb.setAcademicTitle(null);
        cb.setEmail(null);
        cb.setFirstName(null);
        cb.setLastName(null);
        cb.setMessage(null);
        cb.setSubject(null);

        return null;
    }
}
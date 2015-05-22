package hu.util;

import hu.model.users.EUserType;
import hu.model.users.User;
import hu.persistence.IPersistenceProvider;
import hu.persistence.postgres.PgPersistenceProvider;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * This class implements a {@link PhaseListener} and ensures that users only
 * gain access to facelets they are authorized to use.
 */
public class AccessRestrictionEnforcer implements PhaseListener {
    private static final long serialVersionUID = 1L;
    private final Map<String, EUserType> minimumAccessLevels = new HashMap<String, EUserType>();

    public AccessRestrictionEnforcer() {

        ResourceBundle bundle = ResourceBundle.getBundle(
                "hu.configuration.AccessLevels", FacesContext
                        .getCurrentInstance().getViewRoot().getLocale());

        Enumeration<String> keys = bundle.getKeys();

        // Put the key-value pairs to a map to be hold in the internal memory
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            minimumAccessLevels.put(key,
                    Enum.valueOf(EUserType.class, bundle.getString(key)));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPhase(PhaseEvent group) {

        FacesContext cxt = group.getFacesContext();
        ELContext elcxt = cxt.getELContext();
        NavigationHandler nh = cxt.getApplication().getNavigationHandler();
        String viewId = FacesContext.getCurrentInstance().getViewRoot()
                .getViewId();

        // Get the {@link SessionData} of the current users session
        SessionData sessionData = (SessionData) cxt.getApplication()
                .getELResolver().getValue(elcxt, null, "sessionData");

        // Get the {@link IPersistenceProvider} out of the context
        IPersistenceProvider persistenceProvider = (PgPersistenceProvider) cxt
                .getApplication().getELResolver()
                .getValue(elcxt, null, "PersistenceProvider");
        User user;

        // Check if logged in user does still exist and log out if not
        if (sessionData.getUserId() != null
                && persistenceProvider.getUserStore().findById(
                        sessionData.getUserId()) == null) {
            sessionData.setUserId(null);
        }

        // Check access levels and redirect if user level is not sufficient
        if (minimumAccessLevels.get(viewId) != null) {
            if (sessionData.getUserId() != null) {
                user = persistenceProvider.getUserStore().getById(
                        sessionData.getUserId());

                if (minimumAccessLevels.get(viewId).getLevel() > user
                        .getUserType().getLevel()) {
                    nh.handleNavigation(cxt, null, "restrictionError");
                }
            } else if (sessionData.getUserId() == null
                    && minimumAccessLevels.get(viewId) != EUserType.Anonymous) {
                nh.handleNavigation(cxt, null, "login");
            }
        } else {
            nh.handleNavigation(cxt, null, "restrictionError");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforePhase(PhaseEvent group) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
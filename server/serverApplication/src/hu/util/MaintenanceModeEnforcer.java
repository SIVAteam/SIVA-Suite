package hu.util;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * This class implements a {@link PhaseListener} and ensures that access to the
 * application is disabled while the maintenance mode is active.
 */
public class MaintenanceModeEnforcer implements PhaseListener {

    private static final long serialVersionUID = 1L;
    private static final String MAINTENANCE_FACELET = "/xhtml/errors/maintenance";

    @Override
    public void afterPhase(PhaseEvent group) {
        FacesContext fctx = group.getFacesContext();

        // Get current configuration from context
        Configuration configuration = fctx.getApplication()
                .evaluateExpressionGet(fctx, "#{configuration}",
                        Configuration.class);

        if (configuration.getBoolean("maintenance_mode")) {
            String view = fctx.getViewRoot().getViewId();
            if (!view.contains(MAINTENANCE_FACELET)) {
                fctx.getApplication().getNavigationHandler()
                        .handleNavigation(fctx, null, MAINTENANCE_FACELET);
                return;
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent arg0) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
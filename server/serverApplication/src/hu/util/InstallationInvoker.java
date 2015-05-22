package hu.util;

import hu.persistence.IPersistenceProvider;
import hu.persistence.PersistenceSetupException;
import hu.persistence.postgres.PgPersistenceProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * This class implements a {@link PhaseListener} to invoke the installation
 * wizard in case the application's configuration does not exist.
 */
public class InstallationInvoker implements PhaseListener {
    private static final String CFG_IS_INSTALLED = "is_installed";
    private static final long serialVersionUID = 1L;
    private static final String INSTALLATION_FACELET = "/xhtml/common/installation";
    private static final String COMMON_ERROR_FACELET = "/xhtml/errors/commonError";
    private static final String UPDATE_SQL_PATH = "/WEB-INF/classes/hu/persistence/postgres/schema/updateSchema.sql";

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPhase(PhaseEvent group) {
    	FacesContext fctx = group.getFacesContext();

        // No dependency injection into PhaseListener.
        Configuration configuration = fctx.getApplication()
                .evaluateExpressionGet(fctx, "#{configuration}",
                        Configuration.class);

        if (!configuration.getBoolean(CFG_IS_INSTALLED)) {
            String view = fctx.getViewRoot().getViewId();
            if (!view.contains(INSTALLATION_FACELET)) {
                fctx.getApplication().getNavigationHandler()
                        .handleNavigation(fctx, null, INSTALLATION_FACELET);
                return;
            }
            return;
        }
        
        // Check for database updates and perform them if available
        Scanner sc = null;
        try {
        	sc = new Scanner(new File(fctx.getExternalContext().getRealPath(UPDATE_SQL_PATH)));
		} catch (FileNotFoundException ignore) {
			fctx.getApplication().getNavigationHandler()
            .handleNavigation(fctx, null, COMMON_ERROR_FACELET);
			return;
		}
        
        // Check if update file is not empty an perform update if not
        if(sc != null && sc.hasNextLine() && !sc.nextLine().trim().isEmpty()){
        	sc.close();
        	// No dependency injection into PhaseListener.
            IPersistenceProvider persistenceProvider = fctx.getApplication()
                    .evaluateExpressionGet(fctx, "#{PersistenceProvider}",
                            PgPersistenceProvider.class);
            try {
				persistenceProvider.getSetup().update();
			} catch (PersistenceSetupException e) {
				fctx.getApplication().getNavigationHandler()
                .handleNavigation(fctx, null, COMMON_ERROR_FACELET);
				return;
			}
            
            // Empty update file to prevent from further executing
            PrintWriter writer = null;
			try {
				writer = new PrintWriter(new File(fctx.getExternalContext().getRealPath(UPDATE_SQL_PATH)));
				writer.print("");
	            writer.close();
			} catch (FileNotFoundException ignore) {
				// ignore as there must be a redirect earlier
			}
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforePhase(PhaseEvent arg0) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}

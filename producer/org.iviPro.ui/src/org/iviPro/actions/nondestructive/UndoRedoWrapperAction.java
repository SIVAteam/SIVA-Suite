package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
/**
 * Wrapperaction für Undo/Redo Buttons in der Toolbar die es ermöglicht,
 * eigene Icons zu setzen. Führt die übergebenen Standardaktionen von 
 * Eclipse aus.
 * 
 * @author langa
 *
 */
public class UndoRedoWrapperAction extends Action implements IWorkbenchAction {

	public final static String ID = UndoRedoWrapperAction.class.getName();
	private IAction myAction;
	
	public UndoRedoWrapperAction (IWorkbenchWindow window, IAction action) {
		myAction = action;
		setToolTipText(myAction.getToolTipText());
		// Propertychangelistener auf 'enabled' um den Status der Action zu setzen
		myAction.addPropertyChangeListener(new IPropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals("enabled")) { //$NON-NLS-1$
					setEnabled((Boolean)event.getNewValue());
				}
			}
		});
		setEnabled(false);
	}
	
	
	@Override
	public void run() {
		myAction.run();
	}


	@Override
	public void dispose() {
		
	}

}

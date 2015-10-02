package org.iviPro.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.iviPro.application.Application;
import org.iviPro.model.BeanList;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Scene;
import org.iviPro.operations.OperationHistory;

/**
 * Abstrakte Basisklasse fuer alle Editoren. Diese registriert z.B. automatisch
 * die Undo/Redo Action-Handler im globalen Undo-Context und erledigt andere
 * Standard-Aufgaben.
 * 
 * @author dellwo
 * 
 */
public abstract class IAbstractEditor extends EditorPart implements
		PropertyChangeListener {

	/**
	 * Action-Handler registrieren z.B. fuer Undo/Redo.
	 */
	protected void createGlobalActionHandlers() {
		IUndoContext undoContext = OperationHistory.getContext();
		IEditorSite site = getEditorSite();
		// set up action handlers that operate on the current context
		UndoActionHandler undoAction = new UndoActionHandler(site, undoContext);
		RedoActionHandler redoAction = new RedoActionHandler(site, undoContext);
		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
				undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
				redoAction);
	}

	@Override
	public final void createPartControl(Composite parent) {
		registerAsListener();
		createGlobalActionHandlers();
		createPartControlImpl(parent);
	}
	
	/**
	 * Registers the editor as listener to be notified of changes to the media
	 * and scene repositories.
	 */
	private void registerAsListener() {
		Application.getCurrentProject().getMediaObjects().addPropertyChangeListener(this);
		// Need to listen for deletion of an AudioPart/Scene separately;
		// they are not part of the projects' media objects list themselves
		Object keyObject = getKeyObject();
		if (keyObject instanceof AudioPart) {
			((AudioPart)keyObject).getAudio().getAudioParts()
			.addPropertyChangeListener(this);
		} else if (keyObject instanceof Scene) {
			((Scene)keyObject).getVideo().getScenes()
			.addPropertyChangeListener(this);
		}
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	@Override
	public void dispose() {
		Application.getCurrentProject().getMediaObjects().removePropertyChangeListener(this);
		Object keyObject = getKeyObject();
		if (keyObject instanceof AudioPart) {
			((AudioPart)keyObject).getAudio().getAudioParts()
			.removePropertyChangeListener(this);
		} else if (keyObject instanceof Scene) {
			((Scene)keyObject).getVideo().getScenes()
			.removePropertyChangeListener(this);
		}
		super.dispose();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// Close editor when key object or its parent object is deleted
		if (event.getPropertyName().equals(BeanList.PROP_ITEM_REMOVED)) {
			Object deleted = event.getNewValue();
			IAbstractBean keyObject = getKeyObject();
			if (deleted != null && (keyObject == deleted
					|| (keyObject instanceof AudioPart
						&& ((AudioPart)keyObject).getAudio() == deleted)
					|| (keyObject instanceof Scene
						&& ((Scene)keyObject).getVideo() == deleted))) {
				getSite().getPage().closeEditor(this, false);
			}
		}
	}
	
	/**
	 * Initiate an event to let the Editpart update it's dirty status.
	 */
	public void updateDirtyStatus() {
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	/**
	 * Erstellt die SWT-Controls fuer diesen Editor.
	 * 
	 * @param parent
	 *            Das Parent-Composite.
	 */
	protected abstract void createPartControlImpl(Composite parent);
	
	/**
	 * Returns the key media object of this editor. The key object is the media 
	 * object the editor necessarily needs to perform its task (e.g. the video 
	 * object for which a scene editor creates scenes). 
	 */
	protected abstract IAbstractBean getKeyObject();
}

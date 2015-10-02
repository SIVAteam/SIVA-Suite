package org.iviPro.actions.nondestructive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.iviPro.editors.audioeditor.AudioEditor;
import org.iviPro.editors.audioeditor.AudioEditorInput;
import org.iviPro.editors.sceneeditor.DefineScenesEditorInput;
import org.iviPro.editors.sceneeditor.SceneEditor;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Audio;
import org.iviPro.model.resources.AudioPart;
import org.iviPro.model.resources.Picture;
import org.iviPro.model.resources.Video;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaTreeLeaf;
import org.iviPro.views.scenerepository.SceneRepository;
import org.iviPro.views.scenerepository.SceneTreeLeaf;

public class OpenEditorAction extends Action implements
		ISelectionListener, IWorkbenchAction {

	public final static String ID = OpenEditorAction.class.getName();

	private final IWorkbenchWindow window;

	/**
	 * Enthaelt den Namen des gerade ausgewaehlten Videos. Wird in der
	 * Listener-Methode selectionChanged() gesetzt.
	 */
	private IAbstractBean selection;

	/**
	 * Erstellt eine neue OpenSceneEditor Action.
	 * 
	 * @param window
	 */
	public OpenEditorAction(IWorkbenchWindow window) {
		this.window = window;
		setEnabled(false);
		setId(ID);
		setText(Messages.DefineScenes_DefineScene);
		setToolTipText(Messages.DefineScenes_DefineScenesToolTip);
		setImageDescriptor(Icons.ACTION_EDITOR_SCENE.getImageDescriptor());
		setDisabledImageDescriptor(Icons.ACTION_EDITOR_SCENE
				.getDisabledImageDescriptor());
		window.getSelectionService().addSelectionListener(this);
	}

	/**
	 * wird verwendet, wenn die Action direkt aufgerufen wird z.B. über Doppelklick
	 * 
	 * @param window
	 * @param video
	 */
	public OpenEditorAction(IWorkbenchWindow window, Video video) {
		this.window = window;
		this.selection = video;
	}
	
	/**
	 * wird verwendet, wenn die Action direkt aufgerufen wird z.B. über Doppelklick
	 * 
	 * @param window
	 * @param video
	 */
	public OpenEditorAction(IWorkbenchWindow window, SceneRepository sceneRep) {
		this.window = window;
		this.selection = sceneRep.getCurrentSelection().getVideo();
	}

	/**
	 * Die eigentliche Ausführungslogik der Action.
	 */
	@Override
	public void run() {

		// bei einem Video wird der Video Editor geöffnet
		if (selection instanceof Video) {
			// Input-Daten fuer Szenen-Editor erstellen.
			DefineScenesEditorInput input = new DefineScenesEditorInput((Video) selection);
			try {
				// Szenen-Editor oeffnen.
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(input, SceneEditor.ID, true);	
	
				// falls das Video noch keine Szene besitzt öffne automatisch einen SceneDefineWidget zum Erstellen
				// einer neuen Scene
				if (((Video) selection).getScenes().size() == 0) {
					if (page.getActiveEditor() instanceof SceneEditor) {
						((SceneEditor) page.getActiveEditor()).createNewScene();
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		} else
		
		// bei einem Audio/Audio-Part wird der Audio Editor geöffnet
		if (selection instanceof Audio || selection instanceof AudioPart) {
			
			if (selection instanceof AudioPart) {
				selection = ((AudioPart) selection).getAudio();
			}
			// Input-Daten fuer Audio-Editor erstellen.
			AudioEditorInput input = new AudioEditorInput((Audio) selection);
			try {
				// Audio-Editor oeffnen.
				IWorkbenchPage page = window.getActivePage();
				page.openEditor(input, AudioEditor.ID, true);	
	
				// falls das Audio-File noch keine Audio Parts besitzt öffne automatisch einen AudioPartDefineWidget zum Erstellen
				// eines neuen Audio-Part
				if (((Audio) selection).getAudioParts().size() == 0) {
					if (page.getActiveEditor() instanceof AudioEditor) {
						((AudioEditor) page.getActiveEditor()).createNewAudioPart();
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void dispose() {
		window.getSelectionService().removeSelectionListener(this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection newSselection) {
		// Wird aufgerufen, wenn der Benutzer ein Element im Media-Repository
		// auswaehlt oder die Auswahl verändert.
		// Hat der Benutzer einen Film ausgewaehlt, dann soll der
		// SzenenEditor-Button aktiviert werden.

		IStructuredSelection incoming = (IStructuredSelection) newSselection;
		if (incoming.getFirstElement() instanceof MediaTreeLeaf) {
			// Eingehendes Medienobjekt muss ein Video ist
			IAbstractBean mo = ((MediaTreeLeaf) incoming.getFirstElement())
					.getMediaObject();
			if (mo instanceof Video) {
				selection = mo;				
				setEnabled(true); 
				setText(Messages.DefineScenes_DefineScene);
				setToolTipText(Messages.DefineScenes_DefineScenesToolTip);
			} else 
			if (mo instanceof Audio) {
				selection = mo;				
				setEnabled(true); 
				setText(Messages.DefineAudioPart_DefineAudioPart);
				setToolTipText(Messages.DefineAudioParts_DefineAudioPartToolTip);
			} else
			if (mo instanceof AudioPart) {
				selection = ((AudioPart) mo).getAudio();
				setEnabled(true);
				setText(Messages.DefineAudioPart_DefineAudioPart);
				setToolTipText(Messages.DefineAudioParts_DefineAudioPartToolTip);
			} else
			if (mo instanceof Picture) {
				selection = (Picture) mo;
				setEnabled(false);
				setText(Messages.DefinePicture_DefinePicture);
				setToolTipText(Messages.DefinePicture_DefinePictureToolTip);	
			} else {
				setEnabled(false);
			}

		} else if (incoming.getFirstElement() instanceof SceneTreeLeaf) {
			// Eingehende Objekt ist eine Szene
			selection = ((SceneTreeLeaf) incoming.getFirstElement()).getScene().getVideo();
			setText(Messages.DefineScenes_DefineScene);
			setToolTipText(Messages.DefineScenes_DefineScenesToolTip);
			setEnabled(true);
		} else {
			// Selektion ist weder Video noch Szene => Action nicht anwendbar.
			setEnabled(false);
		}
	}

}

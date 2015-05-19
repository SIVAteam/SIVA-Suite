package org.iviPro.operations.graph;

import java.util.Locale;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.iviPro.application.Application;
import org.iviPro.model.LocalizedString;
import org.iviPro.model.graph.AbstractNodeSelectionControl;
import org.iviPro.model.resources.Picture;
import org.iviPro.operations.IAbstractOperation;

/**
 * Operation zum Aendern der Daten eines Selektions-Knotens.
 * 
 * @author dellwo
 * 
 */
public class ModifyAbstractNodeSelectionControlOperation extends IAbstractOperation {

	private final AbstractNodeSelectionControl target;
	private final LocalizedString newTitle;
	private final LocalizedString oldTitle;
	private final Picture newButtonImage;
	private final Picture oldButtonImage;

	/**
	 * @param target
	 * @param newTitle
	 * @throws IllegalArgumentException
	 */
	public ModifyAbstractNodeSelectionControlOperation(AbstractNodeSelectionControl target,
			String newTitle, Picture newButtonImage)
			throws IllegalArgumentException {
		this(target, new LocalizedString(newTitle, Application
				.getCurrentLanguage()), newButtonImage);
	}

	/**
	 * Erstellt eine neue Operation zum Aendern des Titels eines Model-Objekts.
	 * Als Sprache für den Titel wird die aktuelle Projekt-Sprache verwendet.
	 * 
	 * @param target
	 *            Das Model-Objekt dessen Titel geaendert werden soll.
	 * @param newTitle
	 *            Der neue Titel in der aktuellen Projekt-Sprache.
	 * @param newDefaultControl
	 *            Das neue Default-Control oder null, falls kein
	 *            Default-Control.
	 * @param newButtonType
	 *            Der neue Button-Typ.
	 * @param newScreenArea
	 *            Der neue Anzeigebereich.
	 * @throws IllegalArgumentException
	 *             Falls einer der obigen Parameter (ausser newDefaultControl)
	 *             null ist.
	 */
	public ModifyAbstractNodeSelectionControlOperation(AbstractNodeSelectionControl target,
			LocalizedString newTitle, Picture newButtonImage)
			throws IllegalArgumentException {
		super(Messages.ModifyAbstractNodeSelectionControlOperation_Title);
		if (target == null || (newTitle == null && newButtonImage == null)) {
			throw new IllegalArgumentException(
					"Target and title resp. button-image must not be null."); //$NON-NLS-1$
		}
		Locale lang = Application.getCurrentLanguage();
		this.newTitle = newTitle;
		this.oldTitle = new LocalizedString(target.getTitle(lang), lang);
		this.newButtonImage = newButtonImage;
		this.oldButtonImage = target.getButtonImage();
		this.target = target;
	}

	@Override
	public boolean canExecute() {
		return target != null && (newTitle != null || newButtonImage != null);
	}

	@Override
	public String getErrorMessage(Exception e) {
		return Messages.ModifyAbstractNodeSelectionControlOperation_ErrorMsg + e.getMessage();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(newTitle);
		target.setButtonImage(newButtonImage);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		target.setTitle(oldTitle);
		target.setButtonImage(oldButtonImage);
		return Status.OK_STATUS;
	}

}

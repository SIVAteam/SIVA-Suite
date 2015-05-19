package org.iviPro.operations.global;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.iviPro.operations.IAbstractOperation;
import org.iviPro.theme.Icons;
import org.iviPro.views.mediarepository.MediaRepository;
import org.iviPro.views.mediarepository.MediaTreeGroup;

//FIXME nicht fertig(nur für Testzwecke um ordnerstruktur zu speichern, wenn nicht-->nur newfolderAction benutzen
public class NewFolderInMediaRepOperation extends IAbstractOperation {
	TreeViewer treeviewer;
	MediaRepository rep;//necessary for updating the rep view
	
	public NewFolderInMediaRepOperation(TreeViewer treeviewer, MediaRepository rep){
		super("newFolderInMediaRepository");//TODO
		this.treeviewer = treeviewer; //FIXME manu anstatt treeviewer nur firstelemnt übergeben
		this.rep = rep;
		
	}

	@Override
	public String getErrorMessage(Exception e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canExecute() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {

		return redo(monitor, info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// falls das selektierte Objekt ein MediaLeaf ist, prüfe ob das
				// MediaObject
				// Objekt mit dem selektierten übereinstimmt
				if (((IStructuredSelection) treeviewer.getSelection()).getFirstElement() instanceof MediaTreeGroup) {
					MediaTreeGroup leaf = (MediaTreeGroup) ((IStructuredSelection) treeviewer
							.getSelection()).getFirstElement();
					MediaTreeGroup folder = new MediaTreeGroup(leaf, "New Folder", Icons.VIEW_FOLDER);
					leaf.addElement(folder);
					
					rep.updateTreeviewer();
				
				}

		
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		// TODO Auto-generated method stub
		return Status.OK_STATUS;
	}

}

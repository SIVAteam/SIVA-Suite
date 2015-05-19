package org.iviPro.application;

import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.EditorInputTransfer;

public class EditorAreaDropAdapter extends DropTargetAdapter {
	
	public EditorAreaDropAdapter(IWorkbenchWindow window) {
	}

	/*public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	public void dragOperationChanged(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}*/	
	
	public void handleDrop(IWorkbenchPage page, DropTargetEvent event){
		if(EditorInputTransfer.getInstance().isSupportedType(event.currentDataType)){
			EditorInputTransfer.EditorInputData[] editorInputs = (EditorInputTransfer.EditorInputData []) event.data;
			for(int i = 0; i < editorInputs.length; i++){
				IEditorInput editorInput = editorInputs[i].input;
				String editorId = editorInputs[i].editorId;
				openEditor(page, editorInput, editorId);
			}
		}
	}

	private void openEditor(IWorkbenchPage page, IEditorInput editorInput,
			String editorId) {
		// TODO Auto-generated method stub
		
	}
}

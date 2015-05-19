package org.iviPro.editors.scenegraph.editparts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.iviPro.editors.scenegraph.SceneGraphEditor;
import org.iviPro.model.graph.Graph;
import org.iviPro.model.graph.IConnection;
import org.iviPro.model.graph.NodeCondSelection;
import org.iviPro.model.graph.NodeCondSelectionControl;
import org.iviPro.model.graph.NodeEnd;
import org.iviPro.model.graph.NodeQuiz;
import org.iviPro.model.graph.NodeQuizControl;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.model.graph.NodeResume;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.model.graph.NodeSelection;
import org.iviPro.model.graph.NodeSelectionControl;
import org.iviPro.model.graph.NodeStart;

/**
 * Factory die zu einem Model-Element den entsprechenden Edit-Part erstellt.
 * 
 * @author dellwo
 */
public class PartFactory implements org.eclipse.gef.EditPartFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart,
	 * java.lang.Object)
	 */
	public EditPart createEditPart(EditPart context, Object modelElement) {
		// get EditPart for model element
		EditPart part = getPartForElement(modelElement);
		// store model element in EditPart
		part.setModel(modelElement);
		return part;
	}

	/**
	 * Erstellt zu einem Model-Objekt den zugehoerigen EditPart.
	 * 
	 * @throws RuntimeException
	 *             Falls fuer dieses Model-Element kein EditPart bekannt ist,
	 *             was eigentlich nicht passieren sollte.
	 */
	private EditPart getPartForElement(Object modelElement) {
		if (modelElement instanceof Graph) {
			return new EditPartGraph();
		}
		if (modelElement instanceof NodeScene) {
			return new EditPartNodeScene();
		}
		if (modelElement instanceof NodeStart) {
			return new EditPartNodeStart();
		}
		if (modelElement instanceof NodeEnd) {
			return new EditPartNodeEnd();
		}
		if (modelElement instanceof NodeSelection) {
			return new EditPartNodeSelection();
		}
		if (modelElement instanceof NodeSelectionControl) {
			return new EditPartNodeSelectionControl();
		}
		if (modelElement instanceof NodeCondSelection) {
			return new EditPartNodeCondSelection();
		}
		if (modelElement instanceof NodeCondSelectionControl) {
			return new EditPartNodeCondSelectionControl();
		}
		if (modelElement instanceof NodeQuiz) {
			return new EditPartNodeQuiz();
		}
		if (modelElement instanceof NodeQuizControl) {
			return new EditPartNodeQuizControl();
		}
		if (modelElement instanceof NodeRandomSelection) {
			return new EditPartNodeRandomSelection();
		}
		if (modelElement instanceof NodeResume) {
			return new EditPartNodeResume();
		}
		if (modelElement instanceof IConnection) {
			return new EditPartConnection();
		}

		throw new RuntimeException("Can't create part for model element: " //$NON-NLS-1$
				+ ((modelElement != null) ? modelElement.getClass().getName()
						: "null")); //$NON-NLS-1$
	}


	public static IEditPartNode getReferingEditPart(Object modelElement) {
		IEditPartNode result = null;

		//Hole SceneGraphEditor
		IEditorReference[] editorArray = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		IWorkbenchPart workbenchpart = null;
		for(int i=0; i < editorArray.length; i++) {
			String editorID = editorArray[i].getId();
			if(editorID.equals(SceneGraphEditor.ID)) {
				workbenchpart = editorArray[i].getPart(true);
				break;
			}
		}

		if(workbenchpart != null) {
			ScalableFreeformRootEditPart rootEditPart = ((SceneGraphEditor) workbenchpart).getRootEditPart();
			AbstractGraphicalEditPart editPartGraph = (AbstractGraphicalEditPart)rootEditPart.getChildren().get(0);

			//Vergleiche die Modelelemente und filtere passenden Editpart
			for(int i=0; i < editPartGraph.getChildren().size(); i++) {
				IEditPartNode editPartNode = (IEditPartNode)editPartGraph.getChildren().get(i);

				Object model = editPartNode.getModel();
				if(modelElement == model) {
					result = editPartNode;
					break;
				}
			}
		}
		return result;
	}
}
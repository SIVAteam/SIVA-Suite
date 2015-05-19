package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.global.ChangeTitleOperation;

/**
 * Editor for {@link IGraphNode IGraphNodes} for which a title has to be 
 * chosen. It extends the AbstractNodeEditor by adding a {@link TitleSelector} 
 * to the <code>contentComposite</code>. An empty title will be rejected by the
 * editor and trigger a warning message. 
 *  
 * @author John
 */
public class TitledNodeEditor extends AbstractNodeEditor {
	
	private static int WIDTH = 300;
	
	private final IGraphNode titledNode;
	protected TitleSelector titleComp;
	
	/**
	 * Constructor for title only editors. Uses title and minimum
	 * width defined in this class for the editor {@link Shell}.
	 * @param titledNode {@link IGraphNode} which should be edited
	 */
	public TitledNodeEditor(IGraphNode titledNode) {
		this(Messages.TitledNodeEditor_Shell_Title, titledNode, WIDTH);
	}
	
	/**
	 * Constructor for subclasses offering the possibility to define custom
	 * values for title and minimum width of the editor {@link Shell}.
	 * @param title title of the shell
	 * @param titledNode {@link IGraphNode} which should be edited
	 * @param minWidth desired minimum width of the shell
	 */
	public TitledNodeEditor(String title, IGraphNode titledNode, int minWidth) {
		super(title, titledNode.getNodeID(), minWidth);
		this.titledNode = titledNode;
		titleComp = new TitleSelector(contentComposite, titledNode);
	}
	
	@Override
	protected boolean validateInput() {
		if (titleComp.getTitle().isEmpty()) {
			showWarning(Messages.TitledNodeEditor_WarnMessage);
			return false;
		}
		return true;
	}

	@Override
	protected void executeChangeOperation() {
		ChangeTitleOperation operation = new ChangeTitleOperation(
				titledNode, titleComp.getTitle());
		try {
			OperationHistory.execute(operation);	
		} catch (ExecutionException e) {
			MessageDialog.openError(shell,
					Messages.Common_ErrorDialog_Title, e
							.getMessage());
		}	
	}
}

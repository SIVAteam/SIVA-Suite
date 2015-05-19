package org.iviPro.editors.scenegraph.subeditors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.iviPro.model.graph.IGraphNode;
import org.iviPro.model.graph.NodeRandomSelection;
import org.iviPro.operations.OperationHistory;
import org.iviPro.operations.graph.ModifyNodeRandomSelectionOperation;
import org.iviPro.utils.NumericInputListener;
import org.iviPro.utils.widgets.SizedText;

/**
 * Editor for <code>NodeRandomSelection</code> elements. Facilitates to set
 * a title for the respective element as well as to choose selection
 * probabilities for its successors. 
 * @author John
 *
 */
public class NodeRandomSelectionEditor extends TitledNodeEditor {
	private final NodeRandomSelection randomSelection;
	
	private static int WIDTH = 300;

	private Button equalButton;
	private Composite pathComp;
	private Label total;
	private Label totalProb;
	private ArrayList<Label> succTitles = new ArrayList<Label>();
	private ArrayList<SizedText> succProbs = new ArrayList<SizedText>();
	
	
	
	/**
	 * Instantiates an editor for the given <code>NodeRandomSelection</code> 
	 * element. 
	 * @param randomSelection <code>NodeRandomSelection</code> element which 
	 * should be edited
	 */
	public NodeRandomSelectionEditor(NodeRandomSelection randomSelection) {
		super(Messages.NodeRandomSelectionEditor_Shell_Title, 
				randomSelection, WIDTH);
		this.randomSelection = randomSelection;
				
		createProbabilityGroup(contentComposite);
	}
	
	/**
	 * Creates the probability group showing the successor elements of the
	 * related <code>NodeRandomSelection</code> and their assigned 
	 * selection probabilities.
	 * @param parent parent composite
	 */
	private void createProbabilityGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setText(Messages.NodeRandomSelectionEditor_Group_Path_Probability);
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		group.setLayoutData(gd);
				
		Composite equalComp = new Composite(group, SWT.NONE);
		equalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		equalComp.setLayout(new GridLayout(2, false));
		equalButton = new Button(equalComp, SWT.CHECK);
		Label equalLabel = new Label(equalComp, SWT.NONE);
		equalLabel.setText(
				Messages.NodeRandomSelectionEditor_Button_Equal_Probability);
		equalButton.setSelection(randomSelection.useEqualProbability());
		equalButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateProbabilityGroupStatus();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);				
			}
		});
		
		Label separator = new Label(group, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		
		Composite unequalComp = new Composite(group, SWT.NONE);
		unequalComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		GridLayout unequalLayout = new GridLayout(1, false);
		unequalLayout.verticalSpacing = 0;
		unequalLayout.marginWidth = 0;
		unequalLayout.marginHeight = 0;
		unequalComp.setLayout(unequalLayout);
				
		Composite headingComp = new Composite(unequalComp, SWT.NONE);
		headingComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		GridLayout headingLayout = new GridLayout(2, false);
		// Margin needed for placement of % sign because of border of pathComp
		headingLayout.marginRight = 2;
		headingComp.setLayout(headingLayout);
		
		Label pathTitle = new Label(headingComp, SWT.NONE);
		pathTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		pathTitle.setText(Messages.NodeRandomSelectionEditor_Label_Path_Title);
		Label probability = new Label(headingComp, SWT.CENTER);
		// GridData needed for setting the widthHint later
		GridData probGd = new GridData();
		probability.setLayoutData(probGd);
		probability.setText(
				"%"); //$NON-NLS-1$
		
		pathComp = new Composite(unequalComp, SWT.BORDER);
		pathComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, 
				true, false));
		GridLayout pathLayout = new GridLayout(2, false);
		pathComp.setLayout(pathLayout);
		
		// List connected nodes with probabilities
		int probFieldWidth = 0;
		for (IGraphNode successor : randomSelection.getChildren()) {
			Label succTitle = new Label(pathComp, SWT.NONE);
			GridData titleGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			succTitle.setLayoutData(titleGd);
			String title = successor.getTitle();
			if (title.length() > 38) {
				title = title.substring(0, 35) +  "..."; //$NON-NLS-1$
			}
			succTitle.setText(title);
			SizedText succProb = 
					new SizedText(pathComp, SWT.SINGLE | SWT.CENTER 
							| SWT.BORDER, 3);
			succProb.getTextField().addVerifyListener(new NumericInputListener());
			succProb.getTextField().addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					if (totalProb != null) {
						updateTotal();
					}
				}
			});
			succProb.getTextField().addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					Text textField = (Text)e.widget;
					if (textField.getText().isEmpty()) {
						textField.setText("0"); //$NON-NLS-1$
					}
					updateTotal();
				}
				
				@Override
				public void focusGained(FocusEvent e) {}
			});
			
			succProb.getTextField().addTraverseListener(new TraverseListener() {
				
				@Override
				public void keyTraversed(TraverseEvent e) {
					Text textField = (Text)(e.widget);
					if (e.detail == SWT.TRAVERSE_RETURN) {
						if (textField.getText().isEmpty()) {
							textField.setText("0"); //$NON-NLS-1$
						}
						updateTotal();
						// Set cursor before text
						textField.setSelection(0);
					}
				}
			});
			
			if (!randomSelection.useEqualProbability()) {
				int prob = randomSelection.getProbabilityMap().get(successor);
				succProb.setText(Integer.toString(prob));
			}
			probFieldWidth = succProb.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
							
			succTitles.add(succTitle);
			succProbs.add(succProb);
		}
		
		Label totalSep = new Label(pathComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		totalSep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 2, 1));	
		
		total = new Label(pathComp, SWT.NONE);
		GridData totalSepGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		total.setLayoutData(totalSepGd);
		total.setText(Messages.NodeRandomSelectionEditor_Label_Total);
		totalProb = new Label(pathComp, SWT.CENTER);
		GridData totalProbGd = new GridData();
		totalProb.setLayoutData(totalProbGd);
						
		probGd.widthHint = probFieldWidth;
		totalProbGd.widthHint = probFieldWidth;
		
		updateProbabilityGroupStatus();
	}
	
	/**
	 * Activates/deactivates all elements of the probability group according to
	 * whether or not the equal probability button is activated.
	 */
	private void updateProbabilityGroupStatus() {
		if (equalButton.getSelection()) {
			pathComp.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			totalProb.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			for (SizedText prob : succProbs) {
				prob.setEnabled(false);
				prob.getTextField().setBackground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				prob.getTextField().setText(""); //$NON-NLS-1$
			}
			for (Label title : succTitles) {
				title.setEnabled(false);
				title.setBackground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			}
			
			total.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			total.setEnabled(false);
			totalProb.setText(""); //$NON-NLS-1$
									
		} else {
			pathComp.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			totalProb.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			for (SizedText prob : succProbs) {
				prob.setEnabled(true);
				prob.getTextField().setBackground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_WHITE));
				if (prob.getText().isEmpty()) {
					prob.setText("0"); //$NON-NLS-1$
				}
			}
			for (Label title : succTitles) {
				title.setEnabled(true);
				title.setBackground(Display.getCurrent()
						.getSystemColor(SWT.COLOR_WHITE));
			}
			
			total.setBackground(Display.getCurrent()
					.getSystemColor(SWT.COLOR_WHITE));
			total.setEnabled(true);
			updateTotal();
		}	
	}
	
	/**
	 * Calculates the total probability of all paths.
	 * @return total probability of all paths
	 */
	private void updateTotal() {	
		int totalValue = 0;
		for (SizedText prob : succProbs) {
			if (!prob.getText().isEmpty()) {
				totalValue += Integer.parseInt(prob.getText()); 
			}
		}
		totalProb.setText(Integer.toString(totalValue));

	}
	
	@Override
	protected void clearWarning() {
		super.clearWarning();
	}
	
	@Override
	protected boolean validateInput() {
		if (!super.validateInput()) {
			return false;
		}
		if (!equalButton.getSelection() && !randomSelection.getChildren().isEmpty()) {
			if (Integer.parseInt(totalProb.getText()) != 100) {
				showWarning(Messages.NodeRandomSelectionEditor_Label_Warning_Total);
				return false;
			}
		}
		return true;
	};

	@Override
	protected void executeChangeOperation() {
		HashMap<IGraphNode, Integer> probMap = new HashMap<IGraphNode, Integer>();
		if (!equalButton.getSelection() && !randomSelection.getChildren().isEmpty()) {
			Iterator<SizedText> probIter = succProbs.iterator();
			for (IGraphNode successor : randomSelection.getChildren()) {
				Integer prob = Integer.parseInt(probIter.next().getText());
				probMap.put(successor, prob);
			}
		}
		ModifyNodeRandomSelectionOperation op = 
				new ModifyNodeRandomSelectionOperation(randomSelection,
						titleComp.getTitle(), equalButton.getSelection(),
						probMap);
		try {
			OperationHistory.execute(op);
		} catch (ExecutionException e) {
			MessageDialog.openError(shell,
					Messages.Common_ErrorDialog_Title, e
							.getMessage());
		}		
	}

}

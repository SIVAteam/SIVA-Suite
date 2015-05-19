package org.iviPro.editors.scenegraph.subeditors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.theme.Colors;

/**
 * Abstract editor class on which all graph node editors are based. It is 
 * implemented by a {@link Shell} (opened inside the current active shell) which
 * displays a title, the unique ID of the currently edited node and two
 * standard buttons. The desired behavior of the Ok button has to be 
 * defined in the subclasses by implementing {@link #validateInput()} and 
 * {@link #executeChangeOperation()}.
 * <br>
 * All widgets added by subclasses should be added to the 
 * <code>contentComposite</code>.
 *
 * @author John
 */
public abstract class AbstractNodeEditor {
	
	private static int SHELL_MARGINS = 10;
	private static int SHELL_SPACINGS = 10;
	
	protected Display display;
	protected Shell shell;
	protected Composite contentComposite;
	
	private Shell parentShell;
	/**
	 * Minimum desired width for the editor shell.
	 */
	private int width;
	/**
	 * A label used to show warning messages in the editor.
	 */
	private Label warning;
	/**
	 * GridData used to modify appearance of the warning message.
	 */
	private GridData warningGd; 
	
	/**
	 * Creates an editor displaying the given title and node ID. The 
	 * <code>minWidth</code> parameter specifies the minimum width of the 
	 * underlying {@link Shell}.
	 * @param title title of the shell
	 * @param id displayed node id
	 * @param width minimum width of the shell
	 */
	public AbstractNodeEditor(String title, int id, int width) {
		this.width = width;
		display = Display.getCurrent();
		parentShell = display.getActiveShell();
		shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(title);

		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = SHELL_SPACINGS;
		layout.verticalSpacing = SHELL_SPACINGS;
		layout.marginWidth = SHELL_MARGINS;
		layout.marginHeight = SHELL_MARGINS;		
		shell.setLayout(layout);
		
		// Content composite
		contentComposite = new Composite(shell, SWT.NONE);
		GridLayout contentLayout = new GridLayout(1, false);
		contentLayout.marginWidth = 0;		
		contentComposite.setLayout(contentLayout);
		contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		// Warning
		warning = new Label(shell, SWT.WRAP | SWT.LEFT);
		warning.setForeground(Colors.WARNING_FONT.getColor());
		warningGd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		warningGd.heightHint = 0;
		warning.setLayoutData(warningGd);
		warning.setVisible(false);
			
		// Id & Buttons
		Composite buttonPanel = new Composite(shell, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth = 0;
		buttonPanel.setLayout(buttonLayout);
		GridData panelGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		buttonPanel.setLayoutData(panelGd);
		
		Label idLabel = new Label(buttonPanel, SWT.LEFT);
		idLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		idLabel.setText(Messages.AbstractNodeEditor_Label_NodeID + Integer.toString(id));
		idLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));

		Button okButton = new Button(buttonPanel, SWT.PUSH);
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onOk();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		okButton.setText(Messages.Commonr_Button_OK);
		GridData okGd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		okGd.widthHint = 80;
		okButton.setLayoutData(okGd);
		Button cancelButton = new Button(buttonPanel, SWT.CANCEL);
		cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onCancel();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		cancelButton.setText(Messages.Common_Button_Cancel);
		GridData cancelGd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		cancelGd.widthHint = 80;
		cancelButton.setLayoutData(cancelGd);
		
		shell.setDefaultButton(okButton);	
	}
	
	/**
	 * Opens the editor.
	 */
	public void show() {				
		centerShell();
		shell.open();

		// Let the UI thread sleep when event loop is empty
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	/**
	 * Resizes the editor to respect the desired minimum size and centers it
	 * with regard to the parent shell.
	 */
	private void centerShell() {
		Rectangle parentBounds = parentShell.getBounds();
		Point size = shell.computeSize(width, SWT.DEFAULT);
		int x = parentBounds.x + (parentBounds.width - size.x) / 2;
		int y = parentBounds.y + (parentBounds.height - size.y) / 2;
		shell.setBounds(x, y, size.x, size.y);
		// Always retain the initial width of the shell.
		warningGd.widthHint = shell.getClientArea().width - 2*SHELL_MARGINS;
	}
	
	/**
	 * Shows the given message as warning message.
	 * @param msg warning to be shown
	 */
	protected void showWarning(String msg) {
		warning.setText(msg);
		warningGd.heightHint = warning.computeSize(warningGd.widthHint, SWT.DEFAULT, true).y;
		warning.setVisible(true);
		shell.layout(true);
		// Always retain the initial width of the shell.
		shell.setSize(shell.computeSize(width, SWT.DEFAULT));		
	}
	
	/**
	 * Hides the warning.
	 */
	protected void clearWarning() {
		// Hide label by setting its layout height to zero
		warningGd.heightHint = 0;
		warning.setVisible(false);
		shell.layout(true);
		shell.setSize(shell.computeSize(width, SWT.DEFAULT));
	}
	
	/**
	 * Checks if the changes performed in the editor are valid. By default this
	 * method is called in {@link AbstractNodeEditor#onOk()} before any change
	 * operations are executed to ensure only
	 * correct data is persisted.
	 * @return true if the data in the editor is valid - false otherwise
	 */
	protected abstract boolean validateInput();
	
	/**
	 * Creates and executes the change operation associated with the editor.
	 */
	protected abstract void executeChangeOperation();
	
	/**
	 * Defines the actions performed when the Ok button of the 
	 * editor is pressed.
	 */
	protected void onOk() {
		clearWarning();
		if (!validateInput()) {
			return;
		}
		executeChangeOperation();
		shell.close();		
	}
	
	/**
	 * Defines the actions performed when the Cancel button of the 
	 * editor is pressed.
	 */
	protected void onCancel() {
		shell.close();
	}
}

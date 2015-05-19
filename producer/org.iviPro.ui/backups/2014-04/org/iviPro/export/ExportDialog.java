package org.iviPro.export;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.iviPro.application.Application;
import org.iviPro.model.Project;
import org.iviPro.model.graph.NodeScene;
import org.iviPro.utils.PreferencesHelper;

public class ExportDialog {

	private static final String PREF_EXPORT_PATH = "PREF_EXPORT_PATH"; //$NON-NLS-1$
	private static final String SMIL = "SMIL";
	private Shell shell;
	private Text fieldPath;
	// For old export (not only SMIL) uncomment the following
//	private Combo fieldType;
	private Button okButton;
	private Button xmlButton;
	private boolean xmlButtonWasSelected = false;
	private Button zipButton;
	private final IWorkbenchWindow window;

	public ExportDialog(IWorkbenchWindow window) {
		this.window = window;
	}

	public void export() {

		// Default-Directory fuer Export
		String defaultDirectory = PreferencesHelper.getPreference(
				PREF_EXPORT_PATH, ""); //$NON-NLS-1$

		// Fenster erstellen
		Shell parentShell = window.getShell();
		Display display = parentShell.getDisplay();
		shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL
				| SWT.RESIZE);
		shell.setText(Messages.ExportDialog_WindowTitle);

		// Fenstergroesse und zentrieren
		Rectangle bounds = new Rectangle(0, 0, 480, 220);
		Rectangle parentBounds = parentShell.getBounds();
		int x = parentBounds.x + (parentBounds.width - bounds.width) / 2;
		int y = parentBounds.y + (parentBounds.height - bounds.height) / 2;
		bounds.x = x;
		bounds.y = y;
		shell.setBounds(bounds);

		// Layout
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.verticalSpacing = 10;
		shell.setLayout(layout);

		// Create components
		Composite parent = shell;
		Label label;
		GridData gd;

		// PATH
		label = new Label(parent, SWT.NONE);
		label.setText(Messages.ExportDialog_LabelPath);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		fieldPath = new Text(parent, SWT.SINGLE | SWT.BORDER);
		fieldPath.setText(defaultDirectory != null ? defaultDirectory : ""); //$NON-NLS-1$
		fieldPath.setLayoutData(gd);
		fieldPath.setEditable(false);
		final Button buttonSelectPath = new Button(parent, SWT.PUSH);
		buttonSelectPath.setText("..."); //$NON-NLS-1$
		buttonSelectPath.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String newPath = selectExportDirectory(buttonSelectPath
						.getText());
				if (newPath != null) {
					fieldPath.setText(newPath);
					okButton.setEnabled(true);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// For old export (not only SMIL) uncomment the following
		// TYPE
//		label = new Label(parent, SWT.NONE);
//		label.setText(Messages.ExportDialog_LabelExportType);
//		fieldType = createTypeCombo(parent);
//		fieldType.pack();
//		fieldType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		// NUR XML
		label = new Label(parent, SWT.None);
		label.setText(Messages.ExportDialog_Button_OnlyXML);
		xmlButton = new Button(parent, SWT.CHECK);
		xmlButton.setToolTipText(Messages.ExportDialog_Button_OnlyXML_Tooltip);
		xmlButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		
		// EXPORT ALS ZIP
		label = new Label(parent, SWT.None);
		label.setText(Messages.ExportDialog_Button_ZIP);
		zipButton = new Button(parent, SWT.CHECK);
		zipButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		zipButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (zipButton.getSelection()) {
					xmlButtonWasSelected = xmlButton.getSelection();
					xmlButton.setSelection(false);
					xmlButton.setEnabled(false);
				} else  {
					xmlButton.setEnabled(true);
					xmlButton.setSelection(xmlButtonWasSelected);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		// HORIZONTAL RULER
		Label ruler = new Label(shell, SWT.SEPARATOR | SWT.SHADOW_OUT
				| SWT.HORIZONTAL);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.verticalIndent = 5;
		gd.heightHint = 3;
		gd.horizontalSpan = 3;
		ruler.setLayoutData(gd);

		// BUTTONS
		Composite buttonPanel = new Composite(parent, SWT.NONE);
		buttonPanel.setLayout(new GridLayout(2, true));
		okButton = new Button(buttonPanel, SWT.PUSH);
		okButton.setEnabled(defaultDirectory != null);
		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				onOK();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		okButton.setText(Messages.ExportDialog_ButtonOK);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd.widthHint = 80;
		okButton.setLayoutData(gd);
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
		cancelButton.setText(Messages.ExportDialog_ButtonCancel);
		cancelButton.setLayoutData(gd);
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		gd.horizontalSpan = 3;
		buttonPanel.setLayoutData(gd);

		// Fenster oeffnen
		shell.setDefaultButton(cancelButton);
		shell.open();

		// Warten bis Fenster geschlossen wird.
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	private String selectExportDirectory(String defaultDirectory) {
		boolean done = false;
		while (!done) {
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			dialog.setMessage(Messages.ExportDialog_DirectoryDialogText);
			String path = dialog.open();
			if (path == null) {
				// Benutzer hat den Dialog abgebrochen.
				done = true;
			} else {
				File directory = new File(path);
				// Teste ob Verzeichnis valide ist.
				if (directory.exists() && directory.canWrite()
						&& directory.isDirectory()) {
					return directory.getAbsolutePath();
				} else {
					MessageDialog
							.openError(
									shell,
									Messages.ExportDialog_ErrorDirectoryDoesNotExist_Title,
									Messages.ExportDialog_ErrorDirectoryDoesNotExist_Text);
				}
			}
		}
		// Benutzer hat den Dialog abgebrochen und kein Verzeichnis gewaehlt.
		return null;
	}

	private void onOK() {

		// den DirectoryDialog mit dem Stil OPEN öffnen
		String path = fieldPath.getText();
		PreferencesHelper.storePreference(PREF_EXPORT_PATH, path);
		Project project = Application.getCurrentProject();

		// Old standard Export 
//		ExportType type = ExportType.fromDisplayString(fieldType.getText());
		ExportType type = ExportType.fromDisplayString(SMIL);

		// Keine Szenen-Knoten im Szenengraphen => Abbruch
		if (project.getSceneGraph().searchNodes(NodeScene.class, true)
				.isEmpty()) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
					Messages.ExportDialog_ErrorSceneGraphEmpty_Title,
					Messages.ExportDialog_ErrorSceneGraphEmpty_Text);
			return;
		}

		boolean onlyXML = xmlButton.getSelection();
		boolean zip = zipButton.getSelection();
		Exporter exporter = new Exporter(project, path, type, window, onlyXML, zip);

		// Starte den Export
		try {
			new ProgressMonitorDialog(window.getShell()).run(true, true,
					exporter);
		} catch (InvocationTargetException e) {
			// TODO: Fehler ausgeben
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO: Fehler ausgeben
			e.printStackTrace();
		}

		shell.close();
	}

	private void onCancel() {
		shell.close();
	}

	// Combobox only needed for "old, not SMIL only" export 
//	private Combo createTypeCombo(Composite parent) {
//		Combo combo = new Combo(parent, SWT.READ_ONLY);
//		int selIndex = 0;
//		ExportType[] types = ExportType.values();
//		String[] comboItems = new String[types.length];
//		for (int i = 0; i < types.length; i++) {
//			comboItems[i] = types[i].getDisplayString();
//		}
//		combo.setItems(comboItems);
//		combo.select(selIndex);
//		return combo;
//	}

}

package org.iviPro.newExport.view;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.iviPro.newExport.Messages;

public class DirectorySelectorWidget extends Composite {

	private static Logger logger = Logger
			.getLogger(DirectorySelectorWidget.class);

	private final Text directoryText;
	private File directory;
	private final DirectoryChangedListener directoryChangedListener;

	public DirectorySelectorWidget(Composite parent, int style, File directory,
			String directoryTextLabel, String directoryTextToolTip,
			DirectoryChangedListener directoryChangedListener) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);

		this.directory = directory;
		this.directoryChangedListener = directoryChangedListener;

		final Label directoryLabel = new Label(this, SWT.NONE);
		directoryLabel.setText(directoryTextLabel);

		directoryText = new Text(this, SWT.SINGLE | SWT.BORDER);
		directoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		directoryText.setToolTipText(directoryTextToolTip);

		directoryText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setDirectory(directoryText.getText());
			}
		});

		final Button selectFolderButton = new Button(this, SWT.PUSH);
		selectFolderButton.setText(Messages.Browse);
		selectFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onBrowseClicked();
			}
		});
		
		setDirectory(directory.getAbsolutePath());
	}

	private void onBrowseClicked() {
		DirectoryDialog directoryDialog = new DirectoryDialog(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());
		directoryDialog.setFilterPath(directory.getAbsolutePath());
		directoryDialog.setText(Messages.OutputFolder);
		directoryDialog.setMessage(Messages.OutputFolder_ChooseFolder);
		String newDirectory = directoryDialog.open();
		if (newDirectory != null) {
			setDirectory(newDirectory);
		}
	}

	private void setDirectory(String newDirectory) {
		if (validateOutputFolder(newDirectory)) {
			directory = new File(newDirectory);
			directoryChangedListener.onDirectoryChanged(directory);
		}
		directoryText.setText(directory.getAbsolutePath());
	}

	private boolean validateOutputFolder(String outputFolder) {
		File outputDirectory = new File(outputFolder);
		if (!outputDirectory.exists()) {
			MessageDialog messageDialog = new MessageDialog(getShell(),
					Messages.OutputFolder_NoSuchFolderTitle, null,
					Messages.OutputFolder_NoSuchFolderMessage,
					MessageDialog.QUESTION, new String[] { Messages.Yes,
							Messages.No }, 0);
			int result = messageDialog.open();
			if (result == 0) {
				try {
					outputDirectory.mkdirs();
				} catch (SecurityException e) {
					logger.warn(e.getMessage(), e);
					MessageDialog.openError(getShell(), Messages.Error,
							Messages.Error_NoWritePermissionMessage);
					return false;
				}
			}
		}
		if (!outputDirectory.isDirectory()) {
			MessageDialog.openError(getShell(), Messages.Error,
					Messages.Error_NotADirectoryMessage);
			return false;
		}
		if (!outputDirectory.canWrite()) {
			MessageDialog.openError(getShell(), Messages.Error,
					Messages.Error_NoWritePermissionMessage);
			return false;
		}
		return true;
	}

	public File getDirectory() {
		return directory;
	}

}

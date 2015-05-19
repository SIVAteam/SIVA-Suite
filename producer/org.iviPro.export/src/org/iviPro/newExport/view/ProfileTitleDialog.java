package org.iviPro.newExport.view;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportProfile;

public class ProfileTitleDialog extends Dialog {

	private final ExportProfile exportProfile;
	private final List<String> prohibitedTitles;
	private Text titleText;

	public ProfileTitleDialog(Shell parentShell, ExportProfile exportProfile,
			List<String> prohibitedTitles) {
		super(parentShell);
		this.exportProfile = exportProfile;
		this.prohibitedTitles = prohibitedTitles;
	}

	public String getTitle() {
		return titleText.getText();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.ProfileTitleDialog_Title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);

		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		container.setLayout(layout);

		final Label warning = new Label(container, SWT.NONE);
		warning.setText(Messages.ProfileTitleDialog_Warning);
		final FormData warningData = new FormData();
		warningData.top = new FormAttachment(0, 0);
		warningData.left = new FormAttachment(0, 0);
		warning.setLayoutData(warningData);

		final Label message = new Label(container, SWT.NONE);
		message.setText(String.format(Messages.ProfileTitleDialog_Description,
				exportProfile.getProfileTitle()));
		final FormData messageData = new FormData();
		messageData.top = new FormAttachment(warning, 0);
		messageData.left = new FormAttachment(0, 0);
		message.setLayoutData(messageData);

		final Label label = new Label(container, SWT.LEFT);
		label.setText(Messages.Title);
		label.setToolTipText(Messages.TitleToolTip);
		final FormData labelData = new FormData();
		labelData.top = new FormAttachment(message, 20);
		labelData.left = new FormAttachment(0, 0);
		label.setLayoutData(labelData);

		titleText = new Text(container, SWT.SINGLE);
		titleText.setText(exportProfile.getProfileTitle());
		titleText.selectAll();
		final FormData titleTextData = new FormData();
		titleTextData.top = new FormAttachment(message, 20);
		titleTextData.left = new FormAttachment(label, 5);
		titleTextData.right = new FormAttachment(100, 0);
		titleText.setLayoutData(titleTextData);

		return parent;
	}

	@Override
	protected void okPressed() {
		if (prohibitedTitles.contains(titleText.getText().toLowerCase())
				|| titleText.getText().length() > 128) {
			if (titleText.getText().length() > 128) {
				MessageDialog.openWarning(getParentShell(), Messages.Warning,
						String.format(Messages.Warning_TitleTooLong,
								exportProfile.getProfileTitle()));
			} else {
				MessageDialog.openWarning(getParentShell(), Messages.Warning,
						String.format(Messages.Warning_TitleAlreadyInUse,
								exportProfile.getProfileTitle()));
			}
		} else {
			exportProfile.getProfile().getGeneral()
					.setTitle(titleText.getText());
			super.okPressed();
		}
	}
}

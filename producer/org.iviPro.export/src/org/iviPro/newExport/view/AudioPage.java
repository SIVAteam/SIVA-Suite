package org.iviPro.newExport.view;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.AudioConfiguration;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.newExport.profile.AudioVariant;

public class AudioPage extends WizardPage implements
		VariantsCountChangedListener {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AudioPage.class);

	private AudioConfiguration audioConfiguration;

	private AudioVariantsWidget variantsWidget;
	private AudioFormatsWidget audioFormatsWidget;

	public AudioPage(AudioConfiguration audioConfiguration) {
		super(ProfileWizard.WIZARD_TITLE);
		setTitle(Messages.AudioPage_Title);
		setDescription(Messages.AudioPage_Description);
		this.audioConfiguration = audioConfiguration;

		onVariantsCountChanged(audioConfiguration.getAudioVariants().size());
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(container);

		createVariantsControls(container);
		createProfilesControls(container);

		addListeners();
		bindValues();

		setControl(container);
		variantsWidget.selectFirstElement();
	}

	private void createVariantsControls(Composite parent) {
		final Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(Messages.AudioVariants);
		titleLabel.setToolTipText(Messages.AudioVariantsToolTip);
		titleLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		variantsWidget = new AudioVariantsWidget(parent, SWT.NULL,
				audioConfiguration.getAudioVariants(), this);
		variantsWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		final Label separator = new Label(parent, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		separator
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void createProfilesControls(Composite parent) {
		final Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(Messages.AudioFormats);
		titleLabel.setToolTipText(Messages.AudioFormatsToolTip);
		titleLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		audioFormatsWidget = new AudioFormatsWidget(parent, SWT.NULL);
		audioFormatsWidget.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));
	}

	private void bindValues() {

	}

	private void addListeners() {
		variantsWidget.addSelectionListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (!selection.isEmpty()) {
					audioFormatsWidget
							.setAudioProfiles(((AudioVariant) selection
									.getFirstElement()).getAudioProfiles());
				} else {
					audioFormatsWidget
							.setAudioProfiles(new ArrayList<AudioProfile>(0));
				}
			}
		});
	}

	@Override
	public void onVariantsCountChanged(int count) {
		if (count <= 0) {
			setPageComplete(false);
			setMessage("You have to specify at least one audio variant!");
		} else {
			setPageComplete(true);
			setMessage(Messages.AudioPage_Description);
		}
	}
}

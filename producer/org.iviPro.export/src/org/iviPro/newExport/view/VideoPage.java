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
import org.iviPro.newExport.profile.VideoConfiguration;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.profile.VideoVariant;

public class VideoPage extends WizardPage implements
		VariantsCountChangedListener {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(VideoPage.class);

	private VideoConfiguration videoConfiguration;

	private VideoVariantsWidget variantsWidget;
	private VideoFormatsWidget videoFormatsWidget;

	public VideoPage(VideoConfiguration videoConfiguration) {
		super(ProfileWizard.WIZARD_TITLE);
		setTitle(Messages.VideoPage_Title);
		setDescription(Messages.VideoPage_Description);

		this.videoConfiguration = videoConfiguration;
		setPageComplete(true);

		onVariantsCountChanged(videoConfiguration.getVideoVariants().size());
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
		titleLabel.setText(Messages.VideoVariants);
		titleLabel.setToolTipText(Messages.VideoVariantsToolTip);
		titleLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		variantsWidget = new VideoVariantsWidget(parent, SWT.NULL,
				videoConfiguration.getVideoVariants(), this);
		variantsWidget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		final Label separator = new Label(parent, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		separator
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void createProfilesControls(Composite parent) {
		final Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText(Messages.VideoFormats);
		titleLabel.setToolTipText(Messages.VideoFormatsToolTip);
		titleLabel
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		videoFormatsWidget = new VideoFormatsWidget(parent, SWT.NULL);
		videoFormatsWidget.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));
	}

	private void bindValues() {

	}

	private void addListeners() {
		variantsWidget.addSelectionListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (!selection.isEmpty()) {
					videoFormatsWidget
							.setVideoProfiles(((VideoVariant) selection
									.getFirstElement()).getVideoProfiles());
				} else {
					videoFormatsWidget
							.setVideoProfiles(new ArrayList<VideoProfile>(0));
				}
			}
		});
	}

	@Override
	public void onVariantsCountChanged(int count) {
		if (count <= 0) {
			setPageComplete(false);
			setMessage("You have to specify at least one video variant!");
		} else {
			setPageComplete(true);
			setMessage(Messages.VideoPage_Description);
		}
	}

}

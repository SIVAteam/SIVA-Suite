package org.iviPro.newExport.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.jface.databinding.dialog.TitleAreaDialogSupport;
import org.eclipse.jface.databinding.dialog.ValidationMessageProvider;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.VideoBitRateType;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.newExport.view.convert.IntToStringConverter;
import org.iviPro.newExport.view.convert.StringToIntConverter;
import org.iviPro.newExport.view.validate.IntFromStringValidator;
import org.iviPro.newExport.view.validate.IntRangeValidator;
import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioCodec;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.FrameRate;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;
import org.iviPro.transcoding.format.VideoCodec;
import org.iviPro.transcoding.format.VideoCodecQuality;
import org.iviPro.transcoding.format.VideoContainer;

public class VideoProfileDialog extends TitleAreaDialog {

	private final String title;
	private final String message;

	private final VideoProfile videoProfile;
	private final List<VideoContainer> availableContainers;

	private ComboViewer videoContainerCombo;
	private ComboViewer videoCodecCombo;
	private ComboViewer videoCodecQualityCombo;
	private ComboViewer videoBitRateTypeCombo;
	private Text videoBitRateText;
	private ControlDecoration videoBitRateDecoration;
	private ComboViewer frameRateCombo;
	private Label widthLabel;
	private ControlDecoration widthDecoration;
	private Label heightLabel;
	private ControlDecoration heightDecoration;
	private ComboViewer audioCodecCombo;
	private ComboViewer audioBitRateCombo;
	private ComboViewer sampleRateCombo;
	private ComboViewer sampleSizeCombo;
	private ComboViewer channelsCombo;

	public VideoProfileDialog(Shell parentShell, String title, String message,
			VideoProfile videoProfile, List<VideoContainer> availableContainers) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.videoProfile = videoProfile;
		this.availableContainers = availableContainers;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(parent);

		createVideoContainerControl(parent);
		createVideoCodecControl(parent);
		createCodecQualityControl(parent);
		createVideoBitRateControl(parent);
		createFrameRateControl(parent);
		createAudioCodecControl(parent);
		createAudioBitRateControl(parent);
		createSampleRateControl(parent);
		createSampleSizeControl(parent);
		createChannelsControl(parent);

		addVideoContainerChangeListener();
		addVideoCodecChangeListener();
		
		bindValues();

		return parent;
	}

	private void createVideoContainerControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Container);
		label.setToolTipText(Messages.VideoContainerToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		videoContainerCombo = new ComboViewer(parent, SWT.VERTICAL
				| SWT.DROP_DOWN | SWT.BORDER);
		videoContainerCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		videoContainerCombo.setContentProvider(new ArrayContentProvider());
		videoContainerCombo.setInput(availableContainers);
	}

	private void createVideoCodecControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Codec);
		label.setToolTipText(Messages.VideoCodecToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		videoCodecCombo = new ComboViewer(parent, SWT.VERTICAL 
				| SWT.DROP_DOWN	| SWT.BORDER);
		videoCodecCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		videoCodecCombo.setContentProvider(new ArrayContentProvider());
		videoCodecCombo.setInput(videoProfile.getVideoContainer()
				.getSupportedVideoCodecs());

	}
	
	private void createCodecQualityControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.VideoProfileDialog_CodecQuality);
		label.setToolTipText(Messages.VideoProfileDialog_CodecQuality_Tooltip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		videoCodecQualityCombo = new ComboViewer(parent, SWT.VERTICAL 
				| SWT.DROP_DOWN	| SWT.BORDER);
		videoCodecQualityCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		videoCodecQualityCombo.setContentProvider(new ArrayContentProvider());
		if (videoProfile.getVideoCodec().getSupportedQualityProfiles() != null) {
			videoCodecQualityCombo.setInput(videoProfile.getVideoCodec()
					.getSupportedQualityProfiles());
			videoCodecQualityCombo.getControl().setEnabled(true);
		} else {
			videoCodecQualityCombo.getControl().setEnabled(false);
		}
	}

	private void createVideoBitRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.BitRate);
		label.setToolTipText(Messages.VideoBitRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		videoBitRateTypeCombo = new ComboViewer(parent, SWT.VERTICAL
				| SWT.DROP_DOWN | SWT.BORDER);
		videoBitRateTypeCombo
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						onVideoBitRateTypeSelected(((VideoBitRateType) ((IStructuredSelection) event
								.getSelection()).getFirstElement()));
					}
				});
		videoBitRateTypeCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		videoBitRateTypeCombo.setContentProvider(new ArrayContentProvider());
		videoBitRateTypeCombo.setInput(VideoBitRateType.values());

		videoBitRateText = new Text(parent, SWT.SINGLE);
		videoBitRateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		videoBitRateDecoration = new ControlDecoration(videoBitRateText,
				SWT.LEFT | SWT.TOP);
		videoBitRateDecoration.setImage(FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
				.getImage());
	}

	private void createFrameRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.FrameRate);
		label.setToolTipText(Messages.FrameRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		frameRateCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		frameRateCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		frameRateCombo.setContentProvider(new ArrayContentProvider());
		frameRateCombo.setInput(FrameRate.values());
	}

	private void createAudioCodecControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Codec);
		label.setToolTipText(Messages.AudioCodecToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		audioCodecCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		audioCodecCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		audioCodecCombo.setContentProvider(new ArrayContentProvider());
		audioCodecCombo.setInput(videoProfile.getVideoContainer()
				.getSupportedAudioCodecs());
	}

	private void addVideoContainerChangeListener() {
		videoProfile.addPropertyChangeListener("videoContainer", //$NON-NLS-1$
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						audioCodecCombo.setInput(videoProfile
								.getVideoContainer().getSupportedAudioCodecs());
						AudioCodec audioCodec = videoProfile
								.getVideoContainer().getSupportedAudioCodecs()[0];
						videoProfile.setAudioCodec(audioCodec);
						audioCodecCombo.setSelection(new StructuredSelection(
								audioCodec));

						videoCodecCombo.setInput(videoProfile
								.getVideoContainer().getSupportedVideoCodecs());
						VideoCodec videoCodec = videoProfile
								.getVideoContainer().getSupportedVideoCodecs()[0];
						videoProfile.setVideoCodec(videoCodec);
						videoCodecCombo.setSelection(new StructuredSelection(
								videoCodec));
					}
				});
	}
	
	private void addVideoCodecChangeListener() {
		videoProfile.addPropertyChangeListener("videoCodec", //$NON-NLS-1$
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						if (videoProfile.getVideoCodec()
								.getSupportedQualityProfiles() != null) {
						videoCodecQualityCombo.setInput(videoProfile.getVideoCodec()
								.getSupportedQualityProfiles());
						VideoCodecQuality codecQuality = videoProfile.getVideoCodec()
								.getSupportedQualityProfiles()[0];
						System.out.println(codecQuality);
						videoProfile.setVideoCodecQuality(codecQuality);
						videoCodecQualityCombo.getControl().setEnabled(true);
						videoCodecQualityCombo.setSelection(new StructuredSelection(
								codecQuality));
						} else {							
							videoCodecQualityCombo.getControl().setEnabled(false);
							videoCodecQualityCombo.setInput(null);
							videoProfile.setVideoCodecQuality(null);
						}
					}			
				});
	}

	private void createAudioBitRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.BitRate);
		label.setToolTipText(Messages.AudioBitRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		audioBitRateCombo = new ComboViewer(parent, SWT.VERTICAL
				| SWT.DROP_DOWN | SWT.BORDER);
		audioBitRateCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		audioBitRateCombo.setContentProvider(new ArrayContentProvider());
		audioBitRateCombo.setInput(AudioBitRate.values());
	}

	private void createSampleRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.SampleRate);
		label.setToolTipText(Messages.SampleRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		sampleRateCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		sampleRateCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sampleRateCombo.setContentProvider(new ArrayContentProvider());
		sampleRateCombo.setInput(SampleRate.values());
	}

	private void createSampleSizeControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.SampleSize);
		label.setToolTipText(Messages.SampleSizeToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		sampleSizeCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		sampleSizeCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sampleSizeCombo.setContentProvider(new ArrayContentProvider());
		sampleSizeCombo.setInput(SampleSize.values());
	}

	protected void createChannelsControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Channels);
		label.setToolTipText(Messages.ChannelsToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		channelsCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		channelsCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		channelsCombo.setContentProvider(new ArrayContentProvider());
		channelsCombo.setInput(Channels.values());
	}

	private void bindValues() {
		DataBindingContext ctx = new DataBindingContext();
		TitleAreaDialogSupport.create(this, ctx).setValidationMessageProvider(
				new ValidationMessageProvider() {
					@Override
					public String getMessage(
							ValidationStatusProvider statusProvider) {
						if (statusProvider == null) {
							return message;
						}
						return super.getMessage(statusProvider);
					}

					@Override
					public int getMessageType(
							ValidationStatusProvider statusProvider) {
						int type = super.getMessageType(statusProvider);
						if (getButton(IDialogConstants.OK_ID) != null) {
							getButton(IDialogConstants.OK_ID).setEnabled(
									type != IMessageProvider.ERROR);
						}
						return type;
					}
				});
		;

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(videoContainerCombo),
				BeanProperties.value(VideoProfile.class, "videoContainer") //$NON-NLS-1$
						.observe(videoProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(videoCodecCombo),
				BeanProperties.value(VideoProfile.class, "videoCodec").observe( //$NON-NLS-1$
						videoProfile));
		
		ctx.bindValue(
				ViewerProperties.singleSelection().observe(videoCodecQualityCombo),
				BeanProperties.value(VideoProfile.class, "videoCodecQuality").observe( //$NON-NLS-1$
						videoProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(
						videoBitRateTypeCombo),
				BeanProperties
						.value(VideoProfile.class, "videoBitRateType").observe( //$NON-NLS-1$
								videoProfile));

		IConverter stringToIntConverter = new StringToIntConverter();
		IConverter intToStringConverter = new IntToStringConverter();
		UpdateValueStrategy videoBitRateStrategy = new UpdateValueStrategy()
				.setConverter(stringToIntConverter)
				.setAfterGetValidator(
						new IntFromStringValidator(videoBitRateDecoration,
								Messages.VideoProfileDialog_Bitrate_Warn_Num))
				.setAfterConvertValidator(
						new IntRangeValidator(
								videoBitRateDecoration,
								String.format(
										Messages.VideoProfileDialog_Bitrate_Warn_Range,
										1, 99999), 1, 99999));
		ctx.bindValue(
				WidgetProperties.text(SWT.Modify).observe(videoBitRateText),
				BeanProperties.value(VideoProfile.class, "videoBitRate") //$NON-NLS-1$
						.observe(videoProfile),
				videoBitRateStrategy,
				new UpdateValueStrategy().setAfterGetValidator(new IntRangeValidator(
						videoBitRateDecoration,
						String.format(
								Messages.VideoProfileDialog_Bitrate_Warn_Range,
								1, 99999), 1, 99999)).setConverter(intToStringConverter));
		ctx.bindValue(ViewerProperties.singleSelection()
				.observe(frameRateCombo),
				BeanProperties.value(VideoProfile.class, "frameRate").observe( //$NON-NLS-1$
						videoProfile));
		ctx.bindValue(
				ViewerProperties.singleSelection().observe(audioCodecCombo),
				BeanProperties.value(VideoProfile.class, "audioCodec").observe( //$NON-NLS-1$
						videoProfile));
		
		ctx.bindValue(
				ViewerProperties.singleSelection().observe(audioBitRateCombo),
				BeanProperties.value(VideoProfile.class, "audioBitRate") //$NON-NLS-1$
						.observe(videoProfile));
		ctx.bindValue(
				ViewerProperties.singleSelection().observe(sampleRateCombo),
				BeanProperties.value(VideoProfile.class, "sampleRate").observe( //$NON-NLS-1$
						videoProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(sampleSizeCombo),
				BeanProperties.value(VideoProfile.class, "sampleSize").observe( //$NON-NLS-1$
						videoProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(channelsCombo),
				BeanProperties.value(VideoProfile.class, "channels").observe( //$NON-NLS-1$
						videoProfile));
	}

	private void onVideoBitRateTypeSelected(VideoBitRateType videoBitRateType) {
		switch (videoBitRateType) {
		case FIXED:
			videoBitRateText.setEnabled(true);
			break;
		case ORIGINAL:
			videoBitRateText.setEnabled(false);
		}
	}
}

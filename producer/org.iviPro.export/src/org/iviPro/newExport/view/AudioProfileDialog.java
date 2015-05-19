package org.iviPro.newExport.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.transcoding.format.AudioBitRate;
import org.iviPro.transcoding.format.AudioContainer;
import org.iviPro.transcoding.format.Channels;
import org.iviPro.transcoding.format.SampleRate;
import org.iviPro.transcoding.format.SampleSize;

public class AudioProfileDialog extends TitleAreaDialog {

	private final String title;
	private final String message;

	private final AudioProfile audioProfile;
	private final List<AudioContainer> availableContainers;

	protected ComboViewer audioContainerCombo;
	protected ComboViewer audioCodecCombo;
	protected ComboViewer audioBitRateCombo;
	protected ComboViewer sampleRateCombo;
	protected ComboViewer sampleSizeCombo;
	protected ComboViewer channelsCombo;

	public AudioProfileDialog(Shell parentShell, String title, String message,
			AudioProfile audioProfile, List<AudioContainer> availableContainers) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.audioProfile = audioProfile;
		this.availableContainers = availableContainers;
	}

	public AudioProfile getAudioProfile() {
		return audioProfile;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);

		createAudioContainerControl(parent);
		createAudioCodecControl(parent);
		createAudioBitRateControl(parent);
		createSampleRateControl(parent);
		createSampleSizeControl(parent);
		createChannelsControl(parent);

		addAudioContainerChangeListener();

		bindValues();

		return parent;
	}

	protected void createAudioContainerControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Container);
		label.setToolTipText(Messages.AudioContainerToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		audioContainerCombo = new ComboViewer(parent, SWT.VERTICAL
				| SWT.DROP_DOWN | SWT.BORDER);
		audioContainerCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		audioContainerCombo.setContentProvider(new ArrayContentProvider());
		audioContainerCombo.setInput(availableContainers);
	}

	protected void createAudioCodecControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.Codec);
		label.setToolTipText(Messages.AudioCodecToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		audioCodecCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		audioCodecCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		audioCodecCombo.setContentProvider(new ArrayContentProvider());
		audioCodecCombo.setInput(audioProfile.getAudioContainer()
				.getSupportedAudioCodecs());
	}

	protected void addAudioContainerChangeListener() {
		audioProfile.addPropertyChangeListener("audioContainer", //$NON-NLS-1$
				new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent arg0) {
						audioCodecCombo.setInput(audioProfile
								.getAudioContainer().getSupportedAudioCodecs());
						audioProfile
								.setAudioCodec(audioProfile.getAudioContainer()
										.getSupportedAudioCodecs()[0]);
					}
				});
	}

	protected void createAudioBitRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.BitRate);
		label.setToolTipText(Messages.AudioBitRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		audioBitRateCombo = new ComboViewer(parent, SWT.VERTICAL
				| SWT.DROP_DOWN | SWT.BORDER);
		audioBitRateCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		audioBitRateCombo.setContentProvider(new ArrayContentProvider());
		audioBitRateCombo.setInput(AudioBitRate.values());
	}

	protected void createSampleRateControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.SampleRate);
		label.setToolTipText(Messages.SampleRateToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		sampleRateCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		sampleRateCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		sampleRateCombo.setContentProvider(new ArrayContentProvider());
		sampleRateCombo.setInput(SampleRate.values());
	}

	protected void createSampleSizeControl(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(Messages.SampleSize);
		label.setToolTipText(Messages.SampleSizeToolTip);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		sampleSizeCombo = new ComboViewer(parent, SWT.VERTICAL | SWT.DROP_DOWN
				| SWT.BORDER);
		sampleSizeCombo.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));
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
				new GridData(SWT.FILL, SWT.CENTER, false, false));
		channelsCombo.setContentProvider(new ArrayContentProvider());
		channelsCombo.setInput(Channels.values());
	}

	private void bindValues() {
		DataBindingContext ctx = new DataBindingContext();
		ctx.bindValue(
				ViewerProperties.singleSelection().observe(audioContainerCombo),
				BeanProperties.value(AudioProfile.class, "audioContainer") //$NON-NLS-1$
						.observe(audioProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(audioCodecCombo),
				BeanProperties.value(AudioProfile.class, "audioCodec").observe( //$NON-NLS-1$
						audioProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(audioBitRateCombo),
				BeanProperties.value(AudioProfile.class, "bitRate").observe( //$NON-NLS-1$
						audioProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(sampleRateCombo),
				BeanProperties.value(AudioProfile.class, "sampleRate").observe( //$NON-NLS-1$
						audioProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(sampleSizeCombo),
				BeanProperties.value(AudioProfile.class, "sampleSize").observe( //$NON-NLS-1$
						audioProfile));

		ctx.bindValue(
				ViewerProperties.singleSelection().observe(channelsCombo),
				BeanProperties.value(AudioProfile.class, "channels").observe( //$NON-NLS-1$
						audioProfile));
	}
}

package org.iviPro.newExport.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.VideoProfile;
import org.iviPro.transcoding.format.VideoContainer;

public class VideoFormatsWidget extends Composite {

	private List<VideoProfile> videoProfiles;

	private final TableViewer viewer;
	private final Table table;
	private final TableColumnLayout tableColumnLayout;

	private final Button addButton;
	private final Button editButton;
	private final Button deleteButton;

	public VideoFormatsWidget(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		final Composite tableContainer = new Composite(this, SWT.NULL);
		tableColumnLayout = new TableColumnLayout();
		tableContainer.setLayout(tableColumnLayout);
		viewer = new TableViewer(tableContainer, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons(event.getSelection());
			}
		});
		table = viewer.getTable();
		tableContainer.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true, 1, 3));

		addButton = new Button(this, SWT.PUSH);
		addButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		addButton.setEnabled(false);
		editButton = new Button(this, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		editButton.setEnabled(false);
		deleteButton = new Button(this, SWT.PUSH);
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false,
				true));
		deleteButton.setEnabled(false);

		createTable();
		initializeButtons();
	}

	private void initializeButtons() {
		addButton.setText(Messages.Add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddClicked();
			}
		});

		editButton.setText(Messages.Edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onEditClicked((VideoProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});

		deleteButton.setText(Messages.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onDeleteClicked((VideoProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});
	}

	private void onAddClicked() {
		List<VideoContainer> availableContainers = computeAvailableContainers(computeContainersInUse());
		VideoProfile videoProfile = VideoProfile
				.getDefault(availableContainers);

		VideoProfileDialog dialog = new VideoProfileDialog(getShell(),
				"VideoProfileDialog Title", "VideoProfileDialog description",
				videoProfile, availableContainers);
		dialog.create();

		if (dialog.open() == Window.OK) {
			videoProfiles.add(videoProfile);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(videoProfile));

			updateButtons();
		}
	}

	private void onEditClicked(VideoProfile videoProfile) {
		List<VideoContainer> availableContainers = computeAvailableContainers(computeContainersInUse());
		availableContainers.add(0, videoProfile.getVideoContainer());

		VideoProfile videoProfileCopy = new VideoProfile(videoProfile);
		VideoProfileDialog dialog = new VideoProfileDialog(getShell(),
				"VideoProfileDialog Title", "VideoProfileDialog description",
				videoProfileCopy, availableContainers);
		dialog.create();

		if (dialog.open() == Window.OK) {
			videoProfiles.set(videoProfiles.indexOf(videoProfile),
					videoProfileCopy);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(videoProfileCopy));

			updateButtons();
		}
	}

	private void onDeleteClicked(VideoProfile videoProfile) {
		if (videoProfiles.size() > 1) {
			int indexBeforeRemoving = videoProfiles.indexOf(videoProfile);
			videoProfiles.remove(videoProfile);

			viewer.refresh();

			if (videoProfiles.size() > indexBeforeRemoving) {
				viewer.setSelection(new StructuredSelection(videoProfiles
						.get(indexBeforeRemoving)));
			} else {
				viewer.setSelection(new StructuredSelection(videoProfiles
						.get(indexBeforeRemoving - 1)));
			}

			updateButtons();
		}
	}

	private void updateButtons() {
		updateButtons(viewer.getSelection());
	}

	private void updateButtons(ISelection selection) {
		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
		editButton.setEnabled(false);

		if (computeAvailableContainers(computeContainersInUse()).size() > 0) {
			addButton.setEnabled(true);
		}
		if (!selection.isEmpty()) {
			editButton.setEnabled(true);
			if (videoProfiles.size() > 1) {
				deleteButton.setEnabled(true);
			}
		}
	}

	private List<VideoContainer> computeContainersInUse() {
		final List<VideoContainer> usedContainers = new ArrayList<VideoContainer>();
		for (Object element : videoProfiles) {
			usedContainers.add(((VideoProfile) element).getVideoContainer());
		}
		return usedContainers;
	}

	private List<VideoContainer> computeAvailableContainers(
			List<VideoContainer> containersInUse) {
		final List<VideoContainer> availableContainers = new ArrayList<VideoContainer>();
		for (VideoContainer ac : VideoContainer.values()) {
			boolean found = false;
			for (VideoContainer usedContainer : containersInUse) {
				if (ac == usedContainer) {
					found = true;
				}
			}
			if (!found) {
				availableContainers.add(ac);
			}

		}
		return availableContainers;
	}

	private void createTable() {
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}

	private void createColumns() {
		final TableViewerColumn containerColumn = new TableViewerColumn(viewer,
				SWT.LEFT);
		containerColumn.getColumn().setText(Messages.Container);
		containerColumn.getColumn().setToolTipText(
				Messages.AudioContainerToolTip);
		tableColumnLayout.setColumnData(containerColumn.getColumn(),
				new ColumnWeightData(14, 140, true));
		containerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getVideoContainer().toString();
			}
		});

		final TableViewerColumn videoCodecColumn = new TableViewerColumn(
				viewer, SWT.LEFT);
		videoCodecColumn.getColumn().setText(Messages.Codec);
		videoCodecColumn.getColumn().setToolTipText(Messages.VideoCodecToolTip);
		tableColumnLayout.setColumnData(videoCodecColumn.getColumn(),
				new ColumnWeightData(11, 110, true));
		videoCodecColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getVideoCodec().toString();
			}
		});

		final TableViewerColumn videoBitRateColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		videoBitRateColumn.getColumn().setText(Messages.BitRate);
		videoBitRateColumn.getColumn().setToolTipText(
				Messages.VideoBitRateToolTip);
		tableColumnLayout.setColumnData(videoBitRateColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		videoBitRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				VideoProfile videoProfile = (VideoProfile) element;
				switch (videoProfile.getVideoBitRateType()) {
				case ORIGINAL:
					return "Original";
				case FIXED:
				default:
					return String.valueOf(videoProfile.getVideoBitRate());
				}
			}
		});

		final TableViewerColumn frameRateColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		frameRateColumn.getColumn().setText(Messages.FrameRate);
		frameRateColumn.getColumn().setToolTipText(Messages.FrameRateToolTip);
		tableColumnLayout.setColumnData(frameRateColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		frameRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getFrameRate().toString();
			}
		});

		final TableViewerColumn audioCodecColumn = new TableViewerColumn(
				viewer, SWT.LEFT);
		audioCodecColumn.getColumn().setText(Messages.Codec);
		audioCodecColumn.getColumn().setToolTipText(Messages.AudioCodecToolTip);
		tableColumnLayout.setColumnData(audioCodecColumn.getColumn(),
				new ColumnWeightData(11, 110, true));
		audioCodecColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getAudioCodec().toString();
			}
		});

		final TableViewerColumn audioBitRateColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		audioBitRateColumn.getColumn().setText(Messages.BitRate);
		audioBitRateColumn.getColumn().setToolTipText(
				Messages.AudioBitRateToolTip);
		tableColumnLayout.setColumnData(audioBitRateColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		audioBitRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getAudioBitRate().toString();
			}
		});

		final TableViewerColumn sampleRateColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		sampleRateColumn.getColumn().setText(Messages.SampleRate);
		sampleRateColumn.getColumn().setToolTipText(Messages.SampleRateToolTip);
		tableColumnLayout.setColumnData(sampleRateColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		sampleRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getSampleRate().toString();
			}
		});

		final TableViewerColumn sampleSizeColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		sampleSizeColumn.getColumn().setText(Messages.SampleSize);
		sampleSizeColumn.getColumn().setToolTipText(Messages.SampleSizeToolTip);
		tableColumnLayout.setColumnData(sampleSizeColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		sampleSizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getSampleSize().toString();
			}
		});

		final TableViewerColumn channelsColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		channelsColumn.getColumn().setText(Messages.Channels);
		channelsColumn.getColumn().setToolTipText(Messages.ChannelsToolTip);
		tableColumnLayout.setColumnData(channelsColumn.getColumn(),
				new ColumnWeightData(8, 80, true));
		channelsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoProfile) element).getChannels().toString();
			}
		});
	}

	public void setVideoProfiles(List<VideoProfile> videoProfiles) {
		this.videoProfiles = videoProfiles;

		viewer.setInput(videoProfiles);
		if (videoProfiles.size() > 0) {
			viewer.setSelection(new StructuredSelection(videoProfiles.get(0)));
		}

		updateButtons();
	}

	public void addSelectionListener(
			ISelectionChangedListener selectionChangedListener) {
		viewer.addSelectionChangedListener(selectionChangedListener);
	}
}

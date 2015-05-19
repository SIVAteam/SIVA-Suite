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
import org.iviPro.newExport.profile.AudioProfile;
import org.iviPro.transcoding.format.AudioContainer;

public class AudioFormatsWidget extends Composite {

	private List<AudioProfile> audioProfiles;

	private final TableViewer viewer;
	private final Table table;
	private final TableColumnLayout tableColumnLayout;

	private final Button addButton;
	private final Button editButton;
	private final Button deleteButton;

	public AudioFormatsWidget(Composite parent, int style) {
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
					onEditClicked((AudioProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});

		deleteButton.setText(Messages.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onDeleteClicked((AudioProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());

				}
			}
		});
	}

	private void onAddClicked() {
		List<AudioContainer> availableContainers = computeAvailableContainers(computeContainersInUse());
		AudioProfile audioProfile = AudioProfile
				.getDefault(availableContainers);

		AudioProfileDialog dialog = new AudioProfileDialog(getShell(),
				"AudioProfileDialog title", "AudioProfileDialog description",
				audioProfile, availableContainers);
		dialog.create();

		if (dialog.open() == Window.OK) {
			audioProfiles.add(audioProfile);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(audioProfile));

			updateButtons();
		}
	}

	private void onEditClicked(AudioProfile audioProfile) {
		List<AudioContainer> availableContainers = computeAvailableContainers(computeContainersInUse());
		availableContainers.add(0, audioProfile.getAudioContainer());

		AudioProfile audioProfileCopy = new AudioProfile(audioProfile);
		AudioProfileDialog dialog = new AudioProfileDialog(getShell(),
				"AudioProfileDialog title", "AudioProfileDialog description",
				audioProfileCopy, availableContainers);
		dialog.create();

		if (dialog.open() == Window.OK) {
			audioProfiles.set(audioProfiles.indexOf(audioProfile),
					audioProfileCopy);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(audioProfileCopy));

			updateButtons();
		}
	}

	private void onDeleteClicked(AudioProfile audioProfile) {
		if (audioProfiles.size() > 1) {
			int indexBeforeRemoving = audioProfiles.indexOf(audioProfile);
			audioProfiles.remove(audioProfile);

			viewer.refresh();

			if (audioProfiles.size() > indexBeforeRemoving) {
				viewer.setSelection(new StructuredSelection(audioProfiles
						.get(indexBeforeRemoving)));
			} else {
				viewer.setSelection(new StructuredSelection(audioProfiles
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
			if (audioProfiles.size() > 1) {
				deleteButton.setEnabled(true);
			}
		}
	}

	private List<AudioContainer> computeContainersInUse() {
		final List<AudioContainer> usedContainers = new ArrayList<AudioContainer>();
		for (Object element : audioProfiles) {
			usedContainers.add(((AudioProfile) element).getAudioContainer());
		}
		return usedContainers;
	}

	private List<AudioContainer> computeAvailableContainers(
			List<AudioContainer> containersInUse) {
		final List<AudioContainer> availableContainers = new ArrayList<AudioContainer>();
		for (AudioContainer ac : AudioContainer.values()) {
			boolean found = false;
			for (AudioContainer usedContainer : containersInUse) {
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
				new ColumnWeightData(25, 100, true));
		containerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getAudioContainer().toString();
			}
		});

		final TableViewerColumn codecColumn = new TableViewerColumn(viewer,
				SWT.LEFT);
		codecColumn.getColumn().setText(Messages.Codec);
		codecColumn.getColumn().setToolTipText(Messages.AudioCodecToolTip);
		tableColumnLayout.setColumnData(codecColumn.getColumn(),
				new ColumnWeightData(25, 100, true));
		codecColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getAudioCodec().toString();
			}
		});

		final TableViewerColumn bitRateColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		bitRateColumn.getColumn().setText(Messages.BitRate);
		bitRateColumn.getColumn().setToolTipText(Messages.AudioBitRateToolTip);
		tableColumnLayout.setColumnData(bitRateColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		bitRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getBitRate().toString();
			}
		});

		final TableViewerColumn sampleRateColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		sampleRateColumn.getColumn().setText(Messages.SampleRate);
		sampleRateColumn.getColumn().setToolTipText(Messages.SampleRateToolTip);
		tableColumnLayout.setColumnData(sampleRateColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		sampleRateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getSampleRate().toString();
			}
		});

		final TableViewerColumn sampleSizeColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		sampleSizeColumn.getColumn().setText(Messages.SampleSize);
		sampleSizeColumn.getColumn().setToolTipText(Messages.SampleSizeToolTip);
		tableColumnLayout.setColumnData(sampleSizeColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		sampleSizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getSampleSize().toString();
			}
		});

		final TableViewerColumn channelsColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		channelsColumn.getColumn().setText(Messages.Channels);
		channelsColumn.getColumn().setToolTipText(Messages.ChannelsToolTip);
		tableColumnLayout.setColumnData(channelsColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		channelsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((AudioProfile) element).getChannels().toString();
			}
		});
	}

	public void setAudioProfiles(List<AudioProfile> audioProfiles) {
		this.audioProfiles = audioProfiles;

		viewer.setInput(audioProfiles);
		if (audioProfiles.size() > 0) {
			viewer.setSelection(new StructuredSelection(audioProfiles.get(0)));
		}

		updateButtons();
	}

	public void addSelectionListener(
			ISelectionChangedListener selectionChangedListener) {
		viewer.addSelectionChangedListener(selectionChangedListener);
	}
}

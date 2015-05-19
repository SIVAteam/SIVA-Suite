package org.iviPro.newExport.view;

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
import org.iviPro.newExport.profile.MediaVariant;
import org.iviPro.newExport.profile.VideoVariant;

public class VideoVariantsWidget extends Composite {

	private final List<VideoVariant> videoVariants;
	private final VariantsCountChangedListener variantsCountChangedListener;

	private final TableViewer viewer;
	private final Table table;
	private final TableColumnLayout tableColumnLayout;

	private final Button addButton;
	private final Button editButton;
	private final Button deleteButton;

	public VideoVariantsWidget(Composite parent, int style,
			List<VideoVariant> videoVariants,
			VariantsCountChangedListener variantsCountChangedListener) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		this.videoVariants = videoVariants;
		this.variantsCountChangedListener = variantsCountChangedListener;

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
		editButton = new Button(this, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		deleteButton = new Button(this, SWT.PUSH);
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false,
				true));

		createTable();

		initializeButtons();
	}

	private void createTable() {
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(videoVariants);
	}

	private void createColumns() {
		final TableViewerColumn titleColumn = new TableViewerColumn(viewer,
				SWT.LEFT);
		titleColumn.getColumn().setText(Messages.Title);
		titleColumn.getColumn().setToolTipText("Some tool tip...");
		tableColumnLayout.setColumnData(titleColumn.getColumn(),
				new ColumnWeightData(25, 150, true));
		titleColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoVariant) element).getTitle();
			}
		});

		final TableViewerColumn descriptionColumn = new TableViewerColumn(
				viewer, SWT.LEFT);
		descriptionColumn.getColumn().setText(Messages.Description);
		descriptionColumn.getColumn().setToolTipText("Some tool tip...");
		tableColumnLayout.setColumnData(descriptionColumn.getColumn(),
				new ColumnWeightData(75, 450, true));
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((VideoVariant) element).getDescription();
			}
		});
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
					VideoVariant videoVariant = (VideoVariant) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					onEditClicked(videoVariant);
				}
			}
		});

		deleteButton.setText(Messages.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					VideoVariant videoVariant = (VideoVariant) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					onDeleteClicked(videoVariant);
				}
			}
		});
	}

	private void onAddClicked() {
		List<String> titlesInUse = MediaVariant.getAllTitles(videoVariants);
		VideoVariant videoVariant = VideoVariant.getDefault();

		MediaVariantDialog dialog = new MediaVariantDialog(getShell(),
				"Video variant", "Video variant message", videoVariant,
				titlesInUse);
		dialog.create();

		if (dialog.open() == Window.OK) {
			videoVariants.add(videoVariant);
			viewer.refresh();
			variantsCountChangedListener.onVariantsCountChanged(videoVariants
					.size());

			viewer.setSelection(new StructuredSelection(videoVariant));

			updateButtons();
		}
	}

	private void onEditClicked(VideoVariant videoVariant) {
		List<String> titlesInUse = videoVariant.getOthersTitles(videoVariants);
		VideoVariant videoVariantCopy = new VideoVariant(videoVariant);

		MediaVariantDialog dialog = new MediaVariantDialog(getShell(),
				"Video variant", "Video variant message", videoVariantCopy,
				titlesInUse);
		dialog.create();

		if (dialog.open() == Window.OK) {
			videoVariants.set(videoVariants.indexOf(videoVariant),
					videoVariantCopy);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(videoVariantCopy));

			updateButtons();
		}
	}

	private void onDeleteClicked(VideoVariant videoVariant) {
		int indexBeforeRemoving = videoVariants.indexOf(videoVariant);
		videoVariants.remove(videoVariant);

		viewer.refresh();
		variantsCountChangedListener.onVariantsCountChanged(videoVariants
				.size());

		if (videoVariants.size() > 0) {
			if (videoVariants.size() > indexBeforeRemoving) {
				viewer.setSelection(new StructuredSelection(videoVariants
						.get(indexBeforeRemoving)));
			} else {
				viewer.setSelection(new StructuredSelection(videoVariants
						.get(indexBeforeRemoving - 1)));
			}

			updateButtons();
		}
	}

	private void updateButtons() {
		updateButtons(viewer.getSelection());
	}

	private void updateButtons(ISelection selection) {
		addButton.setEnabled(true);
		deleteButton.setEnabled(false);
		editButton.setEnabled(false);

		if (!selection.isEmpty()) {
			editButton.setEnabled(true);
			deleteButton.setEnabled(true);
		}
	}

	public void addSelectionListener(
			ISelectionChangedListener selectionChangedListener) {
		viewer.addSelectionChangedListener(selectionChangedListener);
	}

	public void selectFirstElement() {
		if (viewer.getSelection().isEmpty() && videoVariants.size() > 0) {
			viewer.setSelection(new StructuredSelection(videoVariants.get(0)));
		}
	}
}

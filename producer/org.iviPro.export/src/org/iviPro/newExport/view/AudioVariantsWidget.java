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
import org.iviPro.newExport.profile.AudioVariant;
import org.iviPro.newExport.profile.MediaVariant;

public class AudioVariantsWidget extends Composite {

	private final List<AudioVariant> audioVariants;
	private final VariantsCountChangedListener variantsCountChangedListener;

	private final TableViewer viewer;
	private final Table table;
	private final TableColumnLayout tableColumnLayout;

	private final Button addButton;
	private final Button editButton;
	private final Button deleteButton;

	public AudioVariantsWidget(Composite parent, int style,
			List<AudioVariant> audioVariants,
			VariantsCountChangedListener variantsCountChangedListener) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		this.audioVariants = audioVariants;
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
		viewer.setInput(audioVariants);
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
				return ((AudioVariant) element).getTitle();
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
				return ((AudioVariant) element).getDescription();
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
					AudioVariant audioVariant = (AudioVariant) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					onEditClicked(audioVariant);
				}
			}
		});

		deleteButton.setText(Messages.Delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					AudioVariant audioVariant = (AudioVariant) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					onDeleteClicked(audioVariant);
				}
			}
		});
	}

	private void onAddClicked() {
		List<String> titlesInUse = MediaVariant.getAllTitles(audioVariants);
		AudioVariant audioVariant = AudioVariant.getDefault();

		MediaVariantDialog dialog = new MediaVariantDialog(getShell(),
				"Audio variant", "Audio variant message", audioVariant,
				titlesInUse);
		dialog.create();

		if (dialog.open() == Window.OK) {
			audioVariants.add(audioVariant);

			viewer.refresh();
			variantsCountChangedListener.onVariantsCountChanged(audioVariants
					.size());
			viewer.setSelection(new StructuredSelection(audioVariant));

			updateButtons();
		}
	}

	private void onEditClicked(AudioVariant audioVariant) {
		List<String> titlesInUse = audioVariant.getOthersTitles(audioVariants);
		AudioVariant audioVariantCopy = new AudioVariant(audioVariant);

		MediaVariantDialog dialog = new MediaVariantDialog(getShell(),
				"Audio variant", "Audio variant message", audioVariantCopy,
				titlesInUse);
		dialog.create();

		if (dialog.open() == Window.OK) {
			audioVariants.set(audioVariants.indexOf(audioVariant),
					audioVariantCopy);

			viewer.refresh();
			viewer.setSelection(new StructuredSelection(audioVariantCopy));

			updateButtons();
		}
	}

	private void onDeleteClicked(AudioVariant audioVariant) {
		int indexBeforeRemoving = audioVariants.indexOf(audioVariant);
		audioVariants.remove(audioVariant);

		viewer.refresh();
		variantsCountChangedListener.onVariantsCountChanged(audioVariants
				.size());

		if (audioVariants.size() > 0) {
			if (audioVariants.size() > indexBeforeRemoving) {
				viewer.setSelection(new StructuredSelection(audioVariants
						.get(indexBeforeRemoving)));
			} else {
				viewer.setSelection(new StructuredSelection(audioVariants
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
		if (viewer.getSelection().isEmpty() && audioVariants.size() > 0) {
			viewer.setSelection(new StructuredSelection(audioVariants.get(0)));
		}
	}
}

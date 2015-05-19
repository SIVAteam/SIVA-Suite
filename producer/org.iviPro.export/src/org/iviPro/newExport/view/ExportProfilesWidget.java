package org.iviPro.newExport.view;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.iviPro.newExport.Activator;
import org.iviPro.newExport.ExportException;
import org.iviPro.newExport.Messages;
import org.iviPro.newExport.profile.ExportProfile;
import org.iviPro.newExport.profile.ExportProfileProvider;
import org.iviPro.newExport.profile.Profile;
import org.iviPro.newExport.view.edit.CompressEditingSupport;
import org.iviPro.newExport.view.edit.ConvertEditingSupport;
import org.iviPro.newExport.view.edit.ExportEditingSupport;
import org.iviPro.newExport.view.edit.ExportResourcesEditingSupport;

public class ExportProfilesWidget extends Composite {

	private static final Image CHECKED = AbstractUIPlugin
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/checked.gif") //$NON-NLS-1$
			.createImage();
	private static final Image UNCHECKED = AbstractUIPlugin
			.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
					"icons/unchecked.gif").createImage(); //$NON-NLS-1$

	private static Logger logger = Logger.getLogger(ProfileSelectionPage.class);

	private final ExportProfileProvider provider;
	private final List<ExportProfile> exportProfiles;

	private final TableViewer viewer;
	private final Table table;
	private final TableColumnLayout tableColumnLayout;

	private final Button addButton;
	private final Button editButton;
	private final Button deleteButton;
	private final Button copyButton;

	public ExportProfilesWidget(Composite parent, int style,
			ExportProfileProvider provider, List<ExportProfile> exportProfiles) {
		super(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		this.provider = provider;
		this.exportProfiles = exportProfiles;

		final Composite tableContainer = new Composite(this, SWT.NULL);
		tableColumnLayout = new TableColumnLayout();
		tableContainer.setLayout(tableColumnLayout);
		viewer = new TableViewer(tableContainer, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		table = viewer.getTable();
		tableContainer.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, true, 1, 4));

		addButton = new Button(this, SWT.PUSH);
		addButton
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		addButton.setEnabled(true);

		editButton = new Button(this, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));
		editButton.setEnabled(false);

		copyButton = new Button(this, SWT.PUSH);
		copyButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false,
				true));
		copyButton.setEnabled(false);

		deleteButton = new Button(this, SWT.PUSH);
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false,
				true));
		deleteButton.setEnabled(false);

		createTable();
		initializeButtons();
	}

	private void createTable() {
		createColumns();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection.isEmpty()) {
					editButton.setEnabled(false);
					copyButton.setEnabled(false);
					deleteButton.setEnabled(false);
				} else {
					copyButton.setEnabled(true);
					ExportProfile exportProfile = (ExportProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					if (exportProfile.isProtected()) {
						editButton.setEnabled(false);
						deleteButton.setEnabled(false);
					} else {
						editButton.setEnabled(true);
						deleteButton.setEnabled(true);
					}
				}
			}
		});
		viewer.setInput(exportProfiles);
	}

	private void createColumns() {
		final TableViewerColumn exportColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		exportColumn.getColumn().setText(Messages.ExportProfilesTable_Export);
		exportColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_ExportToolTip);
		tableColumnLayout.setColumnData(exportColumn.getColumn(),
				new ColumnWeightData(10, 50, true));
		exportColumn.setLabelProvider(new CenteredImageLabelProvider() {

			@Override
			public Image getImage(Object element) {
				if (((ExportProfile) element).isChecked()) {
					return CHECKED;
				} else {
					return UNCHECKED;
				}
			}
		});
		exportColumn.setEditingSupport(new ExportEditingSupport(viewer));

		final TableViewerColumn profileColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		profileColumn.getColumn().setText(Messages.ExportProfilesTable_Profile);
		profileColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_ProfileToolTip);
		tableColumnLayout.setColumnData(profileColumn.getColumn(),
				new ColumnWeightData(25, 125, true));
		profileColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExportProfile) element).getProfileTitle();
			}
		});

		final TableViewerColumn descriptionColumn = new TableViewerColumn(
				viewer, SWT.NONE);
		descriptionColumn.getColumn().setText(
				Messages.ExportProfilesTable_Description);
		descriptionColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_DescriptionToolTip);
		tableColumnLayout.setColumnData(descriptionColumn.getColumn(),
				new ColumnWeightData(35, 175, true));
		descriptionColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ExportProfile) element).getProfile().getGeneral()
						.getDescription();
			}
		});

		final TableViewerColumn exportResourcesColumn = new TableViewerColumn(
				viewer, SWT.CENTER);
		exportResourcesColumn.getColumn().setText(
				Messages.ExportProfilesTable_Resources);
		exportResourcesColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_ResourcesToolTip);
		tableColumnLayout.setColumnData(exportResourcesColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		exportResourcesColumn
				.setLabelProvider(new CenteredImageLabelProvider() {

					@Override
					public Image getImage(Object element) {
						if (((ExportProfile) element).isExportResources()) {
							return CHECKED;
						} else {
							return UNCHECKED;
						}
					}
				});

		exportResourcesColumn
				.setEditingSupport(new ExportResourcesEditingSupport(viewer));

		final TableViewerColumn compressColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		compressColumn.getColumn().setText(
				Messages.ExportProfilesTable_Compress);
		compressColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_CompressToolTip);
		tableColumnLayout.setColumnData(compressColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		compressColumn.setLabelProvider(new CenteredImageLabelProvider() {

			@Override
			public Image getImage(Object element) {
				if (((ExportProfile) element).isCompress()) {
					return CHECKED;
				} else {
					return UNCHECKED;
				}
			}
		});
		compressColumn.setEditingSupport(new CompressEditingSupport(viewer));
		
		final TableViewerColumn convertColumn = new TableViewerColumn(viewer,
				SWT.CENTER);
		convertColumn.getColumn().setText(
				Messages.ExportProfilesTable_Convert);
		convertColumn.getColumn().setToolTipText(
				Messages.ExportProfilesTable_ConvertToolTip);
		tableColumnLayout.setColumnData(convertColumn.getColumn(),
				new ColumnWeightData(15, 75, true));
		convertColumn.setLabelProvider(new CenteredImageLabelProvider() {

			@Override
			public Image getImage(Object element) {
				if (((ExportProfile) element).isConvert()) {
					return CHECKED;
				} else {
					return UNCHECKED;
				}
			}
		});
		convertColumn.setEditingSupport(new ConvertEditingSupport(viewer));
	}

	private void initializeButtons() {
		addButton.setText(Messages.Add);
		addButton.setToolTipText(Messages.ProfileSelectionPage_AddToolTip);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddClicked();
			}
		});

		editButton.setText(Messages.Edit);
		editButton.setToolTipText(Messages.ProfileSelectionPage_EditToolTip);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onEditClicked((ExportProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});

		copyButton.setText("Copy");
		copyButton.setToolTipText("ExportProfilesWidget Copy tool tip");
		copyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onCopyClicked((ExportProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});

		deleteButton.setText(Messages.Delete);
		deleteButton
				.setToolTipText(Messages.ProfileSelectionPage_DeleteToolTip);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!viewer.getSelection().isEmpty()) {
					onDeleteClicked((ExportProfile) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement());
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void onAddClicked() {
		ProfileWizard profileWizard = new ProfileWizard(Profile.getDefault(),
				ExportProfile.getAllTitles(exportProfiles));
		WizardDialog dialog = new WizardDialog(getShell(), profileWizard);
		if (dialog.open() == Window.OK) {
			Profile profile = profileWizard.getProfile();
			try {
				provider.storeProfile(profile);
				ExportProfile exportProfile = new ExportProfile(profile,
						provider.getProjectDimension(), false, true, false, true,
						false);

				exportProfiles.add(exportProfile);
				viewer.refresh();

				viewer.setSelection(new StructuredSelection(exportProfile));
			} catch (ExportException cause) {
				logger.error(cause.getMessage());
				MessageDialog.openWarning(getShell(), Messages.Warning, String
						.format(Messages.Warning_AddingProfileFailed,
								profile.getProfileTitle()));
			}
		}
	}

	private void onEditClicked(ExportProfile exportProfile) {
		if (exportProfile.isProtected()) {
			MessageDialog.openWarning(getShell(), Messages.Warning, String
					.format(Messages.Warning_EditingDefaultProfileProhibited,
							exportProfile.getProfileTitle()));
		} else {
			String oldTitle = exportProfile.getProfileTitle();
			Profile profileCopy = new Profile(exportProfile.getProfile());
			ProfileWizard profileWizard = new ProfileWizard(profileCopy,
					exportProfile.getOthersTitles(exportProfiles));
			WizardDialog dialog = new WizardDialog(getShell(), profileWizard);
			if (dialog.open() == Window.OK) {
				exportProfile.setProfile(profileCopy);
				try {
					provider.updateProfile(exportProfile, oldTitle);
					viewer.setSelection(new StructuredSelection(exportProfile));
				} catch (ExportException cause) {
					logger.error(cause.getMessage());
					MessageDialog.openWarning(getShell(), Messages.Warning,
							String.format(
									Messages.Warning_EditingProfileFailed,
									exportProfile.getProfileTitle()));
					exportProfiles.remove(exportProfile);
				} finally {
					viewer.refresh();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void onCopyClicked(ExportProfile exportProfile) {
		ProfileWizard profileWizard = new ProfileWizard(new Profile(
				exportProfile.getProfile()),
				ExportProfile.getAllTitles(exportProfiles));
		WizardDialog dialog = new WizardDialog(getShell(), profileWizard);
		if (dialog.open() == Window.OK) {
			Profile profile = profileWizard.getProfile();
			try {
				provider.storeProfile(profile);
				ExportProfile newExportProfile = new ExportProfile(profile,
						provider.getProjectDimension(), false, true, false, true,
						false);

				exportProfiles.add(newExportProfile);
				viewer.refresh();

				viewer.setSelection(new StructuredSelection(newExportProfile));
			} catch (ExportException cause) {
				logger.error(cause.getMessage());
				MessageDialog.openWarning(
						getShell(),
						Messages.Warning,
						String.format("Warning_CopyingProfileFailed",
								profile.getProfileTitle()));
			}
		}
	}

	private void onDeleteClicked(ExportProfile exportProfile) {
		if (exportProfile.isProtected()) {
			MessageDialog.openWarning(getShell(), Messages.Warning, String
					.format(Messages.Warning_DeletingDefaultProfileProhibited,
							exportProfile.getProfileTitle()));
		} else {
			int indexBeforeRemoving = exportProfiles.indexOf(exportProfile);
			provider.deleteExportProfile(exportProfile);
			exportProfiles.remove(exportProfile);
			viewer.refresh();

			if (exportProfiles.size() > 0) {
				if (exportProfiles.size() > indexBeforeRemoving) {
					viewer.setSelection(new StructuredSelection(exportProfiles
							.get(indexBeforeRemoving)));
				} else {
					viewer.setSelection(new StructuredSelection(exportProfiles
							.get(indexBeforeRemoving - 1)));
				}
			}
		}
	}
}

package org.iviPro.newExport.view.edit;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.iviPro.newExport.profile.ExportProfile;

public class CompressEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	public CompressEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((ExportProfile) element).isCompress();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((ExportProfile) element).setCompress(((Boolean) value).booleanValue());
		viewer.refresh();
	}

}

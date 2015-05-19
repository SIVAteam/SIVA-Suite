package org.iviPro.projectsettings;

import java.util.HashMap;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class PageExport extends PreferencePage {

	private SettingsStorer storer;
	private Exportsettings export;

	public PageExport(SettingsStorer storer) {
		super(Messages.PageExport_Title);
		setDescription(Messages.PageExport_Description);
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {
		export = new Exportsettings(parent, SWT.None);
		return export;
	}

	@Override
	public boolean performOk() {
		// Do nothing, PageStartmode invokes the process
		return true;
	}

	@Override
	protected void performApply() {
		storer.storeSettings();
	}

	public boolean isInit() {
		return export != null;
	}
	
	public HashMap<String, String> getExportSettings() {
		if (isInit()) {
			return export.getExportSettings();
		} else {
			return new HashMap<String, String>();
		}
	}
	
	protected void performDefaults() {
		if (export != null) {
			export.setDefaults();			
		}
		super.performDefaults();
	}

}

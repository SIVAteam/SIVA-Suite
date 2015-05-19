package org.iviPro.projectsettings;

import java.util.HashMap;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.iviPro.application.Application;
import org.iviPro.model.ProjectSettings;

public class PageLayout extends PreferencePage {

	
	private LayoutEditor layout;
	private ProjectSettings settings;
	private SettingsStorer storer;

	public PageLayout(SettingsStorer storer) {
		super(Messages.PageLayout_LayoutTitle);
		setDescription(Messages.PageLayout_LayoutDescription);
		this.storer = storer;
	}

	@Override
	protected Control createContents(Composite parent) {
		layout = new LayoutEditor(parent, SWT.None);
		settings = Application.getCurrentProject()
				.getSettings();
		layout.setLoadedValues(settings.getSizeWidth(), settings.getSizeHeight(),
				settings.getAreaLeftWidth(), settings.getAreaTopHeight(),
				settings.getAreaBottomHeight(), settings.getAreaRightWidth());
		return layout;
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
		return layout != null;
	}
	
	public HashMap<String, Float> getLayout() {
		HashMap<String, Float> theLayout = new HashMap<String, Float>();
		theLayout.put("sizeWidth", (float)layout.getSizeWidth()); //$NON-NLS-1$
		theLayout.put("sizeHeight",(float)layout.getSizeHeight()); //$NON-NLS-1$
		theLayout.put("areaLeftWidth", layout.getAreaLeftWidth()); //$NON-NLS-1$
		theLayout.put("areaRightWidth", layout.getAreaRightWidth()); //$NON-NLS-1$
		theLayout.put("areaTopHeight", layout.getAreaTopHeight()); //$NON-NLS-1$
		theLayout.put("areaBottomHeight", layout.getAreaBottomHeight()); //$NON-NLS-1$
		return theLayout;
	}

	@Override
	protected void performDefaults() {
		if (settings != null && layout != null) {
			layout.init();			
		}
		super.performDefaults();
	}
	
}

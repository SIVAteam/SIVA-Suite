package org.iviPro.projectsettings;

import org.iviPro.model.ProjectSettings;

/**
 * Interface for classes implementing a settings component used inside a
 * <code>PreferencePage</code> or as part of the <code>ProjectCreateWizard</code>.
 *  
 * @author John
 *
 */
public interface SettingsComposite {
	
	/**
	 * Resets all fields shown on the page to their default values.
	 */
	public void setToDefault();
	
	/**
	 * Checks whether or not the chosen settings are valid and can be applied.
	 * @return true if the settings are valid - false otherwise
	 */
	public boolean checkSettings();
	
	/**
	 * Returns the <code>ProjectSettings</code> stored within this composite
	 * incorporating the values actually set in its widgets.
	 */
	public ProjectSettings getSettings();
	
	/**
	 * Updates the project's settings according to the values actually set in 
	 * the widgets of this composite.
	 */
	public void updateProjectSettings();
}

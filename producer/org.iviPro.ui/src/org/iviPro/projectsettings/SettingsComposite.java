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
	 * Writes the currently shown settings of this composite to the given settings
	 * object.
	 */
	public void writeSettingsTo(ProjectSettings settings);
	
	/**
	 * Updates the project's settings according to the values actually set in 
	 * the widgets of this composite. This update should usually support Undo/Redo.
	 */
	public void updateProjectSettings();
}

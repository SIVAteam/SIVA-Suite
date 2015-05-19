package org.iviPro.application;

import org.iviPro.model.Project;

/**
 * Listener Interface, das bei einer Änderungen alle Konsumenten benachrichtigt,
 * das Änderungen am Projekt durchgeführt worden sind.
 * 
 */
public interface ApplicationListener {

	/**
	 * Wird aufgerufen, wenn ein Projekt geoeffnet wurde.
	 * 
	 * @param project
	 *            Das geoeffnete Projekt.
	 */
	public void onProjectOpened(Project project);

	/**
	 * Wird aufgerufen, wenn ein Projekt geschlossen wurde.
	 * 
	 * @param project
	 *            Das geoeffnete Projekt.
	 */
	public void onProjectClosed(Project project);

}

package org.iviPro.newExport.exporter;

import org.iviPro.newExport.Messages;

public enum TaskSettings {

	// @formatter:off
	ALL(Messages.Task_Export, 10500), 
	PREPARING(Messages.Task_Preparing, 250), 
	PROJECT(Messages.Task_LoadProject, 250), 
	DESCRIPTOR(Messages.Task_Descriptor, 250),
	DESCRIPTOR_XML(Messages.Task_XML, 125),
	DESCRIPTOR_SMIL(Messages.Task_SMIL, 125),
	ANNOTATIONS(Messages.Task_Annotations, 9250),
	ANNOTATIONS_STATIC(Messages.Task_Static, 250), 
	ANNOTATIONS_MEDIA(Messages.Task_Media, 9000),  
	PLAYER(Messages.Task_Player, 250),
	PLAYER_FLASH(Messages.Task_Flash, 125),
	PLAYER_HTML(Messages.Task_HTML, 125),
	CLEANUP(Messages.Task_CleaningUp, 250), 
	FINISHING(Messages.Task_Finishing, 250);
	// @formatter:on

	private final String name;
	private final int duration;

	private TaskSettings(String name, int duration) {
		this.name = name;
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public int getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return name;
	}

}

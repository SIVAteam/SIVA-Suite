package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.iviPro.newExport.ExportDefinition;

public abstract class MediaVariant implements Comparable<MediaVariant>,
		ExportDefinition {

	protected String title;
	protected String description;

	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public MediaVariant() {
	}

	public MediaVariant(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		propertyChangeSupport.firePropertyChange("title", this.title, //$NON-NLS-1$
				this.title = title);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		propertyChangeSupport.firePropertyChange("description", //$NON-NLS-1$
				this.description, this.description = description);
	}

	@Override
	public abstract String toString();

	@Override
	public int compareTo(MediaVariant mediaVariant) {
		return title.compareTo(mediaVariant.getTitle());
	}

	public static List<String> getAllTitles(
			List<? extends MediaVariant> mediaVariants) {
		List<String> titlesInUse = new ArrayList<String>();
		for (MediaVariant mediaVariant : mediaVariants) {
			titlesInUse.add(mediaVariant.getTitle());
		}
		return titlesInUse;
	}

	public List<String> getOthersTitles(
			List<? extends MediaVariant> mediaVariants) {
		List<String> titlesInUse = new ArrayList<String>();
		for (MediaVariant mediaVariant : mediaVariants) {
			if (!mediaVariant.getTitle().equalsIgnoreCase(getTitle())) {
				titlesInUse.add(mediaVariant.getTitle());
			}
		}
		return titlesInUse;
	}
}

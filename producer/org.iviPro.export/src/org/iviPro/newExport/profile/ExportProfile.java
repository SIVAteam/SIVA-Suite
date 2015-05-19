package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.iviPro.transcoder.VideoDimension;

public class ExportProfile implements Cloneable {

	public static final String ARCHIVE_EXTENSION = ".zip"; //$NON-NLS-1$
	public static final String PROFILE_EXTENSION = ".xml"; //$NON-NLS-1$

	private Profile profile;
	private VideoDimension projectDimension;
	private boolean isChecked;
	private boolean exportResources;
	private boolean compress;
	private boolean convert;
	private boolean isProtected;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public ExportProfile() {
	}

	public ExportProfile(Profile profile, VideoDimension projectDimension,
			boolean isChecked, boolean exportResources, boolean compress, boolean convert,
			boolean isProtected) {
		this.profile = profile;
		this.projectDimension = projectDimension;
		this.isChecked = isChecked;
		this.exportResources = exportResources;
		this.compress = compress;
		this.convert = convert;
		this.isProtected = isProtected;
	}

	public ExportProfile(ExportProfile exportProfile) {
		this.profile = new Profile(exportProfile.getProfile());
		this.projectDimension = exportProfile.projectDimension;
		this.isChecked = exportProfile.isChecked();
		this.exportResources = exportProfile.isExportResources();
		this.compress = exportProfile.isCompress();
		this.convert = exportProfile.isConvert();
		this.isProtected = exportProfile.isProtected;
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Convenience method for accessing the profile's title directly.
	 * 
	 * @return The title of the profile.
	 */
	public String getProfileTitle() {
		return profile.getGeneral().getTitle();
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		propertyChangeSupport.firePropertyChange("profile", this.profile, //$NON-NLS-1$
				this.profile = profile);
	}

	public VideoDimension getProjectDimension() {
		return projectDimension;
	}

	public void setProjectDimension(VideoDimension projectDimension) {
		this.projectDimension = projectDimension;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		propertyChangeSupport.firePropertyChange("isChecked", this.isChecked, //$NON-NLS-1$
				this.isChecked = isChecked);
	}

	public boolean isExportResources() {
		return exportResources;
	}

	public void setExportResources(boolean exportResources) {
		propertyChangeSupport.firePropertyChange("exportResources", //$NON-NLS-1$
				this.exportResources, this.exportResources = exportResources);
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		propertyChangeSupport.firePropertyChange("compress", this.compress, //$NON-NLS-1$
				this.compress = compress);
	}
	
	public boolean isConvert() {
		return convert;
	}

	public void setConvert(boolean convert) {
		propertyChangeSupport.firePropertyChange("convert", this.convert, //$NON-NLS-1$
				this.convert = convert);
	}

	public boolean isProtected() {
		return isProtected;
	}

	public void setProtected(boolean isProtected) {
		propertyChangeSupport.firePropertyChange("isProtected", //$NON-NLS-1$
				this.isProtected, this.isProtected = isProtected);
	}

	@Override
	public String toString() {
		return "ExportProfile [profile=" + profile //$NON-NLS-1$ //$NON-NLS-2$
				+ ", isChecked=" + isChecked + ", exportResources=" //$NON-NLS-1$ //$NON-NLS-2$
				+ exportResources + ", compress=" + compress + ", convert=" + convert + ", isProtected=" //$NON-NLS-1$ //$NON-NLS-2$
				+ isProtected + "]"; //$NON-NLS-1$
	}

	public static List<String> getAllTitles(
			List<ExportProfile>... exportProfiles) {
		List<String> titlesInUse = new ArrayList<String>();
		for (List<ExportProfile> exportProfileList : exportProfiles) {
			for (ExportProfile exportProfile : exportProfileList) {
				titlesInUse.add(exportProfile.getProfileTitle()
						.toLowerCase());
			}
		}
		return titlesInUse;
	}

	public List<String> getOthersTitles(List<ExportProfile> exportProfiles) {
		List<String> titlesInUse = new ArrayList<String>();
		for (ExportProfile exportProfile : exportProfiles) {
			if (!exportProfile.getProfileTitle().equalsIgnoreCase(
					getProfileTitle())) {
				titlesInUse.add(exportProfile.getProfileTitle()
						.toLowerCase());
			}
		}
		return titlesInUse;
	}

}

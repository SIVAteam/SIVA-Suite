package org.iviPro.newExport.profile;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.iviPro.newExport.ExportDefinition;

public class GeneralConfiguration implements ExportDefinition {

	private String title;
	private String description;
	private boolean exportXml;
	private boolean exportSmil;
	private boolean exportFlashPlayer;
	private boolean exportHtmlPlayer;
	private boolean exportAudioExtensions;
	private boolean exportVideoExtensions;
	private String descriptorDirectory;
	private String imageDirectory;
	private String richPageDirectory;
	private String audioDirectory;
	private String videoDirectory;
	private String pdfDirectory;
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public GeneralConfiguration() {

	}

	public GeneralConfiguration(String title, String description,
			boolean exportXml, boolean exportSmil, boolean exportFlashPlayer,
			boolean exportHtmlPlayer, boolean exportAudioExtensions,
			boolean exportVideoExtensions, String descriptorDirectory,
			String imageDirectory, String richPageDirectory,
			String audioDirectory, String videoDirectory, String pdfDirectory) {
		this.title = title;
		this.description = description;
		this.exportXml = exportXml;
		this.exportSmil = exportSmil;
		this.exportFlashPlayer = exportFlashPlayer;
		this.exportHtmlPlayer = exportHtmlPlayer;
		this.exportAudioExtensions = exportAudioExtensions;
		this.exportVideoExtensions = exportVideoExtensions;
		this.descriptorDirectory = descriptorDirectory;
		this.imageDirectory = imageDirectory;
		this.richPageDirectory = richPageDirectory;
		this.audioDirectory = audioDirectory;
		this.videoDirectory = videoDirectory;
		this.pdfDirectory = pdfDirectory;
	}

	public GeneralConfiguration(GeneralConfiguration generalConfiguration) {
		this.title = generalConfiguration.title;
		this.description = generalConfiguration.description;
		this.exportXml = generalConfiguration.exportXml;
		this.exportSmil = generalConfiguration.exportSmil;
		this.exportFlashPlayer = generalConfiguration.exportFlashPlayer;
		this.exportHtmlPlayer = generalConfiguration.exportHtmlPlayer;
		this.exportAudioExtensions = generalConfiguration.exportAudioExtensions;
		this.exportVideoExtensions = generalConfiguration.exportVideoExtensions;
		this.descriptorDirectory = generalConfiguration.descriptorDirectory;
		this.imageDirectory = generalConfiguration.imageDirectory;
		this.richPageDirectory = generalConfiguration.richPageDirectory;
		this.audioDirectory = generalConfiguration.audioDirectory;
		this.videoDirectory = generalConfiguration.videoDirectory;
		this.pdfDirectory = generalConfiguration.pdfDirectory;
	}

	public static GeneralConfiguration getDefault() {
		return new GeneralConfiguration(PROFILE_DEFAULT_TITLE,
				PROFILE_DEFAULT_DESCRIPTION, true, false, false, false, true,
				true, XML_DIRECTORY, PICTURES_DIRECTORY, RICHTEXTS_DIRECTORY,
				AUDIOS_DIRECTORY, VIDEOS_DIRECTORY, PDF_DIRECTORY);
	}

	public static GeneralConfiguration getHtmlDefault() {
		return new GeneralConfiguration(PROFILE_DEFAULT_TITLE,
				PROFILE_DEFAULT_DESCRIPTION, true, false, false, true, true,
				true, HTML_PREFIX + XML_DIRECTORY, HTML_PREFIX
						+ PICTURES_DIRECTORY,
				HTML_PREFIX + RICHTEXTS_DIRECTORY, HTML_PREFIX
						+ AUDIOS_DIRECTORY, HTML_PREFIX + VIDEOS_DIRECTORY,
						HTML_PREFIX + PDF_DIRECTORY);
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

	public boolean isExportXml() {
		return exportXml;
	}

	public void setExportXml(boolean exportXml) {
		System.out.println(exportXml);
		propertyChangeSupport.firePropertyChange("exportXml", this.exportXml, //$NON-NLS-1$
				this.exportXml = exportXml);
	}

	public boolean isExportSmil() {
		return exportSmil;
	}

	public void setExportSmil(boolean exportSmil) {
		propertyChangeSupport.firePropertyChange("exportSmil", this.exportSmil, //$NON-NLS-1$
				this.exportSmil = exportSmil);
	}

	public boolean isExportFlashPlayer() {
		return exportFlashPlayer;
	}

	public void setExportFlashPlayer(boolean exportFlashPlayer) {
		propertyChangeSupport.firePropertyChange(
				"exportFlashPlayer", //$NON-NLS-1$
				this.exportFlashPlayer,
				this.exportFlashPlayer = exportFlashPlayer);
	}

	public boolean isExportHtmlPlayer() {
		return exportHtmlPlayer;
	}

	public void setExportHtmlPlayer(boolean exportHtmlPlayer) {
		propertyChangeSupport.firePropertyChange(
				"exportHtmlPlayer", this.exportHtmlPlayer, //$NON-NLS-1$
				this.exportHtmlPlayer = exportHtmlPlayer);
	}

	public boolean isExportAudioExtensions() {
		return exportAudioExtensions;
	}

	public void setExportAudioExtensions(boolean exportAudioExtensions) {
		propertyChangeSupport.firePropertyChange(
				"exportAudioExtensions", //$NON-NLS-1$
				this.exportAudioExtensions,
				this.exportAudioExtensions = exportAudioExtensions);
	}

	public boolean isExportVideoExtensions() {
		return exportVideoExtensions;
	}

	public void setExportVideoExtensions(boolean exportVideoExtensions) {
		propertyChangeSupport.firePropertyChange(
				"exportVideoExtensions", //$NON-NLS-1$
				this.exportVideoExtensions,
				this.exportVideoExtensions = exportVideoExtensions);
	}

	public String getDescriptorDirectory() {
		return descriptorDirectory;
	}

	public void setDescriptorDirectory(String descriptorDirectory) {
		propertyChangeSupport.firePropertyChange(
				"descriptorDirectory", //$NON-NLS-1$
				this.descriptorDirectory,
				this.descriptorDirectory = descriptorDirectory);
	}

	public String getImageDirectory() {
		return imageDirectory;
	}

	public void setImageDirectory(String imageDirectory) {
		propertyChangeSupport.firePropertyChange("imageDirectory", //$NON-NLS-1$
				this.imageDirectory, this.imageDirectory = imageDirectory);
	}

	public String getRichPageDirectory() {
		return richPageDirectory;
	}

	public void setRichPageDirectory(String richPageDirectory) {
		propertyChangeSupport.firePropertyChange(
				"richPageDirectory", //$NON-NLS-1$
				this.richPageDirectory,
				this.richPageDirectory = richPageDirectory);
	}

	public String getAudioDirectory() {
		return audioDirectory;
	}

	public void setAudioDirectory(String audioDirectory) {
		propertyChangeSupport.firePropertyChange("audioDirectory", //$NON-NLS-1$
				this.audioDirectory, this.audioDirectory = audioDirectory);
	}

	public String getVideoDirectory() {
		return videoDirectory;
	}

	public void setVideoDirectory(String videoDirectory) {
		propertyChangeSupport.firePropertyChange("videoDirectory", //$NON-NLS-1$
				this.videoDirectory, this.videoDirectory = videoDirectory);
	}
	
	public String getPdfDirectory() {
		return pdfDirectory;
	}

	public void setPdfDirectory(String pdfDirectory) {
		propertyChangeSupport.firePropertyChange("pdfDirectory", //$NON-NLS-1$
				this.pdfDirectory, this.pdfDirectory = pdfDirectory);
	}

	@Override
	public String toString() {
		return "GeneralConfiguration [title=" + title + ", description=" //$NON-NLS-1$ //$NON-NLS-2$
				+ description + ", exportXml=" + exportXml + ", exportSmil=" //$NON-NLS-1$ //$NON-NLS-2$
				+ exportSmil + ", exportFlashPlayer=" + exportFlashPlayer //$NON-NLS-1$
				+ ", exportHtmlPlayer=" + exportHtmlPlayer //$NON-NLS-1$
				+ ", exportAudioExtensions=" + exportAudioExtensions //$NON-NLS-1$
				+ ", exportVideoExtensions=" + exportVideoExtensions //$NON-NLS-1$
				+ ", descriptorDirectory=" + descriptorDirectory //$NON-NLS-1$
				+ ", imageDirectory=" + imageDirectory //$NON-NLS-1$
				+ ", richPageDirectory=" + richPageDirectory //$NON-NLS-1$ 
				+ ", audioDirectory=" + audioDirectory //$NON-NLS-1$
				+ ", videoDirectory=" + videoDirectory //$NON-NLS-1$
				+ ", pdfDirectory=" + pdfDirectory + "]"; //$NON-NLS-1$
	}
}

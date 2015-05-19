package org.iviPro.newExport.resources;

import java.util.HashSet;
import java.util.Set;

public class ProjectResources {

	private final Set<PictureResourceDescriptor> pictures;
	private final Set<ResourceDescriptor> richPages;
	private final Set<TimedResourceDescriptor> audios;
	private final Set<VideoResourceDescriptor> videos;
	private final Set<ResourceDescriptor> pdfDocuments;

	public ProjectResources() {
		this.pictures = new HashSet<PictureResourceDescriptor>();
		this.richPages = new HashSet<ResourceDescriptor>();
		this.audios = new HashSet<TimedResourceDescriptor>();
		this.videos = new HashSet<VideoResourceDescriptor>();
		this.pdfDocuments = new HashSet<ResourceDescriptor>();
	}

	public Set<PictureResourceDescriptor> getPictures() {
		return pictures;
	}

	public Set<ResourceDescriptor> getRichPages() {
		return richPages;
	}

	public Set<TimedResourceDescriptor> getAudios() {
		return audios;
	}

	public Set<VideoResourceDescriptor> getVideos() {
		return videos;
	}
	
	public Set<ResourceDescriptor> getPdfDocuments() {
		return pdfDocuments;
	}

}

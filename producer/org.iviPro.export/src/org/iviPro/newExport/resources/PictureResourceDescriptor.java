package org.iviPro.newExport.resources;

import java.io.File;

import org.iviPro.model.resources.Picture;

public class PictureResourceDescriptor extends ResourceDescriptor {

	private final Picture picture;

	public PictureResourceDescriptor(File source, String target,
			Picture picture) {
		super(source, target);
		this.picture = picture;
	}

	public Picture getPicture() {
		return picture;
	}
}

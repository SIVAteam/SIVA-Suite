package org.iviPro.mediaaccess.mediameta.metaaccessors;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.iviPro.mediaaccess.mediameta.interfaces.I_MediaMetaAccessor;
import org.iviPro.model.resources.Picture;

public class ImageAccessor implements I_MediaMetaAccessor {
	
	private Picture picture;
	private Dimension dim;
	private long size;
	private String format;
	
	public ImageAccessor(Picture picture) {
		this.picture = picture;
		extractData();
	}
	
	private void extractData() {
		String path = picture.getFile().getAbsolutePath();
		File file = new File(path);
		this.size = file.length();		
		this.format = path.substring(path.lastIndexOf(".")).toLowerCase();
		
		URL u;
		try {
			u = file.toURI().toURL();
			ImageDescriptor des = ImageDescriptor.createFromURL(u);	
			// set dimension
			this.dim = new Dimension(des.getImageData().width, des.getImageData().height);			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public long getMediaLengthNano() {
		return 0;
	}
	
	@Override
	public int getFrameRate() {
		return 0;
	}

	@Override
	public long getSize() {
		return this.size;
	}

	@Override
	public String getCodec() {
		return this.format;
	}

	@Override
	public Dimension getDimension() {
		return this.dim;
	}

	@Override
	public double getAspectRatio() {
		return this.dim.getWidth() / this.dim.getHeight();
	}

	@Override
	public double getBitRate() {
		return 0;
	}
}

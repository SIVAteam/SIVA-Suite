package org.iviPro.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * nothing more than a serializable bufferedimage
 * used for preview images
 * @author juhoffma
 */
public class SivaImage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private byte[] byteImage = null;
	
	private transient BufferedImage img;
	 
	public SivaImage(BufferedImage bufferedImage) {
		this.byteImage = toByteArray(bufferedImage);
	}
	
	public BufferedImage getBufferedImage() {
		if (img == null) {
			img =  fromByteArray(byteImage);
		}	
		return img;
	}
	
	private BufferedImage fromByteArray(byte[] imagebytes) {
		try {
			if (imagebytes != null && (imagebytes.length > 0)) {
				BufferedImage im = ImageIO.read(new ByteArrayInputStream(imagebytes));	
				return im;
			}
			return null;
		} catch (IOException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}
	
	private byte[] toByteArray(BufferedImage bufferedImage) {
		if (bufferedImage != null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "png", baos);
				byte[] b = baos.toByteArray();
				return b;
			} catch (IOException e) {
				throw new IllegalStateException(e.toString());
			}
		}
		return new byte[0];
	}	
}

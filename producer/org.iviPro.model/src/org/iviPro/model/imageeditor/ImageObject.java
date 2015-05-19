package org.iviPro.model.imageeditor;

import java.io.Serializable;

import org.eclipse.swt.graphics.RGB;

public abstract class ImageObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public float x;
	public float y;
	public float width;
	public float height;
	public RGB color;
	public float linewidth;
	
	public ImageObject(float x, float y, float width, float height, RGB color,
			float linewidth) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.linewidth = linewidth;
	}
	
	
}

package org.iviPro.model.imageeditor;

import org.eclipse.swt.graphics.RGB;

public class IText extends ImageObject {

	public String text;
	public boolean bold;
	public boolean italic;
	public boolean underline;
	public float fontsize;
	
	public IText(float x, float y, float width, float height, RGB color,
			float linewidth) {
		super(x, y, width, height, color, linewidth);
	}
	
	public IText(float x, float y, String text, RGB color, boolean bold, boolean italic, boolean underline, float fontsize) {
		super(x, y, 0, 0, color, 0);
		this.text = text;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.fontsize = fontsize;
	}

}

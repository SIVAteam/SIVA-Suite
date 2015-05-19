package org.iviPro.export.smil;

/**
 * Class that holds the information about a video that has to be moved.
 */
public class VideoMovingInformation {
	private String attribute;
	private int margin;
	
	public VideoMovingInformation(String attribute, int margin) {
		this.attribute = attribute;
		this.margin = margin;
	}
	
	/**
	 * Standard-getter of the attribute.
	 * @return	The attribute of this object.
	 */
	public String getAttribute() {
		return attribute;
	}
	
	/**
	 * Standard-getter of the margin.
	 * @return	The margin of this object.
	 */
	public int getMargin() {
		return margin;
	}
	
}

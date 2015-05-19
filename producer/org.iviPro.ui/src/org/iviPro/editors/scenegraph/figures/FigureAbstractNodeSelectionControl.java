package org.iviPro.editors.scenegraph.figures;


import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.iviPro.model.resources.Picture;

public abstract class FigureAbstractNodeSelectionControl extends FigureRectangle {
	
	private static final Dimension SIZE = new Dimension(120, 35);
	private static final int SEMANTIC_ZOOM_IMG_PADDING = 2;
	private static final int MAX_SEMANTIC_ZOOM_IMG_BOUNDS = SIZE.height - 4;

	private int zoom =1;
	private Picture buttonImage;

	public FigureAbstractNodeSelectionControl(Point pos, String text, int zoom, 
			Picture buttonImage, Color darkGradient, Color lightGradient) {
		super(pos, SIZE, text, darkGradient, lightGradient, ColorConstants.white);
		setZoom(zoom);
		setButtonImage(buttonImage);
	}
	
	@Override
	protected void paintFigure(Graphics g) {
		paintGradient(g);
		paintSmoothBorder(g);	
	
		if (zoom == 1) {
			String extText=title;
			if (buttonImage != null) {
				extText += " / "; //$NON-NLS-1$
				extText += buttonImage.getTitle();
			}
			paintTitle(g, extText, 0, false);
		}else{
			
			if(buttonImage != null){
				// Display small image in front of title
				Image img = buttonImage.getImage();
				int width= img.getBounds().width;
				int height = img.getBounds().height;
				double ratio = (double)width/(double)height;
				if (width > height) {
					width= MAX_SEMANTIC_ZOOM_IMG_BOUNDS;
					height = (int) (width/ratio);
				} else {
					height = MAX_SEMANTIC_ZOOM_IMG_BOUNDS;
					width= (int) (height*ratio);
				}
				// Center vertically
				int posy = ((bounds.height - height)/2);
				g.drawImage(img, 0, 0, img.getBounds().width,
						img.getBounds().height, 
						bounds.x + SEMANTIC_ZOOM_IMG_PADDING,
						bounds.y + posy,
						width, height);
				
				paintTitle(g, title, width + 3*SEMANTIC_ZOOM_IMG_PADDING
						- getPadding(), false);
			} else {
				paintTitle(g, title, 0, false);
			}
		}
		super.paintFigure(g);
	}
	
	public void setZoom(int zoom){
		this.zoom = zoom;
	}

	public void setButtonImage(Picture buttonImage){
		this.buttonImage = buttonImage;
	}
}

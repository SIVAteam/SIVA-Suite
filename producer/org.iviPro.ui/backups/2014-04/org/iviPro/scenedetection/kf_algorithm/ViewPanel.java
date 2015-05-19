package org.iviPro.scenedetection.kf_algorithm;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import javax.swing.JPanel;

public class ViewPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private BufferedImage imageToDraw;
	private String text;
	
    public ViewPanel(BufferedImage img, String text) {
        super();
        setName(text);
        imageToDraw = img;
        this.setPreferredSize(new Dimension(imageToDraw.getWidth(), imageToDraw.getHeight()));
        this.text = text;
    }
    
    public ViewPanel(int[] img, int width, int height) {
    	super();
    	imageToDraw = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    	WritableRaster raster = Raster.createWritableRaster(imageToDraw.getSampleModel(), null);
    	raster.setPixels(0, 0, width, height, img);
    	imageToDraw.setData(raster);
        this.setPreferredSize(new Dimension(imageToDraw.getWidth(), imageToDraw.getHeight()));
    }
    
    public ViewPanel(int[][] img, int width, int height) {
    	super();
    	int[] image = new int[width * height];
    	for (int i = 0; i < image.length; i++) {
    		int row = (int)Math.floor(i / width);
    		int column = i % width;
    		image[i] = img[row][column];
			if(image[i] > 1) {
   //             System.out.println("Luminanz: "+image[i]);
			}
		}
    	imageToDraw = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    	WritableRaster raster = Raster.createWritableRaster(imageToDraw.getSampleModel(), null);
    	raster.setPixels(0, 0, width, height, image);
    	imageToDraw.setData(raster);
        this.setPreferredSize(new Dimension(imageToDraw.getWidth(), imageToDraw.getHeight()));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(imageToDraw, null, 0, 0);
    }
	
}

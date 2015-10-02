/**
 * 
 */
package org.iviPro.model.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.iviPro.model.IPixelBasedObject;
import org.iviPro.model.LocalizedElement;
import org.iviPro.model.Project;

/**
 * @author dellwo
 */
public class Picture extends IPixelBasedObject implements IResource {
	
	// für Bilder werden an versch. Stellen Vorschaubildchen verwendet
	// z.B. Popup im Med.Rep oder direkt im Med.Rep
	// damit nicht jedes mal neu skaliert werden muss, kann das Bild 
	// in der thumbnail HashMap gespeichert werden und wird beim ersten Bedarf berechnet
	public static final String THUMBNAIL_MED_REP = "THUMBNAIL_MED_REP";
	public static final String THUMBNAIL_MED_POP = "THUMBNAIL_MED_POP";
	public static final String THUMBNAIL_MED_PICGAL = "THUMBNAIL_PIC_GAL";
	public static final String THUMBNAIL_PIC_ANNO = "THUMBNAIL_PIC_ANNO";
	
	// Image kann nicht serialisiert werden => die Liste wird jedes mal neu befüllt
	// deshalb transient weil die Bilder nicht mit abgespeichert werden 
	// Man könnte es zwar über nen ByteStream serialisieren ... habe ich getestet und 
	// ist ziemlich langsam => Laden/Speichern wird lahm ...
	private transient HashMap<String, Image> thumbnails = new HashMap<String, Image>();
	private transient ImageDescriptor des;

	public Picture(File file, Project project) {
		super(file, null, project);
		createThumbnails();
	}
	
	public static boolean isReadableImage(File file) {
		try {
			new ImageData(file.getAbsolutePath());
		} catch (SWTException e) {
			return false;
		}
		return true;
	}
	
	private void createThumbnails() {
		if (this.thumbnails == null) {
			this.thumbnails = new HashMap<String, Image>();
		}
		
		ImageData data = getImageData();		
		if (this.thumbnails.get(THUMBNAIL_MED_REP) == null) {			
			Image repImage = new Image(Display.getDefault(), data.scaledTo(16, 16));
			this.thumbnails.put(THUMBNAIL_MED_REP, repImage);
		}
		if (this.thumbnails.get(THUMBNAIL_MED_POP) == null) {
			// Größen für die Thumnails
			int width = 150;
			double aspect = (double) data.width/(double) data.height;
			int height = (int) (width/aspect);
			Image popImage = new Image(Display.getCurrent(), getImageData().scaledTo(width, height));
			this.thumbnails.put(THUMBNAIL_MED_POP, popImage);
		}
		if (this.thumbnails.get(THUMBNAIL_MED_PICGAL) == null) {
			int width;
			int height;
			// maximale Größe für Breite oder Höhe für ein Bild der Bildergalerie
			int maxImageDimPG = 150;					
			int imgWidth = data.width;
			int imgHeight = data.height;
			if (imgWidth < imgHeight) {
				width = maxImageDimPG;
				double newHeight = (double) imgHeight / (double) imgWidth * width;
				height = (int) newHeight; 	
			} else {
				height = maxImageDimPG;
				double newWidth = (double) imgWidth / (double) imgHeight * height;
				width = (int) newWidth; 											
			}
			Image scaledImage = new Image(Display.getCurrent(), data.scaledTo(width, height));
			this.thumbnails.put(THUMBNAIL_MED_PICGAL, scaledImage);
		}
		
		if (this.thumbnails.get(THUMBNAIL_PIC_ANNO) == null) {
			// maximale Größe für ein Bilder der Einbildannotation
			int maxImageDimP = 450;
			int imgWidth = data.width;
			int imgHeight = data.height;
			int width;
			int height;
			if (imgWidth < maxImageDimP) {
				width = imgWidth;
				height = imgHeight;
			} else {
				// Breite größer => berechne entsprechende Höhe 
				width = maxImageDimP;
				double newHeight = (double) imgHeight / (double) imgWidth * width;
				height = (int) newHeight; 
			}
			Image scaledImage = new Image(Display.getCurrent(), data.scaledTo(width, height));
			this.thumbnails.put(THUMBNAIL_PIC_ANNO, scaledImage);
		}
	}

	/**
	 * liefert das Thumbnail für den Key
	 * @param key
	 * @return
	 */
	public Image getThumbnail(String key) {
		if (this.thumbnails == null) {
			createThumbnails();
		}		
		if (!this.thumbnails.containsKey(key)) {
			createThumbnails();
		}	
		return this.thumbnails.get(key);
	}
	
	public void clearThumbnails() {	
		if (this.thumbnails != null) {
			for (Image img : this.thumbnails.values()) {
				if (img != null && !img.isDisposed()) {
					img.dispose();					
				}
			}
		}
		this.thumbnails = new HashMap<String, Image>();
	}
	
	/**
	 * Bilddeskriptor für das Orginalbild
	 * @return
	 */
	private ImageDescriptor getImageDescriptor() {
		if (des == null) {
			String path = getFile().getAbsolutePath();
			File file = new File(path);
			URL u;
			try {
				u = file.toURI().toURL();
				des = ImageDescriptor.createFromURL(u);		
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}				
		}
		return des;
	}

	/**
	 * Laedt die Bilddaten
	 * 
	 * @return
	 */
	public Image getImage() {
		if (des == null) {
			getImageDescriptor();			
		} 
		if (des != null) {
			return des.createImage();
		}
		return null;
	}
	
	/**
	 * gibt für das Bild das ImageData zurück
	 * Es wird bei einem Bild der Bildtyp nicht richtig gesetzt
	 * egal ob man das Bild direkt erstellt oder z.B. aus ImageData
	 * Das ImageData Objekt des ImageDescriptor hält die Daten der Orginal Datei.
	 * Wenn man daraus ein Bild erzeugt, wird das ImageData im Bild allerdings
	 * wieder nicht gesetzt. Hiermit erhält man dsa korrekt befüllte ImageData Objekt
	 */
	public ImageData getImageData() {
		if (des == null) {
			getImageDescriptor();			
		}
		if (des != null) {
			return des.getImageData();
		}
		return null;	
	}

	@Override
	public List<LocalizedElement> getLocalizedContents() {
		return new ArrayList<LocalizedElement>(getFiles());
	}
}

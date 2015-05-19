package org.iviPro.preview;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.iviPro.editors.imageeditor.ImageEditWidget;
import org.iviPro.model.Picture;
import org.iviPro.model.graph.INodeAnnotation;
import org.iviPro.model.graph.NodeAnnotationPicture;
import org.iviPro.model.graph.NodeAnnotationRichtext;
import org.iviPro.model.graph.NodeAnnotationText;
import org.iviPro.model.imageeditor.ImageObject;

/**
 * Anzeigebereich für eine Annotation
 * @author langa
 *
 */
public class AnnoComposite extends Composite {

	Control content;
	Logger logger = Logger.getLogger(AnnoComposite.class);

	public AnnoComposite(Composite parent, int style) {
		super(parent, style);
		this.setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	/**
	 * Zeigt eine Annotation an
	 * @param annotation Die anzuzeigende Annotation
	 */
	public void showAnnotation(final INodeAnnotation annotation) {
		Rectangle bounds = this.getBounds();
		removeAnnotation();
		if (annotation instanceof NodeAnnotationText) {
			//Textanzeige im simplen Label
			content = new Label(this, SWT.WRAP);
			content.setBounds(0, 0, bounds.width, bounds.height);
			((Label) content).setText(annotation.getDescription());
		} else if (annotation instanceof NodeAnnotationRichtext) {
			// Richtextdarstellung in Browserkomponente anzeigen und Links verarbeiten
			try {
				content = new Browser(this, SWT.None);
				content.setBounds(0, 0, bounds.width, bounds.height);
				String html = "<html><head></head><body style='overflow:hidden'>" + ((NodeAnnotationRichtext) annotation).getRichtext().getContent() + "</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$
				((Browser) content).setText(externalizeLinks(html));
			} catch (SWTError e) {
				logger.error("Browser could not be initialized\n", e); //$NON-NLS-1$
			}
		} else if (annotation instanceof NodeAnnotationPicture) {
			Picture picture = ((NodeAnnotationPicture) annotation).getPicture();
			Image img = new Image(
					Display.getCurrent(), picture.getImageData());

			// Overlayobjekte des Bildes zeichnen
			GC gc = new GC(img);
			gc.setAlpha(255);
			for (ImageObject o : picture.getObjects()) {
				ImageEditWidget.drawObject(gc,
						o, img);
			}
			gc.dispose();

			// Bild für die Anzeige skalieren
			int imagewidth = img.getBounds().width;
			int imageheight = img.getBounds().height;
			int width = getBounds().width;
			int height = getBounds().height;
			if (width <= height) {
				imageheight = (int) ((width * 1.0f / imagewidth * 1.0f) * imageheight);
				imagewidth = width;
			} else {
				imagewidth = (int) ((height * 1.0f / imageheight * 1.0f) * imagewidth);
				imageheight = height;
			}
			if (imagewidth > 0 && imageheight > 0) {
				final Image scaledImage = new Image(Display.getCurrent(), img.getImageData().scaledTo(imagewidth, imageheight));

			content = new Composite(this, SWT.None);
			content.setBounds(0, 0, bounds.width, bounds.height);
			content.addPaintListener(new PaintListener() {
				
				@Override
				public void paintControl(PaintEvent e) {
					e.gc.drawImage(scaledImage, 0, 0);
					
				}
			});
			}

		}
		redraw();
	}
	/**
	 * Fügt zu den Links im Richtext ein "target='_blank'" hinzu um Links
	 * im externe Browser zu öffnen
	 * @param html String ohne externe Weiterleitung
	 * @return String mit externer Weiterleitung
	 */
	public static String externalizeLinks(String html) {
		String externalize = "<a target='_blank' href"; //$NON-NLS-1$
		html = html.replaceAll("<a href", externalize); //$NON-NLS-1$
		return html;
	}

	/**
	 * Leert den Anzeigebereich
	 */
	protected void removeAnnotation() {
		if (content != null) {
			content.dispose();
			content = null;
		}
	}

}

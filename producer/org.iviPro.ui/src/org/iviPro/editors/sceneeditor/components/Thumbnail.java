package org.iviPro.editors.sceneeditor.components;

import java.awt.Dimension;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.iviPro.theme.Colors;

/**
 * Widget-Klasse die ein Thumbnail fuer einen Frame kapselt.
 * 
 */
public class Thumbnail extends Composite implements PaintListener {

	private static Logger logger = Logger.getLogger(Thumbnail.class);

	/**
	 * Groesse der Thumbnails der gegrabbten Frames. Muss in maxThumbSize hinein
	 * passen.
	 */
	public static Dimension thumbSize = null;

	/**
	 * Maximale Groesse der Thumbnails fuer die gegrabbten Frames
	 */
	public static Dimension MAX_THUMB_SIZE = new Dimension(85, 60);

	/**
	 * Das Image fuer das Thumbnail oder null, falls das Thumbnail noch nicht
	 * geladen wurde.
	 */
	public Image image = null;

	/**
	 * Erstellt ein neues Thumbnail-Objekt
	 * 
	 * @param parent
	 *            Parent-Widget
	 * @param style
	 *            SWT-Styles
	 */
	public Thumbnail(Composite parent, int style) {
		super(parent, style);
		setBackground(Colors.VIDEO_TIMELINE_THUMB_BG.getColor());
		addPaintListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events
	 * .PaintEvent)
	 */
	@Override
	public void paintControl(PaintEvent e) {

		// Zeichne das Bild in das Control, falls vorhanden.
		if (thumbSize != null) {
			e.gc.fillRectangle(0, 0, thumbSize.width, thumbSize.height);
			if (image != null) {
				int widthDiff = thumbSize.width - image.getBounds().width;
				int heightDiff = thumbSize.height - image.getBounds().height;
				e.gc.drawImage(image, widthDiff / 2, heightDiff / 2);
			}
			e.gc.drawRectangle(0, 0, thumbSize.width - 1, thumbSize.height - 1);
		} else {
			e.gc.fillRectangle(getBounds());
		}
		e.gc.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int hint, int hint2, boolean changed) {
		// Berechnet Groesse der Komponente, was genau der Thumbnail-
		// groesse entspricht, falls diese schon bekannt ist.
		if (thumbSize == null) {
			return new Point(0, 0);
		} else {
			return new Point(thumbSize.width, thumbSize.height);
		}
	}

	/**
	 * Gibt den Wert von image zurueck
	 * 
	 * @return Wert von image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Setzt den Wert von image.
	 * 
	 * @param image
	 *            Neuer Wert von image
	 */
	public void setImage(Image image) {
		// Setze das Bild und fordere Redraw der Komponente.
		this.image = image;
		if (!isDisposed()) {
			this.redraw();
		}
		logger.debug("Thumbnail.setImage(): imgSize=" + image.getBounds()); //$NON-NLS-1$
	}

	public Dimension getMaxThumbSize() {
		return MAX_THUMB_SIZE;
	}
}

package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.iviPro.model.resources.Picture;

/**
 * Komponente wird zum auswaehlen von Bildern verwendet. Es erstellt eine
 * voransicht des betreffenden Bildes mit Grundinformationen ueber Titel, Pfad,
 * und Resolution.
 * 
 * @author niederhuber
 */
public class Thumbnail extends JComponent implements MouseListener,
		FocusListener {

	private static final String RESOLUTION_SEPARATOR = "x";
	private static final double IMAGE_FACTOR = 0.05;
	private static final double FONT_FACTOR = 0.15;
	private static final int DEFAULT_THUMBNAIL_SIZE = 50;
	private static final Border BORDER = BorderFactory
			.createRaisedBevelBorder();

	private static Logger logger = Logger.getLogger(Thumbnail.class);

	private Picture picture;
	private Image image;

	/**
	 * Erstellt einen neuen Thumbnail
	 * 
	 * @param title
	 *            Bildtitel
	 * @param imageFile
	 *            Bilddatei
	 */
	public Thumbnail(Picture imageFile, int size) {
		super();
		this.setPreferredSize(new Dimension(size, size));
		this.setPicture(imageFile);
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addFocusListener(this);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		// groesze der schrift berechnen abhaengig von der hoehe
		Font font = new Font(g2.getFont().getFamily(), g2.getFont().getStyle(),
				(int) (getHeight() * FONT_FACTOR));
		// schrift hoehe fuer den richtigen abstand berechnen
		g2.setFont(font);
		FontRenderContext frc = g2.getFontRenderContext();
		float fontHeight = font.getLineMetrics(picture.getTitle(), frc).getHeight();
		// offset fuer das bild berechnen
		int imageOffset = (int) (getHeight() * IMAGE_FACTOR);
		int labelOffset = getHeight() / 2;
		// komponenten zeichnen mit bestimmten offset vom rand der komponente
		g2.drawImage(image, imageOffset, imageOffset, getHeight() - imageOffset
				* 2, getHeight() - imageOffset * 2, null);
		g2.drawString(Messages.ImageDialog_Thumbnail_Label_Title + picture.getTitle(),
				getHeight(), labelOffset - fontHeight);
		g2.drawString(Messages.ImageDialog_Thumbnail_Label_Path
				+ picture.getFile().getAbsolutePath(), getHeight(),
				labelOffset);
		g2.drawString(Messages.ImageDialog_Thumbnail_Label_Resolution
				+ picture.getDimension().width + RESOLUTION_SEPARATOR
				+ picture.getDimension().height, getHeight(), labelOffset + fontHeight);
	}

	/**
	 * Setzt das angegebene Datei als Source fuer das Bild.
	 * 
	 * @param imageFile
	 *            Bilddatei
	 */
	public void setPicture(Picture image) {
		picture = image;
		try {
			// laden und verkleiner der bild datei um unoetigen
			// speicherverbrauch zu minimieren
			this.image = (BufferedImage) ImageIO.read(image
					.getFile().getValue());
			int height = getPreferredSize().height;
			// wenn keine groesze gestzt wurde standardwert verwenden
			if (height < 1)
				height = DEFAULT_THUMBNAIL_SIZE;
		} catch (Exception e) {
			e.printStackTrace();
			String title = Messages.ImageDialog_Thumbnail_Error_Dialog_Title;
			String errorMsg = Messages.ImageDialog_Thumbnail_Error_LoadingImage;
			logger.error(errorMsg);
			JOptionPane.showMessageDialog(this, errorMsg, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Gibt die Bilddatei zurueck.
	 * 
	 * @return Bilddatei.
	 */
	public Picture getPicture() {
		return picture;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Setzt die Auswahl auf das Thumbnail.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		this.requestFocusInWindow();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Beim verlieren des Focus wird der Rand entfernt.
	 */
	@Override
	public void focusLost(FocusEvent e) {
		this.setBorder(null);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Beim erhalten des Focus wird ein Rand auf die Komponente gesetzt um sie
	 * hervorzuheben.
	 */
	@Override
	public void focusGained(FocusEvent e) {
		this.setBorder(BORDER);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

}

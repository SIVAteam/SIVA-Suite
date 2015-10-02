package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.iviPro.application.Application;
import org.iviPro.model.IAbstractBean;
import org.iviPro.model.resources.Picture;
import org.iviPro.theme.Icons;

public class InsertImageDialog extends JDialog {

	/**
	 * Document das nur Zahlen kleiner dem positiven Integer-Bereich annimmt.
	 */
	public static class NumbericDocument extends PlainDocument {
		// regex fuer alle zahlen
		private Pattern digits = Pattern.compile("\\d*");

		@Override
		public void insertString(int offs, String str,
				javax.swing.text.AttributeSet a) throws BadLocationException {
			// nur zahleneingaben zulassen
			if (str != null && digits.matcher(str).matches() && getLength() < 9)
				super.insertString(offs, str, a);
		}
	}

	private static final int THUMBNAIL_SIZE = 70;
	private static final int DEFAULT_WINDOW_WIDTH = 400;
	private static final int DEFAULT_WINDOW_HEIGHT = 500;

	private JPanel imageContainer;
	private Picture selected;
	private JButton btnInsert;
	private InitializeThumbnails initThumbnails;

	/**
	 * Thread zum laden der Thumbnails.
	 */
	public class InitializeThumbnails extends Thread {

		private boolean running;

		@Override
		public void run() {
			// Alle komponenten entfernen
			imageContainer.removeAll();
			/*
			 * dummy element um listenform zu gewaehrleisten
			 */
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.weightx = 1.0;
			c.weighty = 1.0;
			// dummy element am ende des panels anfuegen
			c.gridy = Application.getCurrentProject().getMediaObjects().size();
			imageContainer.add(new JPanel(), c);
			c.weighty = 0.0;
			c.gridy = 0;
			/*
			 * liste von thumbnails aus bilder vom media repo
			 */
			for (final IAbstractBean mediaObject : Application
					.getCurrentProject().getMediaObjects()) {
				if (mediaObject instanceof Picture) {
					Picture picture = (Picture) mediaObject;
					final Thumbnail thumbnail = new Thumbnail(picture,
							THUMBNAIL_SIZE);
					// wird ein thumbnail vom benutzer selektiert wir fuer die
					// weitere verarbeitung eine referenz gehalten
					thumbnail.addFocusListener(new FocusListener() {
						@Override
						public void focusLost(FocusEvent arg0) {
						}

						@Override
						public void focusGained(FocusEvent arg0) {
							selected = thumbnail.getPicture();
						}
					});
					c.gridy = c.gridy + 1;
					imageContainer.add(thumbnail, c);
					// focus auf erstes element setzen
					if (c.gridy == 1) {
						// focus auf erstes element setzen
						imageContainer.getComponent(1).requestFocus();
					}
					validate();
				}
				if (!running) {
					break;
				}
			}

		}

		@Override
		public synchronized void start() {
			running = true;
			super.start();
		}

		/**
		 * Bricht das laden der Thumbnails ab.
		 */
		public void stopRunning() {
			running = false;
		}
	}

	public InsertImageDialog(Frame parent) {
		super(parent, Messages.ImageDialog_Title);
		setLayout(new GridBagLayout());
		setSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
		// fensert in der mitte des bildschirms plazieren
		setLocationRelativeTo(null);
		// fenster icon setzen
		setIconImage(Icons.OBJECT_MEDIA_PICTURE.getImageIcon().getImage());
		// komponenten erzeugen
		createThumbnailPane();
		createControlPane();
	}

	/**
	 * Erzeugt die Komponente fuer die Thumbnails und laedt diese.
	 */
	private void createThumbnailPane() {
		// thumbnail container einrichten
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 3;
		c.weightx = 1.0;
		c.weighty = 1.0;
		imageContainer = new JPanel();
		imageContainer.setLayout(new GridBagLayout());
		JScrollPane scrollContainer = new JScrollPane(imageContainer);
		this.add(scrollContainer, c);
	}

	/**
	 * Erzeugt die Controll-Elemente zum Einfuegen und Abbrechen.
	 */
	private void createControlPane() {
		JPanel controls = new JPanel();
		controls.setBorder(BorderFactory.createTitledBorder(""));
		// control position
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 3.0;
		c.gridy = 1;
		c.gridx = 1;
		this.add(controls, c);
		// button zum einfuegen einer grafik inzufuegen
		btnInsert = new JButton(Messages.ImageDialog_Button_Insert);
		btnInsert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initThumbnails.stopRunning();
				imageContainer.removeAll();
				setVisible(false);
			}
		});
		controls.add(btnInsert);
		// button zum schlieszen des dialog feldes
		JButton btnCancle = new JButton(Messages.ImageDialog_Button_Cancel);
		btnCancle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initThumbnails.stopRunning();
				remove(imageContainer);
				imageContainer.removeAll();
				setVisible(false);
			}
		});
		controls.add(btnCancle);
	}

	/**
	 * Gibt den vom Benutzer ausgewaehlten Thumbnail zurueck.
	 * 
	 * @return Ausgewaehltes Bild als Picture.
	 */
	public Picture getSelectedImage() {
		return selected;
	}

	/**
	 * Listener der aufgerufen wird sobald der Benutzer auf Einfuegen klickt.
	 * 
	 * @param action
	 *            Listener
	 */
	public void addInsertListener(ActionListener action) {
		btnInsert.addActionListener(action);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// liste aller bilder aus repository erstellen
			initThumbnails = new InitializeThumbnails();
			initThumbnails.start();
		}
		super.setVisible(visible);
	}
}

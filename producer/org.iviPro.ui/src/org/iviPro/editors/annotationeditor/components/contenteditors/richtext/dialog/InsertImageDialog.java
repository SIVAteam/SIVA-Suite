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
	private JTextField txtWidth;
	private JTextField txtHeight;
	private JCheckBox boxRatio;
	private InitializeThumbnails initThumbnails;

	// ratio of the currently selected image, ratio = width/height
	private double ratio = 1d;
	private boolean keepRatio = true;
	private boolean widthChanged = false;
	private boolean heightChanged = false;

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
							txtWidth.setText(String.valueOf(selected
									.getDimension().width));
							txtHeight.setText(String.valueOf(selected
									.getDimension().height));
							ratio = (double) selected.getDimension().width
									/ (double) selected.getDimension().height;
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
		createOptionsPane();
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
		controls.setBorder(BorderFactory
				.createTitledBorder(Messages.ImageDialog_Group_Controls));
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
	 * Erzeugt die Komponenten zum uebernehmen von Optionen.
	 */
	private void createOptionsPane() {
		JPanel options = new JPanel();
		options.setBorder(BorderFactory
				.createTitledBorder(Messages.ImageDialog_Group_Options));
		options.setLayout(new GridLayout(0, 2));
		// control position
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridy = 1;
		c.gridx = 0;
		this.add(options, c);

		// erstellt ein eingabefeld fuer die laenge des bildes
		JLabel lblWidth = new JLabel(Messages.ImageDialog_Label_Width);
		txtWidth = new JTextField();
		txtWidth.setDocument(new NumbericDocument());
		txtWidth.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				widthChanged = true;
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				widthChanged = true;
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				widthChanged = true;
			}
		});
		txtWidth.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (widthChanged) {
					modHeight();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				widthChanged = false;
			}

			private void modHeight() {
				if (txtWidth.getText().length() > 0 && keepRatio) {
					txtHeight.setText(Integer.toString((int) (Integer
							.parseInt(txtWidth.getText()) / ratio)));
				}
			}
		});

		if (selected != null)
			txtWidth.setText(String.valueOf(selected.getDimension().width));
		options.add(lblWidth);
		options.add(txtWidth);

		// erstellt ein eingabefeld fuer die hoehe des bildes
		JLabel lblHeight = new JLabel(Messages.ImageDialog_Label_Height);
		txtHeight = new JTextField();
		txtHeight.setDocument(new NumbericDocument());
		txtHeight.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				heightChanged = true;
			}

			public void insertUpdate(DocumentEvent documentEvent) {
				heightChanged = true;
			}

			public void removeUpdate(DocumentEvent documentEvent) {
				heightChanged = true;
			}
		});
		txtHeight.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (heightChanged) {
					modHeight();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				heightChanged = false;
			}

			private void modHeight() {
				if (txtHeight.getText().length() > 0 && keepRatio) {
					txtWidth.setText(Integer.toString((int) (Integer
							.parseInt(txtHeight.getText()) * ratio)));
				}
			}
		});
		if (selected != null)
			txtHeight.setText(String.valueOf(selected.getDimension().height));
		options.add(lblHeight);
		options.add(txtHeight);

		JLabel lblRatio = new JLabel(Messages.ImageDialog_Label_Ratio);
		boxRatio = new JCheckBox();
		boxRatio.setSelected(keepRatio);
		boxRatio.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				keepRatio = boxRatio.isSelected();
			}
		});

		options.add(lblRatio);
		options.add(boxRatio);
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
	 * Gibt die vom Benutzer spezifizierte Groesze fuer das Bild an.
	 * 
	 * @return Bildgroesze.
	 */
	public Dimension getOptionSize() {
		return new Dimension(Integer.valueOf(txtWidth.getText()),
				Integer.valueOf(txtHeight.getText()));
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
			// reset textfields
			txtWidth.setText(null);
			txtHeight.setText(null);
			// liste aller bilder aus repository erstellen
			initThumbnails = new InitializeThumbnails();
			initThumbnails.start();
		}
		super.setVisible(visible);
	}
}

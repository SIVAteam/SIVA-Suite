package org.iviPro.editors.annotationeditor.components.contenteditors.richtext.dialog;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;

import org.eclipse.swt.widgets.Shell;
import org.iviPro.theme.Icons;

public class ResizeImageDialog extends Dialog {
	private Element imageElement;
	private Shell shell;
	private JButton btnOK;
	private JTextField txtWidth;
	private JTextField txtHeight;
	private boolean widthChanged;
	private boolean heightChanged;
	private boolean keepRatio = true;
	private double ratio;
	private static final int DEFAULT_WINDOW_WIDTH = 400;
	private static final int DEFAULT_WINDOW_HEIGHT = 130;

	public ResizeImageDialog(Frame parent, Element imageElement) {
		super(parent, Messages.ImageResize_Dialog_Title); // TODO auslagern
		this.imageElement = imageElement;
		setLayout(new GridBagLayout());
		setSize(new Dimension(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
		// fensert in der mitte des bildschirms plazieren
		setLocationRelativeTo(null);
		// fenster icon setzen
		setIconImage(Icons.OBJECT_MEDIA_PICTURE.getImageIcon().getImage());
		// komponenten erzeugen
		// createThumbnailPane();
		createControlPane();
		createOptionsPane();
		setVisible(true);
		ratio = Double.parseDouble(imageElement.getAttributes()
				.getAttribute(HTML.Attribute.WIDTH).toString())
				/ Double.parseDouble(imageElement.getAttributes()
						.getAttribute(HTML.Attribute.HEIGHT).toString());
	}

	/**
	 * Erzeugt die Controll-Elemente zum Einfuegen und Abbrechen.
	 */
	private void createControlPane() {
		JPanel controls = new JPanel();
		controls.setBorder(BorderFactory.createTitledBorder(Messages.ImageResize_Dialog_Options_Title)); // TODO
																			// auslagern
		// control position
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 3.0;
		c.gridy = 1;
		c.gridx = 1;
		this.add(controls, c);
		btnOK = new JButton(Messages.ImageResize_Dialog_Button_OK); // TODO auslagern
		btnOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onOK();
				setVisible(false);
			}
		});
		controls.add(btnOK);
		// button zum schlieszen des dialog feldes
		JButton btnCancle = new JButton(Messages.ImageResize_Dialog_Button_Cancel); // TODO auslagern
		btnCancle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
		options.setBorder(BorderFactory.createTitledBorder(Messages.ImageResize_Dialog_Dimensions_Title)); // TODO
																			// auslagern
		options.setLayout(new GridLayout(0, 2));
		// control position
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridy = 1;
		c.gridx = 0;
		this.add(options, c);

		// erstellt ein eingabefeld fuer die laenge des bildes
		JLabel lblWidth = new JLabel(Messages.ImageResize_Dialog_Width); // TODO auslagern
		txtWidth = new JTextField();
		txtWidth.setDocument(new InsertImageDialog.NumbericDocument());
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

		txtWidth.setText(imageElement.getAttributes()
				.getAttribute(HTML.Attribute.WIDTH).toString());

		options.add(lblWidth);
		options.add(txtWidth);

		// erstellt ein eingabefeld fuer die hoehe des bildes
		JLabel lblHeight = new JLabel(Messages.ImageResize_Dialog_Height); // TODO auslagern
		txtHeight = new JTextField();
		txtHeight.setDocument(new InsertImageDialog.NumbericDocument());
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
					modWidth();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				heightChanged = false;
			}

			private void modWidth() {
				if (txtHeight.getText().length() > 0 && keepRatio) {
					txtWidth.setText(Integer.toString((int) (Integer
							.parseInt(txtHeight.getText()) * ratio)));
				}
			}
		});

		txtHeight.setText(imageElement.getAttributes()
				.getAttribute(HTML.Attribute.HEIGHT).toString());

		options.add(lblHeight);
		options.add(txtHeight);

		JLabel lblRatio = new JLabel(Messages.ImageResize_Dialog_KeepRatio); // TODO auslagern
		final JCheckBox boxRatio = new JCheckBox();
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

	private void onOK() {
		setVisible(false);
	}

	public void addResizeListener(ActionListener al) {
		btnOK.addActionListener(al);
	}

	public int getHeight() {
		try {
			return Integer.parseInt(txtHeight.getText());
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public int getWidth() {
		try {
			return Integer.parseInt(txtWidth.getText());
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}
}
